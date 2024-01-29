package nilsct.leveling.interactions.commands.moderation

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import nilsct.leveling.interactions.Interaction
import nilsct.leveling.interactions.InteractionContext
import nilsct.leveling.interactions.commands.CommandContext
import nilsct.leveling.managers.DevAnalysis
import nilsct.leveling.managers.MemberManager.Companion.memberManager
import nilsct.leveling.managers.RoleManager.Companion.roleManager
import nilsct.leveling.stats.StatsManager.Companion.statsManager

class AddXp : Interaction() {

    override val id = "add-xp"
    override val permission = listOf(Permission.ADMINISTRATOR)
    override val commandData = Commands.slash("add-xp", "Add xp to a member")
        .addOption(OptionType.USER, "member", "this member is going to earn xp", true)
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
        statsManager.addXp(lvlMember.mate, amount, context.channel as GuildMessageChannel)
        if (member != null) roleManager.updateMember(server, context.guild, lvlMember, member)
        context.reply("Added `$amount` xp to ${user.asMention}.").queue()
        lvlMember.recentlyModified = true
        DevAnalysis.addXp++
    }
}