package properties.primitive

import properties.Type


class PInt(value: Int, parent: Type?) : Primitive(value, parent){
    override fun getPValue() = value as Int
}