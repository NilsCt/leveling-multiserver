package nilsct.leveling.managers

import net.dv8tion.jda.api.utils.FileUpload
import nilsct.leveling.entities.LvlUser
import nilsct.leveling.entities.Privacy
import nilsct.leveling.entities.Type
import nilsct.leveling.interactions.InteractionContext
import nilsct.leveling.managers.ComponentManager.Companion.componentManager
import nilsct.leveling.managers.EmbedManager.Companion.embedManager
import nilsct.leveling.stats.Mate
import nilsct.leveling.stats.Stats
import nilsct.leveling.stats.StatsManager.Companion.statsManager
import java.time.Instant

class StatsSender {

    companion object {

        private fun getPrivacyMsg(mate: Mate, viewer: LvlUser): String {
            val lvlUser = mate.lvlUser
            if (lvlUser.id != viewer.id) return ""
            return when (mate.privacy) {
                Privacy.NORMAL -> ""
                Privacy.PRIVATE -> "\n:detective: Nobody can see your stats."
                Privacy.GHOST -> "\n:fire: Your stats aren't tracked anymore! ${nilsct.leveling.Bot.mention("privacy")}"
            }
        }

        private fun getContent(context: InteractionContext, stats: Stats, embed: Boolean): String {
            var content = if (embed) "" else when (stats.type) {
                Type.SERVER, Type.ALL_SERVERS, Type.DISCORD -> ""
                Type.MEMBER -> {
                    stats as Mate
                    "\n**${stats.name}**'s stats in **${stats.server.team.name}**${
                        getPrivacyMsg(
                            stats,
                            context.lvlUser
                        )
                    }"
                }
            }
            if (stats is Mate) {
                val lvlMember = stats.lvlMember
                val random = (1..12).random()
                when {
                    lvlMember.recentlyServerReset -> {
                        content += "\nAn admin recently reset all members' xp."
                        lvlMember.recentlyServerReset = false
                    }

                    lvlMember.recentlyModified -> {
                        content += "\nAn admin recently modified your xp."
                        lvlMember.recentlyModified = false
                    }

                    stats.lvlUser.voiceWarning || random == 1 || random == 2 -> {
                        content += "\nTip: You don't earn xp when you are alone or muted in a voice channel."
                        stats.lvlUser.voiceWarning = false
                    }

                    random == 3 || random == 4 -> content += "\nTip: Your voice xp updates when you leave a voice channel."
                    random == 5 -> content += "\nTip: I can't read the content of your message."
                    random == 6 -> content +=
                        "\nTip: Your stats can be reset after 1 month of inactivity. ${nilsct.leveling.Bot.mention("expiration")}"

                    random == 7 || random == 8 -> content +=
                        "\n`New` Exponential xp system"
                }
            }
            content += "\n*Last edit <t:${Instant.now().epochSecond}:R>*"
            return content.trim()
        }

        fun check(
            context: InteractionContext,
            stats: Stats
        ) {
            context.deferReply(
                { hook ->
                    val reply = hook
                        .sendMessage(getContent(context, stats, false))
                        .addFiles(FileUpload.fromData(CardManager.cardManager.rank(stats)))
                    val buttons = componentManager.getStatsButtons(stats, context.lvlUser, false)
                    if (buttons.isNotEmpty()) reply.addActionRow(buttons)
                    reply.queue()
                },
                ephemeral = !stats.public // ephemeral ne peut pas être modifié après (pour l'instant)
            )
        }


        fun checkEdit(
            context: InteractionContext,
            stats: Stats
        ) {
            context.deferEdit { hook ->
                val edit = hook
                    .editOriginal(getContent(context, stats, false))
                    .setFiles(FileUpload.fromData(CardManager.cardManager.rank(stats)))
                    .setEmbeds(emptyList())
                val buttons = componentManager.getStatsButtons(stats, context.lvlUser, false)
                if (buttons.isEmpty()) {
                    edit.setComponents(emptyList())
                } else {
                    edit.setActionRow(buttons)
                }
                edit.queue() // je ne peux pas modifier ephemeral
            }
        }


        fun checkEmbed(
            context: InteractionContext,
            stats: Stats
        ) {
            context.deferReply({ hook ->
                val reply = hook.sendMessage(getContent(context, stats, true))
                    .addEmbeds(embedManager.rank(stats))
                if (stats.type == Type.DISCORD) {
                    val a = this::class.java.getResourceAsStream("/icons/discord.png")!!
                    reply.addFiles(FileUpload.fromData(a, "discord.png"))
                }
                val buttons = componentManager.getStatsButtons(stats, context.lvlUser, true)
                if (buttons.isNotEmpty()) reply.addActionRow(buttons)
                reply.queue()
            }, !stats.public)
        }


        fun checkEmbedEdit(
            context: InteractionContext,
            stats: Stats
        ) {
            context.deferEdit { hook ->
                val edit = hook
                    .editOriginal(getContent(context, stats, true))
                    .setEmbeds(embedManager.rank(stats))
                    .setFiles()
                if (stats.type == Type.DISCORD) {
                    val a = this::class.java.getResourceAsStream("/icons/discord.png")!!
                    edit.setFiles(FileUpload.fromData(a, "discord.png"))
                }
                val buttons = componentManager.getStatsButtons(stats, context.lvlUser, true)
                if (buttons.isEmpty()) {
                    edit.setComponents(emptyList())
                } else {
                    edit.setActionRow(buttons)
                }
                edit.queue()
            }
        }

        fun testPrivacy(
            context: InteractionContext,
            mate: Mate
        ): Boolean {
            return if (!statsManager.isVisibleBy(mate, context.user)) {
                context.reply(":name_badge: You can't check the stats of ${mate.name}. ${nilsct.leveling.Bot.mention("privacy")}")
                    .queue()
                true
            } else false
        }
    }
}