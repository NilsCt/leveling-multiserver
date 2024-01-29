package nilsct.leveling.entities

import nilsct.leveling.managers.ServerManager
import nilsct.leveling.stats.Group
import nilsct.leveling.stats.Team
import java.time.Instant

class Server(val id: String) {

    val rewardRoles = mutableListOf<RewardRole>()
    var stack = false // si les rôles s'accumulent

    var lvlUpChannel: String = "auto" // "auto" pour endroit où la personne envoie un message / "" si désactivé
    var lvlUpMessage: String = ServerManager.defaultLvlUpMessage

    val team = Team(Type.SERVER, this)
    val group = Group(Type.SERVER, this)

    val lvlMembers = mutableListOf<LvlMember>()

    var lastActivity = Instant.now().epochSecond
}