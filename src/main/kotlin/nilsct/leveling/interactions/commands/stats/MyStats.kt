package nilsct.leveling.interactions.commands.stats

import net.dv8tion.jda.api.interactions.commands.build.Commands
import nilsct.leveling.interactions.Interaction
import nilsct.leveling.interactions.InteractionContext
import nilsct.leveling.managers.DevAnalysis
import nilsct.leveling.managers.StatsSender

class MyStats : Interaction() {

    override val id = "my-stats"
    override val commandData = Commands
        .slash("my-stats", "Check your stats in this server")
        .setGuildOnly(true)

    override fun execute(context: InteractionContext) {
        StatsSender.check(context, context.lvlMember.mate)
        DevAnalysis.myStats++
    }
}