package properties.primitive

import properties.Type


class PDouble(value: Double, parent: Type?) : Primitive(value, parent) {
    override fun getPValue() = value as Double
}