package nilsct.leveling.interactions.components.roles

import net.dv8tion.jda.api.Permission
import nilsct.leveling.entities.Wizard
import nilsct.leveling.interactions.Interaction
import nilsct.leveling.interactions.InteractionContext
import nilsct.leveling.interactions.components.ComponentContext
import nilsct.leveling.managers.RoleManager.Companion.roleManager
import nilsct.leveling.managers.WizardManager.Companion.wizardManager

class Mode : Interaction() {

    override val id = "roles-mode"
    override val permission = listOf(Permission.ADMINISTRATOR)
    override val botPermission = listOf(Permission.MANAGE_ROLES)

    override fun execute(context: InteractionContext) {
        context as ComponentContext
        val server = context.server
        when (context.params.firstOrNull()) {
            "stack" -> server.stack = true
            "replace" -> server.stack = false
        }
        roleManager.updateServer(server, context.guild)
        wizardManager.edit(context, Wizard.Page.ROLE, true)
    }
}