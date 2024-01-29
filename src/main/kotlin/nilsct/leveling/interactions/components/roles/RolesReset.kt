package nilsct.leveling.interactions.components.roles

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.components.buttons.Button
import nilsct.leveling.entities.Wizard
import nilsct.leveling.interactions.Interaction
import nilsct.leveling.interactions.InteractionContext
import nilsct.leveling.interactions.components.ComponentContext
import nilsct.leveling.managers.RoleManager.Companion.roleManager
import nilsct.leveling.managers.WizardManager.Companion.wizardManager

class RolesReset : Interaction() {

    override val id = "roles-reset"
    override val permission = listOf(Permission.ADMINISTRATOR)
    // le bot n'a pas besoin de la permission manage roles car il n'enlève pas les roles directement : voir bouton remove roles

    private val yes = Button.danger("roles-reset;confirm", "Yes I am") // sûr de reset ?
    private val no = Button.secondary("roles-reset;cancel", "No actually")

    private val yes2 = Button.danger("roles-reset;remove-confirm", "Yes") // enlever les rôles des membres ?
    private val no2 = Button.secondary("roles-reset;remove-cancel", "No")

    override fun execute(context: InteractionContext) {
        context as ComponentContext
        val guild = context.guild
        val server = context.server
        when (context.params.firstOrNull()) {
            null -> {
                context.reply(
                    "Are you sure?" +
                            "\nMembers will lost their reward roles."
                )
                    .addActionRow(listOf(yes, no))
                    .queue()
            }

            "cancel" -> {
                context.edit("Roles reset canceled.")
                    .setComponents(emptyList())
                    .queue()
            }

            "confirm" -> {
                context.edit("Do you want to remove reward roles from everyone?")
                    .setActionRow(listOf(yes2, no2))
                    .queue()
            }

            "remove-cancel" -> { // pas vraiment annulé, mais n'enlève pas les rôles discord
                roleManager.reset(server, guild, false)  // n'enlève pas les rôles des membres
                context.edit("Former reward roles will no longer be assigned but the members haven't lost them.")
                    .setComponents(emptyList())
                    .queue()
                wizardManager.edit(context, Wizard.Page.ROLE)
                return
            }

            "remove-confirm" -> {
                roleManager.reset(server, guild, true)
                context.edit("Former reward roles will no longer be assigned and all the members have lost them.")
                    .setComponents(emptyList())
                    .queue()
                wizardManager.edit(context, Wizard.Page.ROLE)
                return
            }
        }
    }
}