package nilsct.leveling

import java.text.NumberFormat
import java.util.*

private val numberFormat = NumberFormat.getNumberInstance(Locale.US)
private val units = listOf("k", "M", "B")

//fun Float.shorten() = ((this * 10).toInt() / 10f) // garde seulement un chiffre après la virgule

// les "format()" transforment un nombre 123456789 en 123,456,789

private fun Long.format(): String = numberFormat.format(this)

//private fun Int.format() = this.toLong().format()

/*
 de 0 à 99 → 71.2
 plus de 1000 → 784,254 (la virgule disparait)
 /!\ les nombres sont arrondis en dessous
 */
//fun Float.format(): String {
//    return if (this < 100) {
//        this.shorten().toString()
//    } else {
//        this.toLong().format()
//    }
//}

/*
 les "simplify()" raccourcissent un nombre grâce à une unité (k, M, B)
 de 1 à 999 → c'est le même nombre
 de 1000 à 4999 → le nombre est formaté 2751 → 2,751
 à partir d'ici seulement 3 chiffres sont affichés (les nombres sont arrondis en dessous)
 de 5000 à 999 999 → k : 7496 → 7.49k 842 369 → 842k
 de 1 000 000 à 999 999 999 → M : 24 578 324 → 24.5M
 /!\ à partir d'ici, la limite de 3 chiffres n'est plus effectives si le nombre sans virgule à plus de 3 chiffres
 plus de 1 000 000 000 → B : 24784 246 245 314 → 24784B
 */
fun Long.simplify(): String {
    return when {
        this < 1000 -> this.toString()
        this < 5000 -> this.format()
        else -> {
            var unitIndex = -1
            var nbr = this.toFloat()
            while (nbr > 1000) {
                unitIndex++
                if (unitIndex == units.size) break // au cas ou il n'y a pas assez d'unités
                nbr /= 1000
            }
            unitIndex = unitIndex.coerceAtMost(units.size - 1) // ici aussi
            var showed = nbr.toString()
            if (nbr > 1000) {
                showed = showed.split(".").first().toLong().format()
            } else {
                if (showed.length > 3) showed = showed.substring(0, 4)
            }
            showed = showed.removeSuffix(".")
            showed + units[unitIndex]
        }
    }
}

fun Int.simplify() = this.toLong().simplify()

//fun Float.simplify(): String { // de 0 a 99 -> 71.2 à partir de 1000 -> 78.4k
//    return if (this < 100) {
//        this.shorten().toString()
//    } else {
//        this.toLong().simplify()
//    }
//}

/*
 exprime un temps en seconde avec l'unité la plus adaptée (la plus grande possible)
 15 → 15s
 64 → 1m
 */
fun Int.timeFormat(): String {
    val years = this / 31536000
    val months = this / 2628000
    val days = this / 86400
    val hours = this / 3600
    val minutes = this / 60
    return when {
        years > 0 -> "${years}y"
        months > 0 -> "${months}mo"
        days > 0 -> "${days}d"
        hours > 0 -> "${hours}h"
        minutes > 0 -> "${minutes}m"
        else -> "${this}s"
    }
}

/*
 exprime un temps en seconde avec toutes les unités non nulles
 60 → 1m
 62 → 1m 2s
 3602 → 1h 2s
 */
fun Int.detailedTimeFormat(): String { // montre toutes les unités qui ont une valeur non nulle
    val years = this / 31536000
    val months = this % 31536000 / 2628000
    val days = this % 31536000 % 2628000 / 86400
    val hours = this % 31536000 % 2628000 % 86400 / 3600
    val minutes = this % 31536000 % 2628000 % 86400 % 3600 / 60
    val seconds = this % 31536000 % 2628000 % 86400 % 3600 % 60
    var msg = ""
    if (years > 0) msg += "${years}y "
    if (months > 0) msg += "${months}mo "
    if (days > 0) msg += "${days}d "
    if (hours > 0) msg += "${hours}h "
    if (minutes > 0) msg += "${minutes}m "
    if (seconds > 0) msg += "${seconds}s "
    if (msg.isEmpty()) msg = "0s"
    return msg
}