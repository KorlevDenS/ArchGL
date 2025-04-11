package domain.specific.lang.parser

class ParserException(msg: String, val tokenIndex: Int) : Exception(msg)