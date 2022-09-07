package node.operator

import Optional
import isInt
import lexer.ExpectedTypeException
import lexer.PositionalException
import node.Assignable
import node.Linkable
import node.Node
import node.statement.Assignment
import properties.Type
import properties.Variable
import properties.primitive.*
import table.SymbolTable
import utils.Utils.toVariable

/**
 * Format: `a[i]` -  `[]` is index, `a` is indexed value
 *  ([PArray] or [PDictionary]) and `i` is [PInt] or key of dictionary.
 *
 * Token that represents taking value from collection by index or key
 */
class Index(
    symbol: String,
    value: String,
    position: Pair<Int, Int>,
    children: List<Node> = listOf()
) : Node(symbol, value, position), Assignable, Linkable {
    constructor(node: Node) : this(
        node.symbol,
        node.value,
        node.position,
        node.children
    )

    init {
        this.children.clear()
        this.children.addAll(children)
    }

    override fun evaluate(symbolTable: SymbolTable): Any {
        val res = evaluateIndex(symbolTable)
        if (res is Primitive)
            return res.getPValue()
        return res
    }

    fun evaluateIndex(symbolTable: SymbolTable): Any {
        val indexed = left.evaluate(symbolTable).toVariable(left)
        val index = right.evaluate(symbolTable)
        return when (indexed) {
            is Indexable -> indexed[index, right, symbolTable.getFileTable()]
            else -> throw ExpectedTypeException(
                listOf(PArray::class, PDictionary::class, PString::class),
                symbolTable.getFileTable().filePath,
                this
            )
        }
    }

    private fun getIndexableAndIndex(symbolTable: SymbolTable): Pair<Indexable, Variable> {
        val indexable = symbolTable.getIdentifier(left)
        val index = right.evaluate(symbolTable).toVariable(right)
        if (indexable is Indexable && indexable.checkIndexType(index))
            return Pair(indexable, index)
        throw ExpectedTypeException(
            listOf(PArray::class, Number::class),
            symbolTable.getFileTable().filePath,
            this,
            expectedMultiple = true
        )
    }

    override fun assign(assignment: Assignment, parent: Type?, symbolTable: SymbolTable, value: Any) {
        if (parent == null || parent.getProperty(getPropertyName()) == PInt(0, parent)) {
            val (arr, ind) = getIndexableAndIndex(symbolTable)
            arr.set(ind, value.toVariable(assignment.right), left, right, symbolTable.getFileTable())
            return
        }
        val property = parent.getProperty(getPropertyName())
        if (property !is Indexable)
            throw ExpectedTypeException(
                listOf(PArray::class, PDictionary::class, PString::class),
                symbolTable.getFileTable().filePath,
                left,
                property
            )
        val index = right.evaluate(symbolTable)
        if (!isInt(index))
            throw PositionalException("Index is not integer", symbolTable.getFileTable().filePath, this)
        property.set(
            index, right.evaluate(symbolTable).toVariable(right), left, right, symbolTable.getFileTable()
        )
    }

    override fun getFirstUnassigned(parent: Type, symbolTable: SymbolTable): Pair<Type, Assignment?> {
        val fromAnother = (left as Assignable).getFirstUnassigned(parent, symbolTable)
        if (fromAnother.second != null) return fromAnother
        val indexUnassigned =
            right.traverseUntilOptional {
                if (it is Assignable
                    && it.getFirstUnassigned(parent, symbolTable).second != null
                ) Optional(it) else Optional()
            }
        if (indexUnassigned.value != null) return (indexUnassigned.value as Assignable).getFirstUnassigned(
            parent,
            symbolTable
        )
        if (parent.getAssignment(this) != null)
            return Pair(parent, parent.getAssignment(this))
        return Pair(parent, null)
    }

    override fun getPropertyName(): Node = (left as Assignable).getPropertyName()
}
