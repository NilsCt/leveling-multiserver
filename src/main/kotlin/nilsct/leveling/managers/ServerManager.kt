package nilsct.leveling.managers

import net.dv8tion.jda.api.entities.Guild
import nilsct.leveling.entities.Server
import nilsct.leveling.managers.MemberManager.Companion.memberManager
import nilsct.leveling.stats.StatsManager
import nilsct.leveling.stats.StatsManager.Companion.statsManager

class ServerManager {

    companion object {
        val serverManager = ServerManager()

        const val defaultLvlUpMessage = "<member> reached level **<lvl>**!"
    }

    val servers = mutableListOf<Server>()

    val size get() = servers.size

    fun get(id: String) = servers.firstOrNull { it.id == id }

    fun getWithName(name: String) =
        servers.firstOrNull { it.team.name.trim().lowercase() == name.trim().lowercase() }

    fun acquire(guild: Guild): Server {
        val server = get(guild.id) ?: new(guild)
        sync(server, guild)
        return server
    }

    private fun new(guild: Guild): Server {
        val server = Server(guild.id)
        servers.add(server)
        statsManager.addStatsToGroup(StatsManager.allServers, server.team)
        Log.newGuild(guild)
        DevAnalysis.newServers++
        return server
    }

    private fun sync(server: Server, guild: Guild) {
        server.team.name = guild.name
        server.group.name = guild.name
        server.team.icon = guild.iconUrl?.removePrefix("https://cdn.discordapp.com/") ?: ""
    }

    fun reset() {
        servers.clear()
    }

    fun delete(server: Server) {
        servers.remove(server)
        statsManager.removeStatsFromGroup(StatsManager.allServers, server.team)
        for (lvlMember in server.lvlMembers.toList()) {
            memberManager.delete(lvlMember)
        }
    }

    fun search(key: String): List<Server> {
        val sorting: (it: Server) -> Int = { it.group.list.size }
        val matching = if (key == "") emptyList() else {
            mutableListOf<Server>().apply {
                addAll(servers.filter { it.id == key })
                addAll(servers.filter { it.team.name == key }.sortedByDescending(sorting))
                addAll(servers.filter { it.id.startsWith(key) }.sortedByDescending(sorting))
                addAll(servers.filter { it.team.name.startsWith(key) }.sortedByDescending(sorting))
                addAll(servers.filter { key in it.team.name }.sortedByDescending(sorting))
            }.distinct()
        }
        // Si aucun server ne correspond aux mots clés, on propose les plus gros
        val final = matching.takeUnless { it.isEmpty() } ?: servers.sortedByDescending(sorting)
        return final.takeUnless { it.size > 25 } ?: final.subList(0, 25)
    }
}