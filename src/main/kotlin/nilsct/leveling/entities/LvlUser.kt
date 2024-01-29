package nilsct.leveling.entities

import java.time.Instant

class LvlUser(val id: String) {

    var privacy = Privacy.NORMAL

    val lvlMembers = mutableListOf<LvlMember>()

    var voiceWarning = true // pas encore prévenu qu'il ne reçoit pas d'xp s'il n'est pas actif sans un salon vocal

    var lastActivity = Instant.now().epochSecond

}