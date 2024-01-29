package nilsct.leveling.stats


import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import nilsct.leveling.entities.Type
import nilsct.leveling.managers.DevAnalysis
import nilsct.leveling.managers.LvlUpManager
import java.time.Instant
import java.util.*

class StatsManager {

    companion object {
        val statsManager = StatsManager()

        val discord = Team(Type.DISCORD, null).apply { name = "Discord" }
        val allServers = Group(Type.ALL_SERVERS, null)
    }

    fun addStatsToGroup(group: Group, stats: Stats) {
        group.list.add(stats)
    }

    fun removeStatsFromGroup(group: Group, stats: Stats) {
        group.list.remove(stats)
    }

    fun getGroup(stats: Stats): Group? {
        return when (stats.type) {
            Type.MEMBER -> {
                stats as? Mate ?: throw Exception("Type is Member but not a mate ${stats.name}")
                stats.server.group
            }

            Type.SERVER -> allServers
            Type.DISCORD -> null
            else -> throw Exception("Invalid stats type (get group) ${stats.type}")
        }
    }

    fun addXp(stats: Stats, value: Int, channel: GuildMessageChannel? = null, load: Boolean = false) {
        stats.run {
            var lvlUp = false
            totalXP += value
            var x = xp + value
            while (x >= requiredXP) { // fait passer les niveaux
                x -= requiredXP
                lvl++
                lvlUp = true
            }
            xp = x
            if (this is Mate && !load) {
                if (lvlUp) onLvlUp(this, channel)
                DevAnalysis.xp += value // compter qu'une fois l'xp (pas member puis user, ensuite server, puis discord)
            }
        }
    }

    fun removeXp(stats: Stats, value: Int): Int {
        stats.run {
            val a = totalXP
            totalXP -= value.coerceAtMost(totalXP) // pas passé en dessous de 0.
            var x = value
            while (x > 0 && lvl >= 1) {
                val sub = x.coerceAtMost(xp)
                x -= sub
                xp -= sub
                if (xp == 0 && x > 0 && lvl > 1) { // repasse au niveau d'en dessous
                    lvl--
                    xp = requiredXP
                } else break
            }
            return value.coerceAtMost(a)
        }
    }

    private fun onLvlUp(mate: Mate, channel: GuildMessageChannel? = null) {
        LvlUpManager.lvlUpManager.sendMessage(mate.server, mate.lvlMember, channel)
    }

    fun addMessage(stats: Stats, channel: GuildMessageChannel? = null) {
        stats.messages++
        addXp(stats, 1, channel)
        if (stats is Mate) {
            addMessage(stats.team)
            addMessage(discord)
        }
    }

    private val today get() = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
    fun testActiveDay(stats: Stats) {
        if (stats.lastActiveDay != today) {
            stats.lastActiveDay = today
            stats.activeDays++
        }
        if (stats is Mate) {
            testActiveDay(stats.team)
            testActiveDay(discord)
        }
    }

    fun addVoice(stats: Stats, time: Int) {
        stats.voice += time
        if (stats is Mate) {
            addVoice(stats.team, time)
            addVoice(discord, time)
        }
    }

    fun addActiveVoice(stats: Stats, time: Int) {
        addXp(stats, time / 60)
        stats.activeVoice += time
        if (stats is Mate) {
            addActiveVoice(stats.team, time)
            addActiveVoice(discord, time)
        }
    }

    fun isVisibleBy(mate: Mate, viewer: User) = mate.public || mate.lvlUser.id == viewer.id

    fun reset(stats: Stats) {
        stats.run {
            xp = 0
            totalXP = 0
            lvl = 1
            messages = 0
            activeDays = 0
            voice = 0
            activeVoice = 0
        }
    }

    // ne pas utiliser lors du reset
    fun clearGroup(group: Group) {
        group.list.clear()
    }

    /*
    Actualise l'xp voice si le membre dans un salon vocal
    Utilisation :
     pas pour discord
     avant save + load (caution)
     embed manager
     card manager
     my roles
     leaderboard (command et button)
     */
    fun update(mate: Mate, callBack: (() -> Unit)? = null) {
        val now = Instant.now().epochSecond
        val vj = mate.voiceJoin // pour smart cast
        if (vj != null) {
            addVoice(mate, (now - vj).toInt())
            mate.voiceJoin = now // ne pas mettre vj
        }
        val a = mate.activeVoiceStart
        if (a != null) {
            addActiveVoice(mate, (now - a).toInt())
            mate.activeVoiceStart = now
        }
        if (callBack != null) callBack()
    }

    fun update(group: Group, callBack: (() -> Unit)? = null) {
        if (group.list.size > 50) {  // pas de mise à jour pour discord et grands serveurs
            if (callBack != null) callBack()
            return
        }
        for (stats in group.list.filterIsInstance<Mate>()) update(stats)
        if (callBack != null) callBack()
    }

    // Donne le meilleur id pour centrer une classe dans un group
    fun getBestId(stats: Stats): String {
        return when (stats.type) {
            Type.MEMBER -> {
                stats as Mate
                stats.lvlUser.id
            }

            Type.SERVER -> {
                stats as Team
                stats.server!!.id
            }

            Type.DISCORD, Type.ALL_SERVERS -> throw Exception("Invalid stats for best id in a group!")
        }
    }
}