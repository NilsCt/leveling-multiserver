package nilsct.leveling.managers

import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu
import net.dv8tion.jda.api.interactions.components.selections.SelectOption
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu
import nilsct.leveling.entities.*
import nilsct.leveling.interactions.InteractionContext
import nilsct.leveling.managers.LeaderboardManager.Companion.leaderboardManager
import nilsct.leveling.stats.*
import nilsct.leveling.stats.StatsManager.Companion.statsManager

class ComponentManager {

    companion object {
        val componentManager = ComponentManager()
    }

    data class StatsContext( // gérer différemment que lbContext
        val stats: Stats,
        val edit: Boolean = true,
        val embed: Boolean = false
    )


    fun getStatsContext(params: List<String>, context: InteractionContext?): StatsContext {
        if (params.isEmpty()) throw Exception("Empty params for GetStatsContext")
        var edit = true // si le message est envoyé ou modifié
        var embed = true // type : embed par défaut
        var server: Server? = null
        var lvlUser: LvlUser? = null
        var discord = false // si stats de discord
        for (param in params) {
            when {
                param == "send" -> edit = false
                param == "image" -> embed = false
                param.startsWith("s") -> server = ServerManager.serverManager.get(param.removePrefix("s"))
                param.startsWith("u") -> lvlUser = UserManager.userManager.get(param.removePrefix("u"))
                param == "d" -> discord = true
                param == "auto" -> {
                    if (context == null) throw Exception("Unable to determine automatically the stats without context")
                    if (context.isFromGuild) server = context.server
                    lvlUser = context.lvlUser
                    context.lvlMember // empêche un bug si le membre n'a pas encore d'instance (créée par cette ligne) (exemple avec bouton Your Stats de help)
                }
            }
        }
        val stats = when { // détermine quelle stats utiliser avec tous les params
            discord -> StatsManager.discord
            server != null && lvlUser != null -> MemberManager.memberManager.get(server, lvlUser)?.mate
                ?: throw Exception("GetStatsContext unknown lvlMember")

            server != null -> server.team
            else -> throw Exception("GetStatsContext no stats given")
        }
        return StatsContext(stats, edit, embed)
    }

    /*
   ex: (max 100 chars)
   "stats;image;s853575759062761482;u405033872561274881"
   "stats;send;d"
   "stats;auto" pour celui qui clique le bouton + le serveur si possible
   */
    private fun getId(stats: Stats, edit: Boolean = true, embed: Boolean = true): String {
        var id = "stats;" // set the id
        if (!edit) id += "send;"
        if (!embed) id += "image;"
        id += when (stats.type) {
            Type.SERVER -> {
                stats as Team
                "s${stats.server!!.id}"
            }

            Type.MEMBER -> {
                stats as Mate
                "s${stats.server.id};u${stats.lvlUser.id}"
            }

            Type.ALL_SERVERS, Type.DISCORD -> "d"
        }
        return id
    }

    fun getPrivacyButtons(mate: Mate, viewer: LvlUser): Button? {
        val lvlUser = mate.lvlUser
        return when {
            lvlUser.id != viewer.id -> null
            mate.privacy == Privacy.NORMAL -> null
            mate.privacy == Privacy.PRIVATE -> Button.secondary("reset-privacy", "Turn off Private Mode")
            mate.privacy == Privacy.GHOST -> Button.secondary("reset-privacy", "Turn off Ghost Mode")
            else -> null
        }
    }

    fun getStatsButtons(stats: Stats, viewer: LvlUser, embed: Boolean): List<Button> {
//        val btn = mutableListOf(Button.secondary(getId(stats), "More").withDisabled(embed))
        val btn = mutableListOf<Button>()
        if (embed) {
            btn.add(Button.secondary(getId(stats, embed = false), "Back"))
        } else {
            btn.add(Button.secondary(getId(stats), "More"))
        }
        if (stats is Mate) getPrivacyButtons(stats, viewer)?.run { btn.add(this) }
        btn.add(Button.secondary(getId(stats, embed = embed), "Reload"))
        return btn
    }

    data class LBContext(
        val group: Group,
        val comparator: Comparator = Comparator.XP,
        val edit: Boolean = true,
        val all: Boolean = false,
        val centered: Stats? = null
    ) {
        /*
     ex :
     leaderboard;s853575759062761482;msg (max 100 chars)
     leaderboard;all-servers
     leaderboard;auto
     leaderboard;s853575759062761482;c405033872561274881 (centre sur Nils dans le classement des membres)
     */
        val id: String
            get() {
                var id = "leaderboard;" // set the id
                if (!edit) id += "send;"
                if (!all) id += "image;"
                id += when (group.type) {
                    Type.SERVER -> "s${group.server!!.id}"
                    Type.ALL_SERVERS -> "all-servers"
                    Type.MEMBER, Type.DISCORD -> throw Exception("Leaderboard id invalid Group (member)")
                }
                id += when (comparator) {
                    Comparator.XP -> ""
                    Comparator.MESSAGES -> ";msg"
                    Comparator.VOICE -> ";voice"
                }
                if (centered != null) id += ";c${statsManager.getBestId(centered)}"
                return id
            }
    }

    // context pour param auto
    fun getLBContext(params: List<String>, context: InteractionContext?): LBContext {
        if (params.isEmpty()) throw Exception("Empty params for GetLeaderboardContext")
        var edit = true // si le message est envoyé ou modifié
        var all = true // type : embed par défaut
        var group: Group? = null
        var comparator = Comparator.XP
        var centered: Stats? = null
        for (param in params) {
            when {
                param == "send" -> edit = false
                param == "image" -> all = false
                param.startsWith("s") -> group =
                    ServerManager.serverManager.get(param.removePrefix("s"))?.group
                        ?: throw Exception("GetLeaderboardContext unknown server")

//                param.startsWith("u") -> group =
//                    UserManager.userManager.get(param.removePrefix("u"))?.group
//                        ?: throw Exception("GetLeaderboardContext unknown lvlUser")

//                param == "all-users" -> group = StatsManager.a
                param == "all-servers" -> group = StatsManager.allServers
                param == "msg" -> comparator = Comparator.MESSAGES
                param == "voice" -> comparator = Comparator.VOICE
                param == "auto" -> {
                    if (context == null) throw Exception("Unable to determine automatically the Group without context")
                    group = if (context.isFromGuild) {
                        context.server.group
                    } else {
                        StatsManager.allServers
                    }
                }

                param.startsWith("c") -> centered =
                    group?.list?.firstOrNull { statsManager.getBestId(it) == param.removePrefix("c") && it.public }
            }
        }
        val t = group ?: throw Exception("GetLeaderboardContext no Group given")
        return LBContext(t, comparator, edit, all, centered)
    }

    // pour décentrer si la stats centrée fait partie des 4 premiers
    private fun newComparator(lbContext: LBContext, comparator: Comparator): String {
        val (group, _, _, _, centered) = lbContext
        val new = if (centered == null || leaderboardManager.notInTop4(group, centered, comparator)) {
            lbContext.copy(comparator = comparator)
        } else {
            lbContext.copy(comparator = comparator, centered = null) // décentre
        }
        return new.id
    }

    private val blueStar = Emoji.fromCustom("blue_star", 996030327157960765L, false)
    private val msg = Emoji.fromUnicode("\uD83D\uDCAC")
    private val voice = Emoji.fromUnicode("\uD83D\uDD0A")
    private val less = Emoji.fromUnicode("\uD83D\uDCC7")
    private val more = Emoji.fromUnicode("\uD83D\uDDD2")
    private val top = Emoji.fromUnicode("\uD83C\uDFC6")
    private val pin = Emoji.fromCustom("blue_pin", 996034173502509087L, false)

    /*
    3 cas :
    pas dans le podium et pas centré : propose centrage
    pas dans le podium et centré : propose de remettre le podium
    dans le podium : ne propose rien

    amélioration possible : custom emote bleu partout
     */
    fun getSelectMenu(lbContext: LBContext, dev: Boolean = false, interesting: Stats? = null): SelectMenu {
        val (group, comparator, _, all, centered) = lbContext
        val xp = SelectOption.of("xp", newComparator(lbContext, Comparator.XP))
            .withDescription("Sort members by xp")
            .withEmoji(blueStar)
        val msg = SelectOption.of("Messages", newComparator(lbContext, Comparator.MESSAGES))
            .withDescription("Sort members by sent messages")
            .withEmoji(msg)
        val voice = SelectOption.of("Voice", newComparator(lbContext, Comparator.VOICE))
            .withDescription("Sort members by active voice time")
            .withEmoji(voice) //🎙️📢 📣
        val selectMenu = StringSelectMenu
            .create("leaderboard")
            .addOptions(listOf(xp, msg, voice))
        selectMenu.setDefaultOptions(
            listOf(
                when (comparator) {
                    Comparator.XP -> xp
                    Comparator.MESSAGES -> msg
                    Comparator.VOICE -> voice
                }
            )
        )
        if (all) {
            selectMenu.addOptions(
                SelectOption.of("Less", lbContext.copy(all = false).id)
                    .withDescription("Show less members")
                    .withEmoji(less)
            )
            /*
            Ne propose pas more si le classement n'est pas centré sur les meilleurs ou si classement des serveurs (sauf dev) pour vie privée
             */
        } else if (centered == null && group.list.size > 6) {
            if (lbContext.group.type != Type.ALL_SERVERS || dev) {
                selectMenu.addOptions(
                    SelectOption.of("All", lbContext.copy(all = true).id)
                        .withDescription("Show all members")
                        .withEmoji(more)
                )
            }
        }
        if (!all) { // si image et que la stats peux être mieux centré (pas top4)
            if (centered != null) { // décentrer
                selectMenu.addOptions(
                    SelectOption.of("The best", lbContext.copy(centered = null).id)
                        .withDescription("Go back to the top of the leaderboard")
                        .withEmoji(top)
                )
            } else if (interesting != null && interesting.public && leaderboardManager.notInTop4(
                    group,
                    interesting,
                    comparator
                )
            ) {
                val (msg1, msg2) = when (interesting.type) {
                    Type.MEMBER -> "You" to "Check your rank"
                    Type.SERVER -> "This server" to "Check the rank of this server"
                    else -> "x" to "x"
                }
                selectMenu.addOptions(
                    SelectOption.of(msg1, lbContext.copy(centered = interesting).id)
                        .withDescription(msg2)
                        .withEmoji(pin)
                )
            }
        }
//        selectMenu.addOption("Reload", lbContext.id)
        return selectMenu.build()
    }
}