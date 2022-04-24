package properties

import token.statement.Assignment

class Object(name: String, assignments: MutableList<Assignment>, fileName: String) :
    Type(name, null, assignments, fileName) {
}