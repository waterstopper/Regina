package properties

import token.statement.TokenAssignment

class Object(name: String, assignments: MutableList<TokenAssignment>) : Type(name,null,assignments) {
}