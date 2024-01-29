package nilsct.leveling.interactions.context

import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.GenericContextInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent
import nilsct.leveling.interactions.InteractionContext

// xd
class ContextContext(private val event: GenericContextInteractionEvent<*>) : InteractionContext(event) {

    override val id = event.fullCommandName

    override val tag = "CONTEXT"
    override val description =
        super.description + if (event is UserContextInteractionEvent) " -> ${targetUser.name} (${targetUser.id})" else ""

    val targetUser: User
        get() {
            if (event is UserContextInteractionEvent) {
                return event.target
            } else {
                throw Exception("Not a user context interaction ${event.type}")
            }
        }
    val targetMember: Member?
        get() {
            if (event is UserContextInteractionEvent) {
                return event.targetMember
            } else {
                throw Exception("Not a user context interaction ${event.type}")
            }
        }
//    val targetMessage: Message
//        get() {
//            if (event is MessageContextInteractionEvent) {
//                return event.target
//            } else {
//                throw Exception("Not a message context interaction ${event.type}")
//            }
//        }
}