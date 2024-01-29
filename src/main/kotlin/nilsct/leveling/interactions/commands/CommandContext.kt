package nilsct.leveling.interactions.commands

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import nilsct.leveling.interactions.InteractionContext

class CommandContext(private val event: SlashCommandInteractionEvent) : InteractionContext(event) {

    override val id = event.fullCommandName
    override val tag = "COMMAND"
    override val description = super.description + event.options.joinToString(
        prefix = " -> ",
        separator = ", "
    ) { "${it.name} \"${it.asString}\"" }

    val channel = event.messageChannel

    fun getOption(name: String) = event.getOption(name)
}