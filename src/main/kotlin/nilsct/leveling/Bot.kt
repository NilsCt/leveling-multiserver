package nilsct.leveling

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import nilsct.leveling.interactions.InteractionManager.Companion.interactionManager
import nilsct.leveling.managers.ChartManager.Companion.chartManager
import nilsct.leveling.managers.DataSaver
import nilsct.leveling.managers.DataSaver.Companion.dataSaver
import nilsct.leveling.managers.DevAnalysis.Companion.devAnalysis
import nilsct.leveling.managers.Log
import nilsct.leveling.managers.PurgeManager.Companion.purgeManager
import java.awt.Color
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

class Bot {

    /*
    Bugs :
    - render pas "1ᴄᴏᴅ\uD83D\uDCA62"

    Améliorations possibles :
        - enlever valeurs aberrantes pour /chart

    Nouveautés 1.2 :
    - records
    - Rank nickname
    - rank roles
    - bouton pour supprimer toutes les données d'un serveur (stats et paramètres)
     */

    companion object {
        lateinit var jda: JDA

        const val official = false // true si vrai bot
        private const val maintenance =
            false // true si le programme sert juste à afficher dans le status une maintenance en cours

        private const val token = "" // très secret
        private const val betaToken = "" // beta bot

        private val gatewayIntents = listOf(
            GatewayIntent.GUILD_MESSAGES, // ne donne pas accès au contenu des messages
            GatewayIntent.GUILD_VOICE_STATES,
            GatewayIntent.GUILD_MEMBERS,  // nécessaire pour traquer les events voice join / leave (+ facilite la gestion des rôles)
        )
        const val inviteLink =
            "https://discord.com/api/oauth2/authorize?client_id=849376795966570517&permissions=412719827968&scope=bot%20applications.commands"
        const val supportInvite = ""
        private const val supportGuildID = ""
        val developers = listOf("")


        private val cyanColor = Color(80, 165, 230)
//        private val burple = Color(88, 101, 242)
//        private val lightBurple = Color(114, 137, 218)

        val blueEmbed: EmbedBuilder
            get() = EmbedBuilder().setColor(cyanColor)


        /*
            Les mentions ne sont pas dans InteractionManager,
            car quand on génère les commandes,
            certaines commandes ont besoin des mentions d'autres commandes en cours de génération et ça bug.
         */
        private val mentions = listOf(
            "</help:958701083553849384>", // all
            "</server:958701084115873883>",
            "</discord:958701085248356362>",
            "</privacy:958701086682808361>",
            "</Check stats:958701168366862377>",
            "</my-stats:1007220831090397184>",
            "</member:1007220831962804234>",
            "</leaderboard:1007220832919109683>",
            "</reset server:1007220834026389555>",
            "</reset member:1007220834026389555>",
            "</roles menu:1007220835058188299>",
            "</roles add:1007220835058188299>",
            "</roles remove:1007220835058188299>",
            "</my-roles:1007220916532543568>",
            "</level-up menu:1007220917593723020>",
            "</Reset stats:1007220919422439425>",
            "</stop:1009485821180657674>",
            "</invite:1009541149641424987>",
            "</add-xp:1009893551875825794>",
            "</remove-xp:1009896015282851981>",
            "</reset-myself:1011238102142046268>",
            "</expiration:1055475364085911612>",
            // --- dev ---
            "</load:1007220831086182400>",
            "</save:1007220832164118600>",
            "</back-up:1007220833497927680>",
            "</chart:1009178330643316816>",
            "</latest:1009501643777257622>",
            "</server-leaderboard:1055154291876302908>",
            "</dev-server:1009509811387371710>",
            "</activity:1021047185263050853>"
        )

        fun mention(command: String) = mentions.first { command in it }


        val readyListener = ReadyListener()
        private val previousDataSaver = DataSaver() // utilisé pour le load initial
        private val dateFormat = SimpleDateFormat("yy-MM-dd")

//        private fun launchTy() {
//            val sup = jda.getGuildById("853575759062761482")!!
//            val cha = sup.getTextChannelById("872055460323274803")!!
//            val writer = FileWriter(File("saves/ty.txt"))
//            loop(writer, cha, "1096464432915300492", 0)
//        }
//
//        private fun loop(writer : FileWriter, cha : TextChannel, last : String, nbr : Int) {
//            cha.getHistoryBefore(last,100).queue { history ->
//                for (msg in history.retrievedHistory) {
//                    writer.append(msg.author.name + "\n")
//                }
//                println("Heartbeat ${nbr + history.size()}")
//                Thread.sleep(1000)
//                loop(writer, cha, history.retrievedHistory.last().id, nbr + history.size())
////                writer.close()
//            }
//        }

        @JvmStatic
        fun main(args: Array<String>) {
            try {
                val start = Instant.now().toEpochMilli()
                val tok =
                    if (official) token else betaToken
                if (maintenance) {
                    JDABuilder.create(tok, gatewayIntents)
                        .setStatus(OnlineStatus.DO_NOT_DISTURB)
                        .setActivity(Activity.playing("Maintenance"))
                        .build()
                } else {
                    val jdaBuilder = JDABuilder.create(tok, gatewayIntents)
                        .setActivity(Activity.playing("Launching"))
                    jda = jdaBuilder.build().awaitReady()
                    interactionManager.syncCommand()
                    val supportGuild =
                        jda.getGuildById(supportGuildID)
                            ?: throw Exception("Support guild null $supportGuildID")
                    interactionManager.syncCommandSupport(supportGuild)
                    Log.info("LAUNCH", "Preparation done ${Instant.now().toEpochMilli() - start} ms")
                    launchDailyTask()
                    previousDataSaver.load(onStart = true, success = {
                        jda.addEventListener(readyListener) // à la fin pour attendre que tout soit bien construit
                        jda.presence.setPresence(
                            OnlineStatus.ONLINE,
                            Activity.watching("/help")
                        )
                        Log.info("LAUNCH", "Done ${Instant.now().toEpochMilli() - start} ms")
                    }, failure = {
                        Log.info("LAUNCH", "Stopped")
                    })
                }
            } catch (e: Exception) {
                Log.error("LAUNCH", e.toString())
            }
        }

        private fun launchDailyTask() {
            val task: TimerTask = object : TimerTask() {
                override fun run() {
                    chartManager.append { // un par un pour éviter les bugs
                        devAnalysis.dailyReport {
                            dataSaver.save(autoSave = true, success = {
                                if (Calendar.getInstance().get(Calendar.DAY_OF_YEAR) % 7 == 0) {
                                    Log.sendFile(
                                        dataSaver.file,
                                        "backup-${dateFormat.format(Date.from(Instant.now()))}.json"
                                    )
                                    Log.sendFile(
                                        chartManager.file,
                                        "growth-${dateFormat.format(Date.from(Instant.now()))}.csv"
                                    )
                                }
                                purgeManager.daily()
                            })
                        }
                    }
                }
            }
            val timer = Timer("AutoSave")
            // attend un jour puis tous les jours (pour éviter les répétitions lors du développement)
            val calendar = Calendar.getInstance()
            val secondsElapsed =
                calendar.get(Calendar.HOUR_OF_DAY) * 3600 + calendar.get(Calendar.MINUTE) * 60 + calendar.get(Calendar.SECOND) // secondes écoulées dans la journée
            println("secondeelapsded $secondsElapsed")
            val delay = if (secondsElapsed > 43200) { // /!\ delay en seconde
                129600L - secondsElapsed // 12h demain
            } else {
                43200L - secondsElapsed  // 12h aujourd'hui
            }
            timer.scheduleAtFixedRate(task, delay * 1000, 86400000)
            Log.log("LAUNCH", "Auto-save started")
        }
    }
}