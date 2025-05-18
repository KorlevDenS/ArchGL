package domain.specific.lang.parser

import domain.specific.lang.model.*
import kotlin.jvm.Throws
import kotlin.math.min
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

    private fun checkStructNameUniqueness(name: String, isActionStruct: Boolean): Boolean {
        return name != app.id &&
                name !in app.data.map { it.id } &&
                name !in app.actors.map { it.id } &&
                (isActionStruct || name !in app.frs.map { it.id })
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

    private fun parseInnerStructStart(isActionStruct: Boolean) {
        when {
            position + 2 >= checkSize -> {
                throw ParserException("Wrong struct definition!", position)
            }

            input[position + 1] !is StructureName -> {
                throw ParserException("Missing struct name", position + 1)
            }

            !checkStructNameUniqueness((input[position + 1] as StructureName).name, isActionStruct) -> {
                throw ParserException("This structure name is already in use!", position + 1)
            }

            input[position + 2] !is OpenCurlyBrace -> {
                throw ParserException("Missing {", position + 2)
            }
        }
        position += 3
    }

    private fun parseActor() {
        parseInnerStructStart(false)
        val actor = Actor()
        actor.id = (input[position - 2] as StructureName).name
        while (position < checkSize) {
            when (input[position]) {
                is Property -> {
                    parseProp(actor)
                }

                is CloseCurlyBrace -> {
                    app.actors.add(actor)
                    app.data.add(
                        Data(
                            id = actor.id,
                            type = "actor-data",
                            retention = 1577880000000,
                            unitVolume = 700,
                            installedProps = mutableSetOf("type", "retention", "unitVolume")
                        )
                    )
                    position++
                    return
                }

                else -> throw ParserException("Expected actor property or }", position)
            }
        }
        throw ParserException("Expected actor property or }", position)
    }

    private fun parseData() {
        parseInnerStructStart(false)
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
            if (action is Accepting) {
                this.add(action)
                return
            } else {
                throw ParserException("First action in fr must be 'accept request from YourActorId'", position)
            }
        }
        when (val previous = this.last()) {
            is Return -> {
                throw ParserException("Actions after 'return' are not allowed!", position)
            }

            is Generative, is Intermediate -> {
                when (action) {
                    is Intermediate, is Return -> {
                        this.add(action)
                        return
                    }

                    is Absorbing -> {
                        (this.last() as PrecedeAbsorbing).absorbers.add(action)
                    }

                    else -> {
                        throw ParserException(
                            "Only intermediate, absorbing and 'return' actions allowed after generative or intermediate!",
                            position
                        )
                    }
                }
            }

            is Accepting -> {
                if (action !is Accepting) {
                    if (previous is AcceptRequestFrom && action !is Generative && action !is Return) {
                        throw ParserException(
                            "Only Generative actions or 'return' can follow 'accept request from'",
                            position
                        )
                    }
                    if (previous is AcceptFrom && action is Generative) {
                        throw ParserException("Only Not Generative actions can follow 'accept YourData from'", position)
                    }
                    if (action is Absorbing) {
                        (this.last() as PrecedeAbsorbing).absorbers.add(action)
                    } else {
                        this.add(action)
                    }
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
            return it
        } ?: run {
            throw ParserException("Data with name $dataName is undefined!", position)
        }
    }

    private fun getActorFromName(actionWords: MutableList<Token>, pos: Int): Actor {
        val actorName = (actionWords[pos] as StructureName).name
        val actor = app.actors.find { it.id == actorName }
        actor?.let {
            return it
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
        } else if (frActionWords.compareWithTemplate(AcceptFrom.template)) {
            this.addActionToStream(
                AcceptFrom(
                    getActorFromName(frActionWords, 3),
                    getDataFromName(frActionWords, 1)
                )
            )
        } else if (frActionWords.compareWithTemplate(Return.template)) {
            this.addActionToStream(
                Return(
                    Data.DefaultRequest,
                    (this.first() as Accepting).sender
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
        } else if (frActionWords.compareWithTemplate(ReadRelated.template)) {
            this.addActionToStream(
                ReadRelated(
                    getDataFromName(frActionWords, 1),
                    getDataFromName(frActionWords, 3)
                )
            )
        } else if (frActionWords.compareWithTemplate(ProcessObtaining.template)) {
            this.addActionToStream(
                ProcessObtaining(
                    Data.DefaultRequest,
                    getDataFromName(frActionWords, 3)
                )
            )
        } else if (frActionWords.compareWithTemplate(Process.template)) {
            this.addActionToStream(
                Process(
                    Data.DefaultRequest
                )
            )
        } else if (frActionWords.compareWithTemplate(SendTo.template)) {
            this.addActionToStream(
                SendTo(
                    Data.DefaultRequest,
                    getActorFromName(frActionWords, 3)
                )
            )
        } else if (frActionWords.compareWithTemplate(Save.template)) {
            this.addActionToStream(
                Save(
                    Data.DefaultRequest
                )
            )
        } else if (frActionWords.compareWithTemplate(SaveRelated.template)) {
            this.addActionToStream(
                SaveRelated(
                    getDataFromName(frActionWords, 1),
                    Data.DefaultRequest
                )
            )
        } else if (frActionWords.compareWithTemplate(Update.template)) {
            this.addActionToStream(
                Update(
                    Data.DefaultRequest
                )
            )
        } else if (frActionWords.compareWithTemplate(UpdateRelated.template)) {
            this.addActionToStream(
                UpdateRelated(
                    getDataFromName(frActionWords, 1),
                    Data.DefaultRequest
                )
            )
        } else if (frActionWords.compareWithTemplate(Delete.template)) {
            this.addActionToStream(
                Delete(
                    Data.DefaultRequest
                )
            )
        } else if (frActionWords.compareWithTemplate(DeleteRelated.template)) {
            DeleteRelated(
                getDataFromName(frActionWords, 1),
                Data.DefaultRequest
            )
        } else if (frActionWords.compareWithTemplate(SendToObtaining.template)) {
            this.addActionToStream(
                SendToObtaining(
                    Data.DefaultRequest,
                    getActorFromName(frActionWords, 3),
                    getDataFromName(frActionWords, 5)
                )
            )
        } else {
            throw ParserException("Unknown action description!", position)
        }

    }

    private fun MutableList<Action>.checkDataStream() {
        if (this.isEmpty()) {
            return
        }
        var streamData: Data
        if (this[0] is AcceptRequestFrom) {
            if (this.size > 1) {
                streamData = this[1].data0
            } else {
                return
            }
        } else if (this[0] is AcceptFrom) {
            streamData = this[0].data0
            for (a in (this[0] as PrecedeAbsorbing).absorbers) {
                a.data0 = streamData
            }
        } else {
            throw ParserException("Unknown first action!", position)
        }
        for (i in 1..<this.size) {
            this[i].data0 = streamData
            if (this[i] is Obtaining) {
                streamData = (this[i] as Obtaining).resData
            }
            if (this[i] is PrecedeAbsorbing) {
                for (a in (this[i] as PrecedeAbsorbing).absorbers) {
                    a.data0 = streamData
                }
            }
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
                    frActions.checkDataStream()
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
        parseInnerStructStart(true)
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

    private fun PrecedeAbsorbing.join(action: PrecedeAbsorbing) {
        for (abs in action.absorbers) {
            if (abs !in this.absorbers) {
                this.absorbers.add(abs)
            }
        }
    }

    private fun joinEponymousFrs() {
        val groupedLists: Map<String, List<FR>> = app.frs.groupBy { it.id }
        app.frs.clear()

        for (key in groupedLists.keys) {
            val unitedFrs = groupedLists[key]!!.toMutableList()

            if (unitedFrs.size > 1) {
                if (unitedFrs.map { it.actions.first() }.distinct().size != 1) {
                    throw ParserException(
                        "In functional requirements with the same names," +
                                "at least the first action must match.", position
                    )
                }

                val blackList: MutableList<Int> = mutableListOf()


                for (i in 1..<unitedFrs.size) {
                    if (unitedFrs[0].actions[0] is PrecedeAbsorbing) {
                        (unitedFrs[0].actions[0] as PrecedeAbsorbing).join(unitedFrs[i].actions[0] as PrecedeAbsorbing)
                    }
                    unitedFrs[i].actions[0] = AcceptingLink(unitedFrs[0].actions[0])
                }

                for (i in 0..<unitedFrs.size) {
                    val mainActions = unitedFrs[i].actions
                    for (index in (i + 1)..<unitedFrs.size) {
                        if (index !in blackList) {
                            val foodActions = unitedFrs[index].actions
                            val minSize = min(mainActions.size, foodActions.size)
                            for (pos in 1..<minSize) {
                                if (mainActions[pos] == foodActions[pos]) {
                                    if (foodActions[pos - 1] is PrecedeAbsorbing) {
                                        (mainActions[pos - 1] as PrecedeAbsorbing)
                                            .join(foodActions[pos - 1] as PrecedeAbsorbing)
                                    }
                                    foodActions[pos - 1] = Dummy()
                                } else {
                                    blackList.add(index)
                                }
                            }
                        }
                    }
                    blackList.clear()
                }
            }
            unitedFrs.forEach { it ->
                it.actions.removeIf { it is Dummy }
            }
            unitedFrs.removeIf { it.actions.isEmpty() }
            unitedFrs.forEach { it ->
                app.frs.add(it)
            }
        }
    }

    @Throws(ParserException::class)
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
        joinEponymousFrs()
        return app
    }


}