package rand

class RandList(var list: List<Any> = mutableListOf(), var amount: Int = 0) : Rand() {

    override fun evaluate(): List<Any> = list.shuffled(Global.random).take(amount)
}