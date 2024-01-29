package nilsct.leveling.managers

import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu
import nilsct.leveling.Bot.Companion.jda
import nilsct.leveling.entities.LvlMember
import nilsct.leveling.entities.Privacy
import nilsct.leveling.entities.Server
import nilsct.leveling.interactions.InteractionContext

class LvlUpManager {

    companion object {
        val lvlUpManager = LvlUpManager()
    }

//    private val textChannelEmoji = Emoji.fromCustom("text-channel", 1006158908097843290, false)
//    private val errorEmoji = Emoji.fromUnicode("\uD83D\uDCDB")
//    private val autoEmoji = Emoji.fromCustom("blue_star", 996030327157960765L, false)

    fun sendMessage(server: Server, lvlMember: LvlMember, contextChannel: GuildMessageChannel?) {
        val channelID = server.lvlUpChannel
        if (channelID == "" || lvlMember.lvlUser.privacy != Privacy.NORMAL) return
        val channel = if (channelID == "auto") {
            contextChannel
        } else {
            jda.getTextChannelById(channelID)
        } ?: return
        val guild = channel.guild
        if (!guild.selfMember.hasAccess(channel) || !channel.canTalk()) return
        try {
            channel
                .sendMessage(
                    server.lvlUpMessage
                        .replace("<member>", "<@${lvlMember.lvlUser.id}>")
                        .replace("<lvl>", lvlMember.mate.lvl.toString())
                )
                .queue(null,
                    ErrorHandler { Log.error("LVL-UP", "(Discord response) ${channel.id} in ${guild.name} $it") }
                )
//            Log.log("LVL-UP", "Message sent in ${channel.id} in ${guild.name} (${guild.id})")
        } catch (e: Exception) {
            Log.error("LVL-UP", "${channel.id} in ${guild.name} $e")
        }
    }

    fun getEmbed(context: InteractionContext): MessageEmbed {
        val server = context.server
        val guild = context.guild
        val eb = nilsct.leveling.Bot.blueEmbed
            .setTitle("Level-up")
            .setDescription("A message will be sent when a member levels up.")
        val channel = when (server.lvlUpChannel) {
            "", "auto" -> null
            else -> {
                val c = guild.getTextChannelById(server.lvlUpChannel) // actualise si le channel a été supprimé
                if (c == null) server.lvlUpChannel = ""
                c
            }
        }
        when (server.lvlUpChannel) {
            "" -> { // désactivé ou channel supprimé
                eb.addField("Feature disabled", "No channel selected", false)
            }

            "auto" -> eb.addField(
                "Channel: <:blue_star:996030327157960765> `Automatic`",
                "The channel in which a member levels up (excluding voice channels)",
                false
            )

            else -> {
                val d = when {
                    !guild.selfMember.hasAccess(channel!!) -> "\n:name_badge: Missing permission to view channel"
                    !channel.canTalk() -> "\n:name_badge: Missing permission to send message"
                    else -> ""
                }
                eb.addField("Channel:", "${channel.asMention}$d", false)
            }
        }
        eb.addField("Message:", server.lvlUpMessage, false)
        return eb.build()
    }

    fun getActionRows(context: InteractionContext): List<ActionRow> {
        val server = context.server
        val guild = context.guild
        if (server.lvlUpChannel != "" && server.lvlUpChannel != "auto" && guild.getTextChannelById(server.lvlUpChannel) == null) { // actualise si channel supprimé
            server.lvlUpChannel = ""
        }

        val channelId = server.lvlUpChannel
//          Avec string select menu (indique problèmes de channel mais il ne s'actualise pas)
//        val selfMember = context.selfMember
//        val selectMenu = StringSelectMenu.create("levelup-channel")
//        selectMenu.placeholder =
//            if (channelId == "") "Set the channel" else "Edit the channel" // normalement edit pas visible
//        val autoOption = SelectOption.of("Automatic", "auto")
//            .withDescription("The channel in which a member levels up")
//            .withEmoji(autoEmoji)
//            .withDefault(server.lvlUpChannel == "auto")
//        selectMenu.addOptions(autoOption)
//        for (channel in guild.textChannels) {
//            val e: Emoji
//            val d: String
//            when {
//                !selfMember.hasAccess(channel) -> {
//                    e = errorEmoji
//                    d = "Missing permission to view channel"
//                }
//
//                !channel.canTalk() -> {
//                    e = errorEmoji
//                    d = "Missing permission to send message"
//                }
//
//                else -> {
//                    e = textChannelEmoji
//                    d = ""
//                }
//            }
//            val option = SelectOption.of(channel.name, channel.id)
//                .withDescription(d)
//                .withEmoji(e)
//                .withDefault(channel.id == server.lvlUpChannel)
//            selectMenu.addOptions(option)
//            if (selectMenu.options.size > 24) break // max 25 + auto
//        }

        val selectMenu = EntitySelectMenu.create("levelup-channel", EntitySelectMenu.SelectTarget.CHANNEL)
            .setPlaceholder("Edit the channel")
            .setChannelTypes(listOf(ChannelType.TEXT, ChannelType.NEWS))

        val actionRows = mutableListOf(ActionRow.of(selectMenu.build()))
        val buttons = mutableListOf<Button>()
        if (channelId != "auto") buttons.add(Button.success("levelup-auto", "Auto channel"))
        buttons.add(Button.secondary("levelup-message", "Edit the message"))
        if (channelId != "") buttons.add(Button.danger("levelup-disable", "Disable"))
        actionRows.add(ActionRow.of(buttons))
        return actionRows
    }
}