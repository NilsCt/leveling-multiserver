package nilsct.leveling.stats

import nilsct.leveling.entities.Server
import nilsct.leveling.entities.Type

/**
 * Has a list of stats in competition
 */
class Group(val type: Type, val server: Server?) {

    val list = mutableListOf<Stats>()

    var name = ""
}