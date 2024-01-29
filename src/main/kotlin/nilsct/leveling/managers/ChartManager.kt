package nilsct.leveling.managers

import com.opencsv.CSVReader
import com.opencsv.CSVWriter
import nilsct.leveling.Bot.Companion.jda
import nilsct.leveling.managers.CardManager.Companion.cardManager
import nilsct.leveling.managers.UserManager.Companion.userManager
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartUtils
import org.jfree.chart.axis.DateAxis
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
import org.jfree.chart.ui.RectangleInsets
import org.jfree.data.time.Day
import org.jfree.data.time.TimeSeries
import org.jfree.data.time.TimeSeriesCollection
import org.jfree.data.xy.XYDataset
import java.awt.BasicStroke
import java.awt.Color
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import javax.swing.JFrame


class ChartManager {

    companion object {
        val chartManager = ChartManager()
    }

    val file = File("saves/growth.csv")
    private val dateFormat = SimpleDateFormat("d/M/yy") // 2/7/2022 (sans les 0 inutiles)

    enum class Chart(val title: String, val column: Int) {
        // global
        SERVERS("Servers", 1), // en réalité, c'est le nombre de guildes
        USERS("Users", 2),

        // quotidien
        NEW_SERVERS("New Servers", 3),
        NEW_USERS("New Members", 4),
        XP("XP", 5),
        INTERACTIONS("Interactions", 6)
    }

    fun append(then: (() -> Unit)? = null) {
        try {
            val csvWriter = CSVWriter(FileWriter(file, true))
            val date = Date.from(Instant.now())
            csvWriter.writeNext(
                arrayOf(
                    dateFormat.format(date),
                    jda.guilds.size.toString(), // attention shard
                    userManager.size.toString(),
                    DevAnalysis.newServers.toString(),
                    DevAnalysis.newUsers.toString(),
                    DevAnalysis.xp.toString(),
                    DevAnalysis.interactions.toString()
                ), false
            )
            csvWriter.close()
            if (then != null) then()
        } catch (e: Exception) {
            Log.error("DAILY", "Save growth $e")
            if (then != null) then()
        }
    }

    fun chart(type: Chart, success: (File) -> Unit, failure: () -> Unit) {
        try {
            val csvReader = CSVReader(FileReader(file))
            val read = csvReader.readAll()
            csvReader.close()
            val series = TimeSeries("Netflix Original Series")
            // /!\ Certains graphiques ne commencent pas à la première ligne (xp, interactions, nouveaux servers, nouveaux members)
            for (line in read) {
                val bits = line[0].split("/")
                val date = Day(bits[0].toInt(), bits[1].toInt(), ("20" + bits[2]).toInt())
                val nbr = line.getOrNull(type.column)?.toIntOrNull()
                nbr?.let { series.addOrUpdate(date, it) }
            }
            val dataset = TimeSeriesCollection()
            dataset.addSeries(series)
            LineChartEx(dataset, type.title, success, failure)
        } catch (e: Exception) {
            Log.error("CHART", "Load chart dataset $type $e")
            failure()
        }
    }

    class LineChartEx(dataset: XYDataset, title: String, success: (File) -> Unit, failure: () -> Unit) : JFrame() {

        companion object {
            private val to = File("/tmp/chart.png")

            private val dark get() = Color(30, 33, 36) // obligé get() pour éviter bug
            private val light get() = Color(66, 69, 73)
            private val lightBurple get() = Color(114, 137, 218)

            private val bigTitle get() = cardManager.getFont("/fonts/GintoNord-Black.ttf").deriveFont(90f)
            private val axe get() = cardManager.getFont("/fonts/Whitney Bold.otf").deriveFont(40f)
            private val tick get() = cardManager.getFont("/fonts/Whitney Medium.otf").deriveFont(25f)
        }

        init {
            try {
                // Créer le graphique
                val chart = ChartFactory.createTimeSeriesChart(title, "time", title.lowercase(), dataset)
                chart.removeLegend()
                chart.backgroundPaint = dark
                val plot = chart.xyPlot

                plot.renderer.run {// courbe
                    setSeriesPaint(0, lightBurple) // couleur du trait
                    setSeriesStroke(0, BasicStroke(4.0f)) // largeur du trait
                    (this as XYLineAndShapeRenderer).setSeriesShapesVisible(0, true) // points
                }

                plot.insets = RectangleInsets(10.0, 20.0, 10.0, 40.0)

                chart.title.run { // titre
                    font = bigTitle
                    paint = lightBurple
                    setPadding(40.0, 0.0, 0.0, 0.0) // écart
                }

                plot.run { // partie graphique
                    backgroundPaint = dark // arrière-plan
                    isRangeGridlinesVisible = true // grille |
                    rangeGridlinePaint = light
                    isDomainGridlinesVisible = true // grille --
                    domainGridlinePaint = light
                    isOutlineVisible = true // bordure du graphique
                    outlinePaint = light
                }

                (plot.domainAxis as DateAxis).run {// axe x
                    isAxisLineVisible = false // lignes d'axes de graduation
                    isTickMarksVisible = false // petites barres
                    labelPaint = light // titre de l'axe
                    labelFont = axe
                    tickLabelPaint = light // étiquette des graduations
                    tickLabelFont = tick
                    dateFormatOverride = SimpleDateFormat("d MMM") // format de la date
                    // cadre pour chaque label qui force à générer moins de tick pour pas qu'ils ne se superposent
//                tickLabelInsets = RectangleInsets(6.0,4.0,6.0,4.0)
                }

                (plot.rangeAxis as NumberAxis).run {// axe y
                    isAxisLineVisible = false // lignes d'axes de graduation
                    isTickMarksVisible = false // petites barres
                    labelPaint = light // titre de l'axe
                    labelFont = axe
                    tickLabelPaint = light // étiquette des graduations
                    tickLabelFont = tick
//                tickLabelInsets = RectangleInsets(6.0,4.0,6.0,4.0) // cadre
//                tickUnit = NumberTickUnit(((plot.rangeAxis.range.length / 5).roundToInt() /10 * 10).to Double()) // intervalle de graduation (obligé plot.rangeAxis)
                }

                // Créer l'image
                ChartUtils.saveChartAsPNG(to, chart, 1200, 800)
                success(to)
            } catch (e: Exception) {
                Log.error("CHART", "Create chart $title $e")
                failure()
            }
        }
    }
}