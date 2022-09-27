package token.declaration

import lexer.SyntaxException
import node.*
import token.Assignment
import token.Token
import token.TokenIdentifier

class TokenImport(
    token: Token
) : Token(token.symbol, token.value, token.position, token.bindingPower, token.nud, token.led, token.std) {

    override fun toNode(filePath: String): Node {
        return ImportNode(symbol, value, position, children.map { it.toNode(filePath) })
    }
}

class TokenType(
    token: Token
) : Token(token.symbol, token.value, token.position, token.bindingPower, token.nud, token.led, token.std) {

    override fun toNode(filePath: String): Node {
        return TypeNode(symbol, value, position, children.map { it.toNode(filePath) })
    }
}

class TokenObject(
    token: Token
) : Token(token.symbol, token.value, token.position, token.bindingPower, token.nud, token.led, token.std) {

    override fun toNode(filePath: String): Node {
        return ObjectNode(symbol, value, position, children.map { it.toNode(filePath) })
    }
}

class TokenFunction(
    token: Token
) : Token(
    token.symbol,
    token.value,
    token.position,
    token.bindingPower,
    token.nud,
    token.led,
    token.std,
    token.children
) {

    override fun toNode(filePath: String): Node {
        checkParameters(children[0].children.subList(1, children[0].children.size), filePath)
        return FunctionNode(symbol, value, position, children.map { it.toNode(filePath) })
    }

    private fun checkParameters(parameters: List<Token>, filePath: String) {
        for (parameter in parameters) {
            if (parameter !is Assignment && parameter !is TokenIdentifier) {
                throw SyntaxException("Expected identifier or assignment as function parameter", filePath, this)
            }
        }
    }
}
