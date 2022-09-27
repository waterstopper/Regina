data class Optional(val value: Any? = null, var isGood: Boolean = false) {
    init {
        if (value != null) {
            isGood = true
        }
    }
}

data class Tuple4<T1, T2, T3, T4>(val first: T1, val second: T2, val third: T3, val fourth: T4)
