package nilsct.leveling.managers

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import nilsct.leveling.Bot.Companion.jda
import nilsct.leveling.Bot.Companion.readyListener
import nilsct.leveling.entities.*
import nilsct.leveling.managers.MemberManager.Companion.memberManager
import nilsct.leveling.managers.ServerManager.Companion.serverManager
import nilsct.leveling.managers.UserManager.Companion.userManager
import nilsct.leveling.stats.Mate
import nilsct.leveling.stats.Stats
import nilsct.leveling.stats.StatsManager
import nilsct.leveling.stats.StatsManager.Companion.statsManager
import nilsct.leveling.stats.Team
import java.io.File
import java.text.DecimalFormat
import java.time.Instant
import java.util.*
import kotlin.math.log10
import kotlin.math.pow


class DataSaver {

    companion object {
        val dataSaver = DataSaver()
    }

    val file = File("saves/backup.json")
    val cautionFile = File("saves/caution-backup.json") // sauvegarde des données avant un load
    private val previousFile =
        File("saves/previous-backup.json") // copie d'une sauvegarde avant une nouvelle sauvegarde
    private val mapper = jacksonObjectMapper()
        .setSerializationInclusion(JsonInclude.Include.NON_NULL) // ignore les valeurs par défaut données en null dans les temp
    private val emptyStats = TempStats(null, null, null, null, null, null, null)

    // Encryption : ids, names and icons

    private data class BackUp(
        val discord: TempStats,
        val servers: List<TempServer>,
        val users: List<TempUser>,
        val members: List<TempMember>
    )

    private data class TempStats(
        val name: String?,
        val icon: String?,
        val totalXP: Int?,
        val messages: Int?,
        val activeDays: Int?,
        val voice: Int?,
        val activeVoice: Int?
    )

    private data class TempServer(
        val id: String,
        val rewardRoles: List<RewardRole>?,
        val stack: Boolean?,
        val lvlUpChannel: String?,
        val lvlUpMessage: String?,
        val stats: TempStats?,
        val lastActivity: Long
    )

    private data class TempUser(
        val id: String,
        val privacy: Privacy?,
        val stats: TempStats?,
        val lastActivity: Long
    )

    private data class TempMember(
        val serverID: String,
        val lvlUserID: String,
        val stats: TempStats?,
        val lastActivity: Long
    )

    fun reset(callBack: (() -> Unit)) {
        serverManager.reset()
        userManager.reset()
        memberManager.reset()
        statsManager.clearGroup(StatsManager.allServers)
        // Pas besoin de reset les mate qui sont supprimés
        statsManager.reset(StatsManager.discord)
        Log.log("LOAD", "Reset done")
        callBack()
    }

    /*
    Je ne stocke pas les icons à l'identique, mais raccourci les données qui peuvent être déduites
     */
    private fun codeStats(stats: Stats): TempStats {
        val icon = stats.icon
        val encodedHash = Base64.encode(icon.split("/").last()) // HASH.extension
        val saved = when {
            icon == "" -> ""
            icon.startsWith("icons/") -> "guild/"
            icon.startsWith("avatars/") -> "user/"
            icon.startsWith("embed/avatars/") -> "default/" // default user avatar
            icon.startsWith("guilds/") -> "member/"
            else -> ""
        } + encodedHash
//        return TempStats(
//            Base64.strangeEncode(stats.name),
//            saved.takeUnless { it.isEmpty() },
//            stats.totalXP.takeUnless { it == 0 },
//            stats.messages.takeUnless { it == 0 },
//            stats.activeDays.takeUnless { it == 0 },
//            stats.voice.takeUnless { it == 0 },
//            stats.activeVoice.takeUnless { it == 0 }
//        )
        return TempStats(
            Base64.strangeEncode(stats.name),
            saved.takeUnless { it.isEmpty() },
            stats.totalXP.takeUnless { it == 0 },
            stats.messages.takeUnless { it == 0 },
            stats.activeDays.takeUnless { it == 0 },
            stats.voice.takeUnless { it == 0 },
            stats.activeVoice.takeUnless { it == 0 }
        )
    }

    private fun decodeStats(stats: Stats, tempStats: TempStats) {
        tempStats.run {
            if (name != null) {
                val decoded = Base64.strangeDecode(name)
                stats.name = decoded
                if (stats is Team && stats.type == Type.SERVER) {
                    stats.server!!.group.name = Base64.strangeDecode(name)
                }
            }
            if (icon != null) {
                val type = stats.type
                val hash = Base64.decode(
                    icon.split("/", limit = 2).last()
                ) // hash.extension (limite de 2 car base64 rajoute des /)
                stats.icon = when {
                    icon.startsWith("guild/") -> {
                        stats as Team
                        val server = stats.server ?: throw Exception("Server null icon url: $type -> guild icon")
                        "icons/${server.id}/$hash"
                    }

                    icon.startsWith("user/") -> {
                        stats as Mate
                        "avatars/${stats.lvlUser.id}/$hash"
                    }

                    icon.startsWith("default/") -> {
                        "embed/avatars/$hash"
                    }

                    icon.startsWith("member/") -> {
                        stats as Mate
                        "guilds/${stats.server.id}/users/${stats.lvlUser.id}/avatars/$hash"
                    }

                    else -> ""
                }
            }
            if (totalXP != null) statsManager.addXp(stats, totalXP, load = true)
            if (messages != null) stats.messages = messages
            if (activeDays != null) stats.activeDays = activeDays
            if (voice != null) stats.voice = voice
            if (activeVoice != null) stats.activeVoice = activeVoice
        }
    }

    private fun codeServers(): List<TempServer> {
        return serverManager.servers.map { server ->
            TempServer(
                Base64.encode(server.id),
                server.rewardRoles.takeUnless { it.isEmpty() },
                server.stack.takeIf { it },
                server.lvlUpChannel.takeUnless { it == "auto" },
                server.lvlUpMessage.takeUnless { it == ServerManager.defaultLvlUpMessage },
                dataSaver.codeStats(server.team).takeUnless { it == emptyStats },
                server.lastActivity
            )
        }
    }

    private fun decodeServers(tempServers: List<TempServer>, callback: () -> Unit) {
        for (tempServer in tempServers) {
            tempServer.run {
                val server = Server(Base64.decode(id))
                if (rewardRoles != null) server.rewardRoles.addAll(rewardRoles)
                if (stack != null) server.stack = stack
                if (lvlUpChannel != null) server.lvlUpChannel = lvlUpChannel
                if (lvlUpMessage != null) server.lvlUpMessage = lvlUpMessage
                if (stats != null) dataSaver.decodeStats(server.team, stats)
                server.lastActivity = lastActivity
                statsManager.addStatsToGroup(StatsManager.allServers, server.team)
                serverManager.servers.add(server)
            }
        }
        callback()
    }

    private fun codeUsers(): List<TempUser> {
        val gen = mutableListOf<TempUser>()
        for (lvlUser in userManager.lvlUsers) {
            if (lvlUser.privacy != Privacy.NORMAL) {
                gen.add(
                    TempUser(
                        Base64.encode(lvlUser.id),
                        lvlUser.privacy,
                        null,
                        lvlUser.lastActivity
                    )
                )
            }
        }
        return gen
    }

    private fun decodeUsers(tempUsers: List<TempUser>, callback: () -> Unit) {
        for (tempUser in tempUsers) {
            tempUser.run {
                val lvlUser = LvlUser(Base64.decode(id))
                if (privacy != null) lvlUser.privacy = privacy
                lvlUser.lastActivity = lastActivity
                userManager.lvlUsers.add(lvlUser)
            }
        }
        callback()
    }

    private fun codeMembers(): List<TempMember> {
        val gen = mutableListOf<TempMember>()
        for (lvlMember in memberManager.lvlMembers) {
            if (lvlMember.mate.totalXP > 30) {
                gen.add(
                    TempMember(
                        Base64.encode(lvlMember.serverID),
                        Base64.encode(lvlMember.lvlUserID),
                        dataSaver.codeStats(lvlMember.mate),
                        lvlMember.lastActivity
                    )
                )
            }
        }
        return gen
    }

    private fun decodeMembers(tempMembers: List<TempMember>, callback: () -> Unit) {
        for (tempMember in tempMembers) {
            tempMember.run {
                val server =
                    serverManager.get(Base64.decode(serverID))
                        ?: throw java.lang.Exception("Decode member Invalid server id $this")
//                val lvlUser =
//                    userManager.get(lvlUserID)
//                        ?: throw java.lang.Exception("Decode member Invalid user id $this")
                val decodedUserId = Base64.decode(lvlUserID)
                val lvlUser = userManager.get(decodedUserId) ?: let {
                    val lvlUser =
                        LvlUser(decodedUserId) // je ne stocke plus une classe user s'il n'y a rien à stocker (privacy == normal)
                    userManager.lvlUsers.add(lvlUser)
                    lvlUser
                }
                val lvlMember = LvlMember(server, lvlUser)
                if (stats != null) dataSaver.decodeStats(lvlMember.mate, stats)
                lvlMember.lastActivity = lastActivity
                server.lvlMembers.add(lvlMember)
                lvlUser.lvlMembers.add(lvlMember)
                memberManager.lvlMembers.add(lvlMember)
                statsManager.addStatsToGroup(server.group, lvlMember.mate)
            }
        }
        callback()
    }

    private fun fileSize(size: Long): String {
        if (size <= 0) return "0"
        val units = arrayOf("B", "kB", "MB", "GB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(size / 1024.0.pow(digitGroups.toDouble())) + units[digitGroups]
    }

    private var working = false

    private fun checkTimeOut(type: String) { // que pour save
        working = true
        val delay = when (type) {
            "SAVE" -> 30
            "LOAD" -> 300
            else -> throw Exception("Invalid type $type (time out data saver)")
        }
        val task: TimerTask = object : TimerTask() {
            override fun run() {
                if (working) {
                    working = false
                    Log.error(type, "More than ${delay}s")
                }
            }
        }
        val timer = Timer("Check time out")
        timer.schedule(task, delay * 1000L)
    }

    fun save(
        caution: Boolean = false, // caution = true si c'est avant un load pour être sûr de pas perdre les données
        autoSave: Boolean = false,
        success: (() -> Unit)? = null,
        failure: (() -> Unit)? = null
    ) {
        val start = Instant.now().toEpochMilli()
        checkTimeOut("SAVE")
        val oFile = if (caution) cautionFile else file
        val msg = when {
            caution -> "(caution)"
            autoSave -> "(auto)"
            else -> ""
        }
        if (!caution) {
            oFile.copyTo(
                previousFile,
                overwrite = true
            ) // copie colle la sauvegarde d'avant (au cas où erreur lors du save puis suppression des données)
        }
        val presence = jda.presence
        val activity = presence.activity
        val onlineStatus = presence.status
        try {
            if (!caution) {
                presence.setPresence(OnlineStatus.DO_NOT_DISTURB, Activity.playing("saving data"))
                jda.removeEventListener(readyListener)
            }
            mapper.writeValue(
                oFile, BackUp(
                    codeStats(StatsManager.discord),
                    codeServers(),
                    codeUsers(),
                    codeMembers()
                )
            )
            working = false
            Log.info("SAVE", "Done ${Instant.now().toEpochMilli() - start} ms ${fileSize(oFile.length())} $msg")
            if (success != null) success()
            if (!caution) {
                jda.addEventListener(readyListener)
                presence.setPresence(onlineStatus, activity)
            }
            Log.log("BASE64", "Supported (${Base64.supportedCharacters.joinToString(separator = "\',\'")})")
        } catch (e: Exception) {
            working = false
            Log.error("SAVE", "$msg ${Instant.now().toEpochMilli() - start} ms $e")
            if (failure != null) failure()
            if (!caution) {
                jda.addEventListener(readyListener)
                presence.setPresence(onlineStatus, activity)
            }
        }
    }

    // ne sync pas les reward roles
    fun load(
        onStart: Boolean = false,
        gFile: File? = null,
        success: (() -> Unit)? = null,
        failure: (() -> Unit)? = null
    ) {
        val start = Instant.now().toEpochMilli()
        checkTimeOut("LOAD")
        val presence = jda.presence
        val activity = presence.activity
        val onlineStatus = presence.status
        try {
            if (!onStart) {
                presence.setPresence(OnlineStatus.DO_NOT_DISTURB, Activity.playing("loading data"))
                jda.removeEventListener(readyListener)
            }
            val oFile = gFile ?: file
            // je télécharge les données
            val backUp: BackUp = mapper.readValue(oFile, BackUp::class.java)
            val actions = {
                decodeStats(StatsManager.discord, backUp.discord)
                decodeServers(backUp.servers) { // un par un pour éviter les bugs
                    decodeUsers(backUp.users) {
                        decodeMembers(backUp.members) { // car les membres ont besoin d'avoir accès aux users et servers
                            if (onStart) {
                                Log.info("LAUNCH", "Load done ${Instant.now().toEpochMilli() - start} ms")
                            } else {
                                Log.info("LOAD", "Done ${gFile?.name} ${Instant.now().toEpochMilli() - start} ms")
                            }
                            working = false
                            if (success != null) success()
                            if (!onStart) {
                                jda.addEventListener(readyListener)
                                presence.setPresence(onlineStatus, activity)
                            }
                        }
                    }
                }
            }
            /*
            Je supprime les données après avoir téléchargé les nouvelles au cas où il y ait un bug.
            Je sauvegarde les anciennes données au cas où c'était une erreur (caution-backup).
             */
            if (onStart) {
                actions()
            } else {
                save(caution = true, success = {
                    reset {
                        actions()
                    }
                }, failure = {
                    working = false
                    Log.info("LOAD", "Stopped (back-up save failed) ${Instant.now().toEpochMilli() - start} ms")
                    if (failure != null) failure()
                    jda.addEventListener(readyListener) // pas besoin de !onStart ici
                    presence.setPresence(onlineStatus, activity)
                })
            }
        } catch (e: Exception) {
            working = false
            Log.error("LOAD", "Data ${Instant.now().toEpochMilli() - start} ms $e")
            if (failure != null) failure()
            if (!onStart) {
                jda.addEventListener(readyListener)
                presence.setPresence(onlineStatus, activity)
            }
        }
    }

    /*
     Restore deleted stats
     (Supprime les données actuelles pour les remplacer par celle du fichier fourni)
     Comme un load partiel, le fichier peut contenir d'autres objets que le serveur en question
     */
    fun restore(
        server: Server,
        file: File,
        success: (() -> Unit),
        failure: (() -> Unit)
    ) {
        try {
            val backUp: BackUp = mapper.readValue(file, BackUp::class.java)
            serverManager.delete(server) // supprime les données actuelles du serveur
            val encodedId = Base64.encode(server.id)
            val tempServer = backUp.servers.first { it.id == encodedId }
            val tempMembers = backUp.members.filter {
                it.serverID == encodedId
            }
            decodeServers(listOf(tempServer)) { // un par un pour éviter les bugs
                decodeMembers(tempMembers) { // car les membres ont besoin d'avoir accès au server
                    Log.info("LOAD", "Restore done ${file.name}")
                    success()
                }
            }
        } catch (e: Exception) {
            working = false
            Log.error("LOAD", "Restore $e")
            failure()
        }
    }
}