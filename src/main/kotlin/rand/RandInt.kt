package rand

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt
import kotlin.random.Random

@Serializable
@SerialName("RandInt")
class RandInt(
    override val from: Int = 0,
    override val until: Int = 1,
    override val step: Int = 1
) : RandNum() {

    override fun evaluate(): Int =
        (Random.nextInt(from, until).toDouble() / step).roundToInt() * step
}