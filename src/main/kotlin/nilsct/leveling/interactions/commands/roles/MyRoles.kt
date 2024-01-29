package nilsct.leveling.interactions.commands.roles

import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.components.buttons.Button
import nilsct.leveling.entities.Privacy
import nilsct.leveling.entities.RewardRole
import nilsct.leveling.interactions.Interaction
import nilsct.leveling.interactions.InteractionContext
import nilsct.leveling.interactions.commands.CommandContext
import nilsct.leveling.managers.ComponentManager.Companion.componentManager
import nilsct.leveling.managers.RoleManager.Companion.roleManager
import nilsct.leveling.stats.StatsManager.Companion.statsManager

class MyRoles : Interaction() {

    override val id = "my-roles"
    override val commandData = Commands
        .slash("my-roles", "Check your reward roles in this server")
        .setGuildOnly(true)

    private fun format(
        rewardRoles: List<RewardRole>,
        higher: List<RewardRole>
    ): String { // higher : liste des id de rôles que le bot ne peut pas gérer
        return rewardRoles.joinToString(separator = "\n") { rewardRole ->
            rewardRole.msg + (if (rewardRole in higher) " :name_badge:" else "")
        }
    }

    override fun execute(context: InteractionContext) {
        context as CommandContext
        val server = context.server
        val guild = context.guild
        val lvlUser = context.lvlUser
        val member = context.member
        val lvlMember = context.lvlMember
        val mate = lvlMember.mate
        val embed = nilsct.leveling.Bot.blueEmbed
            .setTitle("Your reward roles")
            .setAuthor(member.effectiveName, null, member.effectiveAvatarUrl)
        if (server.rewardRoles.isEmpty()) {
            embed.setDescription("This server hasn't added any reward roles yet. :confounded:")
        } else {
            val h: List<RewardRole> = if (context.botCantManageRole()) {
                embed.setDescription(":name_badge: I don't have the permission to manage roles.")
                emptyList()
            } else {
                val (_, cant) = roleManager.manageable(server, guild) // rôle(s) que le bot ne peut pas gérer
                if (cant.size == 1) {
                    embed.setDescription(":name_badge: I don't have the permission to manage a role.")
                } else if (cant.size > 1) {
                    embed.setDescription(":name_badge: I don't have the permission to manage some roles.")
                }
                cant
            }
            statsManager.update(mate)
            roleManager.updateMember(server, guild, lvlMember, context.member)
            val (previous, current, next) = roleManager.inventory(
                server,
                guild,
                lvlMember,
                false
            ) // ne prend pas en compte les rôles pas gérables
            if (server.stack) {
                if (next.isEmpty()) {
                    embed.addField("Next:", "Wow you unlocked every reward roles! :partying_face:", false)
                } else {
                    embed.addField("Next:", format(next, h), false)
                }
                if (current.isEmpty()) {
                    embed.addField(
                        "How to unlock reward roles?",
                        "Send messages or join a voice channel to earn xp and level up.",
                        false
                    )
                } else {
                    embed.addField("Unlocked:", format(current, h), false)
                }
            } else {
                if (next.isNotEmpty()) {
                    embed.addField("Next:", format(next, h), false)
                } else {
                    embed.addField("Next:", "Wow you unlocked every reward roles! :partying_face:", false)
                }
                if (current.isEmpty()) {
                    embed.addField(
                        "How to unlock reward roles?",
                        "Send messages or join a voice channel to earn xp and level up.",
                        false
                    )
                } else {
                    embed.addField("Current:", format(current, h), false)
                }
                if (previous.isNotEmpty()) {
                    embed.addField("Previous:", format(previous, h), false)
                }
            }
        }
        val msg =
            if (lvlUser.privacy == Privacy.NORMAL) "" else ":fire: You don't received your reward roles. ${
                nilsct.leveling.Bot.mention(
                    "privacy"
                )
            }"
        val buttons = mutableListOf<Button>()
        componentManager.getPrivacyButtons(mate, context.lvlUser)?.run { buttons.add(this) }
        if (context.canUseRoleButton()) buttons.add(roleManager.roleButton)
        val action = context.reply(msg)
            .addEmbeds(embed.build())
            .setEphemeral(!mate.public)
        if (buttons.isNotEmpty()) action.addActionRow(buttons)
        action.queue()
    }
}