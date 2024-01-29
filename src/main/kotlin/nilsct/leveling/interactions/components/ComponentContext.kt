package nilsct.leveling.interactions.components

import net.dv8tion.jda.api.events.interaction.component.*
import net.dv8tion.jda.api.interactions.components.ActionRow
import nilsct.leveling.interactions.InteractionContext

class ComponentContext(private val event: GenericComponentInteractionCreateEvent) : InteractionContext(event) {

    override val id = event.componentId

    val params = if (event is ButtonInteractionEvent) {
        id
    } else {
        event as GenericSelectMenuInteractionEvent<*, *>
        selectedOptions.first()
    }.split(";").toMutableList().apply { removeFirst() }.toList() // compatible qu'avec une seule option choisie
    val actionRows: MutableList<ActionRow> = event.message.actionRows

    val selectedOptions: List<String>
        get() {
            return when (event) {
                is ButtonInteractionEvent -> throw Exception("Component is a button")
                is StringSelectInteractionEvent -> event.values
                is EntitySelectInteractionEvent -> event.values.map { it.id }
                else -> throw Exception("Invalid event")
            }
        }

    override val tag = when (event) {
        is ButtonInteractionEvent -> "BUTTON"
        is GenericSelectMenuInteractionEvent<*, *> -> "SELECT"
        else -> throw Exception("Invalid event")
    }

    override val description =
        super.description + " -> $params" + if (event is GenericSelectMenuInteractionEvent<*, *>) selectedOptions.joinToString(
            prefix = " ",
            separator = ", "
        ) else ""
}