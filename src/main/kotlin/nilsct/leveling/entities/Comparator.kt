package nilsct.leveling.entities

import nilsct.leveling.simplify
import nilsct.leveling.stats.Stats
import nilsct.leveling.timeFormat

enum class Comparator(
    val compare: (stats: Stats) -> Int,
    val txt: (stats: Stats) -> String
) {
    XP(
        { stats -> stats.totalXP },
        { stats -> "lvl ${stats.lvl.simplify()}  xp ${stats.xp.simplify()}" }
    ),
    MESSAGES(
        { stats -> stats.messages },
        { stats -> "${stats.messages.simplify()} msg" }
    ),
    VOICE(
        { stats -> stats.activeVoice },
        { stats -> stats.activeVoice.timeFormat() }
    )
}