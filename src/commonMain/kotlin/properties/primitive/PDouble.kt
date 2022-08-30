package properties.primitive

import References
import properties.Type
import utils.Utils.toProperty

class PDouble(value: Double, parent: Type?) : PNumber(value, parent) {
    override fun getIndex() = 3
    override fun getPValue() = value as Double
    override fun toDebugClass(references: References): Any {
        return Pair("Double", getPValue())
    }

    companion object {
        fun initializeDoubleProperties() {
            val d = PDouble(0.0, null)
            setProperty(d, "MIN_VALUE") { Double.MIN_VALUE.toProperty() }
            setProperty(d, "MAX_VALUE") { Double.MAX_VALUE.toProperty() }
        }

        fun initializeEmbeddedDoubleFunctions() {
            val d = PDouble(0.0, null)

        }
    }
}
