package properties.primitive

import properties.Type
import utils.Utils.toProperty


class PDouble(value: Double, parent: Type?) : PNumber(value, parent) {
    override fun getIndex() = 3
    override fun getPValue() = value as Double

    companion object {
        fun initializeDoubleProperties() {
            val d = PDouble(0.0, null)
            setProperty(d, "MIN_VALUE") { p: Primitive -> Double.MIN_VALUE.toProperty() }
            setProperty(d, "MAX_VALUE") { p: Primitive -> Double.MAX_VALUE.toProperty() }
        }
    }
}