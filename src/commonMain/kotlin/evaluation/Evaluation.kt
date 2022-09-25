package evaluation

import lexer.Parser
import lexer.analyzeSemantics
import node.invocation.ResolvingMode
import table.SymbolTable

/**
 * Facade class for language execution
 */
object Evaluation {
    /**
     * Enabling training wheels will run code slower but will give meaningful feedback if something is wrong.
     */
    var trainingWheels = true

    fun eval(code: String, roots: List<String> = mutableListOf("")) {
        val fileTable =
            analyzeSemantics("@NoFile",
                roots,
                Parser(code, "@NoFile").statements().map { it.toNode("@NoFile") })
        fileTable.getMain().body.evaluate(SymbolTable(fileTable = fileTable, resolvingType = ResolvingMode.FUNCTION))
    }

    fun evaluate(fileName: String, roots: List<String> = mutableListOf("")) {
        val fileTable = analyzeSemantics(fileName, roots = roots)
        fileTable.getMain().body.evaluate(SymbolTable(fileTable = fileTable, resolvingType = ResolvingMode.FUNCTION))
    }
}
