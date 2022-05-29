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
        // On second evaluation it should be reset (if function with this token is called twice)
        index = 0
        initialTable = symbolTable
        table = symbolTable.copy()
        if (!checkFirstVariable())
            throw PositionalException("Not found", left)
        table = table.changeVariable(currentVariable!!)
        index++
        while (index < children.size) {
            getNextVariable(
                currentVariable
                    ?: throw PositionalException("Cannot be casted to variable", children[index - 1])
            )
            table = table.changeVariable(currentVariable!!)
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
            is Identifier -> assignCurrentVariable(variable.getProperty(children[index]))
            is Index -> assignCurrentVariable((children[index] as Index).evaluateIndex(table).toVariable(right.right))
        }
        return true
    }

    protected open fun checkFirstVariable(canBeFile: Boolean = true): Boolean {
        when (children[index]) {
//            is TokenArray, is TokenNumber, is TokenString -> {
//                if (!canBeFile)
//                    throw PositionalException("Unexpected token", children[index])
//                assignCurrentVariable(children[index].evaluate(table).toVariable(children[index]))
//            }
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

    protected open fun checkNextVariable(variable: Variable): Optional {
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
                throw PositionalException("not implemented")
            }
        }
        return Optional(isGood = true)
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
            assignCurrentVariable(type)
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
        assignCurrentVariable(functionResult.toVariable(children[index]))
    }

    init {
        if (children.isNotEmpty()) {
            this.children.clear()
            this.children.addAll(children)
        }
    }

    override fun assign(assignment: Assignment, parent: Type?, symbolTable: SymbolTable, value: Any?) {
        if (currentParent is Type)
            currentVariable?.let {
                (currentParent as Type).setProperty(
                    children[children.lastIndex].value,
                    it.toProperty(children[children.lastIndex])
                )
            }
    }

    override fun getFirstUnassigned(parent: Type, symbolTable: SymbolTable): Assignment? {
        index = 0
        initialTable = symbolTable.changeVariable(parent)
        table = initialTable.copy()
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

    override fun getPropertyName(): Token {
        TODO("Not yet implemented")
    }
}