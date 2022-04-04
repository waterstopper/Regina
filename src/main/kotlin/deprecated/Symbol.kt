package deprecated

/**
 * Order is important for lexer!
 */
@Deprecated("")
enum class Symbol(private val content: Regex) {
    REF(Regex("([A-Za-z]\\w*)(\\.[A-Za-z]\\w*)*")), // node_1.another_2.other_3

    // STR(Regex("@\\w+")), // @_123_n_o_d_e_123_ NOT NEEDED RN

    PRE(Regex("\\w+\\[")), // resolves as ref probably: points[iter] -> points1 (if iter==1)
    SUB(Regex("-")), // -
    NUM(Regex("\\d*(\\.\\d*)?")),
    FUN(Regex("[A-Za-z]+\\(")),
    LEFT_SQR(Regex("\\[")),
    RIGHT_SQR(Regex("]")),
    LEFT_PAR(Regex("\\(")),
    RIGHT_PAR(Regex("\\)")),
    SEP(Regex("[,;]")),

    MUL(Regex("\\*")), // *
    INT_DIV(Regex("//")), // //
    DIV(Regex("/")), // /
    ADD(Regex("\\+")), // +


    MORE_EQUAL(Regex(">=")), // >=
    LESS_EQUAL(Regex("<=")), // <=
    MORE(Regex(">")), // >
    LESS(Regex("<")), // <
    EQUAL(Regex("==")), // ==
    UNEQUAL(Regex("!=")), // !=
    ASSIGN(Regex("=")), // =

    LAND(Regex("&")), // &
    LOR(Regex("\\|")), // |

    COND(Regex("\\?")), // ?
    DIVIDER(Regex(":")); // :

    companion object {
        fun getToken(str: String): Symbol? =
            values().find { str.matches(it.content) }
    }
}