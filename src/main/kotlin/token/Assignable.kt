package token

import properties.Type
import table.SymbolTable
import token.statement.Assignment

interface Assignable {
    fun assign(assignment: Assignment, parent: Type, symbolTable: SymbolTable)
    fun getFirstUnassigned(parent: Type): Assignment?
    fun getPropertyName(): Token
}