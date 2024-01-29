package nilsct.leveling.interactions.commands.admin

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.utils.FileUpload
import nilsct.leveling.Bot.Companion.jda
import nilsct.leveling.interactions.Interaction
import nilsct.leveling.interactions.InteractionContext
import nilsct.leveling.interactions.commands.CommandContext
import nilsct.leveling.managers.DevAnalysis.Companion.devAnalysis
import nilsct.leveling.managers.Log

class Latest : Interaction() {

    override val id = "latest"
    override val developer = true
    override val commandData = Commands
        .slash("latest", "Get the latest interactions")
        .setGuildOnly(true)
        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))

    override fun execute(context: InteractionContext) {
        context as CommandContext
        jda.restPing.queue { time ->
            context.reply(
                """
                Guilds: ${jda.guilds.size}
                Rest ping: $time ms
                Image ping: ${devAnalysis.imageDownloadPing()} ms
                Rank ping: ${devAnalysis.rankPing()} ms
                Leaderboard ping: ${devAnalysis.leaderboardPing()} ms
                """.trimIndent()
            )
                .setEphemeral(false) // laisse le fichier
                .addFiles(FileUpload.fromData(Log.latest()))
                .queue()
        }
    }
}