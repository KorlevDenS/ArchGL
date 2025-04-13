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
    "work", "with", "obtaining",
    "generate",
    "return",
    "accept", "request", "from"
)

sealed class Token

data object ApplicationStruct : Token()
data object DataStruct : Token()
data object ActorStruct : Token()
data object FRStruct : Token()

interface ActionWord

data object SendAction : Token(), ActionWord
data object ToAction : Token(), ActionWord
data object SaveAction : Token(), ActionWord
data object ReadAction : Token(), ActionWord
data object DeleteAction : Token(), ActionWord
data object UpdateAction : Token(), ActionWord
data object WorkAction : Token(), ActionWord
data object WithAction : Token(), ActionWord
data object ObtainingAction : Token(), ActionWord
data object GenerateAction : Token(), ActionWord
data object ReturnAction: Token(), ActionWord
data object AcceptAction: Token(), ActionWord
data object RequestAction: Token(), ActionWord
data object FromAction : Token(), ActionWord

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
        "obtaining" -> ObtainingAction
        "generate" -> GenerateAction
        "return" -> ReturnAction
        "accept" -> AcceptAction
        "request" -> RequestAction
        "from" -> FromAction
        else -> throw RuntimeException("Unexpected token $token")
    }
}