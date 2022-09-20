package properties.primitive

import References
import node.Node
import properties.Property
import properties.RFunction
import table.FileTable

class Null : Property(null) {
    override fun getPropertyOrNull(name: String): Property? {
        return null
    }

    override fun getProperty(node: Node, fileTable: FileTable): Property {
        TODO("Not yet implemented")
    }

    override fun getFunctionOrNull(node: Node): RFunction? {
        TODO("Not yet implemented")
    }

    override fun getFunction(node: Node, fileTable: FileTable): RFunction {
        TODO("Not yet implemented")
    }

    override fun getProperties(): PDictionary {
        TODO("Not yet implemented")
    }

    override fun toDebugClass(references: References): Any {
        return Pair("Null", "")
    }
}