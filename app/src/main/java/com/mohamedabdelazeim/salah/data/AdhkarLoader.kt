package com.mohamedabdelazeim.salah.data

import android.content.Context
import org.json.JSONObject

data class AdhkarItem(val id: String, val title: String, val text: String, val repeat: Int, val benefit: String)

object AdhkarLoader {
    fun load(ctx: Context, isMorning: Boolean): List<AdhkarItem> {
        return try {
            val file = if (isMorning) "morning_adhkar.json" else "evening_adhkar.json"
            val json = ctx.assets.open(file).bufferedReader().use { it.readText() }
            val root = JSONObject(json)
            val arr = root.getJSONArray("adhkar")
            (0 until arr.length()).map {
                val item = arr.getJSONObject(it)
                AdhkarItem(
                    id = item.optString("id"),
                    title = item.optString("title"),
                    text = item.optString("text"),
                    repeat = item.optInt("repeat", 1),
                    benefit = item.optString("benefit")
                )
            }
        } catch (e: Exception) { emptyList() }
    }
}
