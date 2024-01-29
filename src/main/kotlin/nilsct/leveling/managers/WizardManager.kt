package nilsct.leveling.managers

import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.ItemComponent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu
import nilsct.leveling.entities.LvlMember
import nilsct.leveling.entities.Wizard
import nilsct.leveling.interactions.InteractionContext
import nilsct.leveling.interactions.components.ComponentContext
import nilsct.leveling.managers.LvlUpManager.Companion.lvlUpManager
import nilsct.leveling.managers.RoleManager.Companion.roleManager

class WizardManager { // amélioration possible : regrouper en une fonction getEmbed et getActionRows en retournant une seule action

    companion object {
        val wizardManager = WizardManager()
    }

    private val next = Button.success("set-up;next", "Next")
    private val done = Button.success("set-up;done", "Done")

    private val closedEmbed = nilsct.leveling.Bot.blueEmbed
        .setTitle("That's all!")
        .setDescription(
            "Edit these settings with ${nilsct.leveling.Bot.mention("level-up menu")} and ${
                nilsct.leveling.Bot.mention(
                    "roles menu"
                )
            }."
        )
        .build()
    private val cutEmbed = nilsct.leveling.Bot.blueEmbed
        .setTitle("Closed")
        .setDescription(
            """
            This menu has been closed because you opened another one.
            > Open menus with ${nilsct.leveling.Bot.mention("level-up menu")} and ${nilsct.leveling.Bot.mention("roles menu")}
            """.trimIndent()
        )
        .build()


    private fun nextPage(page: Wizard.Page) = Wizard.Page.values().getOrNull(page.ordinal + 1)

    //    pas wizard car utilisé aussi quand pas encore créé
    private fun getActionRows(
        context: InteractionContext,
        page: Wizard.Page,
        multiPage: Boolean
    ): List<ActionRow> {
        val actionRows = when (page) {
            Wizard.Page.LVL_UP -> lvlUpManager.getActionRows(context)
            Wizard.Page.ROLE -> roleManager.getActionRows(context)
        }.toMutableList()
        val last = actionRows.last()
        if (multiPage) { // si besoin de bouton pour tourner la page
            val edited =
                if (last.components.first() is SelectMenu) { // ajoute le bouton à la dernière ligne déjà créée si ce n'est pas un select menu
                    mutableListOf<ItemComponent>()
                } else {
                    actionRows.removeLast() // on enlève la dernière ligne pour la modifier
                    last.components.toMutableList()
                }
            edited.add(if (nextPage(page) == null) done else next)
            actionRows.add(ActionRow.of(edited))
        }
        return actionRows
    }

    private fun getEmbed(
        context: InteractionContext,
        page: Wizard.Page
    ): MessageEmbed {
        return when (page) {
            Wizard.Page.LVL_UP -> lvlUpManager.getEmbed(context)
            Wizard.Page.ROLE -> roleManager.getEmbed(context)
        }
    }

    private fun getContent(page: Wizard.Page): String {
        return when (page) {
            Wizard.Page.LVL_UP -> "First let's set a __level up message__."
            Wizard.Page.ROLE -> "Finally let's add some __reward roles__."
        }
    }


    fun open(context: InteractionContext, page: Wizard.Page, multiPage: Boolean) {
        val lvlMember = context.lvlMember
        wizardManager.cut(lvlMember)
        val content = if (multiPage) getContent(page) else ""
        context.reply(content)
            .addEmbeds(getEmbed(context, page))
            .setComponents(getActionRows(context, page, multiPage))
            .queue {
                lvlMember.wizard = Wizard(it, page, multiPage)
            }
    }

    //    Réactualise le menu s'il existe après des changements extérieurs
    fun edit(context: InteractionContext, action: Wizard.Page, editDirectly: Boolean = false) {
        val wizard = context.lvlMember.wizard ?: return
        val page = wizard.page
        if (action != page) return // l'action extérieure ne porte pas sur cette page donc pas besoin d'actualiser
        val multiPage = wizard.multiPage
        val content = if (multiPage) getContent(page) else ""
        if (editDirectly) { // seulement ComponentContext ou ModalContext utilisent editDirectly
            context.edit(content)
                .setEmbeds(getEmbed(context, page))
                .setComponents(getActionRows(context, page, multiPage))
                .queue()
        } else {
            wizard.interactionHook.editOriginal(content)
                .setEmbeds(getEmbed(context, page))
                .setComponents(getActionRows(context, page, multiPage))
                .queue()
        }
    }

    fun next(context: ComponentContext) {
        val wizard = context.lvlMember.wizard ?: return
        if (!wizard.multiPage) return // pas possible normalement
        wizard.page = nextPage(wizard.page) ?: return // pas de page suivante (pas possible normalement)
        val page = wizard.page
        context.edit(getContent(page))
            .setEmbeds(getEmbed(context, page))
            .setComponents(getActionRows(context, page, true))
            .queue()
    }

    //    Ferme si besoin l'ancien wizard quand on en ouvre un nouveau
    private fun cut(lvlMember: LvlMember) {
        val wizard = lvlMember.wizard ?: return
        wizard.interactionHook.editOriginal("")
            .setEmbeds(cutEmbed)
            .setComponents(emptyList())
            .queue()
        lvlMember.wizard = null
    }

    //    Ferme le menu après avoir terminé le set up
    fun close(context: ComponentContext) {
        context.lvlMember.wizard = null
        context.edit()
            .setEmbeds(closedEmbed)
            .setComponents(emptyList())
            .queue()
    }
}