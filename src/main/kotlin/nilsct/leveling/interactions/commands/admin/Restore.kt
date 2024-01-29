package nilsct.leveling.interactions.commands.admin

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import nilsct.leveling.interactions.Interaction
import nilsct.leveling.interactions.InteractionContext
import nilsct.leveling.interactions.commands.CommandContext
import nilsct.leveling.managers.DataSaver
import nilsct.leveling.managers.Log
import nilsct.leveling.managers.ServerManager.Companion.serverManager
import java.io.File

class Restore : Interaction() {

    override val id = "restore"
    override val developer = true
    override val commandData = Commands
        .slash("restore", "Restore deleted data")
        .addOption(OptionType.STRING, "server-name", "name of the server (none for the one you're in)", true, true)
        .addOption(OptionType.ATTACHMENT, "file", "Restore data from this file", true)
        .setGuildOnly(true)
        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))

    private val file = File("saves/custom-load.json")
    private fun downLoad(hook: InteractionHook, file: File, attachment: Message.Attachment, success: (File) -> Unit) {
        Log.log("LOAD", attachment.fileName)
        attachment.proxy.downloadToFile(file).thenAccept(success).exceptionally { e ->
            Log.error("LOAD", "File ${attachment.fileName} $e")
            hook.sendMessage("Error while loading the file ${attachment.fileName}.")
                .setEphemeral(true)
                .queue()
            return@exceptionally null
        }
    }

    override fun execute(context: InteractionContext) {
        context as CommandContext
        val key = context.getOption("server-name")!!.asString
        val server =
            serverManager.get(key) ?: serverManager.getWithName(key) ?: throw Exception("Invalid server key: $key")
        context.deferReply({ hook ->
            val attachment = context.getOption("file")!!.asAttachment
            if (attachment.fileExtension != "json") {
                hook.sendMessage("I can only load data from .json files.")
                    .setEphemeral(true)
                    .queue()
                return@deferReply
            }
            downLoad(hook, file, attachment) { file ->
                DataSaver.dataSaver.restore(
                    file = file,
                    server = server,
                    success = {
                        hook.sendMessage("Data of ${server.team.name} restored.")
                            .setEphemeral(true)
                            .queue()
                    }, failure = {
                        hook.sendMessage("Error while restoring data of ${server.team.name}.")
                            .setEphemeral(true)
                            .queue()
                    }
                )
            }
        })
    }
}