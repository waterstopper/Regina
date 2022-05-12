package token.link

import evaluation.Evaluation.globalTable
import lexer.Parser
import lexer.PositionalException
import properties.Function
import properties.Type
import properties.Variable
import properties.primitive.Primitive
import table.SymbolTable
import token.Assignable
import token.Identifier
import token.Token
import token.invocation.Call
import token.invocation.Constructor
import token.invocation.Invocation
import token.operator.Index
import token.operator.TokenTernary
import token.statement.Assignment
import utils.Utils.toVariable

/** parent, this - special phrases, that should be added to scope table and type assignments specifically **/
open class Link(
    symbol: String,
    value: String,
    position: Pair<Int, Int>,
    bindingPower: Int,
    nud: ((token: Token, parser: Parser) -> Token)?,
    led: ((
        token: Token, parser: Parser, token2: Token
    ) -> Token)?,
    std: ((token: Token, parser: Parser) -> Token)?, children: List<Token> = listOf()
) : Token(symbol, value, position, bindingPower, nud, led, std), Assignable {
    constructor(token: Token) : this(
        token.symbol,
        token.value,
        token.position,
        token.bindingPower,
        token.nud,
        token.led,
        token.std,
        token.children
    )

    var index = -1
    var currentVariable: Variable? = null
    val operations: MutableList<(symbolTable: SymbolTable, args: List<Any>) -> Pair<SymbolTable, Any?>> =
        mutableListOf()
    val arguments = mutableListOf<List<Any>>()

    override fun evaluate(symbolTable: SymbolTable): Any {
        var table = symbolTable.copy()
        var res: Any?
        if (index == -1) {
            index++
            table = getFirstVariable(table)
            arguments.add(emptyList())
        }
        index++
        while (index < children.size) {
            //  val func = operations[index]
            //  val pair = func(table, mutableListOf(arguments[index]))
            //  table = pair.first
            //  res = pair.second

            if (index >= operations.size) {
                addNextLambda(
                    table,
                    currentVariable ?: throw PositionalException(
                        "Cannot be casted to variable",
                        children[index - 1]
                    )
                )
                arguments.add(emptyList())
            }
            index++
        }
        return if (currentVariable is Primitive)
            (currentVariable as Primitive).getPValue() else currentVariable
            ?: throw PositionalException("Unexpected return", children[--index])
    }

    /**
     * Not applicable for 1st element in link
     */
    fun addNextLambda(symbolTable: SymbolTable, variable: Variable): Boolean {
        when (children[index]) {
            is Invocation -> {
                val function = variable.getFunction(children[index].left)
                addFunction(symbolTable, function)
            }
            is Identifier -> {
                return resolveProperty(variable)
            }
            is Index -> {}
        }
        return true
    }

    /**
     * Resolve till operations.last() has properties (primitive, type, object or function call)
     */
    fun getFirstVariable(symbolTable: SymbolTable, canBeFile: Boolean = true): SymbolTable {
        var table = symbolTable.copy()
        when (children[index]) {
            is Identifier -> {
                val variable = table.getVariableOrNull(children[index].value)
                if (variable == null) {
                    val obj = symbolTable.getObjectOrNull(children[index])
                    if (obj == null) {
                        if (canBeFile) {
                            table = addFile(table)
                            index++
                            table = getFirstVariable(table, false)
                        } else throw PositionalException("Object not found in $children[index]", children[index])
                    } else addVariable(obj)
                } else {
                    if (!canBeFile)
                        throw PositionalException("Object not found in $children[index]", children[index])
                    currentVariable = variable
                    addVariable(variable)
                }
            }
            is TokenTernary -> {
                val ternaryResult = children[index].evaluate(table).toVariable(children[index])
                currentVariable = ternaryResult

                addVariable(ternaryResult)
            }
            is Invocation -> {
                val function = table.getFunctionOrNull((children[index] as Invocation).name)
                if (function != null) {
                    children[index] = Call(children[index])
                    addFunction(table, function)
                    return table
                }
                val type = table.getTypeOrNull((children[index] as Invocation).name)
                if (type != null) {
                    children[index] = Constructor(children[index])
                    addVariable(type)
                    return table
                }
                throw PositionalException("Function and type not found", children[index].children[index])
            }
        }
        return table
    }

    private fun addFile(symbolTable: SymbolTable): SymbolTable {
        val fileTable = symbolTable.getImportOrNull(left.value)
        if (fileTable == null) {
            throw PositionalException("Expected variable, object or package name", left)
        } else {
            return symbolTable.changeFile(fileTable.fileName)

            val lambda: (SymbolTable, List<Any>) -> Pair<SymbolTable, Any?> =
                { table: SymbolTable, _: List<Any> ->
                    Pair(table.copy().changeFile(fileTable.fileName), null)
                }
            operations.add(lambda)
        }
    }

    protected fun addVariable(variable: Variable) {

        val lambda: (SymbolTable, List<Any>) -> Pair<SymbolTable, Any> =
            { symbolTable: SymbolTable, _: List<Any> ->
                Pair(symbolTable.copy().changeVariable(variable), variable)
            }
        operations.add(lambda)
    }

    private fun addFunction(symbolTable: SymbolTable, function: Function) {
        val (a, b) = resolveFunctionCall(symbolTable, function)

        val lambda: (SymbolTable, List<Any>) -> Pair<SymbolTable, Any> =
            { symbolTable: SymbolTable, _: List<Any> ->
                resolveFunctionCall(symbolTable, function)
            }
        operations.add(lambda)
    }

    /**
     * Used inside functions. All properties should be already resolved
     */
    open fun resolveProperty(parent: Variable): Boolean {
        val property = parent.getProperty(children[index])
        addVariable(property)
        return true
    }

    fun resolveFunctionCall(symbolTable: SymbolTable, function: Function): Pair<SymbolTable, Any> {
        (children[index] as Call).function = function
        val functionResult = children[index].evaluate(symbolTable)

        currentVariable = functionResult.toVariable(children[index])
        return Pair(symbolTable.copy().changeVariable(functionResult.toVariable(children[index])), functionResult)
    }

    init {
        if (children.isNotEmpty()) {
            this.children.clear()
            this.children.addAll(children)
        }
    }

    /** last variable before its property. For example, in a.b.c `b` is [parent] **/
    lateinit var parent: Variable

    /** shallow or deep link **/
    open fun getAfterDot() = if (right is Link) right.left else right
    open fun getLast(): Any = if (right is Link) (right as Link).getLast() else right
    open fun isResolved(symbolTable: SymbolTable): Boolean {
        return if (right is Link) (right as Link).isResolved(symbolTable)
        else true
    }

    override fun assign(assignment: Assignment, parent: Type, symbolTable: SymbolTable) {
        TODO("Not yet implemented")
    }

    override fun getFirstUnassigned(parent: Type): Assignment? {
        if (left is Call)
            return null
        if (left is Constructor) {
            val type = left.evaluate(globalTable)
        }
        if (!parent.hasProperty(left))
            return parent.getAssignment(left)
        val property = parent.getProperty(left)
        if (property !is Type)
            return null
        if (right is Link) {
            return (right as Link).getFirstUnassigned(property)
        } else if (right is Index) {
            return null
        }
        return null
    }

    override fun getPropertyName(): Token {
        TODO("Not yet implemented")
    }
}