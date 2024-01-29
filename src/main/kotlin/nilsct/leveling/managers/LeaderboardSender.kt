package nilsct.leveling.managers

import net.dv8tion.jda.api.utils.FileUpload
import nilsct.leveling.entities.Comparator
import nilsct.leveling.entities.Type
import nilsct.leveling.interactions.InteractionContext
import nilsct.leveling.managers.CardManager.Companion.cardManager
import nilsct.leveling.managers.ComponentManager.Companion.componentManager
import nilsct.leveling.managers.EmbedManager.Companion.embedManager
import nilsct.leveling.stats.Stats
import java.time.Instant

class LeaderboardSender {

    companion object {

        fun content(lbContext: ComponentManager.LBContext, markdown: Boolean = true): String {
            val (group, comparator) = lbContext
            val t = group.list.first().type.name.lowercase()
            val md = if (markdown) "**" else ""
            var content = if (group.type == Type.SERVER) {
                when (comparator) {
                    Comparator.XP -> "The most ${md}active$md ${t}s of $md${group.name}$md:"
                    Comparator.MESSAGES -> "The ${t}s of $md${group.name}$md who have sent the most ${md}messages$md:"
                    Comparator.VOICE -> "The ${t}s of $md${group.name}$md who have spent the most active time in ${md}voice$md channels:"
                }
            } else {
                when (comparator) {
                    Comparator.XP -> "The most ${md}active$md ${t}s:"
                    Comparator.MESSAGES -> "The ${t}s who sent the most ${md}messages$md:"
                    Comparator.VOICE -> "The ${t}s who spent the most time active in ${md}voice$md channels:"
                }
            }
            if (lbContext.group.list.size < 6) content += "\nMembers must send at least one message to appear on leaderboards"
            content += "\n*Last edit <t:${Instant.now().epochSecond}:R>*"
            return content
        }

        // stats à centrer
        private fun interesting(context: InteractionContext, lbContext: ComponentManager.LBContext): Stats? {
            return when (lbContext.group.type) {
                Type.SERVER -> if (context.isFromGuild) context.lvlMember.mate else null // au cas où
                Type.ALL_SERVERS -> if (context.isFromGuild) context.server.team else null
                else -> null
            }
        }

        fun check(context: InteractionContext, lbContext: ComponentManager.LBContext) {
            context.deferReply({ hook ->
                hook.sendMessage(content(lbContext))
                    .addFiles(FileUpload.fromData(cardManager.leaderboard(lbContext)))
                    .addActionRow(
                        componentManager.getSelectMenu(
                            lbContext,
                            context.isDev,
                            interesting(context, lbContext)
                        )
                    )
                    .queue()
            })
        }

        fun checkAll(context: InteractionContext, lbContext: ComponentManager.LBContext) { // normalement pas utilisé
            context.deferReply({ hook ->
                embedManager.leaderboard(lbContext) { file ->
                    hook.sendMessage(content(lbContext))
                        .addFiles(FileUpload.fromData(file))
                        .addActionRow(componentManager.getSelectMenu(lbContext, context.isDev))
                        .queue()
                }
            })
        }

        fun checkEdit(context: InteractionContext, lbContext: ComponentManager.LBContext) {
            context.deferEdit { hook ->
                hook.editOriginal(content(lbContext))
                    .setFiles(FileUpload.fromData(cardManager.leaderboard(lbContext)))
                    .setActionRow(
                        componentManager.getSelectMenu(
                            lbContext,
                            context.isDev,
                            interesting(context, lbContext)
                        )
                    )
                    .setEmbeds(emptyList())
                    .queue()
            }
        }

        fun checkAllEdit(context: InteractionContext, lbContext: ComponentManager.LBContext) {
            context.deferEdit { hook ->
                embedManager.leaderboard(lbContext) { file ->
                    hook.editOriginal("") // pas une image, mais un fichier txt
                        .setContent(content(lbContext))
                        .setFiles(FileUpload.fromData(file))
                        .setActionRow(componentManager.getSelectMenu(lbContext, context.isDev))
                        .queue()
                }
            }
        }
    }
}