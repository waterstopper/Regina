package utils

import lexer.ExpectedTypeException
import lexer.Parser
import lexer.PositionalException
import node.Identifier
import node.Node
import node.invocation.Invocation
import node.operator.Index
import node.statement.Assignment
import properties.*
import properties.primitive.*
import table.FileTable
import table.SymbolTable
import kotlin.reflect.KClass

object Utils {
    val NULL = Null()
    val FALSE = PInt(0)
    val TRUE = PInt(1)

    init {
        PList.initializeEmbeddedListFunctions()
        PString.initializeEmbeddedStringFunctions()
        PNumber.initializeEmbeddedNumberFunctions()
        PDouble.initializeEmbeddedDoubleFunctions()
        PDictionary.initializeDictionaryFunctions()

        PInt.initializeIntProperties()
        PDouble.initializeDoubleProperties()
        PList.initializeListProperties()
        PString.initializeStringProperties()
        PDictionary.initializeDictionaryProperties()
    }

    fun Boolean.toPInt(): PInt = if (this) TRUE else FALSE

    // fun Boolean.toInt(): Int = if (this) 1 else 0
    fun Boolean.toNonZeroInt(): Int = if (this) 1 else -1

    fun Any.toBoolean(node: Node, fileTable: FileTable): Boolean {
        try {
            return this.toString().toDouble() != 0.0
        } catch (e: NumberFormatException) {
            throw PositionalException("expected numeric value", fileTable.filePath, node)
        }
    }

    fun Any.toVariable(node: Node = Node()): Variable =
        if (this is Variable) this else Primitive.createPrimitive(this, null, node)

    fun Any.toProperty(node: Node = Node(), parent: Type? = null): Property =
        if (this is Property) this else Primitive.createPrimitive(this, parent, node)

    fun parseAssignment(assignment: String) =
        Parser(assignment, "@NoFile").statements().first().toNode("@NoFile") as Assignment

    /**
     * Prints AST with indentation to  show children.
     * **For debug**.
     */
    fun List<Node>.treeView(): String {
        val res = StringBuilder()
        for (t in this) {
            res.append(t.toTreeString(0))
            res.append('\n')
        }
        return res.toString()
    }

    private fun createIdent(node: Node, name: String) = Node(symbol = name, value = name, position = node.position)

    fun getIdent(node: Node, name: String, args: SymbolTable) = args.getIdentifier(createIdent(node, name))
    fun getPDictionary(args: SymbolTable, node: Node, name: String): PDictionary {
        val dictionary = getIdent(node, name, args)
        if (dictionary !is PDictionary)
            throw ExpectedTypeException(listOf(PDictionary::class), args.getFileTable().filePath, node, dictionary)
        return dictionary
    }

    fun getPList(args: SymbolTable, node: Node, name: String): PList {
        val list = getIdent(node, name, args)
        if (list !is PList)
            throw ExpectedTypeException(listOf(PList::class), args.getFileTable().filePath, node, list)
        return list
    }

    fun getPString(args: SymbolTable, node: Node, name: String): PString {
        val str = getIdent(node, name, args)
        if (str !is PString)
            throw ExpectedTypeException(listOf(PString::class), args.getFileTable().filePath, node, str)
        return str
    }

    fun getPNumber(args: SymbolTable, node: Node, name: String? = null): PNumber {
        val num = if (name == null) node.evaluate(args) else getIdent(node, name, args)
        if (num !is PNumber)
            throw ExpectedTypeException(listOf(PNumber::class), args.getFileTable().filePath, node, num)
        return num
    }

    fun getPInt(args: SymbolTable, node: Node, name: String): PInt {
        val int = getIdent(node, name, args)
        if (int !is PInt)
            throw ExpectedTypeException(listOf(PInt::class), args.getFileTable().filePath, node, int)
        return int
    }

    fun getPDouble(args: SymbolTable, node: Node, name: String): PDouble {
        val double = getIdent(node, name, args)
        if (double !is PDouble)
            throw ExpectedTypeException(listOf(PDouble::class), args.getFileTable().filePath, node, double)
        return double
    }

    fun <T> List<T>.subList(start: Int): List<T> = this.subList(start, this.size)

    fun castToPList(list: Any): PList {
        return list as PList
    }

    fun castToPString(str: Any): PString {
        return str as PString
    }

    fun castToPNumber(num: Any): PNumber {
        return num as PNumber
    }

    fun mapToString(mapped: KClass<*>): String {
        return when (mapped) {
            RFunction::class -> "Function"
            PInt::class -> "Int"
            PDouble::class -> "Double"
            PNumber::class -> "Number"
            PString::class -> "String"
            PList::class -> "List"
            PDictionary::class -> "Dictionary"
            Identifier::class -> "Identifier"
            Invocation::class -> "Invocation"
            Index::class -> "Index"
            else -> mapped.toString().split(".").last()
        }
    }
}
