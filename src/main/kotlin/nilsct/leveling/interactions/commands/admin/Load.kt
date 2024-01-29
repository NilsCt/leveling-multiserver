package nilsct.leveling.interactions.commands.admin

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message.Attachment
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import nilsct.leveling.interactions.Interaction
import nilsct.leveling.interactions.InteractionContext
import nilsct.leveling.interactions.commands.CommandContext
import nilsct.leveling.managers.DataSaver.Companion.dataSaver
import nilsct.leveling.managers.Log
import java.io.File

class Load : Interaction() {

    override val id = "load"
    override val developer = true
    override val commandData = Commands
        .slash("load", "Load a previous back-up")
        .addOption(OptionType.STRING, "confirmation", "type \"confirmation\"", true)
        .addOption(OptionType.ATTACHMENT, "file", "Load data from this file")
        .setGuildOnly(true)
        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))

    private val file = File("saves/custom-load.json")


    private fun downLoad(hook: InteractionHook, file: File, attachment: Attachment, success: (File) -> Unit) {
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
        val confirmation = context.getOption("confirmation")!!.asString
        if (confirmation != "confirmation") {
            context.reply("Please type \"confirmation\".")
                .queue()
            return
        }
        context.deferReply({ hook ->
            val attachment = context.getOption("file")?.asAttachment
            if (attachment == null) {
                dataSaver.load(
                    success = {
                        hook.sendMessage("Data loaded.")
                            .setEphemeral(true)
                            .queue()
                    }, failure = {
                        hook.sendMessage("Error while loading data.")
                            .setEphemeral(true)
                            .queue()
                    }
                )
                return@deferReply
            }
            if (attachment.fileExtension != "json") {
                hook.sendMessage("I can only load data from .json files.")
                    .setEphemeral(true)
                    .queue()
                return@deferReply
            }
            downLoad(hook, file, attachment) { file ->
                dataSaver.load(
                    gFile = file,
                    success = {
                        hook.sendMessage("Custom data loaded.")
                            .setEphemeral(true)
                            .queue()
                    }, failure = {
                        hook.sendMessage("Error while loading custom data.")
                            .setEphemeral(true)
                            .queue()
                    }
                )
            }
        })
    }
}