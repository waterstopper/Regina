package properties.primitive

import properties.Type


class PDouble(value: Double, parent: Type?) : PNumber(value, parent) {
    override fun getIndex() = 1
    override fun getPValue() = value as Double
    companion object {
        val functions = PNumber.functions
    }
}