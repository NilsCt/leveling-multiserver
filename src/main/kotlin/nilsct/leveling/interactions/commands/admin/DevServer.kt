package nilsct.leveling.interactions.commands.admin

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import nilsct.leveling.interactions.Interaction
import nilsct.leveling.interactions.InteractionContext
import nilsct.leveling.interactions.commands.CommandContext
import nilsct.leveling.managers.DevAnalysis
import nilsct.leveling.managers.ServerManager.Companion.serverManager
import nilsct.leveling.managers.StatsSender

class DevServer : Interaction() {

    override val id =
        "dev-server" // global modif enlever developer true + guild only true -> false + enlever permission + changer nom en server
    override val developer = true
    override val commandData =
        Commands.slash("dev-server", "Check the stats of a server")
            .addOption(
                OptionType.STRING,
                "server-name",
                "name of the server (none for the one you're in)",
                false,
                true
            )
            .setGuildOnly(true)
            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))


    override fun execute(context: InteractionContext) {
        context as CommandContext
        val key = context.getOption("server-name")?.asString
        val overrideServer = if (key == null) {
            null
        } else {
            serverManager.get(key) ?: serverManager.getWithName(key)
        }
        when {
            !context.isFromGuild && key == null -> {
                context.reply("As you aren't in a server, you must specify the server name.").queue()
            }

            key != null && overrideServer == null -> {
                context.reply("I can't find a server with that name.")
                    .queue()
            }

            else -> {
                val team = (overrideServer ?: context.server).team
                StatsSender.check(context, team)
                DevAnalysis.serverCommand++
            }
        }
    }
}