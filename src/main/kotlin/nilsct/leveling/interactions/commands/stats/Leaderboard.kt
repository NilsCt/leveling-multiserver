package nilsct.leveling.interactions.commands.stats

import net.dv8tion.jda.api.interactions.commands.build.Commands
import nilsct.leveling.interactions.Interaction
import nilsct.leveling.interactions.InteractionContext
import nilsct.leveling.interactions.commands.CommandContext
import nilsct.leveling.managers.ComponentManager
import nilsct.leveling.managers.DevAnalysis
import nilsct.leveling.managers.LeaderboardSender

class Leaderboard : Interaction() {

    override val id = "leaderboard"
    override val commandData = Commands
        .slash("leaderboard", "Get the leaderboard of the most active members of this server")
        .setGuildOnly(true)

    override fun execute(context: InteractionContext) {
        context as CommandContext
        context.lvlMember // pour être sûr que le classement ne soit pas vide
        val group = context.server.group
        val lbContext = ComponentManager.LBContext(group)
        LeaderboardSender.check(context, lbContext)
        DevAnalysis.leaderboard++
    }
}