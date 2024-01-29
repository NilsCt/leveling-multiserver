package nilsct.leveling.interactions.commands.stats

import net.dv8tion.jda.api.interactions.commands.build.Commands
import nilsct.leveling.interactions.Interaction
import nilsct.leveling.interactions.InteractionContext
import nilsct.leveling.interactions.commands.CommandContext
import nilsct.leveling.managers.DevAnalysis
import nilsct.leveling.managers.StatsSender
import nilsct.leveling.stats.StatsManager

class Discord : Interaction() {

    override val id = "discord"
    override val private = true
    override val commandData = Commands.slash("discord", "Check discord global stats")

    override fun execute(context: InteractionContext) {
        context as CommandContext
        StatsSender.check(context, StatsManager.discord)
        DevAnalysis.discord++
    }
}