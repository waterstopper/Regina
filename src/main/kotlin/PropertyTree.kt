import kotlin.reflect.KProperty1

class PropertyTree(private val properties: MutableList<KProperty1<Any, Formula>>, private val obj: Any) {

    private val used = MutableList(properties.size) { false }
    private val g = MutableList(properties.size) { mutableListOf<Int>() }
    private val ans = mutableListOf<KProperty1<Any, Formula>>()

    init {
        properties.removeIf { it.name == "initQueue" || it.name == "resolvedFields" }
    }

    fun getPropertyTree(): List<KProperty1<Any, Formula>> {
        buildGraph()
        topSort()
        return ans
    }

    private fun buildGraph() {
        for (i in properties.indices) {
            g[i] = properties[i].getter(obj).getDependencies(properties)
                .map { properties.indexOfFirst { i -> i == it } } as MutableList<Int>
        }
    }

    private fun dfs(v: Int) {
        used[v] = true
        for (i in 0 until g[v].size) {
            val to: Int = g[v][i]
            if (!used[to]) dfs(to)
        }
        ans.add(properties[v])
    }

    private fun topSort() {
        for (i in properties.indices) used[i] = false
        ans.clear()
        for (i in properties.indices) if (!used[i]) dfs(i)
        //ans.reverse()
    }
}