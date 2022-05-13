package token

import Argumentable
import properties.Type
import table.SymbolTable
import token.statement.Assignment

interface Assignable : Argumentable {
    fun assign(assignment: Assignment, parent: Type?, symbolTable: SymbolTable, value: Any? = null)
    fun getFirstUnassigned(parent: Type): Assignment?
    fun getPropertyName(): Token
}