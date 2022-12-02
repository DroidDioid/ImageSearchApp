package ru.tim.imagesearchapp

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager

/** Отвечает за сохранение данных с помощью [SharedPreferences][android.content.SharedPreferences].*/
object QueryPreferences {

    private const val PREF_SEARCH_QUERY = "search_query"

    /** Извлекает текст запроса из SharedPreferences. */
    fun getStoredQuery(context: Context): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString(PREF_SEARCH_QUERY, "")!!
    }

    /** Сохраняет текст запроса в SharedPreferences. */
    fun setStoredQuery(context: Context, query: String) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit { putString(PREF_SEARCH_QUERY, query) }
    }
}