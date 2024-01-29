package nilsct.leveling.interactions.commands.roles

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.components.buttons.Button
import nilsct.leveling.interactions.Interaction
import nilsct.leveling.interactions.InteractionContext
import nilsct.leveling.interactions.commands.CommandContext
import nilsct.leveling.managers.RoleManager.Companion.roleManager

class Remove : Interaction() {

    override val id = "roles remove"
    override val mainCommand = false
    override val permission = listOf(Permission.ADMINISTRATOR)

    private val yes = Button.danger("roles-remove;remove;ID", "Yes")
    private val no = Button.secondary("roles-remove;cancel;ID", "No")

    override fun execute(context: InteractionContext) {
        context as CommandContext
        val server = context.server
        val role = context.getOption("role")!!.asRole
        val rewardRole = roleManager.get(server, role.id)
        if (rewardRole == null) {
            context
                .reply("${role.asMention} isn't a reward role. Do ${nilsct.leveling.Bot.mention("roles menu")}")
                .queue()
        } else {
            context.reply(
                "Do you want to remove ${role.asMention} from everyone?"
            )
                .addActionRow(
                    listOf(
                        yes.withId("roles-remove;remove;${role.id}"),
                        no.withId("roles-remove;cancel;${role.id}")
                    )
                )
                .queue()
        }
    }
}