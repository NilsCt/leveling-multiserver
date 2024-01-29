package nilsct.leveling.interactions.commands.stats

import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import nilsct.leveling.interactions.Interaction
import nilsct.leveling.interactions.InteractionContext
import nilsct.leveling.interactions.commands.CommandContext
import nilsct.leveling.managers.DevAnalysis
import nilsct.leveling.managers.MemberManager.Companion.memberManager
import nilsct.leveling.managers.StatsSender

class Member : Interaction() {

    override val id = "member"
    override val commandData = Commands.slash("member", "Check the stats of a member")
        .addOption(OptionType.USER, "member", "a member of this server", true)
        .setGuildOnly(true)

    override fun execute(context: InteractionContext) {
        context as CommandContext
        val (_, _, lvlMember) = memberManager.testMemberParameterCommand(context) ?: return
        val stats = lvlMember.mate
        if (StatsSender.testPrivacy(context, stats)) return
        StatsSender.check(context, stats)
        DevAnalysis.memberCommand++
    }
}