package rand

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("RandList")
class RandList(var list: List<Double> = mutableListOf(), var amount: Int = 0) : Rand() {

    override fun evaluate(): List<Double> = list.shuffled(Global.random).take(amount)
}