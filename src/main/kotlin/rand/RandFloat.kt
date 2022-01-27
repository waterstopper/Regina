package rand

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.round
import kotlin.random.Random


//@Serializable
//@SerialName("RandFloat")
class RandFloat( override val from: Float = 0f,
                 override val until: Float = 1f,
                 override val step: Float = 0f): RandNum() {

    override fun evaluate(): Float =
        (round(Random.nextDouble(from.toDouble(), until.toDouble()) / step) * step).toFloat()
}