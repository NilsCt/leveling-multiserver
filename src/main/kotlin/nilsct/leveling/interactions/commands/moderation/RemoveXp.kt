package nilsct.leveling.interactions.commands.moderation

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import nilsct.leveling.interactions.Interaction
import nilsct.leveling.interactions.InteractionContext
import nilsct.leveling.interactions.commands.CommandContext
import nilsct.leveling.managers.DevAnalysis
import nilsct.leveling.managers.Log
import nilsct.leveling.managers.MemberManager.Companion.memberManager
import nilsct.leveling.managers.RoleManager.Companion.roleManager
import nilsct.leveling.stats.StatsManager.Companion.statsManager

class RemoveXp : Interaction() {

    override val id = "remove-xp"
    override val permission = listOf(Permission.ADMINISTRATOR)
    override val commandData = Commands.slash("remove-xp", "Remove xp from a member")
        .addOption(OptionType.USER, "member", "this member is going to lose xp", true)
        .addOptions(
            OptionData(OptionType.INTEGER, "xp", "amount of xp", true)
                .setRequiredRange(1, 100000)
        )
        .setGuildOnly(true)
        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))


    override fun execute(context: InteractionContext) {
        context as CommandContext
        val server = context.server
        val amount = context.getOption("xp")!!.asInt
        val (user, member, lvlMember) = memberManager.testMemberParameterCommand(context) ?: return
        val removed = statsManager.removeXp(lvlMember.mate, amount)
        if (member != null) roleManager.updateMember(server, context.guild, lvlMember, member)
        context.reply("Removed `$removed` xp from ${user.asMention}.").queue()
        Log.reset(
            listOf(
                Log.Companion.ResetObject(
                    "member",
                    "${lvlMember.lvlUserID} in ${server.team.name} (${server.id})",
                    lvlMember.mate.name,
                    "moderator ${context.user.name} (${context.user.id}) removed $removed xp"

                )
            )
        )
        lvlMember.recentlyModified = true
        DevAnalysis.removeXp++
    }
}