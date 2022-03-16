package evaluation

import java.lang.Math.*
import kotlin.math.*
import kotlin.random.Random

object Function {
    //region Random
    private var seed = 42
    private var random = Random(seed)

    fun changeSeed(customSeed: Int) {
        seed = customSeed
        random = Random(seed)
    }
    //endregion

    fun evalFunction(content: String): Any {
        val values = content.split(Regex("[;,]")).map { Evaluation.evaluate(it) }
        return when (values[0]) {
            "cos" -> kotlin.math.cos(values[1].toString().toDouble())
            "sin" -> kotlin.math.sin(values[1].toString().toDouble())
            "pow" -> values[1].toString().toDouble().pow(values[2].toString().toDouble())
            "randNum" -> randNum(values.subList(1, values.size).map { it.toString().toDouble() })
            "randList" -> randList(values.subList(1, values.size))
            "toRadians" -> toRadians(values[1].toString().toDouble())
            "toDegrees" -> toDegrees(values[1].toString().toDouble())
            else -> throw Exception("no function named ${values[0]}")
        }
    }

    private fun randList(values: List<Any>) = values.shuffled(random).take(1)

    private fun randNum(values: List<Double>): Number {
        if (values.size == 2)
            return random.nextDouble(values[0], values[1])
        if (values[2] - values[2].toInt() == 0.0)
            return (random.nextInt(values[0].toInt(), values[1].toInt())
                .toDouble() / values[2].toInt()).roundToInt() * values[2].toInt() + (values[0] - values[0].toInt())
        return kotlin.math.round(random.nextDouble(values[0], values[1]) / values[2]) * values[2]
    }

}