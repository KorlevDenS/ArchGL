package domain.specific.lang.parser

data class Data (

    override var id: String = "",

    var type: String = "",
    var retention: Long = 0,
    var unitVolume: Long = 0,

    override var installedProps: MutableSet<String> = mutableSetOf()

) : LangStructure {

    companion object {

        val DefaultRequest: Data = Data()

        val estimatedProps: List<String> = listOf(
            "type",
            "retention",
            "unitVolume"
        )

        val types: List<String> = listOf(
            "image",
            "text",
            "number",
            "html",
            "file",
            "video",
            "videoStream",
            "audio",
            "audioStream",
            "statusCode"
        )
    }


}
