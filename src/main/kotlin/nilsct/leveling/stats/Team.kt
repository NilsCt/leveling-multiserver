package nilsct.leveling.stats

import nilsct.leveling.entities.Privacy
import nilsct.leveling.entities.Server
import nilsct.leveling.entities.Type

/**
 * Sum of the stats of every mate of the team
 */
class Team(override val type: Type, val server: Server?) : Stats() {

    override val privacy = Privacy.NORMAL
}