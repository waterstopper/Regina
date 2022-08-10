package delete.invocation

import delete.Assignment
import delete.Delete
import delete.Identifier
import lexer.RuntimeError
import table.SymbolTable

open class Invocation(
    val name: String,
    val namedArgs: List<Assignment>,
    val unnamedArgs: List<Identifier>,
    position: Pair<Int, Int>
) : Delete(position) {
    override fun evaluate(symbolTable: SymbolTable): Any {
        throw RuntimeError(
            "Invocations should be replaced with Calls or Constructors",
            this
        )
    }
}