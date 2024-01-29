package nilsct.leveling.interactions.commands.stats

import net.dv8tion.jda.api.interactions.commands.build.Commands
import nilsct.leveling.interactions.Interaction
import nilsct.leveling.interactions.InteractionContext
import nilsct.leveling.interactions.commands.CommandContext
import nilsct.leveling.managers.DevAnalysis
import nilsct.leveling.managers.StatsSender

class Server : Interaction() {

    override val id = "server"
    override val commandData = Commands
        .slash("server", "Check the stats of the server")
        .setGuildOnly(true)

    override fun execute(context: InteractionContext) {
        context as CommandContext
        StatsSender.check(context, context.server.team)
        DevAnalysis.serverCommand++
    }
}