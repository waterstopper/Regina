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

    fun eval(code: String, roots: List<String> = mutableListOf("")) {
        val fileTable = analyzeSemantics("@NoFile", roots, Parser(code).statements().map { it.toNode() })
        fileTable.getMain().body.evaluate(SymbolTable(fileTable = fileTable, resolvingType = false))
    }

    fun evaluate(fileName: String, roots: List<String> = mutableListOf("")) {
        val fileTable = analyzeSemantics(fileName, roots = roots)
        fileTable.getMain().body.evaluate(SymbolTable(fileTable = fileTable, resolvingType = false))
    }
}
