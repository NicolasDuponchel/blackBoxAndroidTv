package com.mk.he_or_her

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Test
import java.net.URL


internal class QuestionTest {

    @Test
    fun `serialization for question list`() {
        val source = javaClass.getResource("/questionsMocked.json").readJson()
        println(source)

        val result = Json.decodeFromString<Questions>(source)
        println(result)
    }

    private fun URL.readJson() = readText().replace("\\s+(?=([^\"]*\"[^\"]*\")*[^\"]*\$)".toRegex(), "")
}