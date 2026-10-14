package com.medassist

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class ParsedMedicineInfo(
    val name: String = "",
    val instructions: String = "",
    val quantity: Int = 1,
    val form: MedForm = MedForm.Tablet,
    val rawText: String = "",
)

object OcrTextRecognizer {

    suspend fun recognizeTextFromUri(context: Context, uri: Uri): ParsedMedicineInfo? {
        return suspendCancellableCoroutine { continuation ->
            try {
                val image = InputImage.fromFilePath(context, uri)
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        val parsed = parsePrescriptionText(visionText)
                        continuation.resume(parsed)
                    }
                    .addOnFailureListener {
                        continuation.resume(null)
                    }
            } catch (e: Exception) {
                continuation.resume(null)
            }
        }
    }

    private fun parsePrescriptionText(visionText: Text): ParsedMedicineInfo {
        val rawText = visionText.text
        if (rawText.isBlank()) return ParsedMedicineInfo()

        val lines = visionText.textBlocks.flatMap { block ->
            block.lines.map { it.text.trim() }
        }.filter { it.isNotBlank() }

        var detectedName = ""
        var detectedInstructions = ""
        var detectedForm = MedForm.Tablet
        var detectedQuantity = 1

        val skipKeywords = listOf(
            "pharmacy", "rx", "doctor", "dr.", "patient", "date", "phone", "address",
            "refill", "qty", "quantity", "tablets", "capsules", "caution", "keep out",
            "store", "take", "sig", "dispense", "npi", "dea", "prov"
        )

        for (line in lines) {
            val lowerLine = line.lowercase()

            // 1. Detect Form (Tablet, Capsule, Syrup, etc.)
            when {
                lowerLine.contains("capsule") || lowerLine.contains("cap") -> detectedForm = MedForm.Capsule
                lowerLine.contains("syrup") || lowerLine.contains("suspension") || lowerLine.contains("liquid") -> detectedForm = MedForm.Syrup
                lowerLine.contains("injection") || lowerLine.contains("vial") -> detectedForm = MedForm.Injection
                lowerLine.contains("drop") -> detectedForm = MedForm.Drops
                lowerLine.contains("ointment") || lowerLine.contains("cream") -> detectedForm = MedForm.Ointment
                lowerLine.contains("inhaler") -> detectedForm = MedForm.Inhaler
                lowerLine.contains("patch") -> detectedForm = MedForm.Patch
                lowerLine.contains("tablet") || lowerLine.contains("tab") || lowerLine.contains("pill") -> detectedForm = MedForm.Tablet
            }

            // 2. Detect Name (Look for uppercase words or lines with dosage like 500mg, 10mg)
            if (detectedName.isEmpty()) {
                val isSkip = skipKeywords.any { lowerLine.contains(it) }
                if (!isSkip && line.length in 3..40) {
                    // Check if line contains a dosage pattern (e.g., 500mg, 10 mg, 5ml) or strong word
                    if (line.contains(Regex("(?i)\\b\\d+\\s*(mg|g|mcg|ml|iu)\\b")) || line.any { it.isUpperCase() }) {
                        detectedName = line
                    }
                }
            }

            // 3. Detect Instructions
            if (detectedInstructions.isEmpty()) {
                if (lowerLine.contains("take") || lowerLine.contains("after") ||
                    lowerLine.contains("before") || lowerLine.contains("daily") ||
                    lowerLine.contains("every") || lowerLine.contains("times") ||
                    lowerLine.contains("morning") || lowerLine.contains("night") || lowerLine.contains("meal")
                ) {
                    detectedInstructions = line
                }
            }

            // 4. Detect Dose Quantity (e.g. Take 1 tablet, Qty: 2)
            if (lowerLine.contains("take") || lowerLine.contains("qty")) {
                val digitMatch = Regex("\\b([1-9])\\b").find(line)
                if (digitMatch != null) {
                    detectedQuantity = digitMatch.groupValues[1].toIntOrNull() ?: 1
                }
            }
        }

        // Fallback for name if none detected
        if (detectedName.isEmpty() && lines.isNotEmpty()) {
            detectedName = lines.firstOrNull { l -> skipKeywords.none { l.lowercase().contains(it) } } ?: lines.first()
        }

        return ParsedMedicineInfo(
            name = detectedName.trim(),
            instructions = detectedInstructions.trim().ifEmpty { "Take as directed" },
            quantity = detectedQuantity,
            form = detectedForm,
            rawText = rawText,
        )
    }
}
