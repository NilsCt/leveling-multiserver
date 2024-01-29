package nilsct.leveling.interactions.commands.stats

import net.dv8tion.jda.api.interactions.commands.build.Commands
import nilsct.leveling.interactions.Interaction
import nilsct.leveling.interactions.InteractionContext
import nilsct.leveling.interactions.commands.CommandContext
import nilsct.leveling.managers.ComponentManager
import nilsct.leveling.managers.DevAnalysis
import nilsct.leveling.managers.LeaderboardSender
import nilsct.leveling.stats.StatsManager

class ServerLeaderboard : Interaction() {

    override val id =
        "server-leaderboard"
    override val commandData =
        Commands.slash("server-leaderboard", "Get the leaderboard of the 10 most active servers")


    override fun execute(context: InteractionContext) {
        context as CommandContext
        val lbContext = ComponentManager.LBContext(StatsManager.allServers)
        LeaderboardSender.check(context, lbContext)
        DevAnalysis.serverLeaderboard++
    }
}