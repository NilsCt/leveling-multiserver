package nilsct.leveling.managers

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.requests.ErrorResponse
import nilsct.leveling.Bot.Companion.jda
import nilsct.leveling.entities.*
import nilsct.leveling.interactions.InteractionContext
import nilsct.leveling.managers.ServerManager.Companion.serverManager

class RoleManager {

    companion object {
        val roleManager = RoleManager()
    }

    fun get(server: Server, id: String) = server.rewardRoles.firstOrNull { it.id == id }

    fun add(server: Server, guild: Guild, role: Role, lvl: Int) {
        server.rewardRoles.add(RewardRole(role.id, lvl))
        updateServer(server, guild)
    }

    fun edit(server: Server, guild: Guild, rewardRole: RewardRole, lvl: Int) {
        rewardRole.lvl = lvl
        updateServer(server, guild)
    }

    // removeRoles true s'il faut enlever ce reward rôle des membres
    fun remove(server: Server, guild: Guild, rewardRole: RewardRole, role: Role, removeRoles: Boolean) {
        server.rewardRoles.remove(rewardRole)
        if (removeRoles) updateServer(server, guild, role)
    }

    fun removeDeletedRole(server: Server, rewardRole: RewardRole) {
        server.rewardRoles.remove(rewardRole)
    }

    fun reset(
        server: Server,
        guild: Guild,
        removeRoles: Boolean
    ) {
        val roles = server.rewardRoles.mapNotNull { guild.getRoleById(it.id) } // exclut les rôles déjà supprimés
        server.rewardRoles.clear()
        if (!removeRoles) return
        for (lvlMember in server.lvlMembers) {
            val lvlUserID = lvlMember.lvlUserID
            try {
                guild.retrieveMemberById(lvlUserID).queue { member ->
                    try {
                        guild.modifyMemberRoles(member, emptyList(), roles).queue()
                    } catch (e: Exception) {
                        Log.error(
                            "ROLE",
                            "Reset ${member.user.name} (${member.id}) in ${guild.name} (${guild.id}) remove: ${roles}\n$e"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.error("ROLE", "Load member $lvlUserID in ${guild.name} (${guild.id}) $e")
            }

        }
    }

    fun manageable(
        server: Server,
        guild: Guild
    ): Pair<List<RewardRole>, List<RewardRole>> { // Les rôles que le bot peut gérer ; les autres
        val can = mutableListOf<RewardRole>()
        val cant = mutableListOf<RewardRole>()
        val deleted = mutableListOf<RewardRole>()
        server.rewardRoles.sortedByDescending { it.lvl }.forEach {
            val role = guild.getRoleById(it.id)
            when {
                role == null -> deleted.add(it) // rôle supprimé
                guild.selfMember.canInteract(role) -> can.add(it) // ça ne prend pas en compte si le bot n'a pas la perm de gérer les rôles
                else -> cant.add(it)
            }
        }
        deleted.forEach { removeDeletedRole(server, it) }
        return can to cant
    }

    /*
    Previous, Current, Next
    Stack : Previous vide, tous dans current
    Replace : Current a max 1 seul élément, le reste passe dans previous
    =====
    real = true détermine l'inventaire qu'un membre doit avoir en excluant les rôles pas gérables
    Si le server est en mode replace et que le dernier rôle d'un membre ne peut pas être
    attribué par manque de permission, c'est le rôle d'en dessous qui est donné
     */
    fun inventory(
        server: Server,
        guild: Guild,
        lvlMember: LvlMember,
        real: Boolean
    ): Triple<List<RewardRole>, List<RewardRole>, List<RewardRole>> {
        var rewardRoles = server.rewardRoles.sortedByDescending { it.lvl }
        if (real) {
            val (can, _) = manageable(server, guild)
            rewardRoles = rewardRoles.filter { it in can }
        }
        val stats = lvlMember.mate
        val lvl = if (stats.privacy == Privacy.NORMAL) stats.lvl else 1 // ils ont les rôles minimum
        val unlocked =
            rewardRoles.filter { it.lvl <= lvl }.toMutableList() // rôles que le membre a / a eu (previous + current)
        val next = rewardRoles.filter { it.lvl > lvl }
        return if (server.stack) {
            Triple(emptyList(), unlocked, next)
        } else {
            val last = unlocked.removeFirstOrNull()
            val current = if (last == null) emptyList() else listOf(last)
            Triple(unlocked, current, next)
        }
    }

    fun updateMemberServers(lvlUser: LvlUser) { // utiliser pour actualiser les reward rôles après modification de privacy
        for (lvlMember in lvlUser.lvlMembers) {
            val guild = jda.getGuildById(lvlMember.serverID) ?: continue
            val server = serverManager.acquire(guild)
            try {
                guild.retrieveMemberById(lvlUser.id).queue(
                    { member ->
                        updateMember(server, guild, lvlMember, member)
                    },
                    ErrorHandler {
                        Log.error(
                            "ROLE",
                            "(member servers) (Discord response) Load member ${lvlUser.id} in ${guild.name} (${guild.id}) $it"
                        )
                    }.ignore(ErrorResponse.UNKNOWN_MEMBER)
                )
            } catch (e: Exception) {
                Log.error("ROLE", "(member servers) Load member ${lvlUser.id} in ${guild.name} (${guild.id}) $e")
            }
        }
    }

    fun updateServer(
        server: Server,
        guild: Guild,
        removeBonus: Role? = null
    ) { // removeBonus utilisé pour quand on supprime un rôle
        if (!guild.selfMember.hasPermission(Permission.MANAGE_ROLES)) return // marche aussi si la permission de gérer les rôles vient de la permission admin
        if (server.rewardRoles.isEmpty() && removeBonus == null) return // ne pas oublier removeBonus pour bien enlever ce rôle des membres quand on le supprime
        for (lvlMember in server.lvlMembers) {
            val lvlUserID = lvlMember.lvlUserID
            try {
                guild.retrieveMemberById(lvlUserID).queue(
                    { member ->
                        updateMember(server, guild, lvlMember, member, removeBonus)
                    },
                    ErrorHandler {
                        Log.error(
                            "ROLE",
                            "(Discord response) Load member $lvlUserID in ${guild.name} (${guild.id}) $it"
                        )
                    }.ignore(ErrorResponse.UNKNOWN_MEMBER)
                )
            } catch (e: Exception) {
                Log.error("ROLE", "Load member $lvlUserID in ${guild.name} (${guild.id}) $e")
            }
        }
    }

    fun updateMember(
        server: Server,
        guild: Guild,
        lvlMember: LvlMember,
        member: Member,
        removeBonus: Role? = null // removeBonus utilisé pour quand on supprime un rôle
    ) {
        if (!guild.selfMember.hasPermission(Permission.MANAGE_ROLES)) return
        if (server.rewardRoles.isEmpty() && removeBonus == null) return // ne pas oublier removeBonus

//         ==== détermination des rôles que le bot ne peut pas gérer ====
        val (_, cant) = manageable(server, guild) // actualise les rôles supprimés

        // ==== détermination des rôles que le membre doit avoir ====
        /*
        real = true détermine l'inventaire qu'un membre doit avoir en excluant les rôles pas gérables
        Il supprime donc déjà les rôles à ajouter que le bot ne peut pas gérer
        /!\ mais il ne s'occupe pas des rôles pas gérables qui doivent être enlevés du membre
        De plus les rôles pas gérables que le membre doit avoir et qu'il a déjà sont supprimés de la liste lors du tri et le bot crois qu'il doit les enlever au membre
        Il est donc important de re-vérifier quels rôles le bot ne doit pas toucher (pas gérables, voir continue dans la boucle)
         */
        val current = inventory(server, guild, lvlMember, true).second

        // ==== détermination des rôles à ajouter ou enlever pour atteindre cet objectif ====
        val owned = member.roles // rôles que le membre a
        val add = mutableListOf<Role>() // rôles que le membre doit gagner pour atteindre l'objectif
        val remove = mutableListOf<Role>() // rôles que le membre doit perdre pour atteindre l'objectif
        for (rewardRole in server.rewardRoles) {
            if (rewardRole in cant) continue // le bot ne peut pas ajouter / enlever ce rôle
            val role = guild.getRoleById(rewardRole.id)
            if (role == null) { // rôle supprimé (normalement déjà trié par manageable)
                removeDeletedRole(server, rewardRole)
                continue
            }
            if (role in owned && rewardRole !in current) { // si le membre a un rôle qu'il ne devrait pas avoir
                remove.add(role) // il l'enlève
            } else if (role !in owned && rewardRole in current) { // s'il n'a pas un rôle qu'il devrait avoir
                add.add(role) // il lui ajoute
            }
        }
        if (removeBonus != null) remove.add(removeBonus)

        // ==== si cet objectif n'est pas déjà atteint on modifie les rôles ====
        if (add.isNotEmpty() || remove.isNotEmpty()) {
            try {
                guild.modifyMemberRoles(member, add, remove).queue({
//                    Log.log(
//                        "ROLE",
//                        "Modify ${member.user.name} (${member.id}) in ${guild.name} (${guild.id}) add: $add remove: $remove"
//                    )
                },
                    ErrorHandler {
                        Log.error(
                            "ROLE",
                            "(Discord response) Modify ${member.user.name} (${member.id}) in ${guild.name} (${guild.id}) add: $add remove: $remove $it"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.error(
                    "ROLE",
                    "Modify ${member.user.name} (${member.id}) in ${guild.name} (${guild.id}) add: $add remove: $remove $e"
                )
            }
        }
    }

    fun getEmbed(context: InteractionContext): MessageEmbed {
        val server = context.server
        val guild = context.guild
        val eb = nilsct.leveling.Bot.blueEmbed
            .setTitle("Reward Roles")
            .setDescription("Members unlock roles when they reach a specific level.")
        if (context.botCantManageRole()) {
            eb.appendDescription("\n:name_badge: **I don't have the __permission__ to** `manage roles`.")
        }
        if (server.stack) {
            eb.addField("Mode: `Stack`", "Members pile up their roles", false)
        } else {
            eb.addField("Mode: `Replace`", "Members only keep their best role", false)
        }
        val (can, cant) = manageable(server, guild)// enlève déjà les rôles supprimés
        if (can.isEmpty() && cant.isEmpty()) {
            eb.addField("Current roles:", "No roles added yet :confused:", false)
        }
        if (can.isNotEmpty()) {
            eb.addField(
                "Current roles:",
                can.joinToString(separator = "\n") { it.msg },
                false
            )
        }
        if (cant.isNotEmpty()) {
            val msg =
                if (cant.size > 1) ":name_badge: I can't manage these roles:" else ":name_badge: I can't manage this role:"
            val msg2 =
                if (cant.size > 1) "\nBecause my best role is lower than them" else "\nBecause my best role is lower"
            eb.addField(
                msg,
                cant.joinToString(separator = "\n", postfix = msg2) { it.msg },
                false
            )
        }
        eb.addField(
            "",
            "${nilsct.leveling.Bot.mention("roles add")} to add or edit a role" +
                    "\n${nilsct.leveling.Bot.mention("roles remove")} to remove a role",
            false
        )
        return eb.build()
    }

    private val replaceButton = Button.primary("roles-mode;replace", "Switch to replace mode")
    private val stackButton = Button.secondary("roles-mode;stack", "Switch to stack mode")
    val roleButton = Button.link(nilsct.leveling.Bot.inviteLink, "Allow me to manage roles")
    private val resetButton = Button.danger("roles-reset", "Reset")

    fun getActionRows(context: InteractionContext): List<ActionRow> {
        val server = context.server
        val buttons = mutableListOf<Button>()
        when {
            context.canUseRoleButton() -> {
                return listOf(ActionRow.of(roleButton))
            }

            context.isAdmin() -> {
                buttons.add(if (server.stack) replaceButton else stackButton)
                if (server.rewardRoles.isNotEmpty()) buttons.add(resetButton)

            }
        }
        return listOf(ActionRow.of(buttons))
    }
}