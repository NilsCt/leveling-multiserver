package nilsct.leveling.stats

import nilsct.leveling.entities.LvlMember
import nilsct.leveling.entities.LvlUser
import nilsct.leveling.entities.Server
import nilsct.leveling.entities.Type

/**
 * Class for members of a server (mate of a team)
 */
class Mate(
    val team: Team,
    val server: Server,
    val lvlUser: LvlUser,
    val lvlMember: LvlMember
) : Stats() {

    override val type = Type.MEMBER

    // Dernière fois que ce membre a rejoint un salon vocal (null s'il n'est pas en vocal)
    var voiceJoin: Long? = null

    // Début d'activité dans un salon vocal : ni muet, ni sourd, ni seul (sans compter les membres muets et sourds)
    var activeVoiceStart: Long? = null


    override val privacy get() = lvlUser.privacy

}