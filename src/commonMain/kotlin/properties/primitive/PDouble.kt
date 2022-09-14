package properties.primitive

import References
import properties.Type
import utils.Utils.toProperty

class PDouble(value: Double, parent: Type? = null) : PNumber(value, parent) {
    override fun getIndex() = 3
    override fun getPValue() = value as Double
    override fun toDebugClass(references: References): Any {
        return Pair("Double", getPValue())
    }

    override operator fun plus(number: PNumber): PNumber {
        return PDouble(getPValue() + number.getPValue().toDouble())
    }

    override operator fun minus(number: PNumber): PNumber {
        return PDouble(getPValue() - number.getPValue().toDouble())
    }

    override operator fun times(number: PNumber): PNumber {
        return PDouble(getPValue() * number.getPValue().toDouble())
    }

    override operator fun rem(number: PNumber): PNumber {
        return PDouble(getPValue() % number.getPValue().toDouble())
    }

    override operator fun unaryMinus(): PNumber = PDouble(-getPValue())

    companion object {
        fun initializeDoubleProperties() {
            val d = PDouble(0.0)
            setProperty(d, "MIN_VALUE") { PDouble(Double.MIN_VALUE).toProperty() }
            setProperty(d, "MAX_VALUE") { PDouble(Double.MAX_VALUE).toProperty() }
        }

        fun initializeEmbeddedDoubleFunctions() {
            val d = PDouble(0.0)
        }
    }
}
