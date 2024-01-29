package nilsct.leveling.interactions.commands.admin

import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import nilsct.leveling.Bot.Companion.jda
import nilsct.leveling.interactions.Interaction
import nilsct.leveling.interactions.InteractionContext
import nilsct.leveling.interactions.commands.CommandContext

class Activity : Interaction() {

    override val id = "activity"
    override val developer = true
    override val commandData = Commands.slash("activity", "Check the stats of an user (none for yourself)")
        .addOptions(
            OptionData(OptionType.STRING, "status", "the bot status", true)
                .addChoice("Online", "ONLINE")
                .addChoice("Offline", "OFFLINE")
                .addChoice("Idle", "IDLE")
                .addChoice("Do not disturb", "DO_NOT_DISTURB")

        )
        .addOptions(
            OptionData(OptionType.STRING, "type", "type of the activity", true)
                .addChoice("Playing", "PLAYING")
                .addChoice("Watching", "WATCHING")
                .addChoice("Listening", "LISTENING")
                .addChoice("Competing", "COMPETING")
        )
        .addOption(OptionType.STRING, "message", "message displayed", true)
        .setGuildOnly(true)
        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))


    override fun execute(context: InteractionContext) {
        context as CommandContext
        val status = OnlineStatus.valueOf(context.getOption("status")!!.asString)
        val type = Activity.ActivityType.valueOf(context.getOption("type")!!.asString)
        val message = context.getOption("message")!!.asString
        if (status == OnlineStatus.OFFLINE) {
            jda.presence.setPresence(status, true)
        } else {
            jda.presence.setPresence(status, Activity.of(type, message))
        }
        context.reply("Presence set to `${status.name.lowercase()}` `${type.name.lowercase()}` `$message`").queue()
    }
}