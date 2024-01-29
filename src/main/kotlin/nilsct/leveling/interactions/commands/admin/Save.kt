package nilsct.leveling.interactions.commands.admin

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.build.Commands
import nilsct.leveling.interactions.Interaction
import nilsct.leveling.interactions.InteractionContext
import nilsct.leveling.interactions.commands.CommandContext
import nilsct.leveling.managers.DataSaver.Companion.dataSaver

class Save : Interaction() {

    override val id = "save"
    override val developer = true
    override val commandData = Commands
        .slash("save", "Save the current data")
        .setGuildOnly(true)
        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))

    override fun execute(context: InteractionContext) {
        context as CommandContext
        context.deferReply({ hook ->
            dataSaver.save(
                success = {
                    hook.sendMessage("Data saved.")
                        .setEphemeral(true)
                        .queue()
                }, failure = {
                    hook.sendMessage("Error while saving data.")
                        .setEphemeral(true)
                        .queue()
                }
            )
        })
    }
}