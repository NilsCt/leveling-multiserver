package nilsct.leveling.interactions.components.levelup

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.Modal
import nilsct.leveling.interactions.Interaction
import nilsct.leveling.interactions.InteractionContext
import nilsct.leveling.interactions.components.ComponentContext
import nilsct.leveling.managers.ServerManager

class Message : Interaction() {

    override val id = "levelup-message"
    override val permission = listOf(Permission.ADMINISTRATOR)

    override fun execute(context: InteractionContext) {
        context as ComponentContext
        val server = context.server
        val modal = Modal
            .create("levelup-message", "Edit the level-up message")
            .addActionRow(
                TextInput
                    .create("message", "Message", TextInputStyle.SHORT)
                    .setRequired(false) // si rien : reset le message
                    .setMaxLength(1000)
                    .setPlaceholder(ServerManager.defaultLvlUpMessage)
                    .setValue(server.lvlUpMessage)
                    .build()
            )
            .build()
        context.modal(modal).queue()
    }
}