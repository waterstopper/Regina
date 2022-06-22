package properties.primitive

import properties.Type
import properties.Variable
import token.Token
import utils.Utils.toProperty
import utils.Utils.toVariable

class PDictionary(value: MutableMap<out Any, out Variable>, parent: Type?) : Primitive(value, parent), Indexable {
    override fun getIndex() = 6
    override fun getPValue() = value as MutableMap<Any, Variable>

    override fun get(index: Any, token: Token): Variable {
        return getPValue()[index] ?: PInt(0, null)
    }

    override fun set(index: Any, value: Any, tokenIndex: Token, tokenValue: Token) {
        getPValue()[index] = value.toVariable(tokenIndex)
    }

    companion object {
        fun initializeDictionaryProperties() {
            val p = PDictionary(mutableMapOf(), null)
            setProperty(p, "size") { pr: Primitive -> (pr as PDictionary).getPValue().size.toProperty() }
        }
    }
}
