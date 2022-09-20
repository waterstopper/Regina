package token.variable

import lexer.Parser
import node.Node
import node.variable.NodeNull
import token.Token

class TokenNull(position: Pair<Int, Int>) :
    Token("(NUMBER)", "null", position, 0, { t: Token, _: Parser -> t }, null, null) {

    override fun toNode(filePath: String): Node {
        return NodeNull(position)
    }
}