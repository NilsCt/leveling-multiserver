package nilsct.leveling.interactions.context

import net.dv8tion.jda.api.interactions.commands.build.Commands
import nilsct.leveling.interactions.Interaction
import nilsct.leveling.interactions.InteractionContext
import nilsct.leveling.managers.MemberManager.Companion.memberManager
import nilsct.leveling.managers.StatsSender

class ContextStats : Interaction() {

    override val id = "Check stats"
    override val commandData = Commands
        .user("Check stats")
        .setGuildOnly(true)

    override fun execute(context: InteractionContext) {
        context as ContextContext
        val (_, _, lvlMember) = memberManager.testMemberParameterContext(context) ?: return
        val stats = lvlMember.mate
        if (StatsSender.testPrivacy(context, stats)) return
        StatsSender.check(context, stats)
    }
}