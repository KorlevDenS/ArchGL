package domain.specific.lang.parser

import domain.specific.lang.lexer.*
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.jvmErasure

class Parser(
    private val input: MutableList<Token>
) {

    private var position = 0
    private var checkSize = 0

    private val app: Application = Application()

    private fun setProperty(struct: LangStructure, propName: String, propValue: Any) {
        when(struct) {
            is Application -> if (propName !in Application.estimatedProps) {
                throw AttrException("No attribute $propName in app struct!")
            }
            is Actor -> if (propName !in Actor.estimatedProps) {
                throw AttrException("No attribute $propName in actor struct!")
            }
            is Data -> if (propName !in Data.estimatedProps) {
                throw AttrException("No attribute $propName in data struct!")
            }
            is FR -> if (propName !in FR.estimatedProps) {
                throw AttrException("No attribute $propName in fr struct!")
            }
        }
        if (propName in struct.installedProps) {
            throw AttrException("Attribute $propName already defined in ${struct.id}!")
        }
        val property = struct::class.declaredMemberProperties.find { it.name == propName }
        property?.let {
            if (property is KMutableProperty1<*, *>) {
                if (property.returnType.jvmErasure == propValue::class) {
                    property.setter.call(struct, propValue)
                    struct.installedProps.add(propName)
                    return
                } else {
                    throw AttrException("Type mismatch for property $propName in ${struct.id}!")
                }
            }
        }
        throw RuntimeException("Critical error while while setting property $propName in ${struct.id}!")
    }

    private fun checkStructNameUniqueness(name: String): Boolean {
        return name != app.id &&
            name !in app.data.map { it.id } &&
            name !in app.actors.map { it.id } &&
            name !in app.frs.map { it.id }
    }

    private fun parseProp(struct: LangStructure) {
        val propName = (input[position] as Property).name
        if (position == checkSize - 1) {
            throw ParserException("Missing property definition!", position)
        }
        position++
        if (input[position] is PropValue) {
            setProperty(struct, propName, (input[position] as PropValue).value)
            position++
        } else {
            throw ParserException("Missing property definition!", position)
        }
    }

    private fun parseInnerStructStart() {
        when {
            position + 2 >= checkSize -> {
                throw ParserException("Wrong struct definition!", position)
            }
            input[position + 1] !is StructureName -> {
                throw ParserException("Missing struct name", position + 1)
            }
            !checkStructNameUniqueness((input[position + 1] as StructureName).name) -> {
                throw ParserException("This structure name is already in use!", position + 1)
            }
            input[position + 2] !is OpenCurlyBrace -> {
                throw ParserException("Missing {", position + 2)
            }
        }
        position += 3
    }

    private fun parseActor() {
        parseInnerStructStart()
        val actor = Actor()
        actor.id = (input[position - 2] as StructureName).name
        while (position < checkSize) {
            when (input[position]) {
                is Property -> {
                    parseProp(actor)
                }
                is CloseCurlyBrace -> {
                    app.actors.add(actor)
                    position++
                    return
                }
                else -> throw ParserException("Expected actor property or }", position)
            }
        }
        throw ParserException("Expected actor property or }", position)
    }

    private fun parseData() {
        parseInnerStructStart()
        val data = Data()
        data.id = (input[position - 2] as StructureName).name
        while (position < checkSize) {
            when (input[position]) {
                is Property -> {
                    parseProp(data)
                }
                is CloseCurlyBrace -> {
                    app.data.add(data)
                    position++
                    return
                }
                else -> throw ParserException("Expected data property or }", position)
            }
        }
        throw ParserException("Expected data property or }", position)
    }

    private fun parseFR() {

    }

    fun parse(): Application {
        when {
            input.size < 4 -> {
                throw ParserException("Incomplete file", 0)
            }
            input[0] !is ApplicationStruct -> {
                throw ParserException("Missing app structure", 0)
            }
            input[1] !is StructureName -> {
                throw ParserException("Missing app name", 1)
            }
            input[2] !is OpenCurlyBrace -> {
                throw ParserException("Missing {", 1)
            }
            input.last() !is CloseCurlyBrace -> {
                throw ParserException("Missing }", input.lastIndex)
            }
        }
        app.id = (input[1] as StructureName).name
        position += 3
        checkSize = input.size - 1

        while (position < checkSize) {
            when (input[position]) {
                is Property -> {
                    parseProp(app)
                }
                is ActorStruct -> {
                    parseActor()
                }
                is DataStruct -> {
                    parseData()
                }
                is FRStruct -> {

                }
                else -> throw ParserException("Expected internal structure or property", position)
            }
        }
        return app
    }


}