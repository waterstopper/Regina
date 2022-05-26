package token

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
        if(left.value=="this")
            println()
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
            is Identifier -> currentVariable = variable.getProperty(children[index])
            is Index -> currentVariable = (children[index] as Index).evaluateIndex(table).toVariable(right.right)
        }
        return true
    }

    protected open fun checkFirstVariable(canBeFile: Boolean = true): Assignment? {
        when (children[index]) {
            is TokenArray, is TokenNumber, is TokenString -> {
                if (!canBeFile)
                    throw PositionalException("Unexpected token", children[index])
                currentVariable = children[index].evaluate(table).toVariable(children[index])
            }
            is Identifier -> {
                val variable = table.getVariableOrNull(children[index].value)
                if (variable == null) {
                    val property = table.getPropertyOrNull(children[index].value)
                    currentVariable = if (property == null) {
                        if ((table.getCurrentType() is Type)
                            && (table.getCurrentType() as Type).getAssignment(children[index]) != null
                        )
                            return (table.getCurrentType() as Type).getAssignment(children[index])!!
                        table.getObjectOrNull(children[index])
                            ?: if (canBeFile) {
                                addFile()
                                index++
                                return checkFirstVariable(false)
                            } else throw PositionalException("Object not found in $children[index]", children[index])
                    } else property
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
        return null
    }

    protected open fun checkNextVariable(variable: Variable): Assignment? {
        when (children[index]) {
            is Invocation -> {
                val function = variable.getFunction((children[index] as Invocation).name)
                children[index] = Call(children[index])
                resolveFunctionCall(function)
            }
            is Identifier -> {
                val property = variable.getPropertyOrNull(children[index].value)
                    ?: return (variable as Type).getAssignment(children[index])
                        ?: throw PositionalException("Property not found", children[index])
                currentVariable = property
            }
            is Index -> {
                throw PositionalException("not implemented")
            }
            else -> throw PositionalException("Unexpected token", children[index])
        }
        return null
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
                    val property = table.getPropertyOrNull(children[index].value)
                    if (property == null) {
                        val obj = table.getObjectOrNull(children[index])
                        if (obj == null) {
                            if (canBeFile) {
                                addFile()
                                index++
                                getFirstVariable(false)
                            } else throw PositionalException("Object not found in $children[index]", children[index])
                        } else currentVariable = obj
                    } else currentVariable = property

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
            // TODO children[index].evaluate()
            // TODO add constructor args similar to call args here
            currentVariable = type
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
        val tableForEvaluation = SymbolTable(
            fileTable = table.getFileTable(),
            variableTable = table.getCurrentType()
        ) // table.changeScope(initialTable.getScope())
        (children[index] as Call).argumentsToParameters(function, initialTable, tableForEvaluation)
        val functionResult = (children[index] as Call).evaluateFunction(tableForEvaluation, function)
        currentVariable = functionResult.toVariable(children[index])
    }

    init {
        if (children.isNotEmpty()) {
            this.children.clear()
            this.children.addAll(children)
        }
    }

    override fun assign(assignment: Assignment, parent: Type?, symbolTable: SymbolTable, value: Any?) {
        TODO("Not yet implemented")
    }

    override fun getFirstUnassigned(parent: Type, symbolTable: SymbolTable): Assignment? {
        index = 0
        arguments.clear()
        initialTable = symbolTable.changeVariable(parent)
        table = initialTable.copy()
        val firstResolved = checkFirstVariable()
        if (firstResolved is Assignment)
            return firstResolved
        index++
        while (index < children.size) {
            val res = checkNextVariable(
                currentVariable ?: throw PositionalException(
                    "Cannot be casted to variable",
                    children[index - 1]
                )
            )
            if (res != null)
                return res
            table = table.changeVariable(currentVariable!!)
            arguments.add(emptyList())
            index++
        }
        return null
    }

    override fun getPropertyName(): Token {
        TODO("Not yet implemented")
    }
}