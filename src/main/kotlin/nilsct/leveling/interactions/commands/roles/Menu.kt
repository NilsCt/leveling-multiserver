package nilsct.leveling.interactions.commands.roles

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import nilsct.leveling.entities.Wizard
import nilsct.leveling.interactions.Interaction
import nilsct.leveling.interactions.InteractionContext
import nilsct.leveling.interactions.commands.CommandContext
import nilsct.leveling.managers.DevAnalysis
import nilsct.leveling.managers.WizardManager.Companion.wizardManager

class Menu : Interaction() {

    override val id = "roles menu"
    override val permission = listOf(Permission.ADMINISTRATOR)
    override val commandData =
        Commands.slash("roles", "Set up reward roles that will be added to members at specific levels")
            .addSubcommands(
                SubcommandData("menu", "Edit the reward roles settings")
            )
            .addSubcommands(
                SubcommandData("add", "Add a reward role")
                    .addOption(OptionType.ROLE, "role", "role added to members", true)
                    .addOptions(
                        OptionData(OptionType.INTEGER, "level", "required level to earn this role", true)
                            .setRequiredRange(2, 5000)
                    )
            )
            .addSubcommands(
                SubcommandData("remove", "Remove a reward role")
                    .addOption(OptionType.ROLE, "role", "the reward role to remove", true)
            ) // amélioration possible : commandes avec comme choix les reward roles
            .setGuildOnly(true)
            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))


    override fun execute(context: InteractionContext) {
        context as CommandContext
        wizardManager.open(context, Wizard.Page.ROLE, false)
        DevAnalysis.rolesMenu++
    }
}