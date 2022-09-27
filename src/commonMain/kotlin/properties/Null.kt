package properties

import References
import lexer.PositionalException
import node.Node
import node.invocation.Call
import properties.primitive.PDictionary
import table.FileTable
import utils.Utils

class Null : Property(null) {
    override fun getPropertyOrNull(name: String): Property? {
        return null
    }

    override fun getProperty(node: Node, fileTable: FileTable): Property {
        TODO("Not yet implemented")
    }

    override fun getFunctionOrNull(node: Node): RFunction? {
        return RFunction.getFunctionOrNull(node as Call, functions)
    }

    override fun getFunction(node: Node, fileTable: FileTable): RFunction {
        return getFunctionOrNull(node) ?: throw PositionalException(
            "null does not contain function",
            fileTable.filePath,
            node
        )
    }

    override fun getProperties(): PDictionary {
        TODO("Not yet implemented")
    }

    override fun toDebugClass(references: References): Any {
        return Pair("Null", "")
    }

    override fun toString() = "null"

    companion object {
        private val functions = mutableListOf<RFunction>()

        fun initializeNullFunctions() {
            functions.add(
                EmbeddedFunction("toString") { token, args ->
                    val n = Utils.getPDictionary(args, token, "this")
                    n.getPValue().toString()
                }
            )
        }
    }
}
