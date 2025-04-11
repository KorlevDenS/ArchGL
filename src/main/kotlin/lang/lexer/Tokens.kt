package domain.specific.lang.lexer

val structureTypes: List<String> = listOf(
    "app",
    "data",
    "actor",
    "fr"
)

val actionWords: List<String> = listOf(
    "send", "to",
    "save",
    "read",
    "update",
    "delete",
    "work", "with",
    "generate",
    "return",
    "accept", "request", "from"
)

sealed class Token

data object ApplicationStruct : Token()
data object DataStruct : Token()
data object ActorStruct : Token()
data object FRStruct : Token()

data object SendAction : Token()
data object ToAction : Token()
data object SaveAction : Token()
data object ReadAction : Token()
data object DeleteAction : Token()
data object UpdateAction : Token()
data object WorkAction : Token()
data object WithAction : Token()
data object GenerateAction : Token()
data object ReturnAction: Token()
data object AcceptAction: Token()
data object RequestAction: Token()
data object FromAction : Token()

interface PropValue {
    val value: Any
}

data class StringValue(override val value: String) : Token(), PropValue
data class LongValue(override val value: Long) : Token(), PropValue
data class DoubleValue(override val value: Double) : Token(), PropValue

data class Property(val name: String) : Token()
data class StructureName(val name: String) : Token()

data object OpenCurlyBrace : Token()
data object CloseCurlyBrace : Token()
data object OpenRoundBrace : Token()
data object CloseRoundBrace : Token()
data object Comma : Token()

data object EOF : Token()

fun getStructToken(token: String): Token {
    return when (token) {
        "app" -> ApplicationStruct
        "data" -> DataStruct
        "actor" -> ActorStruct
        "fr" -> FRStruct
        else -> throw RuntimeException("Unexpected token $token")
    }
}

fun getActionToken(token: String): Token {
    return when (token) {
        "send" -> SendAction
        "to" -> ToAction
        "save" -> SaveAction
        "read" -> ReadAction
        "update" -> UpdateAction
        "delete" -> DeleteAction
        "work" -> WorkAction
        "with" -> WithAction
        "generate" -> GenerateAction
        "return" -> ReturnAction
        "accept" -> AcceptAction
        "request" -> RequestAction
        "from" -> FromAction
        else -> throw RuntimeException("Unexpected token $token")
    }
}