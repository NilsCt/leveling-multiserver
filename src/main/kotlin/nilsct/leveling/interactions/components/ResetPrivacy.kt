package nilsct.leveling.interactions.components

import net.dv8tion.jda.api.interactions.components.buttons.Button
import nilsct.leveling.entities.Privacy
import nilsct.leveling.interactions.Interaction
import nilsct.leveling.interactions.InteractionContext
import nilsct.leveling.managers.DevAnalysis
import nilsct.leveling.managers.RoleManager.Companion.roleManager

class ResetPrivacy : Interaction() {

    override val id = "reset-privacy"
    override val private = true

    override fun execute(context: InteractionContext) { // amélioration possible, ça reset le content
        context as ComponentContext
        val lvlUser = context.lvlUser
        if (lvlUser.privacy == Privacy.NORMAL) {
            context
                .reply("It's already your privacy mode.")
                .queue()
        } else {
            lvlUser.privacy = Privacy.NORMAL
            val row = context.actionRows.first().filterNot { it is Button && it.id == "reset-privacy" }
            val edit = context.edit()
            if (row.isEmpty()) edit.setComponents(emptyList()) else edit.setActionRow(row)
            edit.queue {
                it.sendMessage("Your privacy mode has been reset to `${Privacy.NORMAL.msg}`. ${Privacy.NORMAL.emoji}")
                    .queue()
            }
            roleManager.updateMemberServers(lvlUser)
            DevAnalysis.privacyChanged++
        }
    }
}