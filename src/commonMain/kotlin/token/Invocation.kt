package token

import lexer.Parser
import lexer.SyntaxException
import node.Linkable
import node.Node
import node.invocation.Invocation

open class Invocation(
    symbol: String,
    value: String,
    position: Pair<Int, Int>,
    bindingPower: Int,
    nud: ((node: Token, parser: Parser) -> Token)?,
    led: (
        (
        node: Token, parser: Parser, node2: Token
    ) -> Token
    )?,
    std: ((node: Token, parser: Parser) -> Token)?,
) : Token(symbol, value, position, bindingPower, nud, led, std), Linkable {
    val name: Token
        get() = left

    override fun toNode(filePath: String): Node {
        checkParamsOrArgsOrder(children.subList(1, children.size), filePath)
        return Invocation(symbol, value, position, children.map { it.toNode(filePath) })
    }

    /**
     * Check that default parameters and named arguments are after other ones
     */
    private fun checkParamsOrArgsOrder(params: List<Token>, filePath: String) {
        var wasAssignment = false
        for (param in params)
            when (param) {
                is Assignment -> wasAssignment = true
                is TokenIdentifier -> if (wasAssignment)
                    throw SyntaxException("Default params should be after other", filePath, param)
                else -> if (wasAssignment) throw SyntaxException("Named args should be after other", filePath, param)
            }
    }
}
