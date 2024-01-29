package nilsct.leveling.interactions.components.levelup

import net.dv8tion.jda.api.Permission
import nilsct.leveling.entities.Wizard
import nilsct.leveling.interactions.Interaction
import nilsct.leveling.interactions.InteractionContext
import nilsct.leveling.interactions.components.ComponentContext
import nilsct.leveling.managers.WizardManager.Companion.wizardManager

class Disable : Interaction() {

    override val id = "levelup-disable"
    override val permission = listOf(Permission.ADMINISTRATOR)

    override fun execute(context: InteractionContext) {
        context as ComponentContext
        context.server.lvlUpChannel = ""
        wizardManager.edit(context, Wizard.Page.LVL_UP, true)
    }
}