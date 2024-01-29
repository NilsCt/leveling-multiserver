package nilsct.leveling.interactions.components

import net.dv8tion.jda.api.Permission
import nilsct.leveling.entities.Wizard
import nilsct.leveling.interactions.Interaction
import nilsct.leveling.interactions.InteractionContext
import nilsct.leveling.managers.DevAnalysis
import nilsct.leveling.managers.WizardManager.Companion.wizardManager

class SetUp : Interaction() {

    override val id = "set-up"
    override val permission = listOf(Permission.ADMINISTRATOR)

    override fun execute(context: InteractionContext) {
        context as ComponentContext
        when (context.params.first()) {
            "start" -> {
                wizardManager.open(context, Wizard.Page.LVL_UP, true)
                DevAnalysis.setUp++
            }

            "next" -> wizardManager.next(context) // page suivante

            "done" -> wizardManager.close(context)
        }
    }
}