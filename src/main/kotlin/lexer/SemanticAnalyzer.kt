package lexer

import Logger
import evaluation.Evaluation.globalTable
import evaluation.FunctionEvaluation
import readFile
import table.SymbolTable
import token.Token
import token.TokenDeclaration
import token.TokenFactory.Companion.createSpecificIdentifierFromInvocation
import token.link.Link

class SemanticAnalyzer(private val fileName: String, private val tokens: List<Token>) {
    private var declarations = mutableListOf<Pair<TokenDeclaration, String>>()

    fun analyze(): List<Token> {
        println("Analyzing:$fileName")
        createAssociations()
        // TODO do the same for all imports
        changeIdentTokens(fileName)
        return tokens
    }

    private fun createAssociations() {
        globalTable.addFile(fileName)
        globalTable = globalTable.changeFile(fileName)
        for (token in tokens)
            when (token.symbol) {
                "fun" -> globalTable.addFunction(FunctionEvaluation.createFunction(token))
                "class" -> {
                    declarations.add(Pair(token as TokenDeclaration, fileName))
                    globalTable.addType(token)
                }
                "object" -> globalTable.addObject(token)
                "import" -> {
                    if (globalTable.getImportOrNull(token.left.value) == null) {
                        val isNewFile = globalTable.addFile(token.left.value)
                        globalTable.addImport(token.left)
                        if (isNewFile) {
                            readFile(token.left.value)
                            globalTable = globalTable.changeFile(fileName)
                        }
                    } else Logger.addWarning(token.left, "Same import found above")
                }
                else -> throw PositionalException("class or function can be top level declaration", token)
            }

        // TODO implement
        // initializeSuperTypes()
    }

    private fun changeIdentTokens(fileName: String) {
        for (token in tokens) {
            var table = globalTable.copy()
            if (token.symbol == "fun")
                table = table.changeScope()
            else if (token.symbol == "object")
                table = table.changeType(globalTable.getObjectOrNull((token as TokenDeclaration).name)!!)
            else if (token.symbol == "class")
                table = table.changeType(globalTable.getTypeOrNull((token as TokenDeclaration).name)!!)
            changeTokenType(token, table, 0)
        }
    }

    private fun changeTokenType(token: Token, symbolTable: SymbolTable, linkLevel: Int) {
        for ((index, child) in token.children.withIndex()) {
            when (child.symbol) {
                // ignoring assignments like: a.b = ...
                "(ASSIGNMENT)" -> symbolTable.addVariableOrNot(child.left)
                "(" -> {
                    if (token.value != "fun") {
                        token.children[index] =
                            createSpecificIdentifierFromInvocation(child, symbolTable, linkLevel, token)
                    }
                }
                // "[]" -> token.children[index] = TokenArray(child)
                //"[" -> token.children[index] = TokenIndexing(child)
            }
            changeTokenType(
                token.children[index],
                if (token.children[index].symbol == "fun") symbolTable.changeScope() else symbolTable,
                if (token.children[index] is Link) linkLevel + 1 else 0
            )
        }
    }

    private fun checkIntersections(tokens: List<Token>) {
        val classes = mutableSetOf<String>()
        val functions = SymbolTable.getEmbeddedNames()
        for (token in tokens) {
            when (token.symbol) {
                "class" -> {
                    classes.add((token as TokenDeclaration).name.value)
                }
                "fun" -> {
                    val added = functions.add(token.left.left.value)
                    if (!added)
                        throw  PositionalException(
                            if (SymbolTable.getEmbeddedNames()
                                    .contains(functions.last())
                            ) "reserved function name" else "same function name within one file", token.left.left
                        )
                    checkParams(token.left.children.subList(1, token.left.children.size))
                }
            }
        }
        val intersections = classes.intersect(functions)
        if (intersections.isNotEmpty())
            throw PositionalException("$fileName contains functions and classes with same names: $intersections")
    }


    private fun checkParams(params: List<Token>) {
        for (param in params)
            if (param.symbol != "(IDENT)") throw PositionalException("expected identifier as function parameter", param)
    }

    /**
     * constructor params are assignments, because of the dynamic structure of type
     */
    private fun checkConstructorParams(params: List<Token>) {
        for (param in params)
            if (param.value != "=") throw PositionalException(
                "expected assignment as constructor parameter",
                param
            )
    }

//    private fun getTypeNameAndSuperTypeToken(token: Token):Pair<String,Token?>{
//
//    }

    fun initializeSuperTypes() {
        val types = globalTable.getTypes()
        for ((typeToken, fileName) in declarations) {
//            val token = typeToken.export
//            val typeName = token.left
//            val superTypeName = if (token.right is TokenLink) token.right else token.right

        }
    }

//    private fun initializeSuperTypes() {
//        for (type in globalTable.getTypes())
//        val stack = Stack<Type>()
//        val classDeclarations = types.values.toMutableList()
//        while (true) {
//            if (stack.isEmpty()) {
//                if (classDeclarations.isEmpty())
//                    break
//                stack.push(classDeclarations.first())
//                classDeclarations.removeAt(0)
//            }
//            while (stack.isNotEmpty()) {
//                val type = stack.pop()
//                //val supertypeName = TypeManager.resolvedSupertype(type)
//                if (supertypeName == "")
//                // addType(type)
//                else {
//                    val foundSupertype = classDeclarations.find { TypeManager.getName(it) == supertypeName }
//                        ?: throw Exception("no class with name $supertypeName")
//                    stack.push(type)
//                    stack.push(foundSupertype)
//                    classDeclarations.remove(foundSupertype)
//                }
//            }
//        }
//    }
}