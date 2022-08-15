package node

import lexer.PositionalException

open class Declaration(
    symbol: String,
    value: String,
    position: Pair<Int, Int>,
    children: List<Node>
) : Node(symbol, value, position, children.toMutableList()) {
    constructor(node: Node) : this(
        node.symbol,
        node.value,
        node.position,
        node.children
    )

    val name: Node
        get() = getDeclarationName()
    val supertype: Node
        get() = children[1]

    private fun getDeclarationName(): Node {
        return when (symbol) {
            "fun" -> {
                var res = left
                while (res is Link)
                    res = res.right
                res
            }
            "object" -> left
            "class" -> getSupertype(left).first
            "import" -> if (children.size != 1 || children.first() !is Identifier) throw PositionalException(
                "Expected file name",
                this
            ) else children.first()
            else -> throw PositionalException("Unregistered declaration", this)
        }
    }

    private fun getSupertype(node: Node): Pair<Node, Node?> {
        return if (node.value == ":")
            Pair(node.left, node.right)
        else Pair(node, null)
    }
}

class ImportNode(
    symbol: String,
    value: String,
    position: Pair<Int, Int>,
    children: List<Node>
) : Declaration(symbol, value, position, children.toMutableList()) {
    val fileName: Node
        get() = children[0]
    val importName: String
        get() = right.value
}

class FunctionNode(
    symbol: String,
    value: String,
    position: Pair<Int, Int>,
    children: List<Node>
) : Declaration(symbol, value, position, children.toMutableList()) {
    val functionName: String
        get() = children[0].value
}

class TypeNode(
    symbol: String,
    value: String,
    position: Pair<Int, Int>,
    children: List<Node>
) : Declaration(symbol, value, position, children.toMutableList()) {
    val superTypeNode: Node?
        get() = if (children[1].value != "") children[1] else null
}

class ObjectNode(
    symbol: String,
    value: String,
    position: Pair<Int, Int>,
    children: List<Node>
) : Declaration(symbol, value, position, children.toMutableList())
