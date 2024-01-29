package nilsct.leveling

import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.events.guild.voice.GenericGuildVoiceEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceDeafenEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMuteEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import nilsct.leveling.interactions.InteractionManager.Companion.interactionManager
import nilsct.leveling.managers.ActivityManager
import nilsct.leveling.managers.DevAnalysis

class ReadyListener : ListenerAdapter() {

    private val activityManager = ActivityManager()


    override fun onGenericInteractionCreate(event: GenericInteractionCreateEvent) {
        super.onGenericInteractionCreate(event)
        interactionManager.guide(event)
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        super.onMessageReceived(event)
//        println(event.message.contentRaw) // récupère les emojis custom
        if (!event.isFromGuild) return
        activityManager.onMessage(event)
    }

    override fun onGuildVoiceUpdate(event: GuildVoiceUpdateEvent) {
        super.onGuildVoiceUpdate(event)
        activityManager.onVoice(event)
    }

    override fun onGenericGuildVoice(event: GenericGuildVoiceEvent) {
        super.onGenericGuildVoice(event)
        if (event is GuildVoiceMuteEvent || event is GuildVoiceDeafenEvent) { // (un)mute (un)deaf
            activityManager.onVoiceState(event)
        }
    }

    override fun onGuildLeave(event: GuildLeaveEvent) { // bot pas membre
        super.onGuildLeave(event)
        DevAnalysis.serverLeft++
    }
}

