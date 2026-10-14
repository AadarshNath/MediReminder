package com.medassist

import java.util.Locale

data class MedicineEntity(
    val brandName: String,
    val genericSalt: String,
    val defaultStrength: String = "",
    val defaultForm: MedForm = MedForm.Pill,
)

data class DatabaseMatchResult(
    val entity: MedicineEntity,
    val matchedStrength: String = "",
    val confidenceScore: Float = 0f,
    val isBrandMatch: Boolean = false,
)

object MedicineDatabase {

    val dataset = listOf(
        MedicineEntity("Azee", "Azithromycin", "250mg", MedForm.Pill),
        MedicineEntity("Azee 500", "Azithromycin", "500mg", MedForm.Pill),
        MedicineEntity("Azithral", "Azithromycin", "500mg", MedForm.Pill),
        MedicineEntity("Azithromycin", "Azithromycin", "250mg", MedForm.Pill),
        MedicineEntity("Sinarest", "Paracetamol + Phenylephrine + Chlorpheniramine", "100mg", MedForm.Pill),
        MedicineEntity("Dolo 650", "Paracetamol", "650mg", MedForm.Pill),
        MedicineEntity("Paracetamol", "Paracetamol", "500mg", MedForm.Pill),
        MedicineEntity("Calpol", "Paracetamol", "500mg", MedForm.Pill),
        MedicineEntity("Limcee", "Vitamin C (Ascorbic Acid)", "500mg", MedForm.Pill),
        MedicineEntity("Shelcal", "Calcium & Vitamin D3", "500mg", MedForm.Pill),
        MedicineEntity("Zincovit", "Multivitamin & Zinc", "", MedForm.Pill),
        MedicineEntity("Montair LC", "Montelukast + Levocetirizine", "10mg", MedForm.Pill),
        MedicineEntity("Montelukast", "Montelukast Sodium", "10mg", MedForm.Pill),
        MedicineEntity("Augmentin 625 Duo", "Amoxicillin + Clavulanic Acid", "625mg", MedForm.Pill),
        MedicineEntity("Moxikind CV", "Amoxicillin + Clavulanate", "625mg", MedForm.Pill),
        MedicineEntity("Amoxicillin", "Amoxicillin", "500mg", MedForm.Pill),
        MedicineEntity("Pan D", "Pantoprazole + Domperidone", "40mg", MedForm.Pill),
        MedicineEntity("Pantocid 40", "Pantoprazole", "40mg", MedForm.Pill),
        MedicineEntity("Pantoprazole", "Pantoprazole Sodium", "40mg", MedForm.Pill),
        MedicineEntity("Telma 40", "Telmisartan", "40mg", MedForm.Pill),
        MedicineEntity("Telmisartan", "Telmisartan", "40mg", MedForm.Pill),
        MedicineEntity("Metformin", "Metformin Hydrochloride", "500mg", MedForm.Pill),
        MedicineEntity("Glycomet 500", "Metformin", "500mg", MedForm.Pill),
        MedicineEntity("Atorva 10", "Atorvastatin", "10mg", MedForm.Pill),
        MedicineEntity("Atorvastatin", "Atorvastatin Calcium", "10mg", MedForm.Pill),
        MedicineEntity("Lisinopril", "Lisinopril", "10mg", MedForm.Pill),
        MedicineEntity("Amlokind 5", "Amlodipine", "5mg", MedForm.Pill),
        MedicineEntity("Amlodipine", "Amlodipine Besylate", "5mg", MedForm.Pill),
        MedicineEntity("Cetirizine", "Cetirizine Hydrochloride", "10mg", MedForm.Pill),
        MedicineEntity("Cetzine", "Cetirizine", "10mg", MedForm.Pill),
        MedicineEntity("Allegra 120", "Fexofenadine", "120mg", MedForm.Pill),
        MedicineEntity("Ciprofloxacin", "Ciprofloxacin", "500mg", MedForm.Pill),
        MedicineEntity("Ciplox", "Ciprofloxacin", "500mg", MedForm.Pill),
        MedicineEntity("Levofloxacin", "Levofloxacin", "500mg", MedForm.Pill),
        MedicineEntity("Levoquin", "Levofloxacin", "500mg", MedForm.Pill),
        MedicineEntity("Cefixime", "Cefixime", "200mg", MedForm.Pill),
        MedicineEntity("Taxim O 200", "Cefixime", "200mg", MedForm.Pill),
        MedicineEntity("Ibuprofen", "Ibuprofen", "400mg", MedForm.Pill),
        MedicineEntity("Combiflam", "Ibuprofen + Paracetamol", "400mg", MedForm.Pill),
        MedicineEntity("Omeprazole", "Omeprazole", "20mg", MedForm.Pill),
        MedicineEntity("Omez", "Omeprazole", "20mg", MedForm.Pill),
        MedicineEntity("Ranitidine", "Ranitidine Hydrochloride", "150mg", MedForm.Pill),
        MedicineEntity("Rantac 150", "Ranitidine", "150mg", MedForm.Pill),
        MedicineEntity("Ondansetron", "Ondansetron", "4mg", MedForm.Pill),
        MedicineEntity("Emset 4", "Ondansetron", "4mg", MedForm.Pill),
        MedicineEntity("Voveran SR", "Diclofenac Sodium", "100mg", MedForm.Pill),
        MedicineEntity("Diclofenac", "Diclofenac", "50mg", MedForm.Pill),
        MedicineEntity("UpD3 60K", "Cholecalciferol (Vitamin D3)", "60000 IU", MedForm.Pill),
        MedicineEntity("Vitamin D3", "Cholecalciferol", "60K IU", MedForm.Pill),
        MedicineEntity("Ascoril LS", "Levosalbutamol + Ambroxol + Guaiphenesin", "100ml", MedForm.Syrup),
        MedicineEntity("Benadryl", "Diphenhydramine", "100ml", MedForm.Syrup),
        MedicineEntity("Corex", "Chlorpheniramine + Codeine", "100ml", MedForm.Syrup),
        MedicineEntity("Zyrtec Syrup", "Cetirizine", "60ml", MedForm.Syrup),
        MedicineEntity("Dolo Suspension", "Paracetamol", "60ml", MedForm.Syrup),
        MedicineEntity("Insulin Glargine", "Insulin Glargine", "100 IU/ml", MedForm.Injection),
        MedicineEntity("Lantus", "Insulin Glargine", "100 IU/ml", MedForm.Injection),
        MedicineEntity("Tetanus Toxoid", "Tetanus Vaccine", "0.5ml", MedForm.Injection),
        MedicineEntity("Ciplox Eye Drops", "Ciprofloxacin", "10ml", MedForm.Drops),
        MedicineEntity("Refresh Tears", "Carboxymethylcellulose", "10ml", MedForm.Drops),
        MedicineEntity("Asthalin Inhaler", "Salbutamol", "100 mcg", MedForm.Inhaler),
        MedicineEntity("Foracort Inhaler", "Formoterol + Budesonide", "200 mcg", MedForm.Inhaler),
        MedicineEntity("ORS Powder", "Oral Rehydration Salts", "21g", MedForm.Powder),
        MedicineEntity("Econorm Powder", "Saccharomyces Boulardii", "250mg", MedForm.Powder)
    )

    fun findBestMatch(rawText: String): DatabaseMatchResult? {
        if (rawText.length < 2) return null
        val lowerText = rawText.lowercase(Locale.ROOT)

        var bestMatch: MedicineEntity? = null
        var highestScore = 0f
        var isBrandMatch = false

        // Extract potential dosage string (e.g. 250mg, 500mg, 650mg, 60K)
        val dosageMatch = Regex("(?i)\\b\\d+\\s*(mg|g|mcg|ml|iu|k)?\\b").find(rawText)?.value ?: ""

        for (item in dataset) {
            val brandLower = item.brandName.lowercase(Locale.ROOT)
            val saltLower = item.genericSalt.lowercase(Locale.ROOT)

            var score = 0f
            var itemBrandMatched = false

            // Exact or substring match for brand
            if (lowerText.contains(brandLower) || (brandLower.length >= 4 && lowerText.contains(brandLower.take(4)))) {
                score += 0.8f
                itemBrandMatched = true
            } else {
                val textTokens = lowerText.split(" ", "-", "/")
                val brandTokens = brandLower.split(" ", "-", "/")
                val commonBrandTokens = brandTokens.count { bToken ->
                    bToken.length >= 3 && textTokens.any { tToken -> tToken.contains(bToken) || bToken.contains(tToken) }
                }
                if (commonBrandTokens > 0) {
                    score += 0.5f * (commonBrandTokens.toFloat() / brandTokens.size.toFloat())
                    itemBrandMatched = true
                }
            }

            // Substring or token match for generic salt
            if (lowerText.contains(saltLower)) {
                score += 0.5f
            } else {
                val saltTokens = saltLower.split(" ", "-", "+", "/")
                val textTokens = lowerText.split(" ", "-", "+", "/")
                val commonSaltTokens = saltTokens.count { sToken ->
                    sToken.length >= 4 && textTokens.any { tToken -> tToken.contains(sToken) || sToken.contains(tToken) }
                }
                if (commonSaltTokens > 0) {
                    score += 0.3f * (commonSaltTokens.toFloat() / saltTokens.size.toFloat())
                }
            }

            if (score > highestScore) {
                highestScore = score
                bestMatch = item
                isBrandMatch = itemBrandMatched
            }
        }

        return if (highestScore >= 0.45f && bestMatch != null) {
            DatabaseMatchResult(
                entity = bestMatch,
                matchedStrength = dosageMatch.ifEmpty { bestMatch.defaultStrength },
                confidenceScore = highestScore,
                isBrandMatch = isBrandMatch
            )
        } else null
    }
}
