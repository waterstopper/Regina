import lexer.Parser
import lexer.SyntaxException
import node.Node
import node.variable.NodeNumber
import token.Token

class TokenNumber(value: String, position: Pair<Int, Int>) :
    Token("(NUMBER)", value, position, 0, { t: Token, _: Parser -> t }, null, null) {

    override fun toNode(filePath: String): Node {
        val number: Number = if (isInteger()) {
            val parsedDouble = value.toDouble()
            if (parsedDouble < Int.MIN_VALUE || parsedDouble > Int.MAX_VALUE)
                throw SyntaxException("Integer can be in range [${Int.MIN_VALUE}, ${Int.MAX_VALUE}]", filePath, this)
            value.toInt()
        } else value.toDouble()
        return NodeNumber(value, position, number)
    }

    private fun isInteger() = !value.contains(".")
}
