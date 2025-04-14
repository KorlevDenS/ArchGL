package domain.specific.lang.lexer

import domain.specific.lang.model.*

class Lexer(
    private val input: String
) {

    private var position = 0

    private fun readNumber(): Token {
        val start = position
        while (position < input.length && input[position].isDigit()) {
            position++
        }
        if (position + 1 < input.length && input[position] == '.' && input[position + 1] in '0'..'9') {
            position++
            while (position < input.length && input[position].isDigit()) {
                position++
            }
            return DoubleValue(input.substring(start, position).toDouble())
        } else {
            return LongValue(input.substring(start, position).toLong())
        }
    }

    private fun readStringValue(): String {
        val start = position
        position++
        while (position < input.length && input[position] != '"') {
            position++
        }
        if (position == input.length) {
            throw LexerException("Expected closing quotation mark", start)
        } else {
            val res = if (position == start + 1) {
                ""
            } else {
                input.substring(start + 1, position)
            }
            position++
            return res
        }
    }

    private fun readStringName(): String {
        val start = position
        while (
            position < input.length && (input[position] in 'A'..'Z' || input[position] in 'a'..'z' ||
                    input[position] in '0'..'9' || input[position] == '_')
        ) {
            position++
        }
        return input.substring(start, position)
    }

    fun nextToken(): Pair<Int, Token> {
        val tokenBegin: Int
        while (position < input.length) {

            if (input[position].isWhitespace()) {
                position++
                continue
            }

            when (input[position]) {
                '{' -> {
                    tokenBegin = position
                    position++
                    return Pair(tokenBegin, OpenCurlyBrace)
                }

                '}' -> {
                    tokenBegin = position
                    position++
                    return Pair(tokenBegin, CloseCurlyBrace)
                }

                '(' -> {
                    tokenBegin = position
                    position++
                    return Pair(tokenBegin, OpenRoundBrace)
                }

                ')' -> {
                    tokenBegin = position
                    position++
                    return Pair(tokenBegin, CloseRoundBrace)
                }

                ',' -> {
                    tokenBegin = position
                    position++
                    return Pair(tokenBegin, Comma)
                }

                in '0'..'9' -> {
                    tokenBegin = position
                    return Pair(tokenBegin, readNumber())
                }

                '"' -> {
                    tokenBegin = position
                    return Pair(tokenBegin, StringValue(readStringValue()))
                }

                in 'A'..'Z' -> {
                    tokenBegin = position
                    return Pair(tokenBegin, StructureName(readStringName()))
                }

                in 'a'..'z' -> {
                    tokenBegin = position
                    val str: String = readStringName()
                    if (position < input.length && input[position] == ':') {
                        position++
                        return Pair(tokenBegin, Property(str))
                    }
                    return when (str) {
                        in structureTypes -> Pair(tokenBegin, getStructToken(str))
                        in actionWords -> Pair(tokenBegin, getActionToken(str))
                        else -> throw LexerException("Unexpected expression", tokenBegin)
                    }
                }

                else -> throw LexerException("Unexpected character", position)
            }
        }
        return Pair(input.lastIndex, EOF)
    }

}