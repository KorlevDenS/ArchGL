package domain.specific.archgateway.controller

import domain.specific.archgateway.data.InfoResponse
import domain.specific.lang.generator.StructureException
import domain.specific.lang.lexer.LexerException
import domain.specific.lang.parser.ParserException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class AppAdvice {

    @ExceptionHandler(LexerException::class)
    fun handleLexerException(e: LexerException): ResponseEntity<InfoResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(InfoResponse(400, (e.message ?: "Unknown lexer error") + " on position " + e.charPosition))
    }

    @ExceptionHandler(ParserException::class)
    fun handleParserException(e: ParserException): ResponseEntity<InfoResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(InfoResponse(400, (e.message ?: "Unknown parser error") + " on position " + e.tokenIndex))
    }

    @ExceptionHandler(StructureException::class)
    fun handleStructureException(e: StructureException): ResponseEntity<InfoResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(InfoResponse(400, e.message ?: "Unknown structure error"))
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<InfoResponse> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(InfoResponse(500, e.message ?: "Unknown server error"))
    }

}
