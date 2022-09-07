package node.invocation

import lexer.PositionalException
import node.Identifier
import node.Node
import node.statement.Assignment
import properties.Type
import properties.Type.Companion.resolveTree
import table.SymbolTable
import utils.Utils.toProperty

class Constructor(
    node: Node
) : Invocation(
    node.symbol, node.value,
    node.position,
    node.children
) {
    init {
        this.children.clear()
        this.children.addAll(node.children)
    }

    override fun evaluate(symbolTable: SymbolTable): Any {
        val type = if (left is Identifier) symbolTable.getType(left) else left.evaluate(symbolTable)
        if (type !is Type)
            throw PositionalException("Expected type", symbolTable.getFileTable().filePath,left)
        if(type.index == 0)
            return evaluateType(type.copy(), symbolTable)
        return evaluateType(type, symbolTable)
    }

    private fun evaluateType(type: Type, symbolTable: SymbolTable): Any {
        resolveArguments(type, symbolTable)
        return if (symbolTable.resolvingType) type else resolveTree(
            type,
            symbolTable.changeVariable(type).changeScope()
        )
    }

    private fun resolveArguments(type: Type, symbolTable: SymbolTable) {
        for (arg in children.subList(1, children.size)) {
            if (arg !is Assignment)
                throw PositionalException("Expected assignment",symbolTable.getFileTable().filePath, arg)
            if (arg.left !is Identifier)
                throw PositionalException("Expected property name",symbolTable.getFileTable().filePath, arg)
            type.setProperty(arg.left.value, arg.right.evaluate(symbolTable).toProperty(arg.left, type))
            type.removeAssignment(arg.left)
        }
        type.setProperty("this", type)
    }
}
