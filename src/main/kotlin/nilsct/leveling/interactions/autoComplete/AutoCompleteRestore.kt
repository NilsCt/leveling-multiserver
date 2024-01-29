package nilsct.leveling.interactions.autoComplete

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.commands.Command
import nilsct.leveling.interactions.Interaction
import nilsct.leveling.interactions.InteractionContext
import nilsct.leveling.managers.ServerManager.Companion.serverManager

class AutoCompleteRestore : Interaction() {

    override val id = "restore"
    override val permission = listOf(Permission.ADMINISTRATOR)
    override val developer = true

    override fun execute(context: InteractionContext) {
        context as AutoCompleteContext
        val option = context.focusedOption
        if (option.name != "server-name") return
        val servers = serverManager.search(option.value)
        val choices = servers.filterNot { it.team.name.isEmpty() }
            .map { Command.Choice(it.team.name, it.id) }
        context.replyChoices(choices).queue()
    }
}