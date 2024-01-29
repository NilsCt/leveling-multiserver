package nilsct.leveling.entities

import nilsct.leveling.stats.Mate
import java.time.Instant

class LvlMember(val server: Server, val lvlUser: LvlUser) {

    val serverID = server.id
    val lvlUserID = lvlUser.id

    val mate = Mate(server.team, server, lvlUser, this)

    var wizard: Wizard? = null

    var recentlyModified = false // stats récemment modifiées par un modérateur avec add-xp, remove-xp, reset member
    var recentlyServerReset = false // stats du serveur récemment réinitialisées

    var lastActivity = Instant.now().epochSecond

}