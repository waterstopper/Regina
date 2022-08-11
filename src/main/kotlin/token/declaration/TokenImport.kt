package token.declaration

import lexer.SyntaxException
import node.*
import token.Assignment
import token.Token
import token.TokenIdentifier

class TokenImport(
    token: Token
) : Token(token.symbol, token.value, token.position, token.bindingPower, token.nud, token.led, token.std) {

    override fun toNode(): Node {
        return ImportNode(symbol, value, position, children.map { it.toNode() })
    }
}

class TokenType(
    token: Token
) : Token(token.symbol, token.value, token.position, token.bindingPower, token.nud, token.led, token.std) {

    override fun toNode(): Node {
        return TypeNode(symbol, value, position, children.map { it.toNode() })
    }
}

class TokenObject(
    token: Token
) : Token(token.symbol, token.value, token.position, token.bindingPower, token.nud, token.led, token.std) {


    override fun toNode(): Node {
        return ObjectNode(symbol, value, position, children.map { it.toNode() })
    }
}

class TokenFunction(
    token: Token
) : Token(token.symbol, token.value, token.position, token.bindingPower, token.nud, token.led, token.std, token.children) {

    override fun toNode(): Node {
        checkParameters(children[0].children.subList(1, children[0].children.size))
        return FunctionNode(symbol, value, position, children.map { it.toNode() })
    }

    private fun checkParameters(parameters: List<Token>) {
        for (parameter in parameters) {
            if (parameter !is Assignment && parameter !is TokenIdentifier)
                throw SyntaxException("Expected identifier or assignment as function parameter", this)
        }
    }
}