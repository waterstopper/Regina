package evaluation

import evaluation.FunctionEvaluation.createFunction
import evaluation.FunctionEvaluation.evaluateBlock
import evaluation.FunctionEvaluation.evaluateFunction
import evaluation.TypeEvaluation.resolveTree
import evaluation.TypeEvaluation.resolving
import lexer.PositionalException
import lexer.Token
import properties.Function
import properties.Type
import readFile
import structure.SymbolTable
import java.util.*
import kotlin.random.Random

object Evaluation {
    private const val SEED = 42
    val rnd = Random(SEED)
    val globalTable = SymbolTable(mutableMapOf())
    private lateinit var declarations: List<Token>

//    private fun initializeObjects() {
//        declarations.filter { it.value == "object" }.forEach { TypeManager.addObject(it) }
//    }

//    private fun initializeFunctions() {
//        val names = mutableSetOf<Pair<String, Int>>()
//        declarations.filter { it.value == "fun" }.forEach {
//            FunctionEvaluation.addFunction(it)
//            if (names.add(Pair(functions.last().name, functions.last().args.size)))
//                throw PositionalException(
//                    "Two functions with same name: ${functions.last().name}" +
//                            " and number of arguments: ${functions.last().args.size}", functions.last().body
//                )
//        }
//    }

    fun evaluate(tokens: List<Token>, fileName: String) {
        val queue = ArrayDeque<Pair<Token, String>>()
        queue.addAll(tokens.map { Pair(it, fileName) })
        while (queue.isNotEmpty()) {
            val (token, currentFileName) = queue.pop()
            when (token.symbol) {
                "fun" -> globalTable.addFunction(createFunction(token), currentFileName)
                "class" -> globalTable.addType(token, currentFileName)
                "object" -> {
                }
                "import" -> {
                    if (!globalTable.getImportOrNull(fileName, token.left.value)) {
                        globalTable.addImport(fileName, token.left.value)
                        queue.addAll(readFile(tokenPath = token.left).map { Pair(it, token.left.value) })
                    }
                    /**
                     * TODO: warn about this code (doubling imports):
                     * import abc
                     * import abc
                     * ...
                     */
                }
                else -> throw PositionalException("class or function can be top level declaration", token)
            }
        }
        //initializeSuperTypes()
        globalTable.currentFile = fileName
        val main = globalTable.getMain()
        evaluateBlock(main.body, globalTable)
        println()
    }

    fun evaluateInvocation(token: Token, symbolTable: SymbolTable): Any {
        val invokable = symbolTable.getInvokable(token.left)
        if (invokable is Function)
            return evaluateFunction(
                token,
                invokable,
                token.children.subList(1, token.children.size),
                symbolTable
            )
        else return if(resolving) invokable as Type else resolveTree(invokable as Type)
//        return if (symbolTable.findFunction(token.left.value) != null)
//            evaluateFunction(
//                token, symbolTable.findFunction(token.left.value)!!,
//                token.children.subList(1, token.children.size), symbolTable
//            )
//        else {
//            // bad for properties inside class. Need to create some global variable in TypeEvaluation
//            // if it is true then resolve tree, else just add type-property to existing instance
//            return if (resolving) types[token.left.value]!!.copy()
//            else resolveTree(types[token.left.value]!!.copy())
//        }
    }

//    private fun evaluateType(token: Token) {
//        getTypeName(token.children[0])
//        interpretClassBlock(token.children[1])
//    }

//    private fun initializeSuperTypes() {
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