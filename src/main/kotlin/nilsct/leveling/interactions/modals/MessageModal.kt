package nilsct.leveling.interactions.modals

import net.dv8tion.jda.api.Permission
import nilsct.leveling.entities.Wizard
import nilsct.leveling.interactions.Interaction
import nilsct.leveling.interactions.InteractionContext
import nilsct.leveling.managers.ServerManager
import nilsct.leveling.managers.WizardManager.Companion.wizardManager

class MessageModal : Interaction() {

    override val id = "levelup-message"
    override val permission = listOf(Permission.ADMINISTRATOR)

    override fun execute(context: InteractionContext) {
        context as ModalContext
        var message = context.getValue("message")!!.asString
        if (message == "") message = ServerManager.defaultLvlUpMessage
        context.server.lvlUpMessage = message
        wizardManager.edit(context, Wizard.Page.LVL_UP, true)
    }
}