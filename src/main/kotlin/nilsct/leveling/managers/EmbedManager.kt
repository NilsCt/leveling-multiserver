package nilsct.leveling.managers

import net.dv8tion.jda.api.entities.MessageEmbed
import nilsct.leveling.detailedTimeFormat
import nilsct.leveling.entities.Comparator
import nilsct.leveling.entities.Type
import nilsct.leveling.managers.LeaderboardManager.Companion.leaderboardManager
import nilsct.leveling.simplify
import nilsct.leveling.stats.Mate
import nilsct.leveling.stats.Stats
import nilsct.leveling.stats.StatsManager.Companion.statsManager
import nilsct.leveling.stats.Team
import java.io.File
import java.io.FileWriter
import kotlin.math.roundToInt

class EmbedManager {

    companion object {
        val embedManager = EmbedManager()
    }

    private val file = File("/tmp/text.txt")

    private fun getDownLoad(stats: Stats): String {
        val progression = (stats.xp.toFloat() / stats.requiredXP * 10)
        var msg = ""
        for (e in 0 until progression.toInt()) msg += ":green_square:"
        msg += ":orange_square:"
        for (e in 0 until 9 - progression.toInt()) msg += ":red_square:"
        msg += " ${(progression * 10).roundToInt()}%"
        return msg
    }

    fun rank(stats: Stats): MessageEmbed {
        if (stats is Mate) statsManager.update(stats)
        // Verbeux, car les mates ne sont plus stockées dans une team
        if (stats is Team && stats.server != null) statsManager.update(stats.server.group)
        val group = statsManager.getGroup(stats)
        val rank = if (group != null && stats.type == Type.MEMBER && stats.public) {
            " #${leaderboardManager.rank(group, stats)}"
        } else {
            ""
        }
        val icon =
            if (stats.type == Type.DISCORD) "attachment://discord.png" else "https://cdn.discordapp.com/${stats.icon}"
        val embed = nilsct.leveling.Bot.blueEmbed
            .setAuthor("${stats.name}$rank", null, icon.takeUnless { it.isEmpty() })
            .addField("level", stats.lvl.simplify(), true)
            .addField("xp", "${stats.xp.simplify()} / ${stats.requiredXP}", true)
            .addField("Progression", getDownLoad(stats), false)
            .addField(stats.messages.simplify(), "sent messages", true)
            .addBlankField(true)
            .addField(stats.activeDays.simplify(), "active days", true)
            .addField(stats.voice.detailedTimeFormat(), "voice time", true)
            .addBlankField(true)
            .addField(stats.activeVoice.detailedTimeFormat(), "active voice time", true)
        when (stats.type) {
            Type.DISCORD -> embed.setDescription("Global stats of everyone!") // discord
            Type.MEMBER -> {
                stats as Mate
                embed.setDescription("Only in ${stats.server.team.name}.")
            } // membre
            Type.SERVER, Type.ALL_SERVERS -> {}
        }
        return embed.build()
    }

    fun leaderboard( // amélioration possible : aligner lvl et xp
        lbContext: ComponentManager.LBContext,
        callback: (File) -> Unit
    ) {
        val (group, comparator) = lbContext
        statsManager.update(group)
        val statsList = leaderboardManager.leaderboard(group, comparator)
        val writer = FileWriter(file)
        writer.write(LeaderboardSender.content(lbContext, false) + "\n")
        statsList.forEachIndexed { index, stats ->
            val rank = index + 1
            val nbr = if (rank > 9) "$rank" else "$rank " // pour aligner les premiers numéros
            if (comparator == Comparator.XP) {
                val msg =
                    "$nbr ${stats.name} lvl ${stats.lvl.simplify()}" // je n'utilise pas comparator.txt pour aligner xp et lvl
                val space = " ".repeat((50 - msg.length).coerceAtLeast(0))
                val msg2 = "xp ${stats.xp.simplify()}"
                val space2 = " ".repeat((8 - msg2.length).coerceAtLeast(0))
                writer.append("\n$nbr ${stats.name}  ${space}lvl ${stats.lvl.simplify()}  $space2$msg2")

            } else {
                val msg = "$nbr ${stats.name} ${comparator.txt(stats)}"
                val space = " ".repeat((60 - msg.length).coerceAtLeast(0))
                writer.append("\n$nbr ${stats.name}  $space${comparator.txt(stats)}")
            }
        }
        writer.close()
        callback(file)
    }
}