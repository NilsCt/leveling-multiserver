package nilsct.leveling.managers

import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import nilsct.leveling.entities.LvlMember
import nilsct.leveling.entities.LvlUser
import nilsct.leveling.entities.Server
import nilsct.leveling.interactions.InteractionContext
import nilsct.leveling.interactions.commands.CommandContext
import nilsct.leveling.interactions.context.ContextContext
import nilsct.leveling.managers.ServerManager.Companion.serverManager
import nilsct.leveling.managers.UserManager.Companion.userManager
import nilsct.leveling.stats.StatsManager.Companion.statsManager

class MemberManager {

    companion object {
        val memberManager = MemberManager()
    }

    val lvlMembers = mutableListOf<LvlMember>()

    val size get() = lvlMembers.size

    fun get(server: Server, lvlUser: LvlUser) = server.lvlMembers.firstOrNull { it.lvlUserID == lvlUser.id }

    fun acquire(server: Server, lvlUser: LvlUser, member: Member): LvlMember {
        val lvlMember = get(server, lvlUser) ?: new(server, lvlUser)
        sync(lvlMember, member)
        return lvlMember
    }

    private fun new(server: Server, lvlUser: LvlUser): LvlMember {
        val lvlMember = LvlMember(server, lvlUser)
        server.lvlMembers.add(lvlMember)
        lvlUser.lvlMembers.add(lvlMember)
        lvlMembers.add(lvlMember)
        statsManager.addStatsToGroup(server.group, lvlMember.mate)
        DevAnalysis.newMembers++
        return lvlMember
    }

    private fun sync(lvlMember: LvlMember, member: Member) {
        lvlMember.mate.name = member.effectiveName
        lvlMember.mate.icon = member.effectiveAvatarUrl.removePrefix("https://cdn.discordapp.com/")
    }

    fun reset() {
        lvlMembers.clear()
        for (server in serverManager.servers) {
            server.lvlMembers.clear()
        }
        for (lvlUser in userManager.lvlUsers) {
            lvlUser.lvlMembers.clear()
        }
    }

    fun delete(lvlMember: LvlMember) {
        val server = lvlMember.server
        server.lvlMembers.remove(lvlMember)
        lvlMember.lvlUser.lvlMembers.remove(lvlMember)
        lvlMembers.remove(lvlMember)
        statsManager.removeStatsFromGroup(server.group, lvlMember.mate)
    }

    data class MemberParameterContext( // utilisé dès qu'un membre est en paramètre et qu'on veut être sûr que l'utilisateur donné est valide.
        val user: User,
        val member: Member?,
        val lvlMember: LvlMember
    )

    private fun testMemberParameter(context: InteractionContext, user: User, member: Member?): MemberParameterContext? {
        val server = context.server
        when {
            user.isBot -> {
                context.reply("Unfortunately I don't have stats...")
                    .queue()
                return null
            }

            user.isBot -> {
                context.reply("${user.asMention} is a bot.")
                    .queue()
                return null
            }
        }
        val lvlUser: LvlUser?
        val lvlMember: LvlMember?
        if (member == null) {
            lvlUser = userManager.get(user.id) // n'actualise pas + ne crée pas s'il n'existe pas
            lvlMember = if (lvlUser == null) null else memberManager.get(server, lvlUser)
        } else {
            lvlUser = userManager.acquire(user)
            lvlMember = memberManager.acquire(server, lvlUser, member)
        }
        if (lvlMember == null) {
            context.reply("${user.asMention} hasn't send any messages in this server yet.").queue()
            return null
        }
        return MemberParameterContext(user, member, lvlMember)
    }

    fun testMemberParameterCommand(context: CommandContext): MemberParameterContext? {
        val first = context.getOption("member")!! // paramètre non optionnel
        val user = first.asUser
        val member = first.asMember
        return testMemberParameter(context, user, member)
    }

    fun testMemberParameterContext(context: ContextContext): MemberParameterContext? {
        val user = context.targetUser
        val member = context.targetMember
        return testMemberParameter(context, user, member)
    }
}