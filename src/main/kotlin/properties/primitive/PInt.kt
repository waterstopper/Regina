package properties.primitive

import properties.Type


class PInt(value: Int, parent: Type?) : PNumber(value, parent) {
    override fun getPValue() = value as Int
    override fun getFunctionOrNull(name: String) = PArray.functions.find { it.name == name }


    companion object {
        val functions = PNumber.functions
    }
}