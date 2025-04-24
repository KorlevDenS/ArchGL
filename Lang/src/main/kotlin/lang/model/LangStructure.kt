package domain.specific.lang.model

interface LangStructure {
    var id: String
    var installedProps: MutableSet<String>
}