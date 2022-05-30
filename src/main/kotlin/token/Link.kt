package token

import Optional
import lexer.Parser
import lexer.PositionalException
import properties.Function
import properties.Type
import properties.Variable
import properties.primitive.Primitive
import table.SymbolTable
import token.invocation.Call
import token.invocation.Constructor
import token.invocation.Invocation
import token.operator.Index
import token.operator.TokenTernary
import token.statement.Assignment
import utils.Utils.toProperty
import utils.Utils.toVariable

open class Link(
    symbol: String, value: String, position: Pair<Int, Int>, bindingPower: Int,
    nud: ((token: Token, parser: Parser) -> Token)?,
    led: ((token: Token, parser: Parser, token2: Token) -> Token)?,
    std: ((token: Token, parser: Parser) -> Token)?, children: List<Token> = listOf()
) : Token(symbol, value, position, bindingPower, nud, led, std), Assignable {
    constructor(token: Token) : this(
        token.symbol, token.value,
        token.position, token.bindingPower,
        token.nud, token.led,
        token.std, token.children
    )

    var index = 0
    lateinit var table: SymbolTable
    var currentVariable: Variable? = null
    var currentParent: Variable? = null
    private lateinit var initialTable: SymbolTable

    override fun evaluate(symbolTable: SymbolTable): Any {
        reset(symbolTable)
        if (!checkFirstVariable())
            throw PositionalException("Not found", left)
        table = table.changeVariable(currentVariable!!)
        index++
        while (index < children.size) {
            val isResolved = checkNextVariable(
                currentVariable
                    ?: throw PositionalException("Cannot be casted to variable", children[index - 1])
            )
            if (!isResolved.isGood)
                throw PositionalException("Link not resolved", children[index])
            table = table.changeVariable(currentVariable!!)
            index++
        }
        return if (currentVariable is Primitive)
            (currentVariable as Primitive).getPValue() else currentVariable
            ?: throw PositionalException("Unexpected return", children[--index])
    }

    private fun checkNextVariable(variable: Variable): Optional {
        when (children[index]) {
            is Invocation -> {
                val function = variable.getFunction((children[index] as Invocation).name)
                children[index] = Call(children[index])
                resolveFunctionCall(function)
            }
            is Identifier -> {
                val property = variable.getPropertyOrNull(children[index].value)
                    ?: if (variable is Type) (return Optional(variable.getAssignment(children[index])))
                    else throw PositionalException("Property not found", children[index])
                assignCurrentVariable(property)
            }
            is Index -> {
                var index = children[index].left
                while (index is Index)
                    index = index.left
                variable.getPropertyOrNull(index.value)
                    ?: if (variable is Type) (return Optional(variable.getAssignment(index)))
                    else throw PositionalException("Property not found", index)
                assignCurrentVariable((children[this.index] as Index).evaluateIndex(table).toVariable(right.right))
            }
        }
        return Optional(isGood = true)
    }


    private fun checkFirstVariable(canBeFile: Boolean = true): Boolean {
        when (children[index]) {
            is Identifier -> {
                val identifier = table.getIdentifierOrNull(children[index])
                if (identifier == null) {
                    if (canBeFile) {
                        addFile()
                        index++
                        checkFirstVariable(false)
                    } else return false
                } else assignCurrentVariable(identifier)
            }
            is TokenTernary -> {
                val ternaryResult = children[index].evaluate(table).toVariable(children[index])
                assignCurrentVariable(ternaryResult)
            }
            is Invocation -> resolveInvocation()
            //  is Index -> throw PositionalException("Not implemented")
            // unary minus, (1+2).max(...)
            else -> {
                if (!canBeFile)
                    throw PositionalException("Unexpected token", children[index])
                assignCurrentVariable(children[index].evaluate(table).toVariable(children[index]))
            }
        }
        return true
    }


    /**
     * Resolve till operations.last() has properties (primitive, type, object or function call)
     */
    private fun getFirstVariable(canBeFile: Boolean = true) {
        when (children[index]) {
            is Identifier -> {
                val identifier = table.getIdentifierOrNull(children[index])
                if (identifier == null) {
                    if (canBeFile) {
                        addFile()
                        index++
                        getFirstVariable(false)
                    } else throw PositionalException(
                        "Identifier not found in `${children[index - 1]}`",
                        children[index]
                    )
                } else assignCurrentVariable(identifier)
            }
            is TokenTernary -> {
                val ternaryResult = children[index].evaluate(table).toVariable(children[index])
                assignCurrentVariable(ternaryResult)
            }
            is Invocation -> resolveInvocation()
            // unary minus, (1+2).max(...), [1,2,3].size
            else -> {
                if (!canBeFile)
                    throw PositionalException("Unexpected token", children[index])
                assignCurrentVariable(children[index].evaluate(table).toVariable(children[index]))
            }
        }
    }

    private fun resolveInvocation() {
        val function = table.getFunctionOrNull((children[index] as Invocation).name)
        if (function != null) {
            children[index] = Call(children[index])
            resolveFunctionCall(function)
            return
        }
        val type = table.getTypeOrNull((children[index] as Invocation).name)
        if (type != null) {
            children[index] = Constructor(children[index])
            val instance = children[index].evaluate(initialTable)
            // TODO add constructor args similar to call args here
            assignCurrentVariable(instance.toVariable(children[index]))
            return
        }
        throw PositionalException("Function and type not found", children[index])
    }

    private fun addFile() {
        val fileTable = table.getImportOrNull(left.value)
            ?: throw PositionalException("Expected variable, object or package name", left)
        table = table.changeFile(fileTable.fileName)
    }

    // here symbol table is ignored. Only value with same fileName
    private fun resolveFunctionCall(function: Function) {
        // (children[index] as Call).function = function
        var type = table.getCurrentType()
        if (type == null || type !is Type) {
            type = null
        }
        val tableForEvaluation = SymbolTable(
            fileTable = table.getImportOrNull((type as Type?)?.fileName ?: "")
                ?: table.getFileTable(),
            variableTable = table.getCurrentType()
        ) // table.changeScope(initialTable.getScope())
        (children[index] as Call).argumentsToParameters(function, initialTable, tableForEvaluation)
        val functionResult = (children[index] as Call).evaluateFunction(tableForEvaluation, function)
        assignCurrentVariable(functionResult.toVariable(children[index]))
    }

    init {
        if (children.isNotEmpty()) {
            this.children.clear()
            this.children.addAll(children)
        }
    }

    override fun assign(assignment: Assignment, parent: Type?, symbolTable: SymbolTable, value: Any) {
        // hacky way, not good.
        // TODO in type functions are added to type properties
        getFirstUnassigned(parent ?: Type("@Fictive", null, mutableListOf(), ""), symbolTable)
        // if the last child in link is assigned
        if (index == children.lastIndex)
            currentVariable = currentParent
        if (currentVariable is Type)
            (currentVariable as Type).setProperty(
                children[children.lastIndex].value,
                value.toProperty(assignment.right)
            )
    }

    override fun getFirstUnassigned(parent: Type, symbolTable: SymbolTable): Assignment? {
        reset(symbolTable, parent)
        val unassignedInFirst = left.traverseUnresolved(symbolTable, parent)
        if (unassignedInFirst != null)
            return unassignedInFirst
        val firstResolved = checkFirstVariable()
        if (!firstResolved)
            return parent.getAssignment(left) ?: throw PositionalException("Assignment not found", left)
        index++
        while (index < children.size) {
            val res = checkNextVariable(
                currentVariable ?: throw PositionalException(
                    "Cannot be casted to variable",
                    children[index - 1]
                )
            )
            if (res.isGood)
                return res.value as Assignment?
            table = table.changeVariable(currentVariable!!)
            index++
        }
        return null
    }

    private fun assignCurrentVariable(value: Variable?) {
        currentParent = currentVariable
        currentVariable = value
    }

    private fun reset(symbolTable: SymbolTable, parent: Type? = null) {
        // On second evaluation it should be reset (if function with this token is called twice)
        index = 0
        initialTable = if (parent != null) symbolTable.changeVariable(parent) else symbolTable
        table = symbolTable.copy()
    }

    override fun getPropertyName(): Token = (children.last() as Assignable).getPropertyName()
}