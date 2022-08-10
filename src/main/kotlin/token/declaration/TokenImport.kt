package token.declaration

import node.Declaration
import node.Node
import token.Token

class TokenImport(
    token: Token
) : Token(token.symbol, token.value, token.position, token.bindingPower, token.nud, token.led, token.std) {

    init {
        this.children.clear()
        this.children.addAll(children)
    }

    override fun toNode(): Node {
        return Declaration(symbol, value, position, children.map { it.toNode() })
    }
}

class TokenType(
    token: Token
) : Token(token.symbol, token.value, token.position, token.bindingPower, token.nud, token.led, token.std) {

    init {
        this.children.clear()
        this.children.addAll(children)
    }

    override fun toNode(): Node {
        return Declaration(symbol, value, position, children.map { it.toNode() })
    }
}

class TokenObject(
    token: Token
) : Token(token.symbol, token.value, token.position, token.bindingPower, token.nud, token.led, token.std) {

    init {
        this.children.clear()
        this.children.addAll(children)
    }

    override fun toNode(): Node {
        return Declaration(symbol, value, position, children.map { it.toNode() })
    }
}

class TokenFunction(
    token: Token
) : Token(token.symbol, token.value, token.position, token.bindingPower, token.nud, token.led, token.std) {

    init {
        this.children.clear()
        this.children.addAll(children)
    }

    override fun toNode(): Node {
        return Declaration(symbol, value, position, children.map { it.toNode() })
    }
}