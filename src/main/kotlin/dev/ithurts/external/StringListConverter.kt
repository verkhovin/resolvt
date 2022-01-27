package dev.ithurts.external

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import javax.persistence.AttributeConverter
import dev.ithurts.external.StringListConverter
import java.util.*
import javax.persistence.Converter

@Converter
class StringListConverter : AttributeConverter<List<String>, String> {
    private val objectMapper = jacksonObjectMapper()

    override fun convertToDatabaseColumn(stringList: List<String>): String {
        return objectMapper.writeValueAsString(stringList)
    }

    override fun convertToEntityAttribute(string: String?): List<String> {
        string ?: return emptyList()
        return objectMapper.readValue(string)
    }
}