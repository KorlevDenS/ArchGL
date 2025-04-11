package domain.specific.lang.parser

data class Application (

    override var id: String = "",

    var scaleVertically: String = "no",
    var scaleHorizontally: String = "no",
    var usersNumber: Long = 0,
    var dayUsersNumber: Long = 0,
    var latency: Double = 0.0,
    var availability: Double = 0.0,

    override var installedProps: MutableSet<String> = mutableSetOf(),

    val actors: MutableList<Actor> = mutableListOf(),
    val data: MutableList<Data> = mutableListOf(),
    val frs: MutableList<FR> = mutableListOf()

): LangStructure {

    companion object {

        val estimatedProps: List<String> = listOf(
            "scaleVertically",
            "scaleHorizontally",
            "usersNumber",
            "dayUsersNumber",
            "latency",
            "availability",
        )

    }

}
