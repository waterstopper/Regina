package evaluation

import lexer.Parser
import lexer.analyzeSemantics
import table.SymbolTable

/**
 * Facade class for language execution
 */
object Evaluation {
    /**
     * Enabling training wheels will run code slower but will give meaningful feedback if something is wrong.
     */
    var trainingWheels = true
    var globalTable = SymbolTable(resolvingType = false)

    fun eval(code: String) {
        val fileTable = analyzeSemantics("@NoFile", Parser(code).statements().map { it.toNode() })
        fileTable.getMain().body.evaluate(SymbolTable(fileTable = fileTable, resolvingType = false))
    }

    fun evaluate(fileName: String) {
        val fileTable = analyzeSemantics(fileName)
        fileTable.getMain().body.evaluate(SymbolTable(fileTable = fileTable, resolvingType = false))
    }
}
