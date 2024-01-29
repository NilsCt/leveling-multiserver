package nilsct.leveling.entities

import com.fasterxml.jackson.annotation.JsonIgnore

class RewardRole(val id: String, var lvl: Int) {

    val mention @JsonIgnore get() = "<@&$id>"
    val msg @JsonIgnore get() = "$mention: lvl **$lvl**"

}