package properties

import node.Identifier
import node.Node
import node.invocation.Call
import node.statement.Assignment

/**
 * Function's signature is:
 * 1. Name
 * 2. Overall number of parameters (with and without default parameters)
 */
open class RFunction(
    val name: String,
    val nonDefaultParams: List<Identifier>,
    val defaultParams: List<Assignment>,
    val body: Node
) {
    override fun toString(): String = "$name(${nonDefaultParams.joinToString(separator = ",")})"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RFunction) return false
        if ((nonDefaultParams.size + defaultParams.size) != (other.nonDefaultParams.size + other.defaultParams.size)) {
            return false
        }
        if (name != other.name) return false
        return true
    }

    fun hasParam(name: String): Boolean =
        nonDefaultParams.any { it.value == name } || defaultParams.any { it.name == name }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + (nonDefaultParams.size + defaultParams.size).hashCode()
        return result
    }

    companion object {
        /**
         * Filter all functions that have a same name and not more than [call].children non-default params
         */
        fun getFunctionOrNull(call: Call, functions: Iterable<RFunction>): RFunction? {
            var candidates = functions.filter {
                it.name == call.name.value &&
                    it.nonDefaultParams.size <= call.allArgs.size &&
                    it.nonDefaultParams.size + it.defaultParams.size >= call.allArgs.size
            }
            var unnamedArgsRemoved = 0
            // filter functions by named arguments - if there is name argument
            // that is absent in a function declaration [cand], remove that candidate
            candidates = candidates.filter { cand ->
                unnamedArgsRemoved = 0
                call.namedArgs.all { arg ->
                    if (cand.nonDefaultParams.find { it.value == arg.name } != null) {
                        unnamedArgsRemoved++
                    }
                    cand.hasParam(arg.name)
                } && cand.nonDefaultParams.size - unnamedArgsRemoved <= call.unnamedArgs.size
            }
            return candidates.minByOrNull { it.defaultParams.size + it.nonDefaultParams.size }
        }
    }
}
