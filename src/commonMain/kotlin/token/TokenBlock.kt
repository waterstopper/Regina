package token

import node.Node
import node.statement.Block

class TokenBlock(token: Token) :
    Token(token.symbol, token.value, token.position, token.bindingPower, token.nud, token.led, token.std) {
    constructor(position: Pair<Int, Int>) : this(Token("{", "{", position))

    override fun toNode(): Node {
        return Block(Node(symbol, value, position, children.map { it.toNode() }.toMutableList()))
    }
}
