package token

import lexer.Parser
import lexer.PositionalException

class TokenFactory {
    private val nonArithmeticOperators = listOf("+", "==", "!=")
    private val arithmeticOperators = listOf("-", "*", "/", "%", ">=", "<=", ">", "<", "!", "&", "|")
    private val wordOperators = listOf("if", "is", "!is")

    fun createWordToken(
        symbol: String,
        value: String,
        position: Pair<Int, Int>,
        bindingPower: Int,
        nud: ((token: Token, parser: Parser) -> Token)?,
        led: ((token: Token, parser: Parser, token2: Token) -> Token)?,
        std: ((token: Token, parser: Parser) -> Token)?
    ) {
        when (symbol) {
            in wordOperators -> TokenWordOperator(symbol, value, position, bindingPower, nud, led, std)
        }
    }

    fun createOperator(
        symbol: String,
        value: String,
        position: Pair<Int, Int>,
        bindingPower: Int,
        nud: ((token: Token, parser: Parser) -> Token)?,
        led: ((token: Token, parser: Parser, token2: Token) -> Token)?,
        std: ((token: Token, parser: Parser) -> Token)?
    ): Token {
        return when (symbol) {
            "." -> TokenLink(("(LINK)"), value, position, bindingPower, nud, led, std)
            "=" -> TokenAssignment("(ASSIGNMENT)", value, position, bindingPower, nud, led, std)
            // "[" -> TokenIndexing(symbol, value, position, bindingPower, nud, led, std)
            in nonArithmeticOperators -> TokenOperator(symbol, value, position, bindingPower, nud, led, std)
            in arithmeticOperators -> TokenArithmeticOperator(symbol, value, position, bindingPower, nud, led, std)
            else -> Token(symbol, value, position, bindingPower, nud, led, std)
        }

    }

    companion object {
        fun createSpecificIdentifierFromInvocation(
            tokenIdentifier: Token,
            classes: MutableSet<String>,
            functions: MutableSet<String>
        ): TokenIdentifier {
            if (classes.contains(tokenIdentifier.left.value))
                return TokenConstructor(
                    "(CONSTRUCTOR)",
                    tokenIdentifier.value,
                    tokenIdentifier.position,
                    tokenIdentifier.bindingPower,
                    tokenIdentifier.nud,
                    tokenIdentifier.led,
                    tokenIdentifier.std,
                    tokenIdentifier.children
                )
            if (functions.contains(tokenIdentifier.left.value))
                return TokenCall(
                    "(CALL)",
                    tokenIdentifier.value,
                    tokenIdentifier.position,
                    tokenIdentifier.bindingPower,
                    tokenIdentifier.nud,
                    tokenIdentifier.led,
                    tokenIdentifier.std,
                    tokenIdentifier.children
                )
            throw PositionalException("unknown invocated identifier ${tokenIdentifier.value}", tokenIdentifier)
        }
    }
}