package properties.primitive

import DebugList
import NestableDebug
import References
import elementToDebug
import lexer.PositionalException
import node.Identifier
import node.Node
import node.invocation.Call
import node.invocation.ResolvingMode
import properties.*
import table.FileTable
import table.SymbolTable
import utils.Utils.NULL
import utils.Utils.castToPList
import utils.Utils.getIdent
import utils.Utils.getPInt
import utils.Utils.getPList
import utils.Utils.getPString
import utils.Utils.toPInt
import utils.Utils.toProperty
import utils.Utils.toVariable

class PList(value: MutableList<Variable>, parent: Type?, val id: Int) :
    Primitive(value, parent),
    Indexable,
    NestableDebug,
    Containerable {
    override fun getIndex() = 5
    override fun getPValue() = value as MutableList<Variable>
    override fun get(index: Any, node: Node, fileTable: FileTable): Any {
        if (index !is PInt) {
            throw PositionalException("Expected integer as index", fileTable.filePath, node)
        }
        if (index.getPValue() < 0 || index.getPValue() >= getPValue().size) {
            throw PositionalException("Index out of bounds", fileTable.filePath, node)
        }
        return getPValue()[index.getPValue()]
    }

    override fun toDebugClass(references: References): Any {
        val id = getDebugId()
        references.queue.remove(id)
        if (references.lists[id.second] != null) {
            return id
        }
        val res = DebugList(
            getPValue().map {
                if (it == this) id else elementToDebug(it, references)
            }
        )
        references.lists[id.second as Int] = res
        return id
    }

    override fun getDebugId(): Pair<String, Any> = Pair("List", id)

    override fun set(index: Any, value: Any, nodeIndex: Node, nodeValue: Node, fileTable: FileTable) {
        getPValue()[(index as PInt).getPValue()] = value.toVariable(nodeIndex)
    }

    override fun toString(): String {
        val res = StringBuilder("[")
        for (e in getPValue())
            res.append("${if (e == this) "this" else e.toString()}, ")
        if (res.toString() == "[") {
            return "[]"
        }
        return res.removeRange(res.lastIndex - 1..res.lastIndex).toString() + ']'
    }

    override fun equals(other: Any?): Boolean {
        if (other !is PList) return false
        if (getPValue() == other.getPValue()) {
            return true
        }
        return false
    }

    override fun hashCode(): Int = getPValue().hashCode()

    override fun checkIndexType(index: Variable): Boolean {
        return index is PInt
    }

    override fun getCollection(): Collection<Variable> = getPValue()
    override fun getContainerId(): Int = id

    companion object {
        fun initializeListProperties() {
            val p = PList(mutableListOf(), null, listId++)
            setProperty(p, "size") { pr: Primitive -> PInt((pr as PList).getPValue().size).toProperty() }
        }

        fun initializeEmbeddedListFunctions() {
            val p = PList(mutableListOf(), null, listId++)
            setFunction(
                p,
                EmbeddedFunction("toString") { _, args ->
                    val list = castToPList(args.getPropertyOrNull("this")!!)
                    list.getPValue().toString()
                }
            )
            setFunction(
                p,
                EmbeddedFunction(
                    "add",
                    listOf("element"),
                    listOf("index = this.size")
                ) { token, args ->
                    val list = castToPList(args.getPropertyOrNull("this")!!)
                    val argument = getIdent(token, "element", args)
                    val index = getPInt(args, token, "index")
                    if (index.getPValue() < 0 || index.getPValue() > list.getPValue().size) {
                        throw PositionalException(
                            "Index out of bounds",
                            args.getFileTable().filePath,
                            token.children[1]
                        )
                    }
                    list.getPValue().add(index.getPValue(), argument)
                    NULL
                }
            )
            setFunction(
                p,
                EmbeddedFunction(
                    "remove",
                    args = listOf("element")
                ) { token, args ->
                    val list = castToPList(args.getPropertyOrNull("this")!!)
                    val argument = getIdent(token, "element", args)
                    if (argument is Primitive) {
                        var removedIndex = -1
                        for ((i, e) in list.getPValue().withIndex()) {
                            if (e is Primitive && e == argument) {
                                removedIndex = i
                                list.getPValue().remove(e)
                                break
                            }
                        }
                        PInt(removedIndex)
                    } else {
                        val removedIndex = list.getPValue().indexOf(argument)
                        if (removedIndex != -1) {
                            list.getPValue().removeAt(removedIndex)
                        }
                        PInt(removedIndex)
                    }
                }
            )
            setFunction(
                p,
                EmbeddedFunction("removeAt", listOf("index")) { token, args ->
                    val list = castToPList(args.getPropertyOrNull("this")!!)
                    val index = getPInt(args, token, "index")
                    val res: Any
                    try {
                        res = list.getPValue().removeAt(index.getPValue())
                    } catch (e: IndexOutOfBoundsException) {
                        throw PositionalException(
                            "index ${index.getPValue()} out of bounds for length ${list.getPValue().size}",
                            args.getFileTable().filePath
                        )
                    }
                    res
                }
            )
            setFunction(
                p,
                EmbeddedFunction("has", listOf("element")) { token, args ->
                    val list = castToPList(args.getPropertyOrNull("this")!!)
                    val element = getIdent(token, "element", args)
                    list.getPValue().any { (it is Primitive && it == element) }.toPInt()
                }
            )
            setFunction(
                p,
                EmbeddedFunction("index", listOf("element")) { token, args ->
                    val list = castToPList(args.getPropertyOrNull("this")!!)
                    val element = getIdent(token, "element", args)
                    PInt(list.getPValue().indexOf(element))
                }
            )
            setFunction(
                p,
                EmbeddedFunction(
                    "joinToString",
                    namedArgs = listOf("separator = \", \"", "prefix = \"\"", "postfix = \"\"")
                ) { token, args ->
                    val list = getPList(args, token, "this")
                    val separator = getPString(args, token, "separator")
                    val prefix = getPString(args, token, "prefix")
                    val postfix = getPString(args, token, "postfix")
                    list.getPValue().joinToString(
                        separator = separator.getPValue(),
                        prefix = prefix.getPValue(),
                        postfix = postfix.getPValue(),
                        transform = {
                            val tmp = Node("()")
                            tmp.children.add(Identifier("toString"))
                            val functionNode = Call(tmp)
                            val f = it.getFunctionOrNull(functionNode)
                            if (f != null) {
                                val tableForEvaluation = SymbolTable(
                                    fileTable = if (it is Type) it.fileTable
                                    else args.getFileTable(),
                                    variableTable = it,
                                    resolvingType = ResolvingMode.FUNCTION
                                )
                                val functionResult = functionNode.evaluateFunction(tableForEvaluation, f)
                                functionResult.toString()
                            } else it.toString()
                        }
                    )
                }
            )
            setFunction(
                p,
                EmbeddedFunction("clear") { _, args ->
                    val list = castToPList(args.getPropertyOrNull("this")!!)
                    list.getPValue().clear()
                    NULL
                }
            )
            setFunction(
                p,
                EmbeddedFunction("reverse") { token, args ->
                    val list = getPList(args, token, "this")
                    list.getPValue().reverse()
                    NULL
                }
            )
            setFunction(
                p,
                EmbeddedFunction("reversed") { token, args ->
                    val list = getPList(args, token, "this")
                    list.getPValue().reversed()
                }
            )
            setFunction(
                p,
                EmbeddedFunction("sort", listOf(), listOf("desc = false")) { token, args ->
                    val list = getPList(args, token, "this")
                    val desc = getPInt(args, token, "desc")
                    val comparator = Comparator<Variable> { a, b -> compareVariables(a, b) }
                    list.getPValue().sortWith(comparator)
                    if (desc.getPValue() != 0) {
                        list.getPValue().reverse()
                    }
                    NULL
                }
            )
            setFunction(
                p,
                EmbeddedFunction("sorted", namedArgs = listOf("desc = false")) { token, args ->
                    val list = getPList(args, token, "this")
                    val desc = getPInt(args, token, "desc")
                    val comparator = Comparator<Variable> { a, b -> compareVariables(a, b) }
                    val res = list.getPValue().sortedWith(comparator).toMutableList()
                    if (desc.getPValue() != 0) res.reversed().toMutableList() else res
                }
            )
        }

        private fun compareVariables(a: Variable, b: Variable): Int {
            return if (a is Type) compareType(a, b) else return comparePrimitive(a as Primitive, b)
        }

        private fun compareType(type: Type, variable: Variable): Int {
            if (type is Object && variable is Object) {
                return compareValues(type.name, variable.name)
            }
            return when (variable) {
                is Object -> -1
                is Type -> if (type.equalToType(variable)) 0 else compareValues(type.name, variable.name)
                else -> 1
            }
        }

        private fun comparePrimitive(primitive: Primitive, variable: Variable): Int {
            if (variable is Type) {
                return -1
            }
            if (primitive is PNumber && variable is PNumber) {
                return primitive.compareTo(variable)
            }
            if (primitive.getIndex() != (variable as Primitive).getIndex()) {
                return compareValues(primitive.getIndex(), variable.getIndex())
            }
            return when (variable) {
                is PList -> compareValues((primitive as PList).getPValue().size, variable.getPValue().size)
                is PDictionary -> compareValues((primitive as PDictionary).getPValue().size, variable.getPValue().size)
                is PString -> compareValues((primitive as PString).getPValue(), variable.getPValue())
                else -> throw PositionalException("Non-comparable primitive", "")
            }
        }
    }
}
