import evaluation.FunctionEvaluation.initializeEmbedded
import evaluation.FunctionEvaluation.toVariable
import lexer.PositionalException
import token.Token
import properties.*
import properties.Function
import token.TokenIdentifier
import token.TokenLink

class SymbolTable(
    private val variables: MutableMap<String, Variable> = mutableMapOf(),
    var currentFile: String = "",
    var parent: Type? = null
) {
    companion object {
        private val importMap: MutableMap<String, MutableSet<String>> = mutableMapOf()
        private val types: MutableMap<String, MutableMap<String, Type>> = mutableMapOf()
        private val functions: MutableMap<String, MutableMap<String, Function>> = mutableMapOf()
        private val objects: MutableMap<String, MutableMap<String, Object>> = mutableMapOf()
        private val embedded: MutableMap<String, Function> = initializeEmbedded()

        fun getEmbeddedNames(): MutableSet<String> = embedded.keys.toMutableSet()
    }

    fun assignLValue(token: Token, value: Any, parent: Type?) {
        if (getVariableOrNull(token) != null)
            variables[token.value] = value.toVariable(token, parent)
        if (token is TokenIdentifier)
            throw PositionalException("unknown identifier", token)
        var importTable = this
        var current = token
        while (current is TokenLink) {
            // left is type
            if (importTable.getVariableOrNull(current.left) != null) {
                val type = importTable.getVariableOrNull(current.left)
                if (type is Type) {
                    importTable = type.symbolTable
                    current = current.right
                }
                throw PositionalException("primitive does not contain properties", current.left)
            } else if (importTable.getObjectOrNull(current.left) != null) {
                importTable = importTable.getObjectOrNull(current.left)!!.symbolTable
                current = current.right
            } else if (importTable.getImportOrNull(current.left) != null) {
                importTable = SymbolTable(currentFile = current.left.value)
                current = current.right
            }
        }
        if (current is TokenIdentifier)
            importTable.variables[current.value] = value.toVariable(current, parent)
        throw PositionalException("expected identifier or link", current)
    }

    fun getObjectOrNull(token: Token): Object? {
        val declarations = objects[token.value] ?: return null
        if (declarations[currentFile] != null)
            return declarations[currentFile]
        val filtered = declarations.filter { importMap[currentFile]!!.contains(it.key) }
        if (filtered.size == 1)
            return filtered.values.first()
        return null
    }

    fun getImportOrNull(token: Token): String? {
        val imports = importMap[currentFile] ?: return null
        return imports.find { it == token.value }
    }

    fun getImportOrNull(to: String, from: String) = importMap[to]?.contains(from) ?: false

    fun addImport(importedTo: String, importedFrom: String) {
        if (importMap[importedTo] == null)
            importMap[importedTo] = mutableSetOf(importedFrom)
        else importMap[importedTo]!!.add(importedFrom)
    }

    fun getDeclared(nameToken: Token, map: MutableMap<String, MutableMap<String, Invokable>>): Invokable {
//        if (nameToken.value == ".") {
////            if (nameToken.left.value == currentFile)
////                return map[nameToken.right.value]?.get(currentFile) ?: throw PositionalException(
////                    "identifier not found",
////                    nameToken.right
////                )
//            if (nameToken.right.value == "(") {
//                return map[nameToken.right.left.value]?.get(nameToken.left.value) ?: throw PositionalException(
//                    "identifier not found",
//                    nameToken.right
//                )
//            }
//            return map[nameToken.right.value]?.get(nameToken.left.value) ?: throw PositionalException(
//                "identifier not found",
//                nameToken.right
//            )
//        }
        val declarations =
            map[nameToken.value] ?: throw PositionalException(
                "identifier ${nameToken.value} not found in $currentFile",
                nameToken
            )
        val filtered =
            declarations.filter { importMap[currentFile]?.contains(it.key) ?: false || it.key == currentFile }
        if (filtered.isEmpty())
            throw PositionalException("identifier ${nameToken.right.value} not found in $currentFile", nameToken.right)
        if (filtered.size > 1)
            throw PositionalException("import ambiguity. Such identifier found in ${filtered.keys}")
        return filtered.values.first()
    }

    private fun getDeclaredOrNull(
        nameToken: Token,
        map: MutableMap<String, MutableMap<String, Invokable>>
    ): Invokable? {
        val declarations = map[nameToken.value] ?: return null
        if (declarations[currentFile] != null)
            return declarations[currentFile]
        val filtered =
            declarations.filter { importMap[currentFile]?.contains(it.key) ?: false }
        if (filtered.size != 1)
            return null
        else return filtered.values.first()
    }

    // TODO rewrite bullshit code
    fun getType(name: String): Type? = types[name]?.get(currentFile)

    fun getTypeOrNull(token: Token): Type? =
        getDeclaredOrNull(token, types as MutableMap<String, MutableMap<String, Invokable>>) as Type?

    private fun getType(token: Token): Type {
        return (getDeclared(token, types as MutableMap<String, MutableMap<String, Invokable>>) as Type).copy()
    }

    fun getFunction(token: Token): Function {
        return embedded[token.value] ?: getDeclared(
            token,
            functions as MutableMap<String, MutableMap<String, Invokable>>
        ) as Function
    }

    fun getMain(): Function {
        val mains = functions["main"] ?: throw PositionalException("no main functions found")
        return mains[currentFile] ?: throw PositionalException("no main function in current file")
    }

    fun addFunction(func: Function, fileName: String = currentFile) {
        if (functions[func.getFunctionName()] == null)
            functions[func.getFunctionName()] = mutableMapOf(fileName to func)
        else functions[func.getFunctionName()]!![fileName] = func
    }

    fun addType(token: Token, fileName: String) {
        Type.assignName(
            Type.assignType(
                Type.assignExported(token.left),
                currentFile
            )
        )
//        val name = token.left.left.symbol
//        val type = token.left.right.symbol
//        if (types[name] != null)
//            throw PositionalException("type redeclared", token.left.right.position)
//        if (types[type] == null)
//            throw PositionalException("undefined superclass", token.left.right.position)
        val res = mutableListOf<Assignment>()
        val functions = mutableListOf<Token>()
        for (a in token.right.children) {
            if (a.symbol == "fun")
                functions.add(a)
            else res.add(Assignment(a))
        }
        val added = Type("", Type.name, null, res, Type.exported)
        if (types[Type.name] == null)
            types[Type.name] = mutableMapOf(fileName to added)
        types[Type.name]!![fileName] = added
        for (assignment in added.assignments)
            assignment.parent = added
    }

    fun getVariableOrNull(token: Token) = variables[token.value]

    fun getVariableOrNull(name: String) = variables[name]

    fun getVariable(token: Token): Variable {
        return variables[token.value] ?: throw PositionalException("identifier not found", token)
    }

    fun getVariable(name: String): Variable = variables[name]!!

    fun addVariable(name: String, variable: Variable) {
        variables[name] = variable
    }

    fun getInvokable(token: Token): Invokable {
        val res: Invokable
        try {
            res = getType(token)
        } catch (e: PositionalException) {
            return getFunction(token)
        }
        return res
//        return if (findFunction(token.left.value) != null)
//            FunctionEvaluation.evaluateFunction(
//                token, findFunction(token.left.value)!!,
//                token.children.subList(1, token.children.size), this
//            )
//        else {
//            // bad for properties inside class. Need to create some global variable in TypeEvaluation
//            // if it is true then resolve tree, else just add type-property to existing instance
//            return if (TypeEvaluation.resolving) TypeManager.types[token.left.value]!!.copy()
//            else TypeEvaluation.resolveTree(TypeManager.types[token.left.value]!!.copy())
//        }
    }

//    fun getVariable(token: Token, name: String, fileName: String = ""): Variable {
//        if (variables[name] == null && variables[name]!!.isEmpty())
//            throw PositionalException("", token)
//        if (fileName == "")
//            if (variables[name]!!.size == 1)
//                return variables[name]!![0]
//            else throw PositionalException("ambiguity", token)
//        else{
//            return variables[name]!!.find { it. }
//        }
//    }

    fun isEmpty(): Boolean = variables.isEmpty()

    fun addVariables(variables: List<Variable>, names: List<String>) {
        for (i in variables.indices)
            this.variables[names[i]] = variables[i]
    }

    fun getIdentifier(token: Token): Variable = variables[token.value] ?: getType(token)

    fun getVariableValues() = variables.values

    fun getVariables() = variables.toMutableMap()

    fun copy(): SymbolTable = SymbolTable(variables.toMutableMap())

    fun merge(symbolTable: SymbolTable): SymbolTable {
        return SymbolTable(
            variables.merge(symbolTable.variables),
            //(functions as MutableMap<String, Variable>).merge(symbolTable.variables) as MutableMap<String, Function>
        )
    }

    private fun MutableMap<String, Variable>.merge(other: MutableMap<String, Variable>): MutableMap<String, Variable> {
        for (v in other)
            this[v.key] = v.value
        return this
    }

    override fun toString(): String =
        "variables:${variables}"//"variables:${variables}${if (functions.isNotEmpty()) "\n functions:$functions" else ""}"

    fun toStringWithAssignments(assignments: List<Assignment>): String {
        if (assignments.isEmpty())
            return this.toString()
        var res = "variables:${variables}$"
        res = res.substring(0, res.length - 2)
        res += "${assignments.joinToString(separator = ",")}}"

        return res
    }

    fun getInvokableOrNull(token: Token): Invokable? {
        val res = try {
            getType(token)
        } catch (e: PositionalException) {
            try {
                getFunction(token)
            } catch (e: PositionalException) {
                return null
            }
        }
        return res
    }


    class Type(
        name: String,
        val typeName: String,
        parent: Type?,
        val assignments: MutableList<Assignment>,
        private val exported: Any? = null,
        private var type: Type? = null
    ) :
        Property(name, parent), Invokable {

        val symbolTable = baseSymbolTable()

        override fun toString(): String {
            return "$typeName${if (type != null) ":${type!!.typeName}" else ""}{parent:${parent ?: "-"}, ${
                symbolTable.toStringWithAssignments(
                    assignments
                )
            }${if (exported != null) ",to $exported" else ""}}"
        }

        fun copy(): Type {
            val copy =
                Type(
                    "",
                    typeName,
                    parent?.copy(),
                    assignments.map { it.copy() }.toMutableList(),
                    this.exported,
                    this.type
                )
            copy.assignments.forEach { it.parent = copy }
            return copy
        }

        private fun baseSymbolTable(): SymbolTable {
            if (parent == null)
                return SymbolTable()
            val vars = mutableMapOf<String, Variable>()
            vars["parent"] = parent
            return SymbolTable(vars)
        }

        fun getFirstUnresolved(token: Token): Pair<Type, String>? {
            var linkRoot = token
            var table = symbolTable
            var type = this
            while (linkRoot.value == ".") {
                val nextType = table.getVariableOrNull(linkRoot.left) ?: return Pair(type, linkRoot.left.value)
                if (nextType !is Type)
                    throw PositionalException("expected class instance, but primitive was found", linkRoot.left)
                type = nextType
                table = type.symbolTable
                linkRoot = linkRoot.right
            }
            return null
        }

        companion object {
            /**
             * similar to ValueEvaluation.evaluateLink()
             */
            fun getPropertyNameAndTable(token: Token, symbolTable: SymbolTable): Pair<String, SymbolTable> {
                var linkRoot = token
                var table = symbolTable
                while (linkRoot.value == ".") {
                    val type = table.getVariable(linkRoot.left)
                    if (type !is Type)
                        throw PositionalException("expected class", linkRoot.left)
                    linkRoot = linkRoot.right
                    table = type.symbolTable
                }
                return Pair(linkRoot.value, table)
            }

            var exported: Any? = null
            private var superType: Type? = null
            private var superTypes = mutableMapOf<Pair<String, String>, Token>()
            var name = ""

            fun initializeSuperTypes() {
                for ((pair, token) in superTypes) {
                    val (type, fileName) = pair
                    if (token.value == ".")
                        types[type]!![fileName]!!.type = types[token.right.value]!![token.left.value]
                    else {
                        val parents = types[token.value]!!.filter {
                            importMap[fileName]?.contains(it.key) ?: false
                                    || it.key == fileName
                        }
                        if (parents.isEmpty())
                            throw PositionalException("no superclass ${token.value} found", token)
                        if (parents.size > 1)
                            throw PositionalException(
                                "superclass ambiguity. There are ${parents.size} applicable supertypes in files ${parents.keys}",
                                token
                            )
                        types[type]!![fileName]!!.type = parents[parents.keys.first()]
                    }
                }
                superTypes.clear()
            }

            fun assignExported(token: Token): Token {
                if (token.value == "export") {
                    exported = token.right.value
                    return token.left
                } else
                    exported = null
                return token
            }

            fun assignType(token: Token, fileName: String): Token {
                if (token.value == ":") {
                    superTypes[Pair(token.left.value, fileName)] = token.right
                    return token.left
                } else
                    superType = null
                return token
            }

            fun assignName(token: Token) {
                name = token.value
            }
        }
    }

}