package nilsct.leveling.managers

import club.minnced.discord.webhook.send.WebhookEmbedBuilder
import nilsct.leveling.Bot.Companion.jda
import nilsct.leveling.managers.MemberManager.Companion.memberManager
import nilsct.leveling.managers.ServerManager.Companion.serverManager
import nilsct.leveling.managers.UserManager.Companion.userManager
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

class DevAnalysis {

    companion object {
        val devAnalysis = DevAnalysis()

        // Stats quotidiennes

        var daysCounter = 0

        var newServers = 0
        var newUsers = 0
        var newMembers = 0

        var serverLeft = 0
        var interactions = 0
        var xp = 0 // ajoutés que pour les Mates afin de ne les compter qu'une seule fois
        var messages = 0
        var voice = 0

        var help = 0
        var myStats = 0
        var memberCommand = 0
        var serverCommand = 0
        var leaderboard = 0
        var discord = 0
        var userCommand = 0
        var serverLeaderboard = 0
        var privacyCommand = 0
        var privacyChanged = 0
        var selfReset = 0
        var setUp = 0
        var rolesMenu = 0
        var rolesAdded = 0 // ou edit
        var lvlMenu = 0
        var channelSet = 0 // ne compte pas le channel reset
        var resetMember = 0
        var resetServerCommand = 0
        var resetServer = 0
        var addXp = 0
        var removeXp = 0
    }


    private val dateFormat = SimpleDateFormat("d MMMM yyyy")

    private fun center(title: String, score: Int, second: Int? = null): String {
        var s = score.toString()
        if (second != null) s += " -> $second"
        val space = (30 - title.length - s.length - 2).coerceAtLeast(0)
        return "\n$title${" ".repeat(space)}$s"
    }

    private fun centerBis(title: String, score: Int, second: Int): String {
        val s = "$score  +$second"
        val space = (30 - title.length - s.length - 2).coerceAtLeast(0)
        return "\n$title${" ".repeat(space)}$s"
    }

    fun dailyReport(then: (() -> Unit)) {
        try {
            daysCounter++
            val embed = nilsct.leveling.Bot.blueEmbed
                .setTitle(dateFormat.format(Date.from(Instant.now()))) // on ne peut pas ctrl + f avec les timestamp discord
                .setDescription("day $daysCounter")
                .addField(
                    "",
                    "```" +
                            centerBis("servers", serverManager.size, newServers) +
                            center("guild", jda.guilds.size) +
                            centerBis("users", userManager.size, newUsers) +
                            centerBis("members", memberManager.size, newMembers) +
                            "\n" +
                            center("server left", serverLeft) +
                            center("interactions", interactions) +
                            center("xp", xp) +
                            center("messages", messages) +
                            center("voice", voice) +
                            center("rank ping", rankPing().toInt()) +
                            "\n```",
                    false
                )
                .addField(
                    "Commands",
                    "```" +
                            center("help", help) +
                            center("my-stats", myStats) +
                            center("member", memberCommand) +
                            center("server", serverCommand) +
                            center("leaderboard", leaderboard) +
                            center("discord", discord) +
                            center("user", userCommand) +
                            center("server-lb", serverLeaderboard) +
                            center("privacy", privacyCommand, privacyChanged) +
                            center("self reset", selfReset) +
                            center("set-up", setUp) +
                            center("roles menu", rolesMenu, rolesAdded) +
                            center("lvl menu", lvlMenu, channelSet) +
                            center("reset member", resetMember) +
                            center("reset server", resetServerCommand, resetServer) +
                            center("add-xp", addXp) +
                            center("removeXp", removeXp) +
                            "\n```",
                    false
                )
                .build()
            newServers = 0
            newUsers = 0
            newMembers = 0
            serverLeft = 0
            interactions = 0
            xp = 0
            messages = 0
            voice = 0
            help = 0
            myStats = 0
            memberCommand = 0
            serverCommand = 0
            leaderboard = 0
            discord = 0
            userCommand = 0
            serverLeaderboard = 0
            privacyCommand = 0
            privacyChanged = 0
            selfReset = 0
            setUp = 0
            rolesMenu = 0
            rolesAdded = 0
            lvlMenu = 0
            channelSet = 0
            resetMember = 0
            resetServerCommand = 0
            resetServer = 0
            addXp = 0
            removeXp = 0
            Log.infoHook.send(
                WebhookEmbedBuilder.fromJDA(embed).build()
            )
            Log.log("DAILY", "Report done")
            then()
        } catch (e: Exception) {
            Log.error("DAILY", "Report $e")
            then()
        }
    }

    // Ping moyen

    private val imageDownloadPings = mutableListOf<Long>()
    private val rankPings = mutableListOf<Long>()
    private val leaderboardPings = mutableListOf<Long>()

    fun imageDownloadPing() = imageDownloadPings.average()
    fun rankPing() = rankPings.average()
    fun leaderboardPing() = leaderboardPings.average()

    fun addImageDownloadPing(ping: Long) {
        imageDownloadPings.add(ping)
        if (imageDownloadPings.size > 10) imageDownloadPings.removeFirst()
    }

    fun addRankPing(ping: Long) {
        rankPings.add(ping)
        if (rankPings.size > 10) rankPings.removeFirst()
    }

    fun addLeaderboardPing(ping: Long) {
        leaderboardPings.add(ping)
        if (leaderboardPings.size > 10) leaderboardPings.removeFirst()
    }
}