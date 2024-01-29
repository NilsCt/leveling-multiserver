package nilsct.leveling.interactions.commands

import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.components.buttons.Button
import nilsct.leveling.Bot.Companion.mention
import nilsct.leveling.interactions.Interaction
import nilsct.leveling.interactions.InteractionContext
import nilsct.leveling.managers.DevAnalysis


class Help : Interaction() {

    override val id = "help"
    override val private = true
    override val commandData = Commands.slash("help", "Get more help")

    private val embed = nilsct.leveling.Bot.blueEmbed
        .setTitle("Help")
        .setDescription(
            """
            > :dart: - Members __earn xp and level up__ just like in a game!
            > ㅤㅤSend messages and speak in voice channels to earn xp.
            > ㅤㅤ${mention("my-stats")} check your stats in this server
            > ㅤㅤ${mention("member")} check another member's stats
                    
            > :trophy: - Reach the top of the __leaderboard__.
            > ㅤㅤ${mention("leaderboard")} to get a list of the most active members
                    
            > :cow: -  __Cooperate__ with your friends.
            > ㅤㅤ${mention("server")} check the stats of the server
                    
            > :star2: - Level up and __unlock new roles__.
            > ㅤㅤ${mention("my-roles")} check your reward roles
                    
            > :door: - Hide your stats and stop playing.
            > ㅤㅤ${mention("privacy")} for more information
                    
            You need the `admin` permission to configure this bot.
            Type `/` and select leveling to see all the commands you can use.
            `New` Exponential xp system.
        """.trimIndent()
        ).build()

//      ${mention("expiration")} Your stats expire after 1 month of inactivity

//                    "\n> :crossed_swords: Collaborate with your friends and __compete with__\n> ㅤㅤ__other servers__!"
//                    "\n> ㅤㅤ${mention("server")} to check the stats of the server" +
//                    "\n> ㅤㅤ${mention("server-leaderboard")} to check the most active servers" +

    private val adminButtons = listOf(
        Button.secondary("stats;send;image;auto", "Your stats"),
//        Button.secondary("leaderboard-button;send;image;auto", "Leaderboard"),
        Button.primary("set-up;start", "Set-up"),
        Button.link(nilsct.leveling.Bot.inviteLink, "Add me"),
        Button.link(nilsct.leveling.Bot.supportInvite, "Support Server")
    )
    private val buttons = listOf(
        Button.primary("stats;send;image;auto", "Your stats"),
        Button.secondary("leaderboard-button;send;image;auto", "Leaderboard"),
        Button.link(nilsct.leveling.Bot.inviteLink, "Add me"),
        Button.link(nilsct.leveling.Bot.supportInvite, "Support Server")
    )
    private val privateButtons = listOf(
        Button.link(nilsct.leveling.Bot.inviteLink, "Add me"),
        Button.link(nilsct.leveling.Bot.supportInvite, "Support Server")
    )

    override fun execute(context: InteractionContext) {
        context as CommandContext
        val b = when {
            !context.isFromGuild -> privateButtons
            context.isAdmin() -> adminButtons
            else -> buttons
        }
        context.reply()
            .setEphemeral(false)
            .addEmbeds(embed)
            .addActionRow(b)
            .queue()
        DevAnalysis.help++
    }
}