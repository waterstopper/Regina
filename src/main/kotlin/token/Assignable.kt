package token

import properties.Type
import token.statement.Assignment

interface Assignable {
    fun assign(parent: Type)
    fun getAssignment(parent:Type): Assignment
    fun getFirstUnassigned(parent:Type): Assignment?
}