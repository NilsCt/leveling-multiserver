package nilsct.leveling.managers

import net.dv8tion.jda.api.entities.User
import nilsct.leveling.entities.LvlUser
import nilsct.leveling.managers.MemberManager.Companion.memberManager

class UserManager {

    companion object {
        val userManager = UserManager()
    }

    val lvlUsers = mutableListOf<LvlUser>()  // utilisé pour les lb + discord

    val size get() = lvlUsers.size

    fun get(id: String) = lvlUsers.firstOrNull { it.id == id }

    fun acquire(user: User) = get(user.id) ?: new(user)

    private fun new(user: User): LvlUser {
        if (user.isBot) throw Exception("User ${user.name} is a bot")
        val lvlUser = LvlUser(user.id)
        lvlUsers.add(lvlUser)
        Log.newUser(user)
        DevAnalysis.newUsers++
        return lvlUser
    }

    fun reset() {
        lvlUsers.clear()
    }

    fun delete(lvlUser: LvlUser) {
        lvlUsers.remove(lvlUser)
        for (lvlMember in lvlUser.lvlMembers.toList()) {
            memberManager.delete(lvlMember)
        }
    }
}