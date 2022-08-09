package delete.invocation

import lexer.RuntimeError
import delete.Assignment
import delete.Identifier
import properties.Type
import table.SymbolTable
import utils.Utils.toProperty

class Constructor(val type: Type, val args: List<Assignment>, position: Pair<Int, Int>) :
    Invocation(type.name, args, listOf(), position) {
    override fun evaluate(symbolTable: SymbolTable): Any {
        return evaluateType(type, symbolTable)
    }

    private fun evaluateType(type: Type, symbolTable: SymbolTable): Any {
        resolveArguments(type, symbolTable)
        return if (Type.resolving) type else Type.resolveTree(type, symbolTable.changeVariable(type).changeScope())
    }

    private fun resolveArguments(type: Type, symbolTable: SymbolTable) {
        for (arg in args) {
            if (arg.assigned !is Identifier)
                throw RuntimeError("Expected property name", arg)
            type.setProperty(arg.assigned.name, arg.expression.evaluate(symbolTable).toProperty(arg.assigned, type))
            type.removeAssignment(arg)
        }
        type.setProperty("this", type)
    }
}