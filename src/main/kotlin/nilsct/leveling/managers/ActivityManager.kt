package nilsct.leveling.managers

import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import net.dv8tion.jda.api.events.guild.voice.GenericGuildVoiceEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import nilsct.leveling.entities.Privacy
import nilsct.leveling.entities.Server
import nilsct.leveling.managers.MemberManager.Companion.memberManager
import nilsct.leveling.managers.RoleManager.Companion.roleManager
import nilsct.leveling.managers.ServerManager.Companion.serverManager
import nilsct.leveling.managers.UserManager.Companion.userManager
import nilsct.leveling.stats.StatsManager.Companion.statsManager
import java.time.Instant

class ActivityManager {

    fun onMessage(event: MessageReceivedEvent) {
        val guild = event.guild
        val user = event.author
        val member = event.member
        if (event.isWebhookMessage || user.isBot || member == null) return
        val server = serverManager.acquire(guild)
        val lvlUser = userManager.acquire(user)
        if (lvlUser.privacy == Privacy.GHOST) return
        val lvlMember = memberManager.acquire(server, lvlUser, member)
        val stats = lvlMember.mate
        statsManager.addMessage(stats, event.guildChannel)
        statsManager.testActiveDay(stats)
        roleManager.updateMember(server, guild, lvlMember, member)
    }

    private fun setActives(server: Server, voiceChannel: VoiceChannel) {
        val now = Instant.now().epochSecond
        val actives = voiceChannel.members
            .filter {
                !it.voiceState!!.isDeafened && !it.voiceState!!.isMuted && !it.user.isBot
            }
            .map { it.id }
        for (member in voiceChannel.members) {
            if (member.user.isBot) continue
            val lvlUser = userManager.acquire(member.user)
            if (lvlUser.privacy == Privacy.GHOST) return
            val lvlMember = memberManager.acquire(server, lvlUser, member)
            val stats = lvlMember.mate
            val active = member.id in actives && actives.size > 1
            val previousActive = stats.activeVoiceStart != null
            when {
                active && !previousActive -> stats.activeVoiceStart = now
                !active && previousActive -> {
                    statsManager.addActiveVoice(stats, (now - stats.activeVoiceStart!!).toInt())
                    stats.activeVoiceStart = null
                    roleManager.updateMember(server, voiceChannel.guild, lvlMember, member)
                }
            }
        }
    }

    fun onVoice(event: GuildVoiceUpdateEvent) { // ne prend en compte que les voice channels pour l'instant
        val member = event.member
        val server = serverManager.acquire(event.guild)
        val lvlUser = userManager.acquire(member.user)
        val lvlMember = memberManager.acquire(server, lvlUser, member)
        val stats = lvlMember.mate
        val join = event.channelJoined
        val leave = event.channelLeft
        val now = Instant.now().epochSecond

        statsManager.testActiveDay(stats)
        when {
            join is VoiceChannel && leave is VoiceChannel -> {
                setActives(server, leave)
                setActives(server, join)
            }

            join is VoiceChannel -> {
                if (lvlUser.privacy != Privacy.GHOST) stats.voiceJoin = now
                setActives(server, join)
            }

            leave is VoiceChannel -> {
                setActives(server, leave) // il faut actualiser les autres membres
                // puis actualiser le membre qui a quitté
                if (lvlUser.privacy == Privacy.GHOST) return
                val vj = stats.voiceJoin // pour smart cast
                if (vj != null) {
                    statsManager.addVoice(stats, (now - vj).toInt())
                    stats.voiceJoin = null // ne pas mettre vj
                }
                val va = stats.activeVoiceStart
                if (va != null) {
                    statsManager.addActiveVoice(stats, (now - va).toInt())
                    stats.activeVoiceStart = null
                }
                roleManager.updateMember(server, event.guild, lvlMember, member)
            }
        }
    }

    fun onVoiceState(event: GenericGuildVoiceEvent) { // (un)mute (un)deaf
        val server = serverManager.acquire(event.guild)
        val channel = event.voiceState.channel
        if (channel !is VoiceChannel) return
        setActives(server, channel)
    }
}