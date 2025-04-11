package domain.specific.lang.parser

data class FR (

    override var id: String = "",

    var frequency: Long = 0,
    var actions: MutableList<Action> = mutableListOf(),

    override var installedProps: MutableSet<String> = mutableSetOf()

) : LangStructure {

    companion object {

        val estimatedProps: List<String> = listOf(
            "frequency",
            "actions"
        )

    }

}
