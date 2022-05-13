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
import token.variable.TokenArray
import token.variable.TokenNumber
import token.variable.TokenString
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

    var index = 0
    lateinit var table: SymbolTable
    var currentVariable: Variable? = null
    private lateinit var initialTable: SymbolTable

    // for constructors and calls
    private val arguments = mutableListOf<List<Any>>()

    override fun evaluate(symbolTable: SymbolTable): Any {
        // On second evaluation it should be reset (if function with this token is called twice)
        index = 0
        arguments.clear()
        initialTable = symbolTable
        table = symbolTable.copy()
        getFirstVariable()
        arguments.add(emptyList())

        table = table.changeVariable(currentVariable!!)
        index++
        while (index < children.size) {
            getNextVariable(
                currentVariable ?: throw PositionalException(
                    "Cannot be casted to variable",
                    children[index - 1]
                )
            )
            table = table.changeVariable(currentVariable!!)
            arguments.add(emptyList())
            index++
        }
        return if (currentVariable is Primitive)
            (currentVariable as Primitive).getPValue() else currentVariable
            ?: throw PositionalException("Unexpected return", children[--index])
    }

    /**
     * Not applicable for 1st element in link
     */
    protected open fun getNextVariable(variable: Variable): Boolean {
        when (children[index]) {
            is Invocation -> {
                val function = variable.getFunction((children[index] as Invocation).name)
                children[index] = Call(children[index])
                resolveFunctionCall(function)
                return true
                //  val function = variable.getFunction(children[index].left)
                //    addFunction(function)
            }
            is Identifier -> {
                return resolveProperty(variable)
            }
            is Index -> {
                throw PositionalException("not implemented")
            }
        }
        return true
    }

    /**
     * Resolve till operations.last() has properties (primitive, type, object or function call)
     */
    private fun getFirstVariable(canBeFile: Boolean = true) {
        when (children[index]) {
            is TokenArray, is TokenNumber, is TokenString -> {
                if (!canBeFile)
                    throw PositionalException("Unexpected token", children[index])
                currentVariable = children[index].evaluate(table).toVariable(children[index])
            }
            is Identifier -> {
                val variable = table.getVariableOrNull(children[index].value)
                if (variable == null) {
                    val obj = table.getObjectOrNull(children[index])
                    if (obj == null) {
                        if (canBeFile) {
                            addFile()
                            index++
                            getFirstVariable(false)
                        } else throw PositionalException("Object not found in $children[index]", children[index])
                    } else currentVariable = obj
                } else {
                    if (!canBeFile)
                        throw PositionalException("Object not found in $children[index]", children[index])
                    currentVariable = variable
                }
            }
            is TokenTernary -> {
                val ternaryResult = children[index].evaluate(table).toVariable(children[index])
                currentVariable = ternaryResult
            }
            is Invocation -> resolveInvocation()
            // unary minus, (1+2).max(...)
            else -> {
                if (!canBeFile)
                    throw PositionalException("Unexpected token", children[index])
                currentVariable = children[index].evaluate(table).toVariable(children[index])
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
            currentVariable = type
            return
        }
        throw PositionalException("Function and type not found", children[index].children[index])

    }

    private fun addFile() {
        val fileTable = table.getImportOrNull(left.value)
            ?: throw PositionalException("Expected variable, object or package name", left)
        table = table.changeFile(fileTable.fileName)
    }


    /**
     * Used inside functions. All properties should be already resolved
     */
    open fun resolveProperty(parent: Variable): Boolean {
        currentVariable = parent.getProperty(children[index])
        return true
    }

    // here symbol table is ignored. Only value with same fileName
    private fun resolveFunctionCall(function: Function) {
        (children[index] as Call).function = function
        val tableForEvaluation = table.changeScope(initialTable.getScope())
        (children[index] as Call).argumentsToParameters(function, table, tableForEvaluation)
        val functionResult = (children[index] as Call).evaluateFunction(tableForEvaluation, function)
        currentVariable = functionResult.toVariable(children[index])
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

    override fun assign(assignment: Assignment, parent: Type?, symbolTable: SymbolTable, value: Any?) {
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