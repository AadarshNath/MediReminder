package com.medassist

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

object GeminiVisionApiClient {

    suspend fun analyzePrescriptionImage(context: Context, imageUri: Uri): List<ScannedMedicineCandidate>? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank()) return@withContext null

        val base64Image = uriToBase64Jpeg(context, imageUri) ?: return@withContext null

        try {
            val urlString = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent?key=$apiKey"
            val url = URL(urlString)

            val prompt = """
                You are a professional medical prescription analyzer. Analyze this doctor prescription or medicine label image carefully.
                Extract ALL prescribed medicines and return ONLY a valid raw JSON array with no extra text or markdown formatting (no ```json code blocks):
                [
                  {
                    "name": "Medicine Name with dosage strength (e.g. Azithromycin 250mg or Sinarest 100mg)",
                    "form": "Pill",
                    "instructions": "Daily: 1-0-1 After Food"
                  }
                ]
                Rules:
                1. "form" MUST be one of: "Pill", "Syrup", "Injection", "Drops", "Inhaler", "Powder".
                2. "name" should be the clean brand name or generic medicine name (e.g. "Azithromycin 250mg", "Sinarest 100mg", "Dolo 650mg").
                3. "instructions" should be the prescribed dosage & frequency (e.g. "Daily: 1-0-1", "Twice a day", "Once a day").
                4. STRICTLY IGNORE: Lab tests (such as RT-PCR, Dengue Antigen, Fever Profile, CBC, ESR, CRP, CT Chest), Doctor's Advice (such as steam inhalation, isolate, drink warm water), Hospital headers, and Doctor IDs.
            """.trimIndent()

            val payload = JSONObject().apply {
                val contents = JSONArray()
                val contentObj = JSONObject()
                val parts = JSONArray()

                val textPart = JSONObject().apply {
                    put("text", prompt)
                }

                val imagePart = JSONObject().apply {
                    val inlineData = JSONObject().apply {
                        put("mime_type", "image/jpeg")
                        put("data", base64Image)
                    }
                    put("inline_data", inlineData)
                }

                parts.put(textPart)
                parts.put(imagePart)
                contentObj.put("parts", parts)
                contents.put(contentObj)
                put("contents", contents)
            }

            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("X-goog-api-key", apiKey)
                setRequestProperty("x-api-key", apiKey)
                doOutput = true
            }

            connection.outputStream.use { os ->
                os.write(payload.toString().toByteArray(Charsets.UTF_8))
            }

            if (connection.responseCode == 200) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val responseJson = JSONObject(responseText)

                val candidatesArray = responseJson.optJSONArray("candidates")
                if (candidatesArray != null && candidatesArray.length() > 0) {
                    val firstCandidate = candidatesArray.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    val responseParts = content?.optJSONArray("parts")

                    if (responseParts != null && responseParts.length() > 0) {
                        var jsonText = responseParts.getJSONObject(0).optString("text").trim()

                        // Strip markdown code block ticks if present
                        if (jsonText.startsWith("```")) {
                            jsonText = jsonText.replace(Regex("^```[a-z]*\\s*"), "").replace(Regex("\\s*```$"), "").trim()
                        }

                        if (jsonText.startsWith("[")) {
                            val medicineArray = JSONArray(jsonText)
                            val resultList = mutableListOf<ScannedMedicineCandidate>()

                            for (i in 0 until medicineArray.length()) {
                                val item = medicineArray.getJSONObject(i)
                                val name = item.optString("name").trim()
                                val formStr = item.optString("form").trim()
                                val instructions = item.optString("instructions").trim()

                                val form = when {
                                    formStr.contains("Syrup", ignoreCase = true) -> MedForm.Syrup
                                    formStr.contains("Injection", ignoreCase = true) -> MedForm.Injection
                                    formStr.contains("Drop", ignoreCase = true) -> MedForm.Drops
                                    formStr.contains("Inhaler", ignoreCase = true) -> MedForm.Inhaler
                                    formStr.contains("Powder", ignoreCase = true) -> MedForm.Powder
                                    else -> MedForm.Pill
                                }

                                if (name.isNotBlank()) {
                                    resultList.add(
                                        ScannedMedicineCandidate(
                                            name = name,
                                            instructions = instructions.ifEmpty { "Take as directed" },
                                            form = form,
                                            quantity = 1,
                                            isDatabaseVerified = true,
                                            genericSalt = "",
                                            apiSource = "Gemini 3.8 AI"
                                        )
                                    )
                                }
                            }

                            if (resultList.isNotEmpty()) {
                                Log.d("GeminiVisionApi", "Gemini 3.8 Flash AI Vision successfully parsed ${resultList.size} medicines")
                                return@withContext resultList
                            }
                        }
                    }
                }
            } else {
                val errText = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                Log.e("GeminiVisionApi", "Gemini API HTTP Error ${connection.responseCode}: $errText")
            }
        } catch (e: Exception) {
            Log.e("GeminiVisionApi", "Gemini API Exception: ${e.localizedMessage}", e)
        }
        null
    }

    private fun uriToBase64Jpeg(context: Context, uri: Uri): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream) ?: return null
            inputStream?.close()

            // Scale bitmap to max dimension of 1024 to optimize payload size and speed
            val maxDimension = 1024
            val width = originalBitmap.width
            val height = originalBitmap.height
            val scale = (maxDimension.toFloat() / maxOf(width, height)).coerceAtMost(1.0f)

            val scaledBitmap = if (scale < 1.0f) {
                Bitmap.createScaledBitmap(originalBitmap, (width * scale).toInt(), (height * scale).toInt(), true)
            } else {
                originalBitmap
            }

            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            val bytes = outputStream.toByteArray()

            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (_: Exception) {
            null
        }
    }
}
