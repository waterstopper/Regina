package properties.primitive

import References
import properties.EmbeddedFunction
import properties.Type
import round
import utils.Utils.castToPNumber
import utils.Utils.getPInt
import utils.Utils.getPNumber
import utils.Utils.toPInt
import kotlin.math.pow
import kotlin.math.roundToInt

open class PNumber(value: Number, parent: Type?) : Primitive(value, parent) {
    override fun getIndex() = 1
    override fun getPValue() = value as Number

    override fun equals(other: Any?): Boolean {
        if (other !is PNumber)
            return false
        return getPValue().toDouble() == other.getPValue().toDouble()
    }

    override fun toString(): String {
        return value.toString()
    }

    override fun hashCode(): Int {
        return getPValue().hashCode()
    }

    override fun toDebugClass(references: References): Any {
        throw Exception("class is not instantiable")
    }

    operator fun compareTo(number: PNumber): Int {
        return getPValue().toDouble().compareTo(number.getPValue().toDouble())
    }

    operator fun not(): PNumber {
        return (getPValue().toDouble() == 0.0).toPInt()
    }

    operator fun div(number: PNumber): PNumber {
        return PDouble(getPValue().toDouble() / number.getPValue().toDouble(), null)
    }

    open operator fun plus(number: PNumber): PNumber {
        throw Exception("overriden in inheritants")
    }

    open operator fun minus(number: PNumber): PNumber {
        throw Exception("overriden in inheritants")
    }

    open operator fun times(number: PNumber): PNumber {
        throw Exception("overriden in inheritants")
    }

    open operator fun rem(number: PNumber): PNumber {
        throw Exception("overriden in inheritants")
    }

    open operator fun unaryMinus(): PNumber {
        throw Exception("overriden in inheritants")
    }

    companion object {
        fun initializeEmbeddedNumberFunctions() {
            val n = PNumber(0, null)
            setFunction(
                n,
                EmbeddedFunction("abs") { _, args ->
                    val number = castToPNumber(args.getPropertyOrNull("this")!!)
                    if(number.getPValue().toDouble() < 0) -number else number
                }
            )
            setFunction(
                n,
                EmbeddedFunction("min", listOf("other")) { token, args ->
                    val first = castToPNumber(args.getPropertyOrNull("this")!!)
                    val second = getPNumber(args, token, "other")
                    // 'this' has priority
                    if(first.getPValue().toDouble() > second.getPValue().toDouble()) second else first
                }
            )
            setFunction(
                n,
                EmbeddedFunction("max", listOf("other")) { token, args ->
                    val first = castToPNumber(args.getPropertyOrNull("this")!!)
                    val second = getPNumber(args, token, "other")
                    // 'this' has priority
                    if(first.getPValue().toDouble() < second.getPValue().toDouble()) second else first
                }
            )
            setFunction(
                n,
                EmbeddedFunction("pow", listOf("deg")) { token, args ->
                    val number = castToPNumber(args.getPropertyOrNull("this")!!)
                    val deg = getPNumber(args, token, "deg")
                    PDouble(number.getPValue().toDouble().pow(deg.getPValue().toDouble()))
                }
            )
            setFunction(
                n,
                EmbeddedFunction(
                    "round",
                    namedArgs = listOf("digits = 0")
                ) { token, args ->
                    val number = getPNumber(args, token, "this")
                    val digits = getPInt(args, token, "digits")
                    if (digits.getPValue() < 0) {
                        val divisor = 10.0.pow(-digits.getPValue())
                        PDouble((number.getPValue().toDouble() / divisor).roundToInt() * divisor, null)
                    } else PDouble(round(number.getPValue().toDouble(), digits.getPValue()))
                }
            )
            setFunction(
                n,
                EmbeddedFunction(
                    "intDivide",
                    listOf("divisor")
                ) { token, args ->
                    val number = getPNumber(args, token, "this")
                    val divisor = getPNumber(args, token, "divisor")
                    number.getPValue().toInt() / divisor.getPValue().toInt()
                }
            )
        }
    }
}
