package nilsct.leveling.interactions.components.stats

import nilsct.leveling.interactions.Interaction
import nilsct.leveling.interactions.InteractionContext
import nilsct.leveling.interactions.components.ComponentContext
import nilsct.leveling.managers.ComponentManager.Companion.componentManager
import nilsct.leveling.managers.StatsSender
import nilsct.leveling.stats.Mate

class StatsButton : Interaction() {

    override val id = "stats"
//    override val private = true global modif

    override fun execute(context: InteractionContext) {
        context as ComponentContext
        val statsContext = componentManager.getStatsContext(context.params, context)
        val stats = statsContext.stats
        if (stats is Mate && StatsSender.testPrivacy(context, stats)) return
        when {
            statsContext.embed && statsContext.edit -> StatsSender.checkEmbedEdit(context, stats)
            statsContext.embed -> StatsSender.checkEmbed(context, stats) // pas utilisé
            statsContext.edit -> StatsSender.checkEdit(context, stats) // pas utilisé
            else -> StatsSender.check(context, stats)
        }
    }
}