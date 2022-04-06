package evaluation

import evaluation.FunctionEvaluation.createFunction
import evaluation.FunctionEvaluation.evaluateBlock
import evaluation.FunctionEvaluation.evaluateFunction
import evaluation.FunctionEvaluation.functions
import evaluation.TypeEvaluation.resolveTree
import evaluation.TypeManager.addType
import evaluation.TypeManager.types
import lexer.PositionalException
import lexer.Token
import structure.SymbolTable
import java.util.*
import kotlin.random.Random

object Evaluation {
    private const val SEED = 42
    val rnd = Random(SEED)
    val globalTable = SymbolTable(mutableMapOf(), functions)
    private lateinit var declarations: List<Token>

    private fun initializeObjects() {
        declarations.filter { it.value == "object" }.forEach { TypeManager.addObject(it) }
    }

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

    fun evaluate(tokens: List<Token>) {
        for (token in tokens)
            when (token.symbol) {
                "fun" -> {
                    val func = createFunction(token, null)
                    globalTable.functions[token.children[0].children[0].value] = func
                }
                "class" -> addType(token)
                "object" -> {
                }
                else -> throw PositionalException("class or function can be top level declaration", token)
            }

        val main = globalTable.functions["main"] ?: throw Exception("no main function")
        evaluateBlock(main.body, globalTable)
        println()
    }

    fun evaluateInvocation(token: Token, symbolTable: SymbolTable): Any {
        return if (symbolTable.findFunction(token.children[0].value) != null)
            evaluateFunction(
                token, symbolTable.findFunction(token.children[0].value)!!,
                token.children.subList(1, token.children.size), symbolTable
            )
        else {
            return types[token.children[0].value]!!.copy()
        }
    }

//    private fun evaluateType(token: Token) {
//        getTypeName(token.children[0])
//        interpretClassBlock(token.children[1])
//    }

    private fun initializeTypes() {
        val stack = Stack<Token>()
        val classDeclarations = declarations.filter { it.symbol == "class" }.toMutableList()
        while (true) {
            if (stack.isEmpty()) {
                if (classDeclarations.isEmpty())
                    break
                stack.push(classDeclarations.first())
                classDeclarations.removeAt(0)
            }
            while (stack.isNotEmpty()) {
                val typeToken = stack.pop()
                val supertypeName = TypeManager.resolvedSupertype(typeToken)
                if (supertypeName == "")
                    addType(typeToken)
                else {
                    val foundSupertype = classDeclarations.find { TypeManager.getName(it) == supertypeName }
                        ?: throw Exception("no class with name $supertypeName")
                    stack.push(typeToken)
                    stack.push(foundSupertype)
                    classDeclarations.remove(foundSupertype)
                }
            }
        }
    }
}