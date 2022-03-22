package structure

import lexer.Token

object TypeManager {
    val types = mutableListOf<Type>()

    var exported: Any? = null
    var type: Type? = null
    var name = ""

    fun addType(token: Token) {
        assignName(assignType(assignExported(token.children[0])))

//        val name = token.children[0].children[0].symbol
//        val type = token.children[0].children[1].symbol
//        if (types[name] != null)
//            throw PositionalException("type redeclared", token.children[0].children[1].position)
//        if (types[type] == null)
//            throw PositionalException("undefined superclass", token.children[0].children[1].position)
        val res = mutableListOf<Assignment>()
        val functions = mutableListOf<Token>()
        for (a in token.children[1].children) {
            if (a.symbol == "fun")
                functions.add(a)
            else res.add(Assignment(a))
        }

        types.add(Type(name, name, type, null, res, exported))
        for (func in functions) {
            val type = types.last()
            type.functions.add(
                Function(
                    func.children[0].value,
                    type,
                    (func.children - func.children[0]).map { it.value },
                    func.children[1]
                )
            )
        }
    }

    private fun assignExported(token: Token): Token {
        if (token.value == "export") {
            exported = token.children[1].value
            return token.children[0]
        } else
            exported = null
        return token
    }

    private fun assignType(token: Token): Token {
        if (token.value == ":") {
            type = find(token.children[1].value)
            return token.children[0]
        } else
            type = null
        return token
    }

    private fun assignName(token: Token) {
        name = token.value
    }

    fun getType(name: String): Type {
        if (find(name) == null)
            throw Exception("no type with name $name")
        return find(name)!!
    }

    fun getName(token: Token): String {
        var t = token
        while (t.children.isNotEmpty())
            t = t.children[0]
        return t.value
    }

    fun resolvedSupertype(token: Token): String {
        var t = token
        while (t.children.isNotEmpty()) {
            t = t.children[0]
            if (t.value == ":") {
                return if (find(t.children[1].value) == null)
                    t.children[1].value
                else ""
            }
        }
        return ""
    }

    fun addAllTypes() {

    }

    fun find(name: String) = types.find { it.name == name }

    fun addObject(it: Token) {
        addType(it)
    }
}