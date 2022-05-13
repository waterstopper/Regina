package properties

import lexer.PositionalException
import token.Token
import token.TokenFactory
import token.statement.Assignment

open class Type(
    val name: String,
    parent: Type?,
    val assignments: MutableList<Assignment>,
    private val fileName: String,
    private val exported: Any? = null,
    var supertype: Type? = null
) :
    Property(parent), Invokable {
    private val properties = mutableMapOf<String, Property>()
    val functions = mutableListOf<Function>()

    fun getAssignment(token: Token): Assignment? = assignments.find { it.left == token }
    fun removeAssignment(assignment: Assignment) = assignments.remove(assignment)
    fun removeAssignment(token: Token): Assignment? {
        for (a in assignments)
            if (a.left == token) {
                assignments.remove(a)
                return a
            }
        return null
    }

    fun getResolved(token: Token) =
        properties[token.value] ?: assignments.find { it.left.value == token.value }
        ?: throw PositionalException("unknown property", token)

    override fun getFunctionOrNull(name: String): Function? = functions.find { it.name == name }
    override fun getFunction(token: Token) = functions.find { it.name == token.value }
        ?: throw PositionalException("\"$name\" class does not contain `${token.value}` function", token)

    override fun hasProperty(token: Token): Boolean {
        return properties[token.value] != null
    }


    fun getProperties() = properties.toMutableMap()
    override fun getPropertyOrNull(name: String) = when (name) {
        "parent" -> getParentOrNull()
        else -> properties[name]
    }

    override fun getProperty(token: Token) = when (token.value) {
        "parent" -> getParentOrNull()
        else -> properties[token.value] ?: throw PositionalException("`${token.value}` not found in `$name`", token)
    }

    fun setProperty(token: Token, value: Property) {
        properties[token.value] = value
    }

    override fun toString(): String {
        val res = StringBuilder(name)
        if (supertype != null) {
            res.append(":")
            if (supertype!!.fileName != fileName)
                res.append("${supertype!!.fileName}.")
            res.append(supertype!!.name)
        }
        if (exported != null)
            res.append("->$exported")
        res.append("{parent:${parent ?: "-"}, ${properties}, $assignments}")
        return res.toString()

    }

    fun inherits(other: Type): Boolean {
        var type: Type? = this
        while (type != null) {
            if (type.name == other.name && type.fileName == other.fileName)
                return true
            type = type.supertype
        }
        return false
    }

    fun copy(): Type {
        val copy =
            Type(
                name, parent?.copy(),
                assignments.map { TokenFactory().copy(it) }.toMutableList() as MutableList<Assignment>,
                fileName, this.exported, this.supertype
            )
        copy.assignments.forEach { it.parent = copy }
        return copy
    }

    //    fun getFirstUnresolved(token: Token): Pair<Type, String>? {
//        var linkRoot = token
//        var table = symbolTable
//        var type = this
//        while (linkRoot.value == ".") {
//            val nextType = table.getVariableOrNull(linkRoot.left) ?: return Pair(type, linkRoot.left.value)
//            if (nextType !is Type)
//                throw PositionalException("expected class instance, but primitive was found", linkRoot.left)
//            type = nextType
//            table = type.symbolTable
//            linkRoot = linkRoot.right
//        }
//        return null
//    }
    companion object {

//        fun initializeSuperTypes() {
//            for ((pair, token) in superTypes) {
//                val (type, fileName) = pair
//                if (token.value == ".")
//                    SymbolTable.types[type]!![fileName]!!.supertype =
//                        SymbolTable.types[token.right.value]!![token.left.value]
//                else {
//                    val parents = SymbolTable.types[token.value]!!.filter {
//                        SymbolTable.importMap[fileName]?.contains(it.key) ?: false
//                                || it.key == fileName
//                    }
//                    if (parents.isEmpty())
//                        throw PositionalException("no superclass ${token.value} found", token)
//                    if (parents.size > 1)
//                        throw PositionalException(
//                            "superclass ambiguity. There are ${parents.size} applicable supertypes in files ${parents.keys}",
//                            token
//                        )
//                    SymbolTable.types[type]!![fileName]!!.supertype = parents[parents.keys.first()]
//                }
//            }
//        }
    }
}