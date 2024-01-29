package nilsct.leveling.interactions.components

import nilsct.leveling.entities.Privacy
import nilsct.leveling.interactions.Interaction
import nilsct.leveling.interactions.InteractionContext
import nilsct.leveling.managers.DevAnalysis
import nilsct.leveling.managers.RoleManager.Companion.roleManager

class PrivacyMenu : Interaction() {

    override val id = "privacy"
    override val private = true

    override fun execute(context: InteractionContext) {
        context as ComponentContext
        val option = context.selectedOptions.first()
        val privacy = Privacy.valueOf(option)
        val lvlUser = context.lvlUser
        if (privacy == lvlUser.privacy) { // pas possible normalement
            context
                .reply("It's already your privacy mode.")
                .queue()
        } else {
            lvlUser.privacy = privacy
            context
                .reply("Your privacy mode has been edited to `${privacy.msg}`. ${privacy.emoji}")
                .queue()
            roleManager.updateMemberServers(lvlUser)
            DevAnalysis.privacyChanged++
        }
    }
}