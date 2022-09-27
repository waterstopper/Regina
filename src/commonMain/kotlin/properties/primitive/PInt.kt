package properties.primitive

import References
import properties.Type
import utils.Utils.toProperty

class PInt(value: Int, parent: Type? = null) : PNumber(value, parent) {
    override fun getIndex() = 2
    override fun getPValue() = value as Int
    override fun toDebugClass(references: References): Any {
        return Pair("Int", getPValue())
    }

    override operator fun plus(number: PNumber): PNumber {
        if (number is PDouble) {
            return number.plus(this)
        }
        return PInt(getPValue() + (number.getPValue() as Int), null)
    }

    override operator fun minus(number: PNumber): PNumber {
        if (number is PDouble) {
            return PDouble(getPValue().toDouble() - number.getPValue())
        }
        return PInt(getPValue() - (number.getPValue() as Int), null)
    }

    override operator fun times(number: PNumber): PNumber {
        if (number is PDouble) {
            return number.times(this)
        }
        return PInt(getPValue() * (number.getPValue() as Int), null)
    }

    override operator fun rem(number: PNumber): PNumber {
        if (number is PDouble) {
            return PDouble(getPValue().toDouble() % number.getPValue(), null)
        }
        return PInt(getPValue() % (number.getPValue() as Int), null)
    }

    override operator fun unaryMinus(): PNumber = PInt(-getPValue(), null)

    companion object {
        fun initializeIntProperties() {
            val i = PInt(0, null)
            setProperty(i, "MIN_VALUE") { p: Primitive -> PInt(Int.MIN_VALUE).toProperty() }
            setProperty(i, "MAX_VALUE") { p: Primitive -> PInt(Int.MAX_VALUE).toProperty() }
        }
    }
}
