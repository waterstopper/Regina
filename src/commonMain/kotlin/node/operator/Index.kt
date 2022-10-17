package node.operator

import Optional
import lexer.ExpectedTypeException
import node.Assignable
import node.Linkable
import node.Node
import node.invocation.Invocation
import node.statement.Assignment
import properties.Type
import properties.Variable
import properties.primitive.*
import table.SymbolTable
import utils.Utils.toVariable

/**
 * Format: `a[i]` -  `[]` is index, `a` is indexed value
 *  ([PList] or [PDictionary]) and `i` is [PInt] or key of dictionary.
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
        if (res is Primitive && res !is PNumber)
            return res.getPValue()
        return res
    }

    fun evaluateIndexWithDeepestLeftProperty(prop: Variable, symbolTable: SymbolTable): Any {
        val indexed = if (left is Index) (left as Index).evaluateIndexWithDeepestLeftProperty(prop, symbolTable)
            .toVariable() else prop
        val index = right.evaluate(symbolTable)
        return when (indexed) {
            is Indexable -> indexed[index, right, symbolTable.getFileTable()]
            else -> throw ExpectedTypeException(
                listOf(PList::class, PDictionary::class, PString::class),
                symbolTable.getFileTable().filePath,
                this
            )
        }
    }

    fun evaluateIndex(symbolTable: SymbolTable): Any {
        val indexed = left.evaluate(symbolTable).toVariable(left)
        val index = right.evaluate(symbolTable)
        return when (indexed) {
            is Indexable -> indexed[index, right, symbolTable.getFileTable()]
            else -> throw ExpectedTypeException(
                listOf(PList::class, PDictionary::class, PString::class),
                symbolTable.getFileTable().filePath,
                this
            )
        }
    }

    fun getDeepestLeft(): Node {
        var res = left
        while (res is Index)
            res = res.left
        return res
    }

    override fun assign(assignment: Assignment, parent: Type?, symbolTable: SymbolTable, value: Any) {
        val assignTable = if (parent != null) symbolTable.changeVariable(parent) else symbolTable
        val indexable = getDeepestLeft().evaluate(assignTable).toVariable(left)
        assignWithIndexable(indexable, assignTable, symbolTable, assignment, value)
    }

    fun assignWithIndexable(
        variable: Variable,
        assignTable: SymbolTable,
        symbolTable: SymbolTable,
        assignment: Assignment,
        value: Any
    ) {
        val index = right.evaluate(assignTable).toVariable(right)
        val indexable =
            if (left is Index) (left as Index).evaluateIndexWithDeepestLeftProperty(variable, symbolTable) else variable
        if (indexable is Indexable && indexable.checkIndexType(index)) {
            indexable.set(index, value, right, assignment.right, symbolTable.getFileTable())
        } else throw ExpectedTypeException(
            listOf(PList::class, PNumber::class),
            symbolTable.getFileTable().filePath,
            this,
            expectedMultiple = true
        )
    }

    override fun getFirstUnassigned(parent: Type, symbolTable: SymbolTable): Pair<Type, Assignment?> {
        val fromAnother = (left as Assignable).getFirstUnassigned(parent, symbolTable)
        if (fromAnother.second != null) return fromAnother
        val indexUnassigned =
            right.traverseUntilOptional {
                if (it is Assignable &&
                    it.getFirstUnassigned(parent, symbolTable).second != null
                ) Optional(it) else Optional()
            }
        if (indexUnassigned.value != null) return (indexUnassigned.value as Assignable).getFirstUnassigned(
            parent,
            symbolTable
        )
        if (parent.getAssignment(this) != null) {
            return Pair(parent, parent.getAssignment(this))
        }
        return Pair(parent, null)
    }

    override fun getPropertyName(): Node = (left as Assignable).getPropertyName()

    override fun findUnassigned(symbolTable: SymbolTable, parent: Type): Pair<Type, Assignment>? {
        // find in index value
        val found = right.findUnassigned(symbolTable, parent)
        if (found != null) {
            return found
        }
        // find in indexed value
        return left.findUnassigned(symbolTable, parent)
    }
}
