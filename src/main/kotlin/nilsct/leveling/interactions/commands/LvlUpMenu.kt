package nilsct.leveling.interactions.commands

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import nilsct.leveling.entities.Wizard
import nilsct.leveling.interactions.Interaction
import nilsct.leveling.interactions.InteractionContext
import nilsct.leveling.managers.DevAnalysis
import nilsct.leveling.managers.WizardManager.Companion.wizardManager

class LvlUpMenu : Interaction() {

    override val id = "level-up menu"
    override val permission = listOf(Permission.ADMINISTRATOR)
    override val commandData =
        Commands.slash("level-up", "Set a message that will be automatically sent when a member levels up")
            .addSubcommands(
                SubcommandData("menu", "Edit the level-up message settings")
            )
            .setGuildOnly(true)
            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))

    override fun execute(context: InteractionContext) {
        context as CommandContext
        wizardManager.open(context, Wizard.Page.LVL_UP, false)
        DevAnalysis.lvlMenu++
    }
}