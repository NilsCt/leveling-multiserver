package nilsct.leveling.interactions.commands.admin

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.utils.FileUpload
import nilsct.leveling.interactions.Interaction
import nilsct.leveling.interactions.InteractionContext
import nilsct.leveling.interactions.commands.CommandContext
import nilsct.leveling.managers.ChartManager.Companion.chartManager
import nilsct.leveling.managers.DataSaver.Companion.dataSaver
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

class BackUp : Interaction() {

    override val id = "back-up"
    override val developer = true
    override val commandData = Commands
        .slash("back-up", "Get the latest back-up")
        .setGuildOnly(true)
        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))

    private val dateFormat = SimpleDateFormat("yy-MM-dd")

    override fun execute(context: InteractionContext) {
        context as CommandContext
        val date = dateFormat.format(Date.from(Instant.now()))
        context.reply()
            .setEphemeral(false) // laisse les fichiers dans le channel
            .addFiles(
                listOf(
                    FileUpload.fromData(dataSaver.file, "backup-$date.json"),
                    FileUpload.fromData(dataSaver.cautionFile, "caution-backup-$date.json"),
                    FileUpload.fromData(chartManager.file, "growth-$date.csv")
                )
            )
            .queue()
    }
}