package nilsct.leveling.entities

import net.dv8tion.jda.api.interactions.InteractionHook

class Wizard(
    val interactionHook: InteractionHook,
    var page: Page,
    val multiPage: Boolean
) {

    enum class Page {
        LVL_UP,
        ROLE
    }

}