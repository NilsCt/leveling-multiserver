package nilsct.leveling.managers

import nilsct.leveling.entities.Privacy
import nilsct.leveling.managers.MemberManager.Companion.memberManager
import nilsct.leveling.managers.ServerManager.Companion.serverManager
import nilsct.leveling.managers.UserManager.Companion.userManager
import java.time.Instant

class PurgeManager {

    /*
    Nettoie les membres et serveurs inactifs pendant plus de 1 mois
    (ceux qui n'ont pas fait d'interactions en 1 mois)
    Objectifs : respecter les critères de Discord et nettoyer la base de données.
     */

    companion object {
        val purgeManager = PurgeManager()
    }

    private val expirationTime = 2592000 // 1 mois (30 jours en secondes)

    private fun syncMembers(then: (() -> Unit)) {
        val now = Instant.now().epochSecond
        val copy = memberManager.lvlMembers.toList()
        val objects = mutableListOf<Log.Companion.ResetObject>()
        for (lvlMember in copy) {
            if (lvlMember.lastActivity + expirationTime < now) {
                val obj = Log.Companion.ResetObject(
                    "member",
                    "${lvlMember.lvlUserID} in ${lvlMember.server.team.name} (${lvlMember.server.id})",
                    lvlMember.mate.name,
                    "purged"
                )
                objects.add(obj)
                memberManager.delete(lvlMember)
            }
        }
        Log.log("PURGE", "${objects.size} members purged")
        Log.reset(objects)
        then()
    }

    private fun syncServers(then: (() -> Unit)) {
        val now = Instant.now().epochSecond
        val copy = serverManager.servers.toList()
        val objects = mutableListOf<Log.Companion.ResetObject>()
        for (server in copy) {
            if (server.lastActivity + expirationTime < now) {
                val obj = Log.Companion.ResetObject("server", server.id, server.team.name, "purged")
                objects.add(obj)
                serverManager.delete(server)
            }
        }
        Log.log("PURGE", "${objects.size} servers purged")
        Log.reset(objects)
        then()
    }

    private fun syncUsers(then: (() -> Unit)) {
        val now = Instant.now().epochSecond
        val copy = userManager.lvlUsers.toList()
        val objects = mutableListOf<Log.Companion.ResetObject>()
        for (lvlUser in copy) {
            /*
            Un user avec privacy GHOST n'utilise pas forcément le bot, mais ne veut pas que son compte soit supprimé.
            En effet, si son compte et supprimé, dès qu'il envoie un message il aura un nouveau compte et ses stats
            seront à nouveau traquées !
             */
            if (lvlUser.lastActivity + expirationTime < now && lvlUser.privacy != Privacy.GHOST) {
                val obj = Log.Companion.ResetObject(
                    "user",
                    lvlUser.id,
                    lvlUser.lvlMembers.firstOrNull()?.mate?.name ?: "",
                    "purged"
                )
                objects.add(obj)
                userManager.delete(lvlUser)
            }
        }
        Log.log("PURGE", "${objects.size} users purged")
        Log.reset(objects)
        then()
    }

    fun daily() {
        try {
            syncServers {
                syncUsers {
                    syncMembers {
                        Log.log("DAILY", "Purge done")
                    }
                }
            }
        } catch (e: Exception) {
            Log.error("DAILY", "Purge $e")
        }
    }
}