package nilsct.leveling.interactions.commands

import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.components.buttons.Button
import nilsct.leveling.interactions.Interaction
import nilsct.leveling.interactions.InteractionContext


class Invite : Interaction() {

    override val id = "invite"
    override val private = true
    override val commandData = Commands.slash("invite", "Add the bot to your discord server")

    private val button = Button.link(nilsct.leveling.Bot.inviteLink, "Add the bot")
    private val secondButton = Button.link(nilsct.leveling.Bot.supportInvite, "Support Server")

    override fun execute(context: InteractionContext) {
        context as CommandContext
        context.reply(
            """
            Add me to your discord server.
            > You need the permission `administrator` or `manage server` to add a bot in a server.
            > `New` Join the Support Server if you need help.
            """.trimIndent()
        )
            .addActionRow(listOf(button, secondButton))
            .queue()
    }
}