package token.statement

import evaluation.FunctionEvaluation.toVariable
import lexer.Parser
import lexer.PositionalException
import properties.Type
import table.SymbolTable
import token.Token
import token.TokenIdentifier
import token.link.Link
import token.operator.Indexing
import token.operator.Operator

class Assignment(
    symbol: String,
    value: String,
    position: Pair<Int, Int>,
    bindingPower: Int,
    nud: ((token: Token, parser: Parser) -> Token)?,
    led: ((
        token: Token, parser: Parser, token2: Token
    ) -> Token)?,
    std: ((token: Token, parser: Parser) -> Token)?,
    children: MutableList<Token> = mutableListOf()
) : Operator(symbol, value, position, bindingPower, nud, led, std) {
    init {
        this.children.clear()
        this.children.addAll(children)
    }

    var parent: Type? = null
    val name: String get() = left.value

    fun canEvaluate(): Boolean = right.find("(IDENT)") == null
            && right.find("parent") == null

    override fun evaluate(symbolTable: SymbolTable): Any {
        val value = right.evaluate(symbolTable)
        assignLValue(left, value, symbolTable.getCurrentType(), symbolTable)
        return value
    }

//    override fun copy(): TokenAssignment = TokenAssignment(
//        symbol,
//        value,
//        position,
//        bindingPower,
//        nud,
//        led,
//        std,
//        children.map { it.copy() }.toMutableList()
//    )


    private fun assignLValue(token: Token, value: Any, parent: Type?, symbolTable: SymbolTable) {
        if (token is TokenIdentifier) {
            symbolTable.addVariable(token.value, value.toVariable(token, parent))
            return
        }
        // all variables inside PArray property of type won't have such type as parent
        if (token is Indexing) {
            val (array, index) = token.getArrayAndIndex(symbolTable)
            array.getPValue()[index] = value.toVariable(right, null)
            return
        }
        var importTable = symbolTable
        var current = token
        while (current is Link) {
            // left is type
            if (importTable.getVariableOrNull(current.left.value) != null) {
                val type = importTable.getVariableOrNull(current.left.value)
                if (type is Type) {
                    importTable = symbolTable.changeType(type)
                    current = current.right
                } else throw PositionalException("primitive does not contain properties", current.left)
            } else if (importTable.getObjectOrNull(current.left) != null) {
                importTable = symbolTable.changeType(importTable.getObjectOrNull(current.left)!!)
                current = current.right
            } else if (importTable.getImportOrNull(current.left.value) != null) {
                importTable = symbolTable.changeFile(current.left.value)
                current = current.right
            }
        }
        if (current is TokenIdentifier)
            importTable.addVariable(current.value, value.toVariable(current, parent))
        else throw PositionalException("expected identifier or link", current)
    }
}