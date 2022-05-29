package evaluation

import lexer.Parser
import table.SymbolTable
import token.Token
import kotlin.random.Random

/**
 * Facade class for language execution
 */
object Evaluation {
    private const val SEED = 42
    val rnd = Random(SEED)
    var globalTable = SymbolTable()

    fun eval(code: String) {
        val statements = Parser(code).statements()
        val symbolTable = SymbolTable()
        for (stat in statements)
            stat.evaluate(symbolTable) // TODO here use some default symbol table
    }

    fun evaluate(tokens: List<Token>, fileName: String) {
        val main = globalTable.getMain()
        main.body.evaluate(globalTable)
    }
}