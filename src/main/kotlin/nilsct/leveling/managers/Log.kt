package nilsct.leveling.managers

import club.minnced.discord.webhook.WebhookClient
import club.minnced.discord.webhook.send.WebhookMessageBuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.time.Instant

class Log { // amélioration possible aligner les logs

    companion object {
        private val errorHook = if (nilsct.leveling.Bot.official) {
            WebhookClient
                .withUrl("")
        } else {
            WebhookClient
                .withUrl("")
        }
        val infoHook = if (nilsct.leveling.Bot.official) {
            WebhookClient
                .withUrl("")
        } else {
            WebhookClient
                .withUrl("")
        }
        private val loveHook = WebhookClient
            .withUrl("")
        private val resetHook = if (nilsct.leveling.Bot.official) {
            WebhookClient
                .withUrl("")
        } else {
            WebhookClient
                .withUrl("")
        }


        private val latest = mutableListOf<String>()
        private val dateFormat = SimpleDateFormat("HH:mm:ss")
        private val file = File("/tmp/latest.txt")

        fun newUser(user: User) {
            if (!nilsct.leveling.Bot.official) return
            log("USER", user.name)
            val msg = WebhookMessageBuilder()
                .setUsername(user.name)
                .setAvatarUrl(user.effectiveAvatarUrl)
                .setContent("new user")
                .build()
            loveHook.send(msg)
        }

        fun newGuild(guild: Guild) {
            if (!nilsct.leveling.Bot.official) return
            log("GUILD", guild.name)
            val msg = WebhookMessageBuilder()
                .setUsername(guild.name)
                .setAvatarUrl(guild.iconUrl)
                .setContent("`new server`")
                .build()
            loveHook.send(msg)
        }

        /*
        time (ERROR) TAG content
        tags : USER, GUILD, CHART, ICON, PING, PURGE, LVL-UP, ROLE, CONTEXT (COMMAND, BUTTON, ...), DAILY, BASE64, LOAD, SAVE, LAUNCH,
         */
        fun log(tag: String, content: String) { // log
            val msg = "${dateFormat.format(Instant.now().toEpochMilli())}  $tag $content"
            println(msg)
            latest.add(msg)
            if (latest.size > 1000) latest.removeFirst()
        }

        fun error(tag: String, content: String) { // log + envoie error webhook + ajoute tag ERROR
            log("ERROR $tag", content)
            errorHook.send("`ERROR $tag` $content")
        }

        fun info(tag: String, content: String) { // log + envoie info webhook
            log(tag, content)
            infoHook.send("`$tag` $content")
        }

        /*
       Type : server, user, member
       Description : user + server -> id, member -> id in server_name (server_id)
       Reason : purged, moderator @user (id), self
        */
        class ResetObject(
            val type: String,
            val desc: String,
            val name: String,
            val reason: String
        )

        fun reset(objects: List<ResetObject>) {
            var content = ""
            for (obj in objects) {
                obj.run {
                    content += "$type $desc $name `$reason`\n"
                }
                if (content.length > 1700) {
                    resetHook.send(content)
                    content = ""
                }
            }
            if (content != "") resetHook.send(content)
        }

        fun latest(): File {
            val writer = FileWriter(file)
            writer.write(latest.joinToString(separator = "\n"))
            writer.close()
            return file
        }

        fun sendFile(file: File, name: String) { // envoie un fichier dans le salon log
            infoHook.send(file, name)
        }
    }
}