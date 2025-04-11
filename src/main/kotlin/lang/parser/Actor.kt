package domain.specific.lang.parser

data class Actor(

    override var id: String = "",

    var type: String = "",

    override var installedProps: MutableSet<String> = mutableSetOf()

) : LangStructure {

    companion object {

        val AppItself: Actor = Actor()

        val estimatedProps: List<String> = listOf(
            "type"
        )

        val types: List<String> = listOf(
            "service",
            "web-client"
        )

    }

}
