package token

import SymbolTable
import evaluation.FunctionEvaluation.toVariable
import lexer.Parser
import lexer.PositionalException

class TokenAssignment(
    symbol: String,
    value: String,
    position: Pair<Int, Int>,
    bindingPower: Int,
    nud: ((token: Token, parser: Parser) -> Token)?,
    led: ((
        token: Token, parser: Parser, token2: Token
    ) -> Token)?,
    std: ((token: Token, parser: Parser) -> Token)?
) : Token(symbol, value, position, bindingPower, nud, led, std) {
    override fun evaluate(symbolTable: SymbolTable): Any {
        val value = right.evaluate(symbolTable)
        assignLValue(left, value, symbolTable.parent, symbolTable)
        return value
    }

    private fun assignLValue(token: Token, value: Any, parent: SymbolTable.Type?, symbolTable: SymbolTable) {
        if (token is TokenIdentifier) {
            symbolTable.addVariable(token.value, value.toVariable(token, parent))
            return
        }
        // all variables inside PArray property of type won't have such type as parent
        if (token is TokenIndexing) {
            val (array, index) = token.getArrayAndIndex(symbolTable)
            array.getArray()[index] = value.toVariable(right, null)
            return
        }
        var importTable = symbolTable
        var current = token
        while (current is TokenLink) {
            // left is type
            if (importTable.getVariableOrNull(current.left) != null) {
                val type = importTable.getVariableOrNull(current.left)
                if (type is SymbolTable.Type) {
                    importTable = type.symbolTable
                    current = current.right
                } else throw PositionalException("primitive does not contain properties", current.left)
            } else if (importTable.getObjectOrNull(current.left) != null) {
                importTable = importTable.getObjectOrNull(current.left)!!.symbolTable
                current = current.right
            } else if (importTable.getImportOrNull(current.left) != null) {
                importTable = SymbolTable(currentFile = current.left.value)
                current = current.right
            }
        }
        if (current is TokenIdentifier)
            importTable.addVariable(current.value, value.toVariable(current, parent))
        else throw PositionalException("expected identifier or link", current)
    }
}