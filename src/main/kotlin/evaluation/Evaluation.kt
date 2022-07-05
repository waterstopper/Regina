package evaluation

import lexer.Parser
import lexer.SemanticAnalyzer
import lexer.SemanticAnalyzer.Companion.clearAnalyzer
import table.SymbolTable
import table.SymbolTable.Companion.clearTable
import token.Token

/**
 * Facade class for language execution
 */
object Evaluation {
    /**
     * Enabling training wheels will run code slower but will give meaningful feedback if something is wrong.
     */
    var trainingWheels = true
    var globalTable = SymbolTable()

    fun eval(code: String) {
        val statements = SemanticAnalyzer("@NoFile", Parser(code).statements()).analyze()
        SemanticAnalyzer.initializeSuperTypes()
        val main = globalTable.getMain()
        main.body.evaluate(globalTable)
//        for (stat in statements)
//            stat.evaluate(symbolTable) // TODO here use some default symbol table
        clear()
    }

    fun evaluate(fileName: String) {}

    @Deprecated("bad signature with tokens")
    fun oldEvaluate(tokens: List<Token>, fileName: String) {
        val main = globalTable.getMain()
        main.body.evaluate(globalTable)
        clear()
    }

    fun clear() {
        clearTable()
        clearAnalyzer()
    }
}
