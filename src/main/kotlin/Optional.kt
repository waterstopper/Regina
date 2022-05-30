data class Optional(val value: Any? = null, var isGood: Boolean = false) {
    init {
        if (value != null)
            isGood = true
    }
}