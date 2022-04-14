package token

import evaluation.TypeEvaluation.resolveTree
import evaluation.TypeEvaluation.resolving
import lexer.Parser
import SymbolTable

class TokenConstructor(
    symbol: String,
    value: String,
    position: Pair<Int, Int>,
    bindingPower: Int,
    nud: ((token: Token, parser: Parser) -> Token)?,
    led: ((
        token: Token, parser: Parser, token2: Token
    ) -> Token)?,
    std: ((token: Token, parser: Parser) -> Token)?, children: List<Token>
) : TokenIdentifier(symbol, value, position, bindingPower, nud, led, std) {
    init {
        this.children.clear()
        this.children.addAll(children)
    }

    override fun evaluate(symbolTable: SymbolTable): Any {
        val type = symbolTable.getInvokable(left)
        return if (resolving) type as SymbolTable.Type else resolveTree(type as SymbolTable.Type)
    }
}