package properties

import SymbolTable

class Object(name: String) : Property(name, null) {
    val symbolTable = SymbolTable()
}