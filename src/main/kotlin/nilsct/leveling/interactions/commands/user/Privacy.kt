package nilsct.leveling.interactions.commands.user

import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.components.selections.SelectOption
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu
import nilsct.leveling.Bot.Companion.blueEmbed
import nilsct.leveling.interactions.Interaction
import nilsct.leveling.interactions.InteractionContext
import nilsct.leveling.interactions.commands.CommandContext
import nilsct.leveling.managers.DevAnalysis

class Privacy : Interaction() {

    override val id = "privacy"
    override val private = true
    override val commandData = Commands.slash("privacy", "Select who can see your stats and disable the bot tracking")

    private val embed = blueEmbed
        .setDescription(
            "You don't want others to know how many messages you've sent?" +
                    "\nSelect your favorite mode among these:"
        )
        .addField(
            ":yum: - Normal",
            """
            `Everybody can see your stats.`
            The bot tracks your messages and the time you spend in voice channels.
            __The bot can't read the content of your messages!__
            """.trimIndent(),
            false
        )
        .addField(
            ":detective: - Private",
            "`Nobody except you can see your stats`, and you won't appear on any leaderboards." +
                    "\nYou don't receive your reward roles.",
            false
        )
        .addField(
            ":ghost: - Ghost",
            "`Your messages and voice time aren't counted.`" +
                    "\nOnly you can see your previous stats.",
            false
        )
        .addField(
            "",
            """
            ${nilsct.leveling.Bot.mention("reset-myself")} to reset your stats in a server
            ${nilsct.leveling.Bot.mention("stop")} to switch between normal and ghost quickly
            ${nilsct.leveling.Bot.mention("expiration")} Your stats expire after 1 month of inactivity
            """.trimIndent(),
            false
        )

    private val selectionMenu = StringSelectMenu
        .create("privacy")
        .addOption("Normal", "NORMAL", "Everybody can see your stats", Emoji.fromUnicode("\uD83D\uDE0B")) // :yum:
        .addOption(
            "Private",
            "PRIVATE",
            "Only you can see your stats",
            Emoji.fromUnicode("\uD83D\uDD75️\u200D♂️")
        ) // :detective:
        .addOption("Ghost", "GHOST", "Your stats aren't tracked", Emoji.fromUnicode("\uD83D\uDC7B")) // :ghost:

    override fun execute(context: InteractionContext) {
        context as CommandContext
        val lvlUser = context.lvlUser
        val privacy = lvlUser.privacy.msg
        context.reply()
            .addEmbeds(embed.setTitle("Your PrivacyMode: `$privacy`").build())
            .addActionRow(
                selectionMenu
                    .setDefaultOptions(listOf(SelectOption.of(privacy, privacy.uppercase())))
                    .build()
            )
            .queue()
        DevAnalysis.privacyCommand++
    }
}