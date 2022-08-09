package node.variable

import table.SymbolTable
import node.Node
import utils.Utils.toVariable

class NodeDictionary(node: Node) : Node(
    node.symbol,
    node.value,
    node.position,
    node.bindingPower,
    node.nud,
    node.led,
    node.std,
    node.children
) {
    init {
        this.children.clear()
        this.children.addAll(children)
    }

    override fun evaluate(symbolTable: SymbolTable): Any {
        return children.associate {
            Pair(
                it.left.evaluate(symbolTable).toVariable(it.left),
                it.right.evaluate(symbolTable).toVariable(it.right)
            )
        }.toMutableMap()
    }
}
