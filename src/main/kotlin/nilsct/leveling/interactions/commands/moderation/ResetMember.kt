package nilsct.leveling.interactions.commands.moderation

import net.dv8tion.jda.api.Permission
import nilsct.leveling.interactions.Interaction
import nilsct.leveling.interactions.InteractionContext
import nilsct.leveling.interactions.commands.CommandContext
import nilsct.leveling.managers.DevAnalysis
import nilsct.leveling.managers.Log
import nilsct.leveling.managers.MemberManager.Companion.memberManager
import nilsct.leveling.managers.RoleManager.Companion.roleManager
import nilsct.leveling.stats.StatsManager.Companion.statsManager

class ResetMember : Interaction() {

    override val id = "reset member"
    override val mainCommand = false
    override val permission = listOf(Permission.ADMINISTRATOR)

    override fun execute(context: InteractionContext) {
        context as CommandContext
        val server = context.server
        val (user, member, lvlMember) = memberManager.testMemberParameterCommand(context) ?: return
        val mate = lvlMember.mate
        val totalXp = mate.totalXP
        statsManager.reset(mate)
        if (member != null) roleManager.updateMember(server, context.guild, lvlMember, member)
        context.reply("${user.asMention}'s have been reset (`$totalXp` xp removed).").queue()
        Log.reset(
            listOf(
                Log.Companion.ResetObject(
                    "member",
                    "${lvlMember.lvlUserID} in ${server.team.name} (${server.id})",
                    mate.name,
                    "moderator ${context.user.name} (${context.user.id})"
                )
            )
        )
        lvlMember.recentlyModified = true
        DevAnalysis.resetMember++
    }
}