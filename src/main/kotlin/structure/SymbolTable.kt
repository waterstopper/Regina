package structure

import evaluation.FunctionEvaluation.initializeEmbedded
import lexer.PositionalException
import lexer.Token
import properties.*
import properties.Function

class SymbolTable(
    private val variables: MutableMap<String, Variable> = mutableMapOf(),
) {
    var currentFile: String = ""

    companion object {
        private val importMap: MutableMap<String, MutableSet<String>> = mutableMapOf()
        private val types: MutableMap<String, MutableMap<String, Type>> = mutableMapOf()
        private val functions: MutableMap<String, MutableMap<String, Function>> = mutableMapOf()
        private val objects: MutableMap<String, MutableMap<String, Object>> = mutableMapOf()
        private val embedded: MutableMap<String, Function> = initializeEmbedded()
    }

    fun getImportOrNull(to: String, from: String) = importMap[to]?.contains(from) ?: false

    fun addImport(importedTo: String, importedFrom: String) {
        importMap[importedTo]?.add(importedFrom) ?: mutableSetOf(importedFrom)
    }

    private fun getDeclared(nameToken: Token, map: MutableMap<String, MutableMap<String, Invokable>>): Invokable {
        if (nameToken.value == ".") {
            if (nameToken.left.value == currentFile)
                return map[nameToken.right.value]?.get(currentFile) ?: throw PositionalException(
                    "identifier not found",
                    nameToken.right
                )
            return map[nameToken.right.value]?.get(nameToken.left.value) ?: throw PositionalException(
                "identifier not found",
                nameToken.right
            )
        }
        val declarations =
            map[nameToken.value] ?: throw PositionalException("identifier ${nameToken.value} not found", nameToken)
        val filtered =
            declarations.filter { importMap[currentFile]?.contains(it.key) ?: false || it.key == currentFile }
        if (filtered.isEmpty())
            throw PositionalException("identifier ${nameToken.right.value} not found", nameToken.right)
        if (filtered.size > 1)
            throw PositionalException("import ambiguity. Such identifier found in ${filtered.keys}")
        return filtered.values.first()
    }

    // TODO rewrite bullshit code
    fun getType(name: String): Type? = types[name]?.get(currentFile)


    fun getType(token: Token): Type {
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
        TypeManager.assignName(TypeManager.assignType(TypeManager.assignExported(token.left)))
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
        val added = Type("", TypeManager.name, TypeManager.superType, null, res, TypeManager.exported)
        if (types[TypeManager.name] == null)
            types[TypeManager.name] = mutableMapOf(fileName to added)
        types[TypeManager.name]!![fileName] = added
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

    fun getVariables() = variables.values

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
        "variables:${variables}"//"variables:${variables}${if (functions.isNotEmpty()) "\nfunctions:$functions" else ""}"

    fun toStringWithAssignments(assignments: List<Assignment>): String {
        if (assignments.isEmpty())
            return this.toString()
        var res = "variables:${variables}$"
        res = res.substring(0, res.length - 2)
        res += "${assignments.joinToString(separator = ",")}}"

        return res
    }

    object TypeManager {
        var exported: Any? = null
        var superType: Type? = null
        var superTypes = mutableMapOf<String, String>()
        var name = ""

        fun assignExported(token: Token): Token {
            if (token.value == "export") {
                exported = token.right.value
                return token.left
            } else
                exported = null
            return token
        }

        fun assignType(token: Token): Token {
            if (token.value == ":") {
                return token.left
            } else
                superType = null
            return token
        }

        fun assignName(token: Token) {
            name = token.value
        }

        fun getName(token: Token): String {
            var t = token
            while (t.children.isNotEmpty())
                t = t.left
            return t.value
        }

//        fun resolvedSupertype(token: Token): String {
//            var t = token
//            while (t.children.isNotEmpty()) {
//                t = t.left
//                if (t.value == ":") {
//                    return if (find(t.right.value) == null)
//                        t.right.value
//                    else ""
//                }
//            }
//            return ""
//        }
    }
}