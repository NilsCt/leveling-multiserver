package nilsct.leveling.interactions

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.commands.build.CommandData

abstract class Interaction {

    abstract val id: String
    open val private = false // true si on peut utiliser cette commande en message privé

    open val permission: List<Permission> = emptyList()
    open val botPermission: List<Permission> = emptyList()
    open val developer = false // true si réservé aux développeurs

    /*
    true pour commandes seules et commandes principales d'un groupe de commandes
    (1 seule par groupe qui contient les commands data voir roles menu)
     */
    open val mainCommand = true
    open val commandData: CommandData? = null // only for slash & context commands


    abstract fun execute(context: InteractionContext)

}