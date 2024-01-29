package nilsct.leveling.interactions.components

import net.dv8tion.jda.api.interactions.components.buttons.Button
import nilsct.leveling.interactions.Interaction
import nilsct.leveling.interactions.InteractionContext
import nilsct.leveling.managers.DevAnalysis
import nilsct.leveling.managers.Log
import nilsct.leveling.stats.StatsManager.Companion.statsManager

class SelfResetButton : Interaction() {

    override val id = "self-reset"

    private val button = Button.danger("self-reset;all", "Reset your stats in every server")

    override fun execute(context: InteractionContext) {
        context as ComponentContext
        when (context.params.first()) {
            "cancel" -> {
                context.edit("Reset canceled.")
                    .setComponents(emptyList())
                    .queue()
            }

            "confirm" -> {
                val lvlMember = context.lvlMember
                val mate = lvlMember.mate
                val totalXp = mate.totalXP
                statsManager.reset(mate)
                context.edit("Your stats in __this server__ have been reset (`$totalXp` xp removed).")
                    .setActionRow(button)
                    .queue()
                Log.reset(
                    listOf(
                        Log.Companion.ResetObject(
                            "member",
                            "${lvlMember.lvlUserID} in ${context.server.team.name} (${context.server.id})",
                            mate.name,
                            "self"
                        )
                    )
                )
                DevAnalysis.selfReset++
            }

            "all" -> {
                var totalXp = 0
                for (lvlMember in context.lvlUser.lvlMembers) {
                    totalXp += lvlMember.mate.totalXP
                    statsManager.reset(lvlMember.mate)
                }
                context.edit("Your stats in __every server__ have been reset (`$totalXp` xp removed).")
                    .setComponents(emptyList())
                    .queue()
                Log.reset(listOf(Log.Companion.ResetObject("user", context.lvlUser.id, context.user.name, "self")))
            }
        }
    }
}