package properties

import table.FileTable
import token.statement.Assignment

/**
 * Object is a [singleton][https://en.wikipedia.org/wiki/Singleton_pattern] Type
 */
class Object(name: String, assignments: MutableList<Assignment>, fileName: FileTable) :
    Type(name, null, assignments, fileName)