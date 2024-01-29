package nilsct.leveling.interactions.components.levelup

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import nilsct.leveling.entities.Wizard
import nilsct.leveling.interactions.Interaction
import nilsct.leveling.interactions.InteractionContext
import nilsct.leveling.interactions.components.ComponentContext
import nilsct.leveling.managers.DevAnalysis
import nilsct.leveling.managers.WizardManager.Companion.wizardManager

class Channel : Interaction() { // amélioration possible : update les menus quand il y a une modif de permission

    override val id = "levelup-channel"
    override val permission = listOf(Permission.ADMINISTRATOR)

    override fun execute(context: InteractionContext) {
        context as ComponentContext
        val server = context.server
        val guild = context.guild
        val id = context.selectedOptions.first()
        if (id == "auto") {
            server.lvlUpChannel = id
            wizardManager.edit(context, Wizard.Page.LVL_UP, true)
            return
        }
        val channel = guild.getTextChannelById(id) ?: guild.getNewsChannelById(id)
        when {
            channel !is TextChannel && channel !is NewsChannel -> { // impossible
                context
                    .reply(":name_badge: You can't select this type of channel.")
                    .queue()
            }

            !guild.selfMember.hasAccess(channel) -> {
                context
                    .reply(":name_badge: First allow me to view this channel.")
                    .queue()
            }

            !channel.canTalk() -> {
                context
                    .reply(":name_badge: First allow me to send message in this channel.")
                    .queue()
            }

            else -> {
                server.lvlUpChannel = channel.id
                wizardManager.edit(context, Wizard.Page.LVL_UP, true)
                DevAnalysis.channelSet++
            }
        }
    }
}