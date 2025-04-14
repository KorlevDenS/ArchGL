package domain.specific.lang.model

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

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("Application: $id\n")
        sb.append("scaleVertically: $scaleVertically\n")
        sb.append("scaleHorizontally: $scaleHorizontally\n")
        sb.append("usersNumber: $usersNumber\n")
        sb.append("dayUsersNumber: $dayUsersNumber\n")
        sb.append("latency: $latency\n")
        sb.append("availability: $availability")
        sb.append("installedProps: $installedProps\n")
        sb.append("actors: $actors\n")
        sb.append("data: $data\n")

        sb.append("frs: [\n")
        for (fr in frs) {
            sb.append("FR ${fr.id}: (\n")
            sb.append("  frequency: ${fr.frequency}\n")
            sb.append("  installed props: ${fr.installedProps}\n")
            sb.append("  actions: [\n")
            for (act in fr.actions) {
                sb.append("    $act\n")
            }
            sb.append("  ]\n")
            sb.append("),\n")
        }
        sb.append("]\n")
        return sb.toString()
    }


}
