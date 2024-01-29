package nilsct.leveling.managers

import nilsct.leveling.entities.Type
import nilsct.leveling.managers.DevAnalysis.Companion.devAnalysis
import nilsct.leveling.managers.LeaderboardManager.Companion.leaderboardManager
import nilsct.leveling.simplify
import nilsct.leveling.stats.Mate
import nilsct.leveling.stats.Stats
import nilsct.leveling.stats.StatsManager.Companion.statsManager
import nilsct.leveling.stats.Team
import nilsct.leveling.timeFormat
import java.awt.Color
import java.awt.Font
import java.awt.Graphics
import java.awt.geom.Ellipse2D
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.net.URL
import java.time.Instant
import javax.imageio.ImageIO

class CardManager {

    companion object {
        val cardManager = CardManager()
    }

    private data class FontDetails(val font: Font, val width: Int, val height: Int)

    private val profileBg = "/backgrounds/profile.png"
    private val leaderboardBg = "/backgrounds/leaderboard.png"
    private val emptyLb = File(this.javaClass.getResource(("/backgrounds/emptyLeaderboard.png"))!!.path)

    private val format = "png"
    private val to = File("/tmp/image.png")

    private val iconClip = Ellipse2D.Float(50f, 50f, 160f, 160f)
    private val levelClip = RoundRectangle2D.Float(400f, 260f, 1100f, 80f, 60f, 60f)

    private val lightCyan = Color(100, 220, 255) // nom profile
    private val cyan = Color(90, 200, 255) // barre xp profile + rank lb

    private val regular = getFont("/fonts/Whitney Medium.otf") // tout
    private val semiBold = getFont("/fonts/Whitney SemiBold.otf") // nom
    private val bold = getFont("/fonts/Whitney Bold.otf")
    private val xpFont = bold.deriveFont(50f) // xp dans la barre

    private val gray = Color(175, 175, 175)


    fun getFont(path: String): Font { // utilisé aussi dans dev analysis pour les graphiques
        val stream = this.javaClass.getResourceAsStream(path)
        val font = Font.createFont(Font.TRUETYPE_FONT, stream)
        stream?.close()
        return font
    }

    private fun max(graphics: Graphics, userName: String, freeSpace: Int, font: Font): String {
        var shown = userName
        while (graphics.getFontMetrics(font).stringWidth(shown) > freeSpace) {
            shown = shown.substring(0, shown.length - 1)
        }
        return shown
    }

    private fun getIcon(stats: Stats): InputStream {
        if (stats.type == Type.DISCORD) return this::class.java.getResourceAsStream("/icons/discord.png")!! // discord
        val icon = stats.icon
        val default = this::class.java.getResourceAsStream("/icons/default.png")!!
        if (icon == "") return default // icon url empty
        try {
            val start = Instant.now().toEpochMilli()
            val url = URL("https://cdn.discordapp.com/$icon")
            val openConnection = url.openConnection()
            openConnection.addRequestProperty("User-Agent", "leveling bot")
//            openConnection.setRequestProperty(
//                "Player-Agent",
//                "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11"
//            )
            openConnection.connect()
            if (openConnection.contentLength > 8000000) return default // file size too big (max 8mb)
            val ping = Instant.now().toEpochMilli() - start
            if (ping > 500) {
                Log.log("PING", "Load image ${stats.name} $url $ping ms")
            }
            devAnalysis.addImageDownloadPing(ping)
            return openConnection.getInputStream()
        } catch (e: Exception) {
            if (e is FileNotFoundException) return default
            Log.error("ICON", "Load ${stats.name} $e")
            return default
        }
    }

    private fun getFontDetails(
        graphics: Graphics,
        msg: String,
        maxSize: Int,
        maxHeight: Int,
        maxWidth: Int,
        minHeight: Int,
        originalFont: Font = regular,
    ): FontDetails {
        var fontSize = maxSize.toFloat()
        var height = 1000
        var width = 1000
        while ((height > maxHeight || width > maxWidth) && height > minHeight) {
            fontSize -= 2
            val font = originalFont.deriveFont(fontSize)
            val metrics = graphics.getFontMetrics(font)
            height = metrics.height
            width = metrics.stringWidth(msg)
        }
        val font = originalFont.deriveFont(fontSize)
        return FontDetails(font, width, height)
    }

    fun rank(stats: Stats): File {
        val start = Instant.now().toEpochMilli()

        if (stats is Mate) statsManager.update(stats)
        // Verbeux, car les mates ne sont plus stockées dans une team
        if (stats is Team && stats.server != null) statsManager.update(stats.server.group)
        val group = statsManager.getGroup(stats)
        val bg = this.javaClass.getResourceAsStream(profileBg) // charge le background
        val template: BufferedImage = ImageIO.read(bg)
        bg?.close()
        val graphics = template.graphics

        graphics.clip = iconClip // dessine l'icône (et l'arrondi)
        val inputStream = getIcon(stats)
        val icon = ImageIO.read(inputStream)
        inputStream.close()
//        val icon = ImageIO.read(URL("http://cdn.discordapp.com/avatars/405033872561274881/0daa96c50fcbbcf4e499e8e209212952.png"))
        graphics.drawImage(icon, 50, 50, 160, 160, null)
        graphics.clip = null

        graphics.color =
            lightCyan // dessine le nom (avec la place pour le rang) (pas exactement même couleur que la barre)
        val name = stats.name
        val (nameFont, _, nameHeight) = getFontDetails(graphics, name, 130, 150, 850, 100, semiBold)
        graphics.font = nameFont
        val rank = if (group != null && stats.public) "#${leaderboardManager.rank(group, stats)}" else ""
        val (rankFont, rankWidth, rankHeight) = getFontDetails(graphics, rank, 110, 170, 400, 0)
        val shown = max(graphics, name, 1250 - rankWidth, nameFont)
        graphics.drawString(shown, 250, 130 + nameHeight / 4)

        graphics.color = Color.white // dessine le rang
        if (stats.type == Type.MEMBER && stats.public) { // global modif : remplacer par group != null
            graphics.font = rankFont
            graphics.drawString(rank, 1540 - rankWidth, 130 + rankHeight / 4)
        }

        val lvl = "level: ${stats.lvl.simplify()}" // dessine le level
        val (lvlFont, lvlWidth, lvlHeight) = getFontDetails(graphics, lvl, 75, 100, 340, 50)
        graphics.font = lvlFont
        val lvlX = 20 + (380 - lvlWidth) / 2
        val lvlY = 300 + lvlHeight / 4
        graphics.drawString(lvl, lvlX, lvlY)

        graphics.clip = levelClip // dessine la barre d'xp
        val xp = stats.xp
        val requiredXP = stats.requiredXP
        val progression = xp.toFloat() / requiredXP
        graphics.color = gray
        graphics.fillRoundRect(400, 260, 1100, 80, 60, 60)
        graphics.color = cyan
        graphics.fillRoundRect(400, 260, (80 + (1020 * progression).toInt()), 80, 60, 60)
        graphics.clip = null

        graphics.font = xpFont // dessine l'xp
        graphics.color = Color.darkGray
        val xpMsg = "${xp.simplify()} / ${requiredXP.simplify()} xp"
        val xpX = (1470 - graphics.getFontMetrics(xpFont).stringWidth(xpMsg))
        graphics.drawString(xpMsg, xpX, 318)

        graphics.color = Color.lightGray // cela dessine les msg envoyés et le temps de voc
        val sentMessages = "${stats.messages.simplify()} sent messages"
        val voiceTime = "${stats.voice.timeFormat()} voice time"
        val (msgFont, msgWidth, msgHeight) = getFontDetails(graphics, sentMessages + voiceTime, 70, 100, 1300, 50)
        graphics.font = msgFont
        val space = (1600 - msgWidth) / 3
        val msgY = 430 + msgHeight / 4
        val voiceX = graphics.getFontMetrics(msgFont).stringWidth(sentMessages) + space * 2
        graphics.drawString(sentMessages, space, msgY)
        graphics.drawString(voiceTime, voiceX, msgY)

//        graphics.color = Color.red
//        // icon margin
//        graphics.drawLine(0,50,1600,50) // top
//        graphics.drawLine(0,210,1600,210) // bottom
//        graphics.drawLine(50,0,50,210) // left
//        graphics.drawLine(210,0,210,210) // right
//
//        // name & rank
//        graphics.drawLine(0,130,1600,130) // alignement y (avec icon)
//        graphics.drawLine(250,50,250,210) // left (name)
//        graphics.drawLine(1140,50,1140,210) // left (rank) départ max
//        graphics.drawLine(1540,50,1540,210) // right (rank) le rank est serré à droite
//
//        // lvl, xp & barre
//        graphics.drawLine(0,300,1600,300) // alignement y
//        graphics.drawLine(20,260,20,340) // left (lvl) départ max
//        graphics.drawLine(1460,260,1460,340) // right (xp) l'xp est serré à droite
//
//        // msg & voice
//        graphics.drawLine(0,430,1600,430) // alignement y
//            // left = right = espace entre les 2 (les deux textes ne sont pas forcément centrés, mais optimisent l'espace)

        ImageIO.write(template, format, to)

        val ping = Instant.now().toEpochMilli() - start
        if (ping > 4000) {
            Log.log("PING", "Rank $ping ms")
        }
        devAnalysis.addRankPing(ping)
        return to
    }

    // 1600*1200
    fun leaderboard(lbContext: ComponentManager.LBContext): File {
        val start = Instant.now().toEpochMilli()

        val (group, comparator, _, _, centered) = lbContext
        statsManager.update(group)
        val (statsList, startRank) = leaderboardManager.six(group, comparator, centered)
        if (statsList.size > 6) throw Exception("Too many elements (max 6)")
        if (statsList.isEmpty()) return emptyLb
        val bg = this.javaClass.getResourceAsStream(leaderboardBg) // charge le background
        val template: BufferedImage = ImageIO.read(bg)
        bg?.close()
        val graphics = template.graphics

//        var longestRank = "" // trouve le rang le plus long
//        var longestW = 0
//        val metrics = graphics.getFontMetrics(xpFont) // random font
//        for (index in lb.indices) {
//            val rank = "#${(start + index + 1)}"
//            val w = metrics.stringWidth(rank)
//            if(w > longestW) {
//                longestW = w
//                longestRank = rank
//            }
//        }

        val longestRank = statsList.indices.map { "#${it + 1}" }.maxByOrNull { it.length }!!
        val (rankFont, rankWidth, rankHeight) = getFontDetails(graphics, longestRank, 80, 100, 250, 50)
        val rankY = 100 + rankHeight / 3
        val iconX = rankWidth + 80 // le décalage de l'icône par rapport au rang le plus long

        for ((index, stats) in statsList.withIndex()) {
            val rank = startRank + index
            val alignment = 100 + index * 200

            graphics.color = cyan // Color(150, 150, 150) // dessine le rang
            graphics.font = rankFont
            val rankMsg = "#$rank"
            val rankX = iconX / 2 - graphics.getFontMetrics(rankFont)
                .stringWidth(rankMsg) / 2 // centre le rang entre la jauge et l'icône
            graphics.drawString(rankMsg, rankX, rankY + 200 * index)

            val iconClip = Ellipse2D.Float(iconX.toFloat(), 30f + 200 * index, 140f, 140f) // dessine l'icône
            graphics.clip = iconClip
            val inputStream = getIcon(stats)
            val icon = ImageIO.read(inputStream)
            inputStream.close()
            graphics.drawImage(icon, iconX, 30 + 200 * index, 140, 140, null)
            graphics.clip = null

            graphics.color = Color.lightGray // dessine l'élément de comparaison
            val txt = comparator.txt(stats)
            val (txtFont, txtWidth, txtHeight) = getFontDetails(graphics, txt, 65, 80, 450, 60)
            graphics.font = txtFont
            graphics.drawString(txt, 1550 - txtWidth, alignment + txtHeight / 4)

            graphics.color = Color.white // dessine le nom
            val name = stats.name
            val freeSpace = 1340 - txtWidth - iconX
            val (nameFont, _, nameHeight) = getFontDetails(graphics, name, 100, 120, freeSpace, 80)
            graphics.font = nameFont
            val shown = max(graphics, name, freeSpace, nameFont)
            graphics.drawString(shown, iconX + 170, alignment + nameHeight / 4)
//
            if (index != statsList.size - 1) { // dessine la ligne de séparation
                graphics.color = Color(80, 80, 80)
                graphics.fillRect(150, (index + 1) * 200 - 2, 1300, 2)
            }

//            graphics.color = Color.red
//            graphics.drawLine(0,alignment,1600,alignment) // alignement
//            graphics.drawLine(rankX,index*200,rankX,(index+1)*200) // rank left
//            graphics.drawLine(1510-txtW,index*200,1510-txtW,(index+1)*200) // name : right (maximum)
        }

//        graphics.color = Color.red
//        // icon
//        graphics.drawLine(iconX,0,iconX,1200) // left
//        // name
//        graphics.drawLine(iconX + 170,0,iconX + 170,1200) // left
//        // lvl & xp
//        graphics.drawLine(1550,0,1550,1200) // right (serré à droite)

        ImageIO.write(template, format, to)

        val ping = Instant.now().toEpochMilli() - start
        if (ping > 7000) {
            Log.log("PING", "Leaderboard $ping ms")
        }
        devAnalysis.addLeaderboardPing(ping)
        return to
    }
}