package properties

import SymbolTable
import token.statement.TokenAssignment

class Object(name: String, val assignments: MutableList<TokenAssignment>) : Variable(name, null) {
    override val symbolTable = SymbolTable()
}