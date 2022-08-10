import lexer.Parser
import node.Node
import node.variable.NodeNumber
import token.Token

class TokenNumber(value: String, position: Pair<Int, Int>) :
    Token("(NUMBER)", value, position, 0, { t: Token, _: Parser -> t }, null, null) {
    override fun toNode(): Node {
        return NodeNumber(value, position)
    }
}
