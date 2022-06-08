package token

/**
 * Marks tokens that can be in [Link] at any place.
 *
 * This is checked during link creation with [lexer.Lexer.isLinkable]
 */
interface Linkable