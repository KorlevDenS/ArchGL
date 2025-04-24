package domain.specific.lang.lexer

class LexerException(msg: String, val charPosition: Int) : Exception(msg)