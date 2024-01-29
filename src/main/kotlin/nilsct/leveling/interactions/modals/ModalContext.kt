package nilsct.leveling.interactions.modals

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import nilsct.leveling.interactions.InteractionContext

class ModalContext(private val event: ModalInteractionEvent) : InteractionContext(event) {

    override val id = event.modalId
    override val tag = "MODAL"
    override val description = super.description + event.values.joinToString(
        prefix = " -> ",
        separator = ", "
    ) { "${it.id} \"${it.asString}\"" }

    fun getValue(id: String) = event.getValue(id)

}