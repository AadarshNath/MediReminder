package com.medassist

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class OnlineApiMatchResult(
    val candidate: ScannedMedicineCandidate,
    val apiSource: String,
)

object OnlineMedicineApiClient {

    suspend fun searchOnline(query: String): OnlineApiMatchResult? = withContext(Dispatchers.IO) {
        if (query.length < 3) return@withContext null

        val fdaResult = searchOpenFda(query)
        if (fdaResult != null) return@withContext fdaResult

        val rxNormResult = searchRxNorm(query)
        if (rxNormResult != null) return@withContext rxNormResult

        null
    }

    suspend fun searchOpenFda(query: String): OnlineApiMatchResult? = withContext(Dispatchers.IO) {
        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val urlString = "https://api.fda.gov/drug/label.json?search=openfda.brand_name:\"$encodedQuery\"+openfda.substance_name:\"$encodedQuery\"&limit=1"
            val url = URL(urlString)

            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 3000
                readTimeout = 3000
                setRequestProperty("Accept", "application/json")
            }

            if (connection.responseCode == 200) {
                val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(jsonString)
                val results = root.optJSONArray("results")

                if (results != null && results.length() > 0) {
                    val firstItem = results.getJSONObject(0)
                    val openfda = firstItem.optJSONObject("openfda")

                    if (openfda != null) {
                        val brandArray = openfda.optJSONArray("brand_name")
                        val substanceArray = openfda.optJSONArray("substance_name")

                        val brandName = brandArray?.optString(0)?.capitalizeWords() ?: query
                        val genericSalt = substanceArray?.optString(0)?.capitalizeWords() ?: ""

                        val candidate = ScannedMedicineCandidate(
                            name = brandName,
                            instructions = "Take as directed",
                            form = MedForm.Pill,
                            quantity = 1,
                            isDatabaseVerified = true,
                            genericSalt = genericSalt
                        )

                        return@withContext OnlineApiMatchResult(candidate, "OpenFDA")
                    }
                }
            }
        } catch (_: Exception) {
            // Graceful fallback on network timeout or exception
        }
        null
    }

    suspend fun searchRxNorm(query: String): OnlineApiMatchResult? = withContext(Dispatchers.IO) {
        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val urlString = "https://rxnav.nlm.nih.gov/REST/drugs.json?name=$encodedQuery"
            val url = URL(urlString)

            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 3000
                readTimeout = 3000
                setRequestProperty("Accept", "application/json")
            }

            if (connection.responseCode == 200) {
                val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(jsonString)
                val drugGroup = root.optJSONObject("drugGroup")
                val conceptGroup = drugGroup?.optJSONArray("conceptGroup")

                if (conceptGroup != null) {
                    for (i in 0 until conceptGroup.length()) {
                        val group = conceptGroup.getJSONObject(i)
                        val conceptProperties = group.optJSONArray("conceptProperties")

                        if (conceptProperties != null && conceptProperties.length() > 0) {
                            val firstProp = conceptProperties.getJSONObject(0)
                            val name = firstProp.optString("name").capitalizeWords()
                            val synonym = firstProp.optString("synonym").capitalizeWords()

                            val candidate = ScannedMedicineCandidate(
                                name = name,
                                instructions = "Take as directed",
                                form = MedForm.Pill,
                                quantity = 1,
                                isDatabaseVerified = true,
                                genericSalt = synonym
                            )

                            return@withContext OnlineApiMatchResult(candidate, "RxNorm")
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Graceful fallback on network timeout or exception
        }
        null
    }

    private fun String.capitalizeWords(): String {
        return this.lowercase().split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }
}
