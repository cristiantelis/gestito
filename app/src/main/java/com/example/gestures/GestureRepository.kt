package com.example.gestures

import android.content.Context
import android.gesture.Gesture
import android.gesture.GestureLibraries
import android.gesture.GestureLibrary
import org.json.JSONObject
import java.io.File

object GestureRepository {

    private const val MAPPINGS_PREF = "gesture_mappings"

    private fun gesturesDir(context: Context): File {
        val dir = File(context.filesDir, "gestures")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun gesturesFile(context: Context): File {
        return File(gesturesDir(context), "gestures")
    }

    fun getLibrary(context: Context): GestureLibrary? {
        val file = gesturesFile(context)
        if (file.exists()) {
            val lib = GestureLibraries.fromFile(file.path)
            if (lib.load()) return lib
        }
        return null
    }

    fun addGesture(context: Context, name: String, gesture: Gesture): Boolean {
        val file = gesturesFile(context)
        val lib = GestureLibraries.fromFile(file.path)
        lib.addGesture(name, gesture)
        return lib.save()
    }

    fun getGestureNames(context: Context): List<String> {
        val lib = getLibrary(context) ?: return emptyList()
        return lib.gestureEntries.toList()
    }

    // Mappings: JSON object stored in SharedPreferences: { "gesture_name": "actionString", ... }
    fun saveMapping(context: Context, gestureName: String, action: String) {
        val prefs = context.getSharedPreferences(MAPPINGS_PREF, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(MAPPINGS_PREF, "{}")
        val obj = JSONObject(jsonStr!!)
        obj.put(gestureName, action)
        prefs.edit().putString(MAPPINGS_PREF, obj.toString()).apply()
    }

    fun removeMapping(context: Context, gestureName: String) {
        val prefs = context.getSharedPreferences(MAPPINGS_PREF, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(MAPPINGS_PREF, "{}")
        val obj = JSONObject(jsonStr!!)
        obj.remove(gestureName)
        prefs.edit().putString(MAPPINGS_PREF, obj.toString()).apply()
    }

    fun loadMappings(context: Context): Map<String, String> {
        val prefs = context.getSharedPreferences(MAPPINGS_PREF, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(MAPPINGS_PREF, "{}")
        val obj = JSONObject(jsonStr!!)
        val res = mutableMapOf<String, String>()
        val keys = obj.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            res[k] = obj.getString(k)
        }
        return res
    }

    fun getActionForGesture(context: Context, gestureName: String): String? {
        return loadMappings(context)[gestureName]
    }
}
