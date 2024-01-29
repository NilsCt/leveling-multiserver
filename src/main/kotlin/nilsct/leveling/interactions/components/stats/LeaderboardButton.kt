package nilsct.leveling.interactions.components.stats

import nilsct.leveling.interactions.Interaction
import nilsct.leveling.interactions.InteractionContext
import nilsct.leveling.interactions.components.ComponentContext
import nilsct.leveling.managers.ComponentManager.Companion.componentManager
import nilsct.leveling.managers.LeaderboardSender

class LeaderboardButton : Interaction() { // dans help

    override val id = "leaderboard-button"
//    override val private = true global modif

    override fun execute(context: InteractionContext) {
        context as ComponentContext
        val lbContext = componentManager.getLBContext(context.params, context)
        LeaderboardSender.check(context, lbContext)
    }
}