package properties.primitive

import References
import isInt
import lexer.PositionalException
import node.Node
import properties.EmbeddedFunction
import properties.Type
import properties.Variable
import table.FileTable
import utils.Utils.castToPString
import utils.Utils.getPInt
import utils.Utils.getPString
import utils.Utils.toProperty

class PString(value: String, parent: Type? = null) : Primitive(value, parent), Indexable {
    override fun getIndex() = 4
    override fun getPValue() = value as String
    override fun get(index: Any, node: Node, fileTable: FileTable): Any {
        if (!isInt(index))
            throw PositionalException("Expected integer",fileTable.filePath, node)
        if ((index as Int) < 0 || index >= getPValue().length)
            throw PositionalException("Index out of bounds", fileTable.filePath,node)
        return getPValue()[index]
    }
    override fun toDebugClass(references: References): Any {
        return Pair("String", getPValue())
    }

    override fun set(index: Any, value: Any, nodeIndex: Node, nodeValue: Node, fileTable: FileTable) {
        throw PositionalException("Set is not implemented for String",fileTable.filePath, nodeValue)
    }

    override fun toString(): String {
        return value.toString()
    }

    override fun checkIndexType(index: Variable): Boolean {
        return index is PInt
    }

    companion object {
        fun initializeStringProperties() {
            val s = PString("", null)
            setProperty(s, "size") { p: Primitive -> PInt((p as PString).getPValue().length).toProperty() }
        }

        /**
         * * substring(start, end=this.size)
         * * replace(oldString, newString)
         * * reversed
         * * lowercase
         * * uppercase
         */
        fun initializeEmbeddedStringFunctions() {
            val s = PString("", null)
            setFunction(
                s,
                EmbeddedFunction("substring", listOf("start"), listOf("end = this.size")) { token, args ->
                    val string = castToPString(args.getPropertyOrNull("this")!!)
                    val start = getPInt(args, token, "start")
                    val end = getPInt(args, token, "end")
                    string.getPValue().substring(start.getPValue(), end.getPValue())
                }
            )
            setFunction(
                s,
                EmbeddedFunction("replace", listOf("oldString", "newString")) { token, args ->
                    val string = castToPString(args.getPropertyOrNull("this")!!)
                    val oldString = getPString(args, token, "oldString")
                    val newString = getPString(args, token, "newString")
                    string.getPValue().replace(oldString.getPValue(), newString.getPValue())
                }
            )
            setFunction(
                s,
                EmbeddedFunction("reversed") { _, args ->
                    val string = castToPString(args.getPropertyOrNull("this")!!)
                    string.getPValue().reversed()
                }
            )
            setFunction(
                s,
                EmbeddedFunction("lowercase") { _, args ->
                    val string = castToPString(args.getPropertyOrNull("this")!!)
                    string.getPValue().lowercase()
                }
            )
            setFunction(
                s,
                EmbeddedFunction("uppercase") { _, args ->
                    val string = castToPString(args.getPropertyOrNull("this")!!)
                    string.getPValue().uppercase()
                }
            )
        }
    }
}
