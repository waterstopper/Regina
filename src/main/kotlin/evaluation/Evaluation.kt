package evaluation

import table.SymbolTable
import token.Token
import kotlin.random.Random

object Evaluation {
    private const val SEED = 42
    val rnd = Random(SEED)
    var globalTable = SymbolTable()

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
        val main = globalTable.getMain()
        main.body.evaluate(globalTable)
        //println()
    }

//    fun evaluateInvocation(token: Token, symbolTable: SymbolTable): Any {
//        val invokable = symbolTable.getInvokable(token.left)
//        if (invokable is Function)
//            return evaluateFunction(
//                token,
//                invokable,
//                token.children.subList(1, token.children.size),
//                symbolTable
//            )
//        else return if (resolving) invokable as Type else resolveTree(invokable as Type)
////        return if (symbolTable.findFunction(token.left.value) != null)
////            evaluateFunction(
////                token, symbolTable.findFunction(token.left.value)!!,
////                token.children.subList(1, token.children.size), symbolTable
////            )
////        else {
////            // bad for properties inside class. Need to create some global variable in TypeEvaluation
////            // if it is true then resolve tree, else just add type-property to existing instance
////            return if (resolving) types[token.left.value]!!.copy()
////            else resolveTree(types[token.left.value]!!.copy())
////        }
//    }

//    private fun evaluateType(token: Token) {
//        getTypeName(token.children[0])
//        interpretClassBlock(token.children[1])
//    }
}