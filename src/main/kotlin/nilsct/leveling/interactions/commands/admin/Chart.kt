package nilsct.leveling.interactions.commands.admin

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.utils.FileUpload
import nilsct.leveling.interactions.Interaction
import nilsct.leveling.interactions.InteractionContext
import nilsct.leveling.interactions.commands.CommandContext
import nilsct.leveling.managers.ChartManager
import nilsct.leveling.managers.ChartManager.Companion.chartManager

class Chart : Interaction() {

    override val id = "chart"
    override val developer = true
    override val commandData = Commands
        .slash("chart", "Get a chart")
        .addOptions(
            OptionData(OptionType.STRING, "type", "the type of chart you want", true)
                .addChoice("Servers", "SERVERS")
                .addChoice("Users", "USERS")
                .addChoice("New Servers", "NEW_SERVERS")
                .addChoice("New Users", "NEW_USERS")
                .addChoice("XP", "XP")
                .addChoice("Interactions", "INTERACTIONS")
        )
        .setGuildOnly(true)
        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))


    override fun execute(context: InteractionContext) {
        context as CommandContext
        val option = context.getOption("type")!!.asString
        val type = ChartManager.Chart.valueOf(option)
        chartManager.chart(type,
            success = {
                context.reply()
                    .setEphemeral(false) // laisse le fichier
                    .addFiles(FileUpload.fromData(it))
                    .queue()
            }, failure = {
                context.reply("Error while creating chart!").queue()
            })
    }
}