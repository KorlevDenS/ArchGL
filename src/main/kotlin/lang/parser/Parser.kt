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
        when (struct) {
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

    private fun MutableList<Action>.addActionToStream(action: Action) {
        if (this.isEmpty()) {
            if (action is AcceptRequestFrom) {
                this.add(action)
                return
            } else {
                throw ParserException("First action in fr must be 'accept request from YourActorId'", position)
            }
        }
        val previous = this.last()
        when (previous) {
            is Return -> {
                throw ParserException("Actions after 'return' are not allowed!", position)
            }

            is Absorbing -> {
                if (action is Return) {
                    this.add(action)
                    return
                } else {
                    throw ParserException("Only 'return' is allowed after absorbing action!", position)
                }
            }

            is Generative, is Intermediate -> {
                if (action is Intermediate || action is Absorbing || action is Return) {
                    this.add(action)
                    return
                } else {
                    throw ParserException(
                        "Only intermediate, absorbing and 'return' actions allowed after generative or intermediate!",
                        position
                    )
                }
            }

            is AcceptRequestFrom -> {
                if (action !is AcceptRequestFrom) {
                    this.add(action)
                    return
                } else {
                    throw ParserException("Accept action is allowed once in fr!", position)
                }
            }

            else -> {
                throw ParserException("Not supported action!", position)
            }
        }
    }

    private fun MutableList<Token>.compareWithTemplate(otherList: List<Token>): Boolean {
        return otherList.size == this.size &&
                otherList.zip(this).all { (a, b) -> a::class == b::class }
    }

    private fun getDataFromName(actionWords: MutableList<Token>, pos: Int): Data {
        val dataName = (actionWords[pos] as StructureName).name
        val data = app.data.find { it.id == dataName }
        data?.let {
            return data
        } ?: run {
            throw ParserException("Data with name $dataName is undefined!", position)
        }
    }

    private fun getActorFromName(actionWords: MutableList<Token>, pos: Int): Actor {
        val actorName = (actionWords[pos] as StructureName).name
        val actor = app.actors.find { it.id == actorName }
        actor?.let {
            return actor
        } ?: run {
            throw ParserException("Actor with name $actorName is undefined!", position)
        }
    }

    private fun MutableList<Action>.parseAndAddAction(frActionWords: MutableList<Token>) {
        if (frActionWords.compareWithTemplate(AcceptRequestFrom.template)) {
            this.addActionToStream(
                AcceptRequestFrom(
                    getActorFromName(frActionWords, 3),
                    Data.DefaultRequest
                )
            )
        } else if (frActionWords.compareWithTemplate(Return.template)) {
            this.addActionToStream(
                Return(
                    Data.DefaultRequest,
                    Actor.AppItself
                )
            )
        } else if (frActionWords.compareWithTemplate(Generate.template)) {
            this.addActionToStream(
                Generate(
                    getDataFromName(frActionWords, 1)
                )
            )
        } else if (frActionWords.compareWithTemplate(Read.template)) {
            this.addActionToStream(
                Read(
                    getDataFromName(frActionWords, 1)
                )
            )
        } else if (frActionWords.compareWithTemplate(WorkWithObtaining.template)) {
            this.addActionToStream(
                WorkWithObtaining(
                    getDataFromName(frActionWords, 2),
                    getDataFromName(frActionWords, 4)
                )
            )
        } else if (frActionWords.compareWithTemplate(WorkWith.template)) {
            this.addActionToStream(
                WorkWith(
                    getDataFromName(frActionWords, 2)
                )
            )
        } else if (frActionWords.compareWithTemplate(SendTo.template)) {
            this.addActionToStream(
                SendTo(
                    getDataFromName(frActionWords, 1),
                    getActorFromName(frActionWords, 3)
                )
            )
        } else if (frActionWords.compareWithTemplate(Save.template)) {
            this.addActionToStream(
                Save(
                    getDataFromName(frActionWords, 1)
                )
            )
        } else if (frActionWords.compareWithTemplate(Update.template)) {
            this.addActionToStream(
                Update(
                    getDataFromName(frActionWords, 1)
                )
            )
        } else if (frActionWords.compareWithTemplate(Delete.template)) {
            this.addActionToStream(
                Delete(
                    getDataFromName(frActionWords, 1)
                )
            )
        } else if (frActionWords.compareWithTemplate(SendToObtaining.template)) {
            this.addActionToStream(
                SendToObtaining(
                    getDataFromName(frActionWords, 1),
                    getActorFromName(frActionWords, 3),
                    getDataFromName(frActionWords, 5)
                )
            )
        } else {
            throw ParserException("Unknown action description!", position)
        }

    }

    private fun parseActionsProp(fr: FR) {
        val frActions = mutableListOf<Action>()

        val frActionWords = mutableListOf<Token>()

        position++
        if (position + 1 >= checkSize && input[position] !is OpenRoundBrace) {
            throw ParserException("List of action in () expected!", position)
        }
        position++
        while (position < checkSize) {
            when (input[position]) {
                is ActionWord, is StructureName -> {
                    frActionWords.add(input[position])
                    position++
                }

                is Comma -> {
                    if (frActionWords.isEmpty()) {
                        throw ParserException("No action before comma in fr!", position)
                    }
                    frActions.parseAndAddAction(frActionWords)
                    position++
                    frActionWords.clear()
                }

                is CloseRoundBrace -> {
                    if (frActionWords.isEmpty()) {
                        throw ParserException("No action before ) in fr!", position)
                    }
                    frActions.parseAndAddAction(frActionWords)
                    fr.actions = frActions
                    fr.installedProps.add("actions")
                    position++
                    return
                }

                else -> {
                    throw ParserException("Expected expression inside Actions!", position)
                }
            }
        }
        throw ParserException("Expected expression inside Actions!", position)
    }

    private fun parseFR() {
        parseInnerStructStart()
        val fr = FR()
        fr.id = (input[position - 2] as StructureName).name
        while (position < checkSize) {
            when (input[position]) {
                is Property -> {
                    if ((input[position] as Property).name == "actions") {
                        parseActionsProp(fr)
                    } else {
                        parseProp(fr)
                    }
                }

                is CloseCurlyBrace -> {
                    app.frs.add(fr)
                    position++
                    return
                }

                else -> throw ParserException("Expected fr property or }", position)
            }
        }
        throw ParserException("Expected fr property or }", position)
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
                    parseFR()
                }

                else -> throw ParserException("Expected internal structure or property", position)
            }
        }
        return app
    }


}