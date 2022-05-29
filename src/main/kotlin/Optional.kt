data class Optional(val value: Any? = null, var isGood: Boolean = true) {
    init {
        if (value == null)
            isGood = false
    }
}