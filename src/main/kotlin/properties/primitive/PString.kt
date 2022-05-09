package properties.primitive

import properties.Type

class PString(value: String, parent: Type?) : Primitive(value, parent) {
    override fun getIndex() = 3
    override fun getPValue() = value as String
}