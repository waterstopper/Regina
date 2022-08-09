package node.operator

import lexer.PositionalException
import table.SymbolTable
import node.Node

class NodeTernary(node: Node) :
    Node(node.symbol, node.value, node.position, node.bindingPower, node.nud, node.led, node.std) {

    override fun evaluate(symbolTable: SymbolTable): Any {
        if (children.size != 3)
            throw PositionalException("ternary if should have else branch", this)
        val condition = evaluateCondition(symbolTable)
        return if (condition != 0)
            right.evaluate(symbolTable)
        else children[2].evaluate(symbolTable)
    }

    fun evaluateCondition(symbolTable: SymbolTable): Any =
        left.evaluate(symbolTable)
}
