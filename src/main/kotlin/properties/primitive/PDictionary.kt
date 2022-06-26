package properties.primitive

import evaluation.FunctionFactory.getDictionary
import evaluation.FunctionFactory.getIdent
import properties.EmbeddedFunction
import properties.Type
import properties.Variable
import token.Token
import utils.Utils.toProperty
import utils.Utils.toVariable

class PDictionary(value: MutableMap<out Any, out Variable>, parent: Type?) : Primitive(value, parent), Indexable {
    override fun getIndex() = 6
    override fun getPValue() = value as MutableMap<Any, Variable>

    override fun get(index: Any, token: Token): Variable {
        return getPValue()[index.toVariable(token)] ?: PInt(0, null)
    }

    override fun set(index: Any, value: Any, tokenIndex: Token, tokenValue: Token) {
        getPValue()[index.toVariable(tokenIndex)] = value.toVariable(tokenValue)
    }

    companion object {
        fun initializeDictionaryProperties() {
            val p = PDictionary(mutableMapOf(), null)
            setProperty(p, "size") { pr: Primitive -> (pr as PDictionary).getPValue().size.toProperty() }
            setProperty(p, "keys") { pr: Primitive ->
                (pr as PDictionary).getPValue().keys.toMutableList().toProperty()
            }
            setProperty(p, "values") { pr: Primitive ->
                (pr as PDictionary).getPValue().values.toMutableList().toProperty()
            }
        }

        fun initializeDictionaryFunctions() {
            val p = PDictionary(mutableMapOf(), null)
            setFunction(p, EmbeddedFunction("remove", listOf(Token(value = "key"))) { token, args ->
                val dict = getDictionary(token, "this", args)
                val key = getIdent(token, "key", args)
                dict.getPValue().remove(key)?.toVariable(token) ?: PInt(0, null)
            })
        }
    }
}
