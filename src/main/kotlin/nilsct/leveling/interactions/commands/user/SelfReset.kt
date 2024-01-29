package nilsct.leveling.interactions.commands.user

import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.components.buttons.Button
import nilsct.leveling.interactions.Interaction
import nilsct.leveling.interactions.InteractionContext
import nilsct.leveling.interactions.commands.CommandContext


class SelfReset : Interaction() {

    override val id = "reset-myself"
    override val commandData = Commands.slash("reset-myself", "Reset your stats in this server")
        .setGuildOnly(true)


    private val buttons =
        listOf(Button.danger("self-reset;confirm", "Confirm"), Button.secondary("self-reset;cancel", "Cancel"))

    override fun execute(context: InteractionContext) {
        context as CommandContext
        context.reply(
            "Are you sure?" +
                    "\nYour xp, messages, voice and reward roles will be lost."
        )
            .addActionRow(buttons)
            .queue()
    }
}