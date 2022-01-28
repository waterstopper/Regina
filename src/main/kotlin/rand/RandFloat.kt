package rand

import kotlin.math.round
import kotlin.random.Random

@Deprecated("use RandNum")
//@Serializable
//@SerialName("RandFloat")
class RandFloat(
    val from: Float = 0f,
    val until: Float = 1f,
    val step: Float = 0f
) {//: RandNum() {

    fun evaluate(): Float =
        (round(Random.nextDouble(from.toDouble(), until.toDouble()) / step) * step).toFloat()
}