package nilsct.leveling.interactions

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.GenericContextInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.modals.Modal
import net.dv8tion.jda.api.requests.restaction.interactions.MessageEditCallbackAction
import net.dv8tion.jda.api.requests.restaction.interactions.ModalCallbackAction
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction
import nilsct.leveling.entities.LvlMember
import nilsct.leveling.entities.Server
import nilsct.leveling.managers.MemberManager.Companion.memberManager
import nilsct.leveling.managers.ServerManager.Companion.serverManager
import nilsct.leveling.managers.UserManager.Companion.userManager

abstract class InteractionContext(private val genericEvent: GenericInteractionCreateEvent) {

    abstract val id: String

    val isFromGuild = genericEvent.isFromGuild
    val guild: Guild
        get() {
            if (isFromGuild) {
                return genericEvent.guild ?: throw Exception("Guild is null")
            } else {
                throw Exception("No guild for dm interaction")
            }
        }
    val user = genericEvent.user
    val member: Member
        get() {
            if (isFromGuild) {
                return genericEvent.member ?: throw Exception("Member is null")
            } else {
                throw Exception("No member for dm interaction")
            }
        }
    val server: Server
        get() {
            if (isFromGuild) {
                return serverManager.acquire(guild)
            } else {
                throw Exception("No server for dm interaction")
            }
        }
    val lvlUser = userManager.acquire(user)
    val lvlMember: LvlMember
        get() {
            return when {
                !isFromGuild -> throw Exception("No lvlMember for dm interaction")
                else -> memberManager.acquire(server, lvlUser, member)
            }
        }
    val selfMember: Member
        get() {
            if (isFromGuild) {
                return guild.selfMember
            } else {
                throw Exception("No self member for dm interaction")
            }
        }

    abstract val tag: String

    // get pour avoir l'id de la classe fille
    open val description get() = "$id ${user.name} (${lvlUser.id}) in ${if (isFromGuild) "${guild.name} (${server.id})" else "dm"}"

    // Membre
    val permissions get() = member.permissions
    fun isAdmin() = member.hasPermission(Permission.ADMINISTRATOR)
    val isDev = user.id in nilsct.leveling.Bot.developers

    /*
     * Il faut la permission admin ou manager server pour ajouter un bot et les commandes
     * MAIS un membre ne peut pas donner une permission qu'il n'a pas au bot
     * cas problématique : quand le membre a seulement la perm manage server il ne peut pas forcément donner toutes les autres permissions demandées.
     */
    private fun canAddBot() = isAdmin() || member.hasPermission(Permission.MANAGE_SERVER)
    private fun canManageRole() = member.hasPermission(Permission.MANAGE_ROLES)

    // Bot
    val botPermissions get() = selfMember.permissions
    fun botCantManageRole() =
        !selfMember.hasPermission(Permission.MANAGE_ROLES) // marche aussi si la permission de gérer les roles provient de la permission admin

    /*
     Si le membre peut rajouter la permission manquante manage roles avec le bouton invite
     Même si le membre peut ajouter des bots, s'il n'a pas la permission de gérer les roles, il ne peut la donner.
     */
    fun canUseRoleButton() = botCantManageRole() && canAddBot() && canManageRole()

    fun reply(content: String = ""): ReplyCallbackAction {
        return when (genericEvent) {
            is SlashCommandInteractionEvent -> {
                genericEvent.reply(content)
            }

            is GenericComponentInteractionCreateEvent -> {
                genericEvent.reply(content)
            }

            is GenericContextInteractionEvent<*> -> {
                genericEvent.reply(content)
            }

            is ModalInteractionEvent -> {
                genericEvent.reply(content)
            }

            else -> throw Exception("Unknown interaction (reply) $this $content")
        }
            .setEphemeral(true)
    }

    fun deferReply(callback: (InteractionHook) -> Unit, ephemeral: Boolean = false) {
        when (genericEvent) {
            is SlashCommandInteractionEvent -> {
                genericEvent.deferReply(ephemeral).queue(callback)
            }

            is GenericComponentInteractionCreateEvent -> {
                genericEvent.deferReply(ephemeral).queue(callback)
            }

            is GenericContextInteractionEvent<*> -> {
                genericEvent.deferReply(ephemeral).queue(callback)
            }

            is ModalInteractionEvent -> {
                genericEvent.deferReply(ephemeral).queue(callback)
            }

            else -> throw Exception("Unknown interaction (defer) $this")
        }
    }

    fun modal(modal: Modal): ModalCallbackAction {
        return when (genericEvent) {
            is SlashCommandInteractionEvent -> {
                genericEvent.replyModal(modal)
            }

            is GenericComponentInteractionCreateEvent -> {
                genericEvent.replyModal(modal)
            }

            is GenericContextInteractionEvent<*> -> {
                genericEvent.replyModal(modal)
            }

            else -> throw Exception("Unknown interaction (reply) $this")
        }
    }

    fun edit(content: String = ""): MessageEditCallbackAction {
        return when (genericEvent) {
            is GenericComponentInteractionCreateEvent -> {
                genericEvent.editMessage(content)
            }

            is ModalInteractionEvent -> {
                genericEvent.editMessage(content)
            }

            else -> throw Exception("Unknown interaction (edit) $this")
        }
    }

    fun deferEdit(callback: (InteractionHook) -> Unit) {
        when (genericEvent) {
            is GenericComponentInteractionCreateEvent -> {
                genericEvent.deferEdit().queue(callback)
            }

            is ModalInteractionEvent -> {
                genericEvent.deferEdit().queue(callback)
            }

            else -> throw Exception("Unknown interaction (defer edit) $this")
        }
    }
}