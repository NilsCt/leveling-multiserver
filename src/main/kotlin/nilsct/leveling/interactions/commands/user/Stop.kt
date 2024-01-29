package nilsct.leveling.interactions.commands.user

import net.dv8tion.jda.api.interactions.commands.build.Commands
import nilsct.leveling.entities.Privacy
import nilsct.leveling.interactions.Interaction
import nilsct.leveling.interactions.InteractionContext
import nilsct.leveling.interactions.commands.CommandContext
import nilsct.leveling.managers.DevAnalysis
import nilsct.leveling.managers.RoleManager.Companion.roleManager

class Stop : Interaction() {

    override val id = "stop"
    override val private = true
    override val commandData = Commands.slash("stop", "Disable your message and voice tracking")

    override fun execute(context: InteractionContext) {
        context as CommandContext
        val lvlUser = context.lvlUser
        if (lvlUser.privacy == Privacy.GHOST) {
            lvlUser.privacy = Privacy.NORMAL
            context.reply(
                "Welcome back!" +
                        "\nYour privacy mode has been edited to `${Privacy.NORMAL.msg}`! ${Privacy.NORMAL.emoji}"
            )
                .queue()
        } else {
            lvlUser.privacy = Privacy.GHOST

            context.reply(
                """
                Your privacy mode has been edited to `${Privacy.GHOST.msg}`! ${Privacy.GHOST.emoji}
                
                > - Your messages and time spent in voice channels won't be counted in any server anymore
                > - __You will no longer earn xp__
                > - Your reward roles will be temporarily removed
                
                Use ${nilsct.leveling.Bot.mention("stop")} again to disable this.
                Get more info with ${nilsct.leveling.Bot.mention("privacy")}.
                Note: Even without this mode, the bot can't read the content of your messages.
                """.trimIndent()
            ).queue()
        }
        roleManager.updateMemberServers(lvlUser)
        DevAnalysis.privacyCommand++
    }
}