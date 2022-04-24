package properties.primitive

import properties.Type


class PDouble(value: Double, parent: Type?) : PNumber(value, parent) {
    override fun getPValue() = value as Double
    override fun getFunctionOrNull(name: String) = PArray.functions.find { it.name == name }

    companion object {
        val functions = PNumber.functions
    }
}