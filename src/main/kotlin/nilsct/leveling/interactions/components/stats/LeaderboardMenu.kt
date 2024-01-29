package nilsct.leveling.interactions.components.stats

import nilsct.leveling.interactions.Interaction
import nilsct.leveling.interactions.InteractionContext
import nilsct.leveling.interactions.components.ComponentContext
import nilsct.leveling.managers.ComponentManager.Companion.componentManager
import nilsct.leveling.managers.LeaderboardSender

class LeaderboardMenu : Interaction() {

    override val id = "leaderboard"
    override val private = true

    override fun execute(context: InteractionContext) {
        context as ComponentContext
        val lbContext = componentManager.getLBContext(context.params, context)
        val (_, _, edit, embed, _) = lbContext
        when {
            embed && edit -> LeaderboardSender.checkAllEdit(context, lbContext)
            embed -> LeaderboardSender.checkAll(context, lbContext) // pas utilisé
            edit -> LeaderboardSender.checkEdit(context, lbContext)
            else -> LeaderboardSender.check(context, lbContext)
        }
    }
}