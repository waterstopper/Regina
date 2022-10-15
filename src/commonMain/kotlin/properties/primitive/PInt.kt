package properties.primitive

import References
import utils.Utils.toProperty

class PInt(value: Int) : PNumber(value) {
    override fun getIndex() = 2
    override fun getPValue() = value as Int
    override fun toDebugClass(references: References, copying: Boolean): Pair<String, Any> {
        return Pair("Int", getPValue())
    }

    override operator fun plus(number: PNumber): PNumber {
        if (number is PDouble) {
            return number.plus(this)
        }
        return PInt(getPValue() + (number.getPValue() as Int))
    }

    override operator fun minus(number: PNumber): PNumber {
        if (number is PDouble) {
            return PDouble(getPValue().toDouble() - number.getPValue())
        }
        return PInt(getPValue() - (number.getPValue() as Int))
    }

    override operator fun times(number: PNumber): PNumber {
        if (number is PDouble) {
            return number.times(this)
        }
        return PInt(getPValue() * (number.getPValue() as Int))
    }

    override operator fun rem(number: PNumber): PNumber {
        if (number is PDouble) {
            return PDouble(getPValue().toDouble() % number.getPValue())
        }
        return PInt(getPValue() % (number.getPValue() as Int))
    }

    override operator fun unaryMinus(): PNumber = PInt(-getPValue())

    companion object {
        fun initializeIntProperties() {
            val i = PInt(0)
            setProperty(i, "MIN_VALUE") { p: Primitive -> PInt(Int.MIN_VALUE).toProperty() }
            setProperty(i, "MAX_VALUE") { p: Primitive -> PInt(Int.MAX_VALUE).toProperty() }
        }
    }
}
