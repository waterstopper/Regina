# Thoughts on formatting implementation

There is a major restriction to formatting: formatting should not be happening within a file, because user might not
want save format (or memento pattern should be on use to save previous code versions - for now it is delegated to ide's)
.

Obvious choice is walking over all tokens in a syntax tree, but there is a problem too: not all symbols map to tokens.
For instance, expression parentheses and closed brackets do not.