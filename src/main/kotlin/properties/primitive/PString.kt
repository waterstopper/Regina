package properties.primitive

import properties.Type

class PString(value: String, parent: Type?) : Primitive(value, parent) {
    override fun getPValue() = value as String
}