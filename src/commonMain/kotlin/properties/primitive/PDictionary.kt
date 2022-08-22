package properties.primitive

import DebugDictionary
import References
import evaluation.FunctionFactory.getDictionary
import evaluation.FunctionFactory.getIdent
import node.Node
import properties.EmbeddedFunction
import properties.Type
import properties.Variable
import utils.Utils.toProperty
import utils.Utils.toVariable

class PDictionary(value: MutableMap<out Any, out Variable>, parent: Type?) : Primitive(value, parent), Indexable {
    override fun getIndex() = 6
    override fun getPValue() = value as MutableMap<Any, Variable>

    override fun get(index: Any, node: Node): Variable {
        return getPValue()[index.toVariable(node)] ?: PInt(0, null)
    }

    override fun set(index: Any, value: Any, nodeIndex: Node, nodeValue: Node) {
        getPValue()[index.toVariable(nodeIndex)] = value.toVariable(nodeValue)
    }

    override fun toString(): String {
        if(getPValue().isEmpty())
            return "{}"
        val res = StringBuilder("{")
        for ((key, value) in getPValue()) {
            if (key == this)
                res.append("this")
            else res.append(key)
            res.append("=")
            res.append(if (value == this) "this" else value)
            res.append(", ")
        }
        res.deleteRange(res.lastIndex - 1, res.length)
        res.append("}")
        return res.toString()
    }

    override fun toDebugClass(references: References): Any {
        val id = hashCode()
        if (references.dictionaries[id] != null)
            return Pair("dictionary", id)
        val res = DebugDictionary(getPValue().map {
            val key = if (it.key is Variable) (if (it.key == this) Pair("dictionary", id)
            else (it.key as Variable).toDebugClass(references))
            else it.key
            val value = if (it.value == this) Pair("dictionary", id)
            else it.value.toDebugClass(references)
            key to value
        }.toMap().toMutableMap())
        references.dictionaries[id] = res
        return Pair("dictionary", id)
    }

    override fun checkIndexType(index: Variable): Boolean {
        return true
    }

    companion object {
        fun initializeDictionaryProperties() {
            val p = PDictionary(mutableMapOf(), null)
            setProperty(p, "size") { pr: Primitive ->
                (pr as PDictionary).getPValue().size.toProperty()
            }
            setProperty(p, "keys") { pr: Primitive ->
                (pr as PDictionary).getPValue().keys.toMutableList().toProperty()
            }
            setProperty(p, "values") { pr: Primitive ->
                (pr as PDictionary).getPValue().values.toMutableList().toProperty()
            }
        }

        fun initializeDictionaryFunctions() {
            val p = PDictionary(mutableMapOf(), null)
            setFunction(p, EmbeddedFunction("remove", listOf("key")) { token, args ->
                val dict = getDictionary(token, "this", args)
                val key = getIdent(token, "key", args)
                dict.getPValue().remove(key)?.toVariable(token) ?: PInt(0, null)
            })
        }
    }
}
