package token

import lexer.Parser
import lexer.PositionalException

open class Declaration(
    symbol: String,
    value: String,
    position: Pair<Int, Int>,
    bindingPower: Int,
    nud: ((token: Token, parser: Parser) -> Token)?,
    led: (
        (
        token: Token, parser: Parser, token2: Token
    ) -> Token
    )?,
    std: ((token: Token, parser: Parser) -> Token)?,
    children: List<Token>
) : Token(symbol, value, position, bindingPower, nud, led, std) {
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

    val name: Token
        get() = getDeclarationName()
    val supertype: Token
        get() = children[1]

    private fun getDeclarationName(): Token {
        return when (symbol) {
            "fun" -> {
                var res = left
                while (res is Link)
                    res = res.right
                res
            }
            "object" -> left
            "class" -> getSupertype(left).first
            "import" -> if (children.size != 1 || children.first() !is Identifier) throw PositionalException(
                "Expected file name",
                this
            ) else children.first()
            else -> throw PositionalException("Unregistered declaration", this)
        }
    }

    private fun getSupertype(token: Token): Pair<Token, Token?> {
        return if (token.value == ":")
            Pair(token.left, token.right)
        else Pair(token, null)
    }
}

class ImportDeclaration(
    symbol: String,
    value: String,
    position: Pair<Int, Int>,
    bindingPower: Int,
    nud: ((token: Token, parser: Parser) -> Token)?,
    led: (
        (
        token: Token, parser: Parser, token2: Token
    ) -> Token
    )?,
    std: ((token: Token, parser: Parser) -> Token)?,
) : Declaration(symbol, value, position, bindingPower, nud, led, std, listOf()) {

}