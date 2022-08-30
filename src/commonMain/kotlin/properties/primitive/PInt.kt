package properties.primitive

import References
import properties.Type
import utils.Utils.toProperty

class PInt(value: Int, parent: Type?) : PNumber(value, parent) {
    override fun getIndex() = 2
    override fun getPValue() = value as Int
    override fun toDebugClass(references: References): Any {
        return Pair("Int", getPValue())
    }

    companion object {
        fun initializeIntProperties() {
            val i = PInt(0, null)
            setProperty(i, "MIN_VALUE") { p: Primitive -> Int.MIN_VALUE.toProperty() }
            setProperty(i, "MAX_VALUE") { p: Primitive -> Int.MAX_VALUE.toProperty() }
        }
    }
}
