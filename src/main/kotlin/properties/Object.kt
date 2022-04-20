package properties

import token.statement.TokenAssignment

class Object(name: String, assignments: MutableList<TokenAssignment>, fileName: String) :
    Type(name, null, assignments, fileName) {
}