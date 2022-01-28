package rand

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.round
import kotlin.math.roundToInt

@Serializable
@SerialName("RandNum")
class RandNum(
    val from: Double = 0.0,
    val until: Double = 0.0,
    val step: Double = 1.0
) :
    Rand() {

    // @Contextual
    //val from: Number = if(a is Int) a.toInt() else a.toDouble()

    // @Contextual
    //abstract val until: Number

    // @Contextual
    // abstract val step: Number

    public override fun evaluate(): Number {
        if (from.roundToInt() == from.toInt()
            && until.roundToInt() == until.toInt()
            && step.roundToInt() == step.toInt()
        )
            return evaluateInt()
        return evaluateDouble()
    }

    private fun evaluateDouble(): Double =
        (round(Global.random.nextDouble(from, until) / step) * step)


    private fun evaluateInt(): Int =
        (Global.random.nextInt(from.toInt(), until.toInt()).toDouble() / step.toInt()).roundToInt() * step.toInt()
}