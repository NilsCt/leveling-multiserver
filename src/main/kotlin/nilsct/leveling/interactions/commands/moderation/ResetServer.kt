package nilsct.leveling.interactions.commands.moderation

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.interactions.components.buttons.Button
import nilsct.leveling.interactions.Interaction
import nilsct.leveling.interactions.InteractionContext
import nilsct.leveling.interactions.commands.CommandContext
import nilsct.leveling.managers.DevAnalysis
import java.time.Instant

class ResetServer : Interaction() {

    override val id = "reset server"
    override val permission = listOf(Permission.ADMINISTRATOR)
    override val commandData = Commands.slash("reset", "Reset stats in this server")
        .addSubcommands(
            SubcommandData("server", "Reset the stats of every member and of the server")
        )
        .addSubcommands(
            SubcommandData("member", "Reset the stats of a member")
                .addOption(OptionType.USER, "member", "this member is going to lose their stats", true)
        )
        .setGuildOnly(true)
        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))

    private val yes = Button.danger("reset-server;", "Yes")
    private val no = Button.secondary("reset-server;cancel", "No")

    override fun execute(context: InteractionContext) {
        context as CommandContext
        context.reply(
            "The stats of __every member and of the server__ will be deleted forever." +
                    "\nDo you still want to continue?"
        )
            .addActionRow(listOf(yes.withId("reset-server;${Instant.now().epochSecond}"), no))
            .queue()
        context.server.lvlMembers.forEach { it.recentlyServerReset = true }
        DevAnalysis.resetServerCommand++
    }
}