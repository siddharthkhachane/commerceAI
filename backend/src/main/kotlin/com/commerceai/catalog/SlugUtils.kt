package com.commerceai.catalog

import java.text.Normalizer
import java.util.Locale

fun slugify(value: String): String {
    val normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "")
        .lowercase(Locale.US)
        .replace(Regex("[^a-z0-9\\s-]"), "")
        .trim()
        .replace(Regex("\\s+"), "-")
        .replace(Regex("-+"), "-")

    return normalized.ifBlank { "item" }
}
