package token

import lexer.Parser
import lexer.PositionalException
import table.SymbolTable
import token.invocation.TokenCall
import token.invocation.TokenConstructor
import token.operator.TokenArithmeticOperator
import token.operator.TokenOperator
import token.operator.TokenTypeOperator
import token.statement.TokenAssignment

class TokenFactory {
    private val nonArithmeticOperators = listOf("+", "==", "!=")
    private val arithmeticOperators = listOf("-", "*", "/", "%", ">=", "<=", ">", "<", "!", "&", "|")
    private val wordOperators = listOf("is", "isnot")

    fun createWordToken(
        symbol: String,
        value: String,
        position: Pair<Int, Int>,
        bindingPower: Int,
        nud: ((token: Token, parser: Parser) -> Token)?,
        led: ((token: Token, parser: Parser, token2: Token) -> Token)?,
        std: ((token: Token, parser: Parser) -> Token)?
    ): Token {
        return when (symbol) {
            in wordOperators -> TokenTypeOperator(symbol, value, position, bindingPower, nud, led, std)
            else -> TokenIdentifier(symbol, value, position, bindingPower, nud, led, std)
        }
    }

    fun copy(token: Token): Token {
        return token::class.constructors.toMutableList()[0].call(
            token.symbol,
            token.value,
            token.position,
            token.bindingPower,
            token.nud,
            token.led,
            token.std,
            token.children
        )
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
            symbolTable: SymbolTable
        ): TokenIdentifier {
            if (symbolTable.getTypeOrNull(tokenIdentifier.left) != null)
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
            if (symbolTable.getFunctionOrNull(tokenIdentifier.left) != null)
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