package token

import lexer.Parser
import lexer.PositionalException
import table.SymbolTable
import token.invocation.Call
import token.invocation.Constructor
import token.link.Link
import token.operator.ArithmeticOperator
import token.operator.Index
import token.operator.Operator
import token.operator.TypeOperator
import token.statement.Assignment
import token.variable.TokenArray
import token.variable.TokenNumber
import token.variable.TokenString

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
            in wordOperators -> TypeOperator(symbol, value, position, bindingPower, nud, led, std)
            else -> Identifier(symbol, value, position, bindingPower, nud, led, std)
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
            "." -> Link(("(LINK)"), value, position, bindingPower, nud, led, std)
            "=" -> Assignment("(ASSIGNMENT)", value, position, bindingPower, nud, led, std)
            // "[" -> TokenIndexing(symbol, value, position, bindingPower, nud, led, std)
            in nonArithmeticOperators -> Operator(symbol, value, position, bindingPower, nud, led, std)
            in arithmeticOperators -> ArithmeticOperator(symbol, value, position, bindingPower, nud, led, std)
            else -> Token(symbol, value, position, bindingPower, nud, led, std)
        }
    }

    companion object {
        fun createSpecificIdentifierFromInvocation(
            tokenIdentifier: Token,
            symbolTable: SymbolTable,
            linkLevel: Int,
            upperToken: Token
        ): Identifier {
            // TODO not checking that variable contains function
            // TODO not checking a[i].b where a[i] is object
            if (symbolTable.getFunctionOrNull(tokenIdentifier.left) != null
                || linkLevel >= 2
                || (upperToken is Link && (symbolTable.getVariableOrNull(upperToken.left.value) != null
                        || upperToken.left is TokenArray
                        || upperToken.left is TokenString
                        || upperToken.left is TokenNumber
                        || upperToken.left is Index))
            ) {
                return Call(
                    "(CALL)",
                    tokenIdentifier.value,
                    tokenIdentifier.position,
                    tokenIdentifier.bindingPower,
                    tokenIdentifier.nud,
                    tokenIdentifier.led,
                    tokenIdentifier.std,
                    tokenIdentifier.children
                )
            } else if (symbolTable.getTypeOrNull(tokenIdentifier.left) != null && linkLevel <= 1)
                return Constructor(
                    "(CONSTRUCTOR)",
                    tokenIdentifier.value,
                    tokenIdentifier.position,
                    tokenIdentifier.bindingPower,
                    tokenIdentifier.nud,
                    tokenIdentifier.led,
                    tokenIdentifier.std,
                    tokenIdentifier.children
                )
            throw PositionalException("Unknown invocated identifier `${tokenIdentifier.left.value}`", tokenIdentifier)
        }
    }
}