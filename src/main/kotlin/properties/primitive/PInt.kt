package properties.primitive

import properties.Type


class PInt(value: Int, parent: Type?) : PNumber(value, parent) {
    override fun getIndex() = 2
    override fun getPValue() = value as Int

    companion object {
        val functions = PNumber.functions
    }
}