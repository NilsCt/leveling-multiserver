package nilsct.leveling.interactions.components

import net.dv8tion.jda.api.Permission
import nilsct.leveling.interactions.Interaction
import nilsct.leveling.interactions.InteractionContext
import nilsct.leveling.managers.DevAnalysis
import nilsct.leveling.managers.Log
import nilsct.leveling.managers.RoleManager.Companion.roleManager
import nilsct.leveling.stats.StatsManager.Companion.statsManager

class ResetServerButton : Interaction() {

    override val id = "reset-server"
    override val permission = listOf(Permission.ADMINISTRATOR)

    override fun execute(context: InteractionContext) {
        context as ComponentContext
        val server = context.server
        if (context.params.first() == "cancel") {
            context.edit("Server reset canceled.")
                .setComponents(emptyList())
                .queue()
        } else {
            /*
            Pas un serveur delete car tous les paramètres sont conservés (reward roles, lvl up)
            Seulement les stats sont réinitialisées
             */
            statsManager.reset(server.team)
            for (stats in server.group.list) {
                statsManager.reset(stats)
            }
            roleManager.updateServer(server, context.guild)
            context.edit("The stats of the server and of the members have been reset.")
                .setComponents(emptyList())
                .queue()
            Log.reset(
                listOf(
                    Log.Companion.ResetObject(
                        "server",
                        server.id,
                        server.team.name,
                        "moderator ${context.user.name} (${context.user.id})"
                    )
                )
            )
            DevAnalysis.resetServer++
        }
    }
}