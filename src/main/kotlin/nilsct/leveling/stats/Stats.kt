package nilsct.leveling.stats

import nilsct.leveling.entities.Privacy
import nilsct.leveling.entities.Type
import java.util.*
import kotlin.math.pow

/**
 * Has stats like amount of sent messages
 */
sealed class Stats {

    abstract val type: Type

    var xp = 0
    var totalXP = 0
    var lvl = 1

    //    val requiredXP get() = 20 + 5 * lvl
    val requiredXP get() = (20 + 5 * lvl + 0.2 * lvl.toDouble().pow(2)).toInt()

    var messages = 0
    var activeDays = 0
    var voice = 0
    var activeVoice = 0 // pas seul, mute ou sourd dans un vocal

    var lastActiveDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR) - 1

    abstract val privacy: Privacy
    val public get() = privacy == Privacy.NORMAL // visible par tout le monde

    /*
     Utilisé pour les leaderboards car je ne peux récupérer les noms et avatars au moment voulu
     Priorité :
        Guild Nickname > User Name
        Guild Avatar > User Avatar > Default Avatar
     Liens (https://cdn.discordapp.com/ + ...) :
        server : icons/GUILD_ID/HASH.(extension)
        user : avatars/USER_ID/HASH.(extension)
        member : guilds/GUILD_ID/users/USER_ID/avatars/HASH.(extension)
        default user : embed/avatars/USER_DISCRIMINATOR.(extension)
     Notes :
        Si je ne mets pas d'extension ça renvoie un gif si possible
        Si je mets .png ça donne la première image du gif
        La plupart des images ont l'air d'être en .png
     Extensions possibles :
        .jpg .jpeg, .png, .webp, .gif
     */
    var name = ""
    var icon = ""

}