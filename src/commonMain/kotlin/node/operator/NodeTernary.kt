package node.operator

import lexer.PositionalException
import node.Node
import node.statement.Assignment
import properties.Type
import table.SymbolTable
import utils.Utils.FALSE

class NodeTernary(node: Node) :
    Node(node.symbol, node.value, node.position, node.children) {

    override fun evaluate(symbolTable: SymbolTable): Any {
        if (children.size != 3) {
            throw PositionalException("ternary if should have else branch", symbolTable.getFileTable().filePath, this)
        }
        val condition = evaluateCondition(symbolTable)
        return if (condition != FALSE) {
            right.evaluate(symbolTable)
        } else children[2].evaluate(symbolTable)
    }

    fun evaluateCondition(symbolTable: SymbolTable): Any = left.evaluate(symbolTable)

    /**
     * This method finds unassigned fields in ternary condition.
     * Ternary branches are not checked for unassigned fields.
     */
    override fun findUnassigned(symbolTable: SymbolTable, parent: Type): Pair<Type, Assignment>? {
        val found = left.findUnassigned(symbolTable, parent)
        if (found != null) {
            return found
        }
        return null
    }
}
