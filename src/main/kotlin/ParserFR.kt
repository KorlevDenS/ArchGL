package domain.specific

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

import kotlinx.serialization.*
import kotlinx.serialization.json.*

// 1. DTO-модели под нашу доменную схему
@Serializable
data class EntityDto(
    val name: String,
    val type: String
)

@Serializable
data class EntitiesResponse(
    val entities: List<EntityDto>
)

@Serializable
data class Relation(
    val source: String,
    val relation: String,
    val target: String
)

@Serializable
data class RelationsResponse(
    val relations: List<Relation>
)

@Serializable
data class DomainGraph(
    val entities: List<EntityDto>,
    val relations: List<Relation>
)

fun printRelations(
    rawResponse: String,
    printPrettyJson: Boolean = true
) {
    val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    val root = json.parseToJsonElement(rawResponse).jsonObject
    val contentString = root["message"]
        ?.jsonObject
        ?.get("content")
        ?.jsonPrimitive
        ?.content
        ?: run {
            println("Row: $rawResponse")
            println("❌ Ошибка rels: Поле 'message.content' не найдено.")
            return
        }
    val result = json.decodeFromString<RelationsResponse>(contentString)
    println("┌────────────────────────────────────────────────────────┐")
    println("│ 🔗 СВЯЗИ (${result.relations.size.toString().padStart(2)}) │")
    println("├────────────────────────────────────────────────────────┤")
    result.relations.forEach { rel ->
        println(
            " • ${rel.source} ──(${rel.relation})──> ${rel.target}"
        )
    }
    println("└────────────────────────────────────────────────────────┘")
    if (printPrettyJson) {
        println()
        println("📄 Итоговый JSON:")
        println(
            json.encodeToString(
                RelationsResponse.serializer(),
                result
            )
        )
    }
}

fun printEntities(
    rawResponse: String
): String {
    val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    val root = json.parseToJsonElement(rawResponse).jsonObject

    val contentString = root["message"]
        ?.jsonObject
        ?.get("content")
        ?.jsonPrimitive
        ?.content
        ?: run {
            println("Row: $rawResponse")
            return "❌ Ошибка ent: Поле 'message.content' не найдено."
        }

    val result = json.decodeFromString<EntitiesResponse>(contentString)

    println("┌────────────────────────────────────────────────────────┐")
    println("│              📦 СУЩНОСТИ (${result.entities.size.toString().padStart(2)})                         │")
    println("├────────────────────────────────────────────────────────┤")

    result.entities.forEach { entity ->
        val typeBadge = "[${entity.type}]".padEnd(24)
        println("  • $typeBadge ${entity.name}")
    }

    println("└────────────────────────────────────────────────────────┘")

    return json.encodeToString(
        EntitiesResponse.serializer(),
        result
    )
}

val entitiesJsonSchema = """
    {
      "type": "object",
      "properties": {
        "entities": {
          "type": "array",
          "items": {
            "type": "object",
            "properties": {
              "name": { "type": "string" },
              "type": { 
                "type": "string", 
                "enum": [
                  "${EntityType.ACTOR.name}", 
                  "${EntityType.BUSINESS_ENTITY.name}", 
                  "${EntityType.BUSINESS_ACTION.name}", 
                  "${EntityType.BUSINESS_STATE.name}", 
                  "${EntityType.BUSINESS_EVENT.name}", 
                  "${EntityType.EXTERNAL_PARTY.name}"
                ] 
              }
            },
            "required": ["name", "type"]
          }
        }
      },
      "required": ["entities"]
    }
""".trimIndent()

val relationsJsonSchema = """
    {
      "type": "object",
      "properties": {
        "relations": {
          "type": "array",
          "items": {
            "type": "object",
            "properties": {
              "source": { "type": "string" },
              "relation": { 
                "type": "string", 
                "enum": [
                  "${RelationType.INITIATES.name}", 
                  "${RelationType.AFFECTS.name}", 
                  "${RelationType.USES.name}", 
                  "${RelationType.PRODUCES.name}", 
                  "${RelationType.CHANGES_STATE_TO.name}", 
                  "${RelationType.TRIGGERS.name}", 
                  "${RelationType.CONTAINS.name}", 
                  "${RelationType.BELONGS_TO.name}"
                ] 
              },
              "target": { "type": "string" }
            },
            "required": ["source", "relation", "target"]
          }
        }
      },
      "required": ["relations"]
    }
""".trimIndent()

val entitiesSystemPrompt = """
    Ты — строго детерминированный анализатор требований и эксперт по Domain-Driven Design (DDD).
    Твоя задача — извлечь ТОЛЬКО сущности из предоставленного текста функционального требования и классифицировать их по заданным типам.
    
    ### ЖЕСТКИЕ ПРАВИЛА (СТРОГОЕ СОБЛЮДЕНИЕ):

    1. ПРИНЦИП ЗАКРЫТОГО МИРА (Strict Closed-World Assumption):
    - Каждая сущность должна быть явно основана на слове или словосочетании из входного текста.
    - Если субъект действия явно не указан в тексте, НЕ создавай сущность ${EntityType.ACTOR.name}.

    2. ПРИНЦИП ТЕКСТОВОЙ ТОЧНОСТИ
    - Используй формулировки максимально близкие к тексту требования.
    - Не добавляй отсутствующие уточнения.

    3. ПРИНЦИП МИНИМАЛЬНОСТИ
    - Извлекай только значимые сущности предметной области.
    - Не извлекай служебные слова.
    - Не извлекай местоимения.
    - Не извлекай общие слова без бизнес-смысла.
    - Не создавай дублирующие сущности.

    Разрешенные типы сущностей (Entity Types):
    - ${EntityType.ACTOR.name}: Роль или субъект, инициирующий действие.
    - ${EntityType.BUSINESS_ENTITY.name}: Пассивный бизнес-объект или документ.
    - ${EntityType.BUSINESS_ACTION.name}: Создается ОБЯЗАТЕЛЬНО если в требовании есть функция, бизнес-процесс или действие, даже если действие выражено глаголом.
    - ${EntityType.BUSINESS_STATE.name}: Пассивное состояние/статус объекта.
    - ${EntityType.BUSINESS_EVENT.name}: Факт в прошедшем времени, служащий триггером других событий.
    - ${EntityType.EXTERNAL_PARTY.name}: Внешний контрагент или система.
    
    Формат ответа: СТРОГО JSON с ключом "entities".
""".trimIndent()

val systemPrompt = """
    Ты — строго детерминированный анализатор требований и эксперт по Domain-Driven Design (DDD).
    Твоя задача — извлечь связи ИСКЛЮЧИТЕЛЬНО из предоставленного текста функционального требования.
    
    ### ЖЕСТКИЕ ПРАВИЛА (СТРОГОЕ СОБЛЮДЕНИЕ):

    1. ПРИНЦИП ЗАКРЫТОГО МИРА (Strict Closed-World Assumption):
    - Запрещено добавлять сущности, которых нет в тексте (например: НЕ ДОБАВЛЯЙ "Пользователь", "Чек", "БанковскаяКарта", "Оплачен", если этих слов НЕТ в тексте).
    - Каждая сущность в "entities" должна быть явно основана на слове или словосочетании из входного текста.
    - Если в тексте не указан субъект (кто именно делает действие), НЕ создавай сущность ${EntityType.ACTOR.name}.

    2. ССЫЛОЧНАЯ ЦЕЛОСТНОСТЬ ГРАФА:
    - Значения "source" и "target" в массиве "relations" МОГУТ ССЫЛАТЬСЯ ТОЛЬКО на поля "name" из сформированного массива "entities".
    - Запрещено использовать в "relations" имена, отсутствующие в "entities".

    3. ИЗОЛЯЦИЯ ПРИМЕРОВ:
    - Все термины в квадратных скобках (например: [Сущность_A]) приведены исключительно как абстрактные схемы.
    - Запрещено копировать названия из примеров формата в итоговый результат.

    Разрешенные типы сущностей (Entity Types):
    - ${EntityType.ACTOR.name}: Роль или субъект, инициирующий действие.
    - ${EntityType.BUSINESS_ENTITY.name}: Пассивный бизнес-объект или документ.
    - ${EntityType.BUSINESS_ACTION.name}: Функция, бизнес-процесс или действие.
    - ${EntityType.BUSINESS_STATE.name}: Пассивное состояние/статус объекта.
    - ${EntityType.BUSINESS_EVENT.name}: Факт в прошедшем времени, служащий триггером.
    - ${EntityType.EXTERNAL_PARTY.name}: Внешний контрагент или система.

    Разрешенные типы связей (Relation Types) и их семантика:
    1. ${RelationType.INITIATES.name} (${EntityType.ACTOR.name}/${EntityType.EXTERNAL_PARTY.name} -> ${EntityType.BUSINESS_ACTION.name})
       Семантика: Запуск бизнес-действия ролью или внешним участником.
    2. ${RelationType.AFFECTS.name} (${EntityType.BUSINESS_ACTION.name}/${EntityType.BUSINESS_EVENT.name} -> ${EntityType.BUSINESS_ENTITY.name})
       Семантика: Изменение состояния или воздействие на уже существующий объект.
    3. ${RelationType.USES.name} (${EntityType.BUSINESS_ACTION.name} -> ${EntityType.BUSINESS_ENTITY.name}/${EntityType.EXTERNAL_PARTY.name})
       Семантика: Использование инструмента, реквизитов или внешнего сервиса для выполнения действия.
    4. ${RelationType.PRODUCES.name} (${EntityType.BUSINESS_ACTION.name}/${EntityType.BUSINESS_EVENT.name} -> ${EntityType.BUSINESS_ENTITY.name})
       Семантика: Создание нового объекта или документа "с нуля" в результате процесса.
    5. ${RelationType.CHANGES_STATE_TO.name} (${EntityType.BUSINESS_ACTION.name}/${EntityType.BUSINESS_EVENT.name} -> ${EntityType.BUSINESS_STATE.name})
       Семантика: Перевод сущности в определенное пассивное состояние или статус.
    6. ${RelationType.TRIGGERS.name} (${EntityType.BUSINESS_ACTION.name}/${EntityType.BUSINESS_EVENT.name} -> ${EntityType.BUSINESS_EVENT.name}/${EntityType.BUSINESS_ACTION.name})
       Семантика: Переход к следующему шагу процесса или генерация события-триггера.
    7. ${RelationType.CONTAINS.name} (${EntityType.BUSINESS_ENTITY.name} -> ${EntityType.BUSINESS_ENTITY.name})
       Семантика: Связь "часть-целое" (композиция/агрегация).
    8. ${RelationType.BELONGS_TO.name} (${EntityType.BUSINESS_ENTITY.name} -> ${EntityType.ACTOR.name}/${EntityType.BUSINESS_ENTITY.name})
       Семантика: Принадлежность объекта конкретному владельцу.
    
    Формат ответа: СТРОГО JSON с ключами "entities" и "relations".
""".trimIndent()


fun parseEntity(fr: String) : String {
    val client = HttpClient.newHttpClient()

    // Escaping текста для JSON-нагрузки
    fun String.escapeJson(): String = this
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")

    val jsonBody = """
        {
          "model": "qwen2.5-fr",
          "messages": [
            {"role": "system", "content": "${"Ты — аналитик DDD. Извлеки сущности из функционального требования и классифицируй их по типам: ACTOR, BUSINESS_ENTITY, BUSINESS_ACTION, BUSINESS_STATE, BUSINESS_EVENT, EXTERNAL_PARTY. Формат ответа: СТРОГО JSON с ключом \"entities\".".escapeJson()}"},
            {"role": "user", "content": "${fr.escapeJson()}"}
          ],
          "format": $entitiesJsonSchema,
          "options": {
            "num_ctx": 4096,
            "temperature": 0.0
          },
          "stream": false
        }
    """.trimIndent()

    // 3. Отправляем POST-запрос к Ollama
    val request = HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:11434/api/chat"))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
        .build()

    val response = client.send(request, HttpResponse.BodyHandlers.ofString())
    // 4. Выводим результат
    return response.body()
}

fun parseRelation(fr: String, entities: String) : String {
    val client = HttpClient.newHttpClient()

    // Escaping текста для JSON-нагрузки
    fun String.escapeJson(): String = this
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")

    val jsonBody = """
        {
          "model": "qwen2.5-fr",
          "messages": [
            {"role": "system", "content": "${"Ты — аналитик DDD. На основе функционального требования и списка сущностей в формате JSON свяжи эти сущности между собой. Используй типы связей: INITIATES, AFFECTS, USES, PRODUCES, CHANGES_STATE_TO, TRIGGERS, CONTAINS, BELONGS_TO. Формат ответа: СТРОГО JSON с ключом \"relations\".".escapeJson()}"},
            {"role": "user", "content": "${("Требование: $fr\n\nСущности:\n$entities").escapeJson()}"}
          ],
          "format": $relationsJsonSchema,
          "options": {
            "num_ctx": 4096,
            "temperature": 0.0
          },
          "stream": false
        }
    """.trimIndent()

    // 3. Отправляем POST-запрос к Ollama
    val request = HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:11434/api/chat"))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
        .build()

    val response = client.send(request, HttpResponse.BodyHandlers.ofString())
    // 4. Выводим результат
    return response.body()
}