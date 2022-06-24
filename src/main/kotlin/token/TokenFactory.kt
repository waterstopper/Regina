package token

import lexer.Parser
import lexer.PositionalException
import table.SymbolTable
import token.invocation.Call
import token.invocation.Constructor
import token.invocation.Invocation
import token.operator.ArithmeticOperator
import token.operator.Operator
import token.operator.TypeOperator
import token.statement.Assignment

object TokenFactory {
    private val nonArithmeticOperators = listOf("+", "==", "!=")
    private val arithmeticOperators = listOf("-", "*", "/", "%", ">=", "<=", ">", "<", "!", "&", "|")
    private val wordOperators = listOf("is")

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
        if (token is Assignment) {
            val res = Assignment(
                token.symbol,
                token.value,
                token.position,
                token.bindingPower,
                token.nud,
                token.led,
                token.std,
                token.children
            )
            res.isProperty = token.isProperty
            return res
        }
        // unused
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
            "!is" -> TypeOperator(symbol, value, position, bindingPower, nud, led, std)
            "(" -> Invocation(symbol, value, position, bindingPower, nud, led, std)
            "." -> Link(("(LINK)"), value, position, bindingPower, nud, led, std)
            "=" -> Assignment("(ASSIGNMENT)", value, position, bindingPower, nud, led, std)
            ";", "\n", "\r\n", "\r" -> Token("(SEP)", value, position, bindingPower, nud, led, std)
            // "[" -> TokenIndexing(symbol, value, position, bindingPower, nud, led, std)
            in nonArithmeticOperators -> Operator(symbol, value, position, bindingPower, nud, led, std)
            in arithmeticOperators -> ArithmeticOperator(symbol, value, position, bindingPower, nud, led, std)
            else -> Token(symbol, value, position, bindingPower, nud, led, std)
        }
    }

    fun createSpecificInvocation(
        tokenIdentifier: Token,
        symbolTable: SymbolTable,
        upperToken: Token,
        index: Int
    ): Invocation {
        if (symbolTable.getFunctionOrNull(Call(tokenIdentifier)) != null)
            upperToken.children[index] = Call(tokenIdentifier)
        else if (symbolTable.getTypeOrNull(tokenIdentifier.left) != null)
            upperToken.children[index] = Constructor(tokenIdentifier)
        else throw PositionalException("No class and function found", tokenIdentifier.left)
        return upperToken.children[index] as Invocation
    }

    fun changeInvocationOnSecondPositionInLink(symbolTable: SymbolTable, link: Link): Invocation {
        if (symbolTable.getVariableOrNull(link.left.value) != null ||
            symbolTable.getObjectOrNull(link.left) != null
        ) {
            link.children[1] = Call(link.right)
            return link.children[1] as Call
        }
        val fileTable = symbolTable.getImport(link.left)
        if (fileTable.getFunctionOrNull(Call(link.right)) != null)
            link.children[1] = Call(link.right)
        else if (fileTable.getTypeOrNull(link.right.left.value) != null)
            link.children[1] = Constructor(link.right)
        else throw PositionalException("No class and function found in `${fileTable.fileName}`", link.right)
        return link.children[1] as Invocation
    }

}
