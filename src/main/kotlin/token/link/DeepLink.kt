package token.link

class DeepLink(token: Link) : Link(
    token.symbol,
    token.value,
    token.position,
    token.bindingPower,
    token.nud,
    token.led,
    token.std,
    token.children
) {
    override fun getAfterDot() = right as Link
    override fun getLast() = (right as Link).getLast()
}