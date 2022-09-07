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
