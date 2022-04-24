package token

import lexer.Parser
import lexer.PositionalException
import token.link.Link

class TokenDeclaration(
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
    val export: Token?
        get() = getSupertype(getExport(this)).second

    private fun getDeclarationName(): Token {
        return when (symbol) {
            "fun" -> {
                var res = left
                while (res is Link)
                    res = res.right
                res
            }
            "object" -> left
            "class" -> getSupertype(getExport(left)).first
            "import" -> if (children.size != 1 || children.first() !is TokenIdentifier) throw PositionalException(
                "Expected file name",
                this
            ) else children.first()
            else -> throw PositionalException("Unregistered declaration", this)
        }
    }

    private fun getExport(token: Token): Token {
        return if (token.value == "export")
            token.left
        else token
    }

    private fun getSupertype(token: Token): Pair<Token, Token?> {
        return if (token.value == ":")
            Pair(token.left, token.right)
        else Pair(token, null)
    }
}