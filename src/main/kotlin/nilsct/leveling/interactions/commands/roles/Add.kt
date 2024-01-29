package nilsct.leveling.interactions.commands.roles

import net.dv8tion.jda.api.Permission
import nilsct.leveling.entities.Wizard
import nilsct.leveling.interactions.Interaction
import nilsct.leveling.interactions.InteractionContext
import nilsct.leveling.interactions.commands.CommandContext
import nilsct.leveling.managers.DevAnalysis
import nilsct.leveling.managers.RoleManager.Companion.roleManager
import nilsct.leveling.managers.WizardManager.Companion.wizardManager

class Add : Interaction() {

    override val id = "roles add"
    override val mainCommand = false
    override val permission = listOf(Permission.ADMINISTRATOR)
    override val botPermission = listOf(Permission.MANAGE_ROLES)


    override fun execute(context: InteractionContext) {
        context as CommandContext
        val server = context.server
        val guild = context.guild
        val role = context.getOption("role")!!.asRole
        val lvl = context.getOption("level")!!.asInt
        val rewardRole = roleManager.get(server, role.id)
        when {
            role.isPublicRole -> {
                context
                    .reply("Uh, ${role.asMention} already has this role...")
                    .queue()
            }

            role.tags.isBot -> {
                context
                    .reply("${role.asMention} is only for a specific bot.")
                    .queue()
            }

            role.tags.isBoost -> {
                context
                    .reply("${role.asMention} is only for nitro boosters...")
                    .queue()
            }

            role.tags.isIntegration -> {
                context
                    .reply("I can't use ${role.asMention} because it is managed by an integration.")
                    .queue()
            }

            !context.selfMember.canInteract(role) -> {
                context
                    .reply("I can't manage ${role.asMention} because it is higher than my best role.")
                    .queue()
            }

            lvl < 2 -> { // normalement pas possible avec la required range
                context
                    .reply("The selected level must be equal to or greater than 2.")
                    .queue()
            }

            lvl > 5000 -> { // normalement pas possible avec la required range
                context
                    .reply("Nobody will ever reach this level. (max 1000)")
                    .queue()
            }

            rewardRole?.lvl == lvl -> {
                context
                    .reply("${rewardRole.mention} has already been added at level `$lvl`.")
                    .queue()
            }

            else -> {
                val content = if (rewardRole == null) {
                    roleManager.add(server, guild, role, lvl)
                    "${role.asMention} has been **added** at level `$lvl`."
                } else {
                    val previous = rewardRole.lvl
                    roleManager.edit(server, guild, rewardRole, lvl)
                    "${role.asMention} has been **edited** from level `$previous to $lvl`."
                }
                context.reply(content)
                    .queue()
                wizardManager.edit(context, Wizard.Page.ROLE)
                DevAnalysis.rolesAdded++
            }
        }
    }
}