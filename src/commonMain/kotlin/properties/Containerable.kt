package properties

import node.statement.Assignment

/**
 * Might contain other instances with unresolved assignments
 */
interface Containerable {
    fun getCollection(): Collection<Variable>
    fun getContainerId(): Int
}