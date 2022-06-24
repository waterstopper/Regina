package properties

import token.Token
import token.invocation.Call
import token.statement.Assignment

/**
 * Function's signature is:
 * 1. Name
 * 2. Overall number of parameters (with and without default)
 */
open class Function(
    val name: String,
    val nonDefaultParams: List<Token>,
    val defaultParams: List<Assignment>,
    val body: Token
) {
    override fun toString(): String = "$name(${nonDefaultParams.joinToString(separator = ",")})"

    // TODO wtf is this equals
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Function) return false
        if ((nonDefaultParams.size + defaultParams.size) != (other.nonDefaultParams.size + other.defaultParams.size))
            return false
        if (name != other.name) return false
        return true
    }

    fun hasParam(name: String): Boolean =
        nonDefaultParams.any { it.value == name } || defaultParams.any { it.name == name }

    /**
     * We should not consider default parameters, because calling functions
     * `fun someFun` and `fun someFun(a=0)` can be achieved like: `someFun()` - and there is no way to distinct those
     */
    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + (nonDefaultParams.size + defaultParams.size).hashCode()
        return result
    }

    companion object {
        /**
         * Filter all functions that have a same name and not more than [call].children non-default params
         */
        fun getFunctionOrNull(call: Call, functions: Iterable<Function>): Function? {
            var candidates = functions.filter {
                it.name == call.name.value &&
                        it.nonDefaultParams.size <= call.allArgs.size &&
                        it.nonDefaultParams.size + it.defaultParams.size >= call.allArgs.size
            }
            var unnamedArgsRemoved = 0
            // filter functions by named arguments - if there is name argument
            // that is absent in a function declaration [fitF], remove that candidate
            candidates = candidates.filter { candidate ->
                call.namedArgs.all { arg ->
                    unnamedArgsRemoved = 0
                    if (candidate.nonDefaultParams.find { it.value == arg.value } != null)
                        unnamedArgsRemoved++
                    candidate.hasParam(arg.name)
                } && candidate.nonDefaultParams.size - unnamedArgsRemoved <= call.unnamedArgs.size
            }
            return candidates.minByOrNull { it.defaultParams.size + it.nonDefaultParams.size }
            // ?: throw PositionalException("Function `${call.name}` not found", call)
        }
    }
}
