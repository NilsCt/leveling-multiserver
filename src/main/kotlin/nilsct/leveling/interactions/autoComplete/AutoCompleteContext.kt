package nilsct.leveling.interactions.autoComplete

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import nilsct.leveling.interactions.InteractionContext

class AutoCompleteContext(private val event: CommandAutoCompleteInteractionEvent) : InteractionContext(event) {

    override val id = event.fullCommandName

    val focusedOption = event.focusedOption

    fun replyChoices(choices: List<Command.Choice>) = event.replyChoices(choices)

    override val tag = "COMPLETE"
    override val description = super.description + " -> ${focusedOption.name} \"${focusedOption.value}\""

}