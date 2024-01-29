package nilsct.leveling.interactions.commands.user

import net.dv8tion.jda.api.interactions.commands.build.Commands
import nilsct.leveling.Bot.Companion.blueEmbed
import nilsct.leveling.interactions.Interaction
import nilsct.leveling.interactions.InteractionContext
import nilsct.leveling.interactions.commands.CommandContext

class Expiration : Interaction() {

    override val id = "expiration"
    override val private = true
    override val commandData = Commands.slash("expiration", "Get more information about your data expiration")

    private val embed = blueEmbed
        .setTitle("Data Expiration")
        .setDescription(
            """
                To protect your privacy we cannot store your data indefinitely. 
                `Your stats in a specific server will be reset after 1 month of inactivity in that server.`
                
                You are considered inactive if __you do not use any commands or buttons for 1 month__.
                
                :warning: **Warning:**
                Please note that sending a message does not count as an activity. 
                The stats of a server can also be reset if no member is active.
            """.trimIndent()
        )

    override fun execute(context: InteractionContext) {
        context as CommandContext
        context.reply()
            .addEmbeds(embed.build())
            .queue()
    }
}