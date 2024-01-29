package nilsct.leveling.interactions

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.GenericContextInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericSelectMenuInteractionEvent
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.components.buttons.Button
import nilsct.leveling.Bot.Companion.jda
import nilsct.leveling.interactions.autoComplete.AutoCompleteContext
import nilsct.leveling.interactions.autoComplete.AutoCompleteRestore
import nilsct.leveling.interactions.autoComplete.AutoCompleteServer
import nilsct.leveling.interactions.autoComplete.AutoCompleteServerLeaderboard
import nilsct.leveling.interactions.commands.CommandContext
import nilsct.leveling.interactions.commands.Help
import nilsct.leveling.interactions.commands.Invite
import nilsct.leveling.interactions.commands.LvlUpMenu
import nilsct.leveling.interactions.commands.admin.*
import nilsct.leveling.interactions.commands.moderation.AddXp
import nilsct.leveling.interactions.commands.moderation.RemoveXp
import nilsct.leveling.interactions.commands.moderation.ResetMember
import nilsct.leveling.interactions.commands.moderation.ResetServer
import nilsct.leveling.interactions.commands.roles.Add
import nilsct.leveling.interactions.commands.roles.Menu
import nilsct.leveling.interactions.commands.roles.MyRoles
import nilsct.leveling.interactions.commands.roles.Remove
import nilsct.leveling.interactions.commands.stats.*
import nilsct.leveling.interactions.commands.user.Expiration
import nilsct.leveling.interactions.commands.user.Privacy
import nilsct.leveling.interactions.commands.user.SelfReset
import nilsct.leveling.interactions.commands.user.Stop
import nilsct.leveling.interactions.components.*
import nilsct.leveling.interactions.components.levelup.Auto
import nilsct.leveling.interactions.components.levelup.Channel
import nilsct.leveling.interactions.components.levelup.Disable
import nilsct.leveling.interactions.components.levelup.Message
import nilsct.leveling.interactions.components.roles.Mode
import nilsct.leveling.interactions.components.roles.RemoveRoles
import nilsct.leveling.interactions.components.roles.RolesReset
import nilsct.leveling.interactions.components.stats.LeaderboardButton
import nilsct.leveling.interactions.components.stats.LeaderboardMenu
import nilsct.leveling.interactions.components.stats.StatsButton
import nilsct.leveling.interactions.context.ContextContext
import nilsct.leveling.interactions.context.ContextReset
import nilsct.leveling.interactions.context.ContextStats
import nilsct.leveling.interactions.modals.MessageModal
import nilsct.leveling.interactions.modals.ModalContext
import nilsct.leveling.managers.DevAnalysis
import nilsct.leveling.managers.Log
import nilsct.leveling.managers.RoleManager.Companion.roleManager
import java.time.Instant

class InteractionManager {

    companion object {
        val interactionManager = InteractionManager()
    }

    private val commands = listOf(
        Help(),
        Invite(),
        MyStats(),
        Member(),
        Server(), // global modif supprimer server (remplacé par DevServer)
        Leaderboard(),
        Discord(),
        ServerLeaderboard(),
        ResetServer(),// dev only
        ResetMember(),
        AddXp(),
        RemoveXp(),
        Privacy(),
        Expiration(),
        Stop(),
        SelfReset(),
        LvlUpMenu(),
        Menu(),
        Add(),
        Remove(),
        MyRoles(),
        Load(),
        Save(),
        BackUp(),
        Chart(),
        Latest(),
        DevServer(),
        Activity(),
        DevServerLeaderboard(),
        Restore()

    )
    private val autoCompletes = listOf(
        AutoCompleteServer(), // dev only
        AutoCompleteServerLeaderboard(),
        AutoCompleteRestore()
    )
    private val contextCommands = listOf(
        ContextStats(),
        ContextReset()
    )
    private val buttons = listOf(
        StatsButton(),
        LeaderboardButton(),
        SetUp(),
        ResetServerButton(),
        Mode(),
        RolesReset(),
        RemoveRoles(),
        ResetPrivacy(),
        Auto(),
        Disable(),
        Message(),
        SelfResetButton()
    )
    private val selectionMenus = listOf(
        PrivacyMenu(),
        LeaderboardMenu(),
        Channel()
    )
    private val modals = listOf(
        MessageModal()
    )

    /*
    Main commands = commandes seules ou principales d'un groupe de commandes
    Elles contiennent les command data
     */
    private val mainCommands = mutableListOf<Interaction>().apply {
        addAll(commands.filter { it.mainCommand && !it.developer })
        addAll(contextCommands.filter { it.mainCommand && !it.developer })
    }
    private val developerMainCommands = mutableListOf<Interaction>().apply {
        addAll(commands.filter { it.mainCommand && it.developer })
        addAll(contextCommands.filter { it.mainCommand && it.developer })
    }

    private fun getPermissionsMsg(permissions: List<Permission>): String {
        return when {

            Permission.ADMINISTRATOR in permissions -> { // pas les 2 ensembles, car admin donne la permission de gérer les roles
                "administrator"
            }

            Permission.MANAGE_ROLES in permissions -> {
                "to manage roles"
            }

            else -> throw Exception("Weird missing permissions $permissions")
        }
    }

    fun guide(event: GenericInteractionCreateEvent) {
        val interaction: Interaction
        val context: InteractionContext
        when (event) {
            is SlashCommandInteractionEvent -> {
                interaction = commands.first { it.id == event.fullCommandName }
                context = CommandContext(event)
            }

            is CommandAutoCompleteInteractionEvent -> { // GenericAutoCompleteInteractionEvent
                interaction = autoCompletes.first { it.id == event.fullCommandName }
                context = AutoCompleteContext(event)
            }

            is GenericContextInteractionEvent<*> -> {
                interaction = contextCommands.first { it.id == event.fullCommandName }
                context = ContextContext(event)
            }

            is ButtonInteractionEvent -> {
                interaction = buttons.first { event.componentId.startsWith(it.id) }
                context = ComponentContext(event)
            }

            is GenericSelectMenuInteractionEvent<*, *> -> {
                interaction = selectionMenus.first { it.id == event.componentId }
                context = ComponentContext(event)
            }

            is ModalInteractionEvent -> {
                interaction = modals.first { it.id == event.modalId }
                context = ModalContext(event)
            }

            else -> throw Exception("Invalid interaction event ${event.interaction.type}")
        }
        try {
            if (test(interaction, context)) interaction.execute(context)
            Log.log(context.tag, context.description.removeSuffix(" -> "))
            val now = Instant.now().epochSecond
            context.lvlUser.lastActivity = now
            if (context.isFromGuild) {
                context.lvlMember.lastActivity = now
                context.server.lastActivity = now
            }
            DevAnalysis.interactions++
        } catch (e: Exception) {
            Log.error(context.tag, "${context.description} $e")
        }
    }

    private fun test(interaction: Interaction, context: InteractionContext): Boolean {
        val content: String
        val buttons = mutableListOf<Button>()
        val missingPermissions =
            if (context.isFromGuild) interaction.permission.filterNot { it in context.permissions } else emptyList()
        val botMissingPermissions =
            if (context.isFromGuild) interaction.botPermission.filterNot { it in context.botPermissions } else emptyList()

        when {
            !interaction.private && !context.isFromGuild -> {
                content = ":name_badge: You can't use this command in direct messages."
            }

            missingPermissions.isNotEmpty() -> {
                content = ":name_badge: You need the permission `${getPermissionsMsg(missingPermissions)}`."
            }

            botMissingPermissions.isNotEmpty() -> {
                content = ":name_badge: I don't have the permission `${getPermissionsMsg(botMissingPermissions)}`."
                if (context.canUseRoleButton()) buttons.add(roleManager.roleButton)
            }

            interaction.developer && !context.isDev -> {
                content = ":name_badge: This command is only for developers."
            }

            else -> return true
        }

        // On ne peut pas reply avec cette interaction
        if (context is AutoCompleteContext) return false
        val reply = context.reply(content)
        if (buttons.isNotEmpty()) reply.addActionRow(buttons)
        reply.queue()
        return false
    }


    fun syncCommand() {
        try {
            jda.retrieveCommands().queue(
                { botCommands ->
                    Log.log("LAUNCH", "Sync commands")
                    for (interaction in mainCommands) {
                        val commandData = interaction.commandData
                            ?: throw Exception("Interaction without command data ${interaction.id}")
                        val interactionName = if (commandData.type == Command.Type.SLASH) {
                            interaction.id.split(" ").first() // pour les groupes de commandes
                        } else {
                            interaction.id // /!\ les context commands peuvent avoir des espaces
                        }
                        val cmd = botCommands.firstOrNull { it.name == interactionName }
                        botCommands.remove(cmd)
                        if (cmd == null // si commande n'est pas encore ajoutée
                            || commandData.toData().toString() != CommandData.fromCommand(cmd).toData().toString()
                        ) { // ou plus à jour (toData().toString() done JSON sous forme de string)
                            jda.upsertCommand(commandData).queue({
                                Log.log("LAUNCH", "Upsert command $interactionName")
                            }, ErrorHandler {
                                Log.error("LAUNCH", "Upsert command $interactionName $it")
                            }) // on l'ajoute / modifie
                        }
                    }
                    for (cmd in botCommands) { // (commandes restantes qui ne correspondent pas à une interaction)
                        cmd.delete().queue({ // on les supprime
                            Log.log("LAUNCH", "Delete command ${cmd.name}")
                        }, ErrorHandler {
                            Log.error("LAUNCH", "Delete command ${cmd.name} $it")
                        })
                    }
                },
                ErrorHandler { Log.error("LAUNCH", "Retrieve commands $it") }
            )
        } catch (e: Exception) {
            Log.error("LAUNCH", "Sync commands $e")
        }
    }

    fun syncCommandSupport(guild: Guild) {
        try {
            guild.retrieveCommands()
                .queue(
                    { botCommands -> // je pars du principe que ça ne nous donne pas les commandes des autres bots
                        Log.log("LAUNCH", "Sync support commands")
                        for (interaction in developerMainCommands) {
                            val commandData = interaction.commandData
                                ?: throw Exception("Interaction without command data ${interaction.id}")
                            val interactionName = interaction.id.split("/").first()  // pour les groupes de commandes
                            val cmd = botCommands.firstOrNull { it.name == interactionName }
                            botCommands.remove(cmd)
                            if (cmd == null // si commande n'est pas encore ajoutée
                                || commandData.toData().toString() != CommandData.fromCommand(cmd).toData().toString()
                            ) { // ou plus à jour (toData().toString() done JSON sous forme de string)
                                guild.upsertCommand(commandData).queue(null, ErrorHandler {// on l'ajoute / modifie
                                    Log.error("LAUNCH", "Upsert support $interactionName $it")
                                })
                            }
                        }
                        for (cmd in botCommands) { // (commandes restantes qui ne correspondent pas à une interaction)
                            cmd.delete().queue(null, ErrorHandler {// on les supprime
                                Log.error("LAUNCH", "Delete support ${cmd.name} $it")
                            })
                        }
                    },
                    ErrorHandler { Log.error("LAUNCH", "Retrieve support commands ${guild.name} $it") }
                )
        } catch (e: Exception) {
            Log.error("LAUNCH", "Sync support commands ${guild.name} $e")
        }
    }
}

/*
Pour récupérer les mentions des commandes :
guild.retrieveCommands().queue { commands ->
            println(commands.joinToString("\n") { cmd ->
                if(cmd.subcommands.isEmpty()) {
                    "\"</${cmd.name}:${cmd.id}>\", "
                }else{
                    cmd.subcommands.joinToString("\n") {
                        "\"</${cmd.name} ${it.name}:${cmd.id}>\", "
                    }
                }
            })
        }
 */