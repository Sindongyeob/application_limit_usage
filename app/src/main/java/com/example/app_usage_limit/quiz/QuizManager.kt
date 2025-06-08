package com.example.app_usage_limit.quiz

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object QuizManager {
    suspend fun generateQuiz(apiKey: String, keyword: String): Pair<String, String> {

        if (apiKey.isBlank()) throw IllegalArgumentException("OpenAI API 키가 비어 있습니다.")

        val prompt = """
            "$keyword"라는 주제로 한 문장짜리 단답형 퀴즈를 만들어주세요.
            출력 형식은 다음 JSON 객체 형태로만 해주세요 (설명 없이):
            {"question": "여기에 질문", "answer": "여기에 정답"}
        """.trimIndent()

        val messagesArray = org.json.JSONArray().apply {
            put(JSONObject().apply {
                put("role", "user")
                put("content", prompt)
            })
        }

        val requestBody = JSONObject().apply {
            put("model", "gpt-3.5-turbo")
            put("messages", messagesArray)
        }

        val body = requestBody.toString()
            .toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .header("Authorization", "Bearer $apiKey")
            .post(body)
            .build()

        return withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: throw Exception("응답 본문이 없습니다.")

            val content = try {
                JSONObject(responseBody)
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                    .trim()
            } catch (e: Exception) {
                throw Exception("OpenAI 응답 파싱 실패:\n$responseBody")
            }

            val jsonString = extractJsonString(content)

            try {
                val quizJson = JSONObject(jsonString)
                val question = quizJson.getString("question")
                val answer = quizJson.getString("answer")
                Pair(question, answer)
            } catch (e: Exception) {
                throw Exception("퀴즈 JSON 파싱 오류:\n$content")
            }
        }
    }

    private fun extractJsonString(raw: String): String {
        return raw.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
    }
}
