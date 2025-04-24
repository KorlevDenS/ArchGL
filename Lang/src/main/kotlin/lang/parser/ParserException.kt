package domain.specific.lang.parser

class ParserException(msg: String, var tokenIndex: Int) : Exception(msg)