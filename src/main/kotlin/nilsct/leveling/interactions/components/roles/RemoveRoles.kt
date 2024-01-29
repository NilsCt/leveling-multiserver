package nilsct.leveling.interactions.components.roles

import net.dv8tion.jda.api.Permission
import nilsct.leveling.entities.Wizard
import nilsct.leveling.interactions.Interaction
import nilsct.leveling.interactions.InteractionContext
import nilsct.leveling.interactions.components.ComponentContext
import nilsct.leveling.managers.RoleManager.Companion.roleManager
import nilsct.leveling.managers.WizardManager.Companion.wizardManager

class RemoveRoles : Interaction() {

    override val id = "roles-remove"
    override val permission = listOf(Permission.ADMINISTRATOR)
    override val botPermission = listOf(Permission.MANAGE_ROLES)

    override fun execute(context: InteractionContext) {
        context as ComponentContext
        val server = context.server
        val guild = context.guild
        val params = context.params
        val first = params.first()
        val role = guild.getRoleById(params[1])
        val rewardRole = roleManager.get(server, params[1])
        when {
            role == null -> {
                if (rewardRole != null) roleManager.removeDeletedRole(server, rewardRole) // actualise
                context.edit("This role has been deleted.")
                    .setComponents(emptyList())
                    .queue()
            }

            rewardRole == null -> {
                context.edit("${role.asMention} isn't a reward role.")
                    .setComponents(emptyList())
                    .queue()
            }

            first == "cancel" -> {
                roleManager.remove(server, guild, rewardRole, role, false) // n'enlève pas le rôle des membres
                context.edit("${role.asMention} will no longer be assigned but it hasn't been removed from members.")
                    .setComponents(emptyList())
                    .queue()
                wizardManager.edit(context, Wizard.Page.ROLE)
            }

            first == "remove" -> {
                roleManager.remove(server, guild, rewardRole, role, true)
                context.edit("${role.asMention} will no longer be assigned and it has been removed from all members.")
                    .setComponents(emptyList())
                    .queue()
                wizardManager.edit(context, Wizard.Page.ROLE)
            }
        }
    }
}