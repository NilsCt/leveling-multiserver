package nilsct.leveling.managers

import nilsct.leveling.entities.Comparator
import nilsct.leveling.stats.Group
import nilsct.leveling.stats.Stats

class LeaderboardManager {

    companion object {
        val leaderboardManager = LeaderboardManager()
    }

    fun leaderboard(group: Group, comparator: Comparator = Comparator.XP): List<Stats> {
        return group.list.filter { it.public }.sortedByDescending(comparator.compare)
    }

    fun rank(group: Group, stats: Stats, comparator: Comparator = Comparator.XP): Int {
        if (!stats.public) throw Exception("Private stats (rank) ${stats.name}")
        val index = leaderboard(group, comparator).indexOf(stats)
        if (index == -1) throw Exception("Stats isn't in Group ${group.name}, ${stats.name}")
        return index + 1
    }

    private fun podium(group: Group, comparator: Comparator = Comparator.XP): List<Stats> {
        val lb = leaderboard(group, comparator)
        return lb.subList(0, 6.coerceAtMost(lb.size))
    }

    fun notInTop4(group: Group, centered: Stats, comparator: Comparator = Comparator.XP): Boolean {
        val podium = podium(group, comparator)
        return centered !in podium.subList(0, 4.coerceAtMost(podium.size))
    }

    fun six( // seulement pour les images
        group: Group,
        comparator: Comparator = Comparator.XP,
        centered: Stats? = null
    ): Pair<List<Stats>, Int> { // stats list, first rank
        if (centered == null) return podium(group, comparator) to 1
        val lb = leaderboard(group, comparator)
        val rank = rank(group, centered, comparator)
        return lb.subList(
            (rank - 4).coerceAtLeast(0), // 3 avant
            (rank + 2).coerceAtMost(lb.size) // 2 après
        ) to (rank - 3).coerceAtLeast(0) // first rank
    }
}