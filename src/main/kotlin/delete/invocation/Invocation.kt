package delete.invocation

import lexer.RuntimeError
import delete.Assignment
import delete.Identifier
import delete.Delete
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