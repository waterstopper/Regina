package token

import Optional
import Tuple4
import lexer.NotFoundException
import lexer.Parser
import lexer.PositionalException
import properties.Function
import properties.Object
import properties.Type
import properties.Variable
import properties.primitive.Primitive
import table.SymbolTable
import token.invocation.Call
import token.invocation.Constructor
import token.invocation.Invocation
import token.operator.Index
import token.statement.Assignment
import utils.Utils.toProperty
import utils.Utils.toVariable

/**
 * Format: `a.b.c.d` - `a`, `b`, `c` and `d` are children of link
 *
 * Represents tokens separated by dots. These tokens are link children. In Regina, links have the following purposes:
 * 1. A property of class, object or a primitive: `Point.x` or `Segment.parent.iter`
 * 2. A function of class, object or a primitive: `Double.round()`
 * 3. Reference to a class, object or a function from another file: `importedFile.className`
 * 4.
 * That's why links are complex, they should be carefully evaluated and assigned.
 *
 * Link invariants:
 * * First token in link might be anything
 * * n-th token is [Linkable]: [Identifier], [Invocation] or [Index]
 */
open class Link(
    symbol: String,
    value: String,
    position: Pair<Int, Int>,
    bindingPower: Int,
    nud: ((token: Token, parser: Parser) -> Token)?,
    led: ((token: Token, parser: Parser, token2: Token) -> Token)?,
    std: ((token: Token, parser: Parser) -> Token)?,
    children: List<Token> = listOf()
) : Token(symbol, value, position, bindingPower, nud, led, std), Assignable {
    constructor(token: Token) : this(
        token.symbol, token.value,
        token.position, token.bindingPower,
        token.nud, token.led,
        token.std, token.children
    )

    init {
        if (children.isNotEmpty()) {
            this.children.clear()
            this.children.addAll(children)
        }
    }

    override fun evaluate(symbolTable: SymbolTable): Any {
        var table = symbolTable.copy()
        var (index, currentVariable) = checkFirstVariable(0, symbolTable.copy(), symbolTable)
        if (currentVariable == null)
            throw NotFoundException(left, file = symbolTable.getFileTable())
        table = table.changeVariable(currentVariable)
        index++
        while (index < children.size) {
            val isResolved =
                checkNextVariable(index = index, table = table, initialTable = symbolTable, currentVariable!!)
            if (isResolved.value !is Variable)
                throw PositionalException("Link not resolved", children[index])
            currentVariable = isResolved.value as Variable
            table = table.changeVariable(currentVariable)
            index++
        }
        return if (currentVariable!! is Primitive)
            (currentVariable as Primitive).getPValue() else currentVariable
    }

    private fun checkNextVariable(
        index: Int,
        table: SymbolTable,
        initialTable: SymbolTable,
        variable: Variable
    ): Optional {
        when (children[index]) {
            is Call -> return Optional(
                resolveFunctionCall(
                    index = index,
                    table = table,
                    initialTable = initialTable,
                    currentVariable = variable,
                    function = variable.getFunction((children[index] as Call))
                ).first
            )
            is Identifier -> {
                if (variable is Type && variable !is Object) {
                    val assignment = variable.getLinkedAssignment(this, index)
                    if (assignment != null)
                        return Optional(assignment)
                }
                val property = variable.getPropertyOrNull(children[index].value) ?: Optional()
                return Optional(property)
            }
            is Index -> {
                var indexToken = children[index].left
                while (indexToken is Index)
                    indexToken = indexToken.left
                variable.getPropertyOrNull(indexToken.value)
                    ?: if (variable is Type) (return Optional(variable.getAssignment(indexToken)))
                    else throw PositionalException("Property not found", indexToken)
                return Optional((children[index] as Index).evaluateIndex(table).toVariable(right.right))
            }
            else -> throw PositionalException("Unexpected token", children[index])
        }
    }

    /**
     * Get first variable and index of it
     */
    private fun checkFirstVariable(
        index: Int,
        table: SymbolTable,
        initialTable: SymbolTable,
        canBeFile: Boolean = true
    ): Pair<Int, Variable?> {
        when (children[index]) {
            is Identifier -> {
                val identifier = table.getIdentifierOrNull(children[index])
                return if (identifier == null) {
                    if (canBeFile) {
                        val nextTable = addFile(table) ?: return Pair(0, null)
                        checkFirstVariable(
                            index + 1,
                            table = nextTable,
                            initialTable = initialTable,
                            canBeFile = false
                        )
                    } else Pair(0, null)
                } else Pair(0, identifier)
            }
            is Call -> return Pair(
                index,
                resolveFunctionCall(
                    index, table, initialTable,
                    null, table.getFunction(children[index])
                ).first
            )
            is Constructor -> return Pair(index, children[index].evaluate(initialTable).toVariable(children[index]))
            // unary minus, (1+2).max(...)
            else -> {
                if (!canBeFile)
                    throw PositionalException("Unexpected token", children[index])
                return Pair(index, children[index].evaluate(table).toVariable(children[index]))
            }
        }
    }

    /**
     * Update fileTable
     */
    private fun addFile(table: SymbolTable): SymbolTable? {
        val fileTable = table.getImportOrNull(left.value) ?: return null
        //  ?: throw PositionalException("Expected variable, object or package name", left)
        return table.changeFile(fileTable)
    }

    /**
     * Return function result and parent of function
     * here symbol table is ignored. Only value with same fileName
     */
    private fun resolveFunctionCall(
        index: Int,
        table: SymbolTable,
        initialTable: SymbolTable,
        currentVariable: Variable?,
        function: Function
    ): Pair<Variable, Variable?> {
        var type = table.getCurrentType()
        if (type == null || type !is Type)
            type = null
        val tableForEvaluation = SymbolTable(
            fileTable = if (type is Type) table.getFileFromType(type, children[index])
            else table.getFileTable(),
            variableTable = table.getCurrentType()
        ) // table.changeScope(initialTable.getScope())
        (children[index] as Call).argumentsToParameters(function, initialTable, tableForEvaluation)
        val functionResult = (children[index] as Call).evaluateFunction(tableForEvaluation, function)
        return Pair(functionResult.toVariable(children[index]), currentVariable)
    }

    override fun assign(assignment: Assignment, parent: Type?, symbolTable: SymbolTable, value: Any) {
        // hacky way, not good.
        val (_, currentParent, _, index) = safeEvaluate(
            parent ?: Type(
                "@Fictive",
                null, mutableSetOf(), symbolTable.getImport(Token(value = "@global"))
            ),
            symbolTable
        )
        if (currentParent !is Type || index != children.lastIndex)
            throw PositionalException("Link not resolved")
        currentParent.setProperty(children[children.lastIndex].value, value.toProperty(assignment.right))
    }

    /**
     * @return currentVariable, its parent, assignment in parent if currentVariable is null, index of currentVariable
     */
    private fun safeEvaluate(parent: Type, symbolTable: SymbolTable): Tuple4<Variable?, Variable?, Assignment?, Int> {
        var currentParent: Variable? = null
        var table = symbolTable.copy()
        val initialTable = symbolTable.changeVariable(parent)
        var (index, currentVariable) = checkFirstVariable(0, table, initialTable)
        // first variable in link is not assigned => there is no such property in class if assignment not found
        if (currentVariable == null)
            return Tuple4(
                null, parent,
                parent.getAssignment(left)
                    ?: throw PositionalException("Assignment not found", left),
                index
            )
        table = table.changeVariable(currentVariable)
        index++
        while (index < children.size) {
            val res = checkNextVariable(index, table = table, initialTable = initialTable, currentVariable!!)
            // if property not yet assigned and assignment is found, return parent
            if (res.isGood && res.value is Assignment)
                return Tuple4(null, currentVariable, res.value, index)
            // property not assigned and assignment not found
            if (res.value !is Variable)
                return Tuple4(null, currentVariable, null, index)
            currentParent = currentVariable
            table = table.changeVariable(res.value)
            currentVariable = res.value
            index++
        }
        // Here index == children.size. Decrease it because index is number of the currentVariable token
        // only there currentParent might be null if it's import link
        return Tuple4(currentVariable, currentParent, null, --index)
    }

    override fun getFirstUnassigned(parent: Type, symbolTable: SymbolTable): Pair<Type, Assignment?> {
        val (type, assignment) = getFirstUnassignedOrNull(parent, symbolTable)
        // Happens if both type nad assignment are null.
        // It means that it's import link with two children and Type of Function is imported => all is assigned
        if (type == null)
            return Pair(parent, assignment)
        return Pair(type, assignment)
    }

    /**
     * @return assignment of unresolved or its parent. Both can be null simultaneously if variable is assigned
     */
    fun getFirstUnassignedOrNull(
        parent: Type,
        symbolTable: SymbolTable,
        forLValue: Boolean = false
    ): Pair<Type?, Assignment?> {
        val (currentVariable, currentParent, assignment, index) = safeEvaluate(parent, symbolTable)
        if (currentParent != null && currentParent !is Type)
            throw PositionalException("Expected type", children[index - 1])
        // left hand-side can be assigned if last link child is not assigned
        if (forLValue && index == children.lastIndex)
            return Pair(parent, null)
        // nor assignment, nor property is found
        if (currentVariable == null && assignment == null && index < children.size) {
            return Pair(
                parent, parent.getLinkedAssignment(this, 0)
                    ?: throw PositionalException("Assignment not found")
            )
        }
        return Pair(currentParent as Type?, assignment)
    }

    override fun getPropertyName(): Token = (children.last() as Assignable).getPropertyName()
}
