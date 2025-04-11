package domain.specific.lang.parser

interface LangStructure {
    var id: String
    var installedProps: MutableSet<String>
}