package nilsct.leveling.interactions.context

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.build.Commands
import nilsct.leveling.interactions.Interaction
import nilsct.leveling.interactions.InteractionContext
import nilsct.leveling.managers.DevAnalysis
import nilsct.leveling.managers.Log
import nilsct.leveling.managers.MemberManager.Companion.memberManager
import nilsct.leveling.managers.RoleManager.Companion.roleManager
import nilsct.leveling.stats.StatsManager.Companion.statsManager

class ContextReset : Interaction() {

    override val id = "Reset stats"
    override val permission = listOf(Permission.ADMINISTRATOR)
    override val commandData = Commands
        .user("Reset stats")
        .setGuildOnly(true)
        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))

    override fun execute(context: InteractionContext) {
        context as ContextContext
        val server = context.server
        val (user, member, lvlMember) = memberManager.testMemberParameterContext(context) ?: return
        val totalXp = lvlMember.mate.totalXP
        statsManager.reset(lvlMember.mate)
        if (member != null) roleManager.updateMember(server, context.guild, lvlMember, member)
        context.reply("${user.asMention}'s stats have been reset (`$totalXp` xp removed).").queue()
        Log.reset(
            listOf(
                Log.Companion.ResetObject(
                    "member",
                    "${lvlMember.lvlUserID} in ${server.team.name} (${server.id})",
                    lvlMember.mate.name,
                    "moderator ${context.user.name} (${context.user.id})"
                )
            )
        )
        lvlMember.recentlyModified = true
        DevAnalysis.resetMember++
    }
}