package com.example.gestures

import android.content.Context
import android.gesture.Gesture
import android.gesture.GestureLibraries
import android.gesture.GestureLibrary
import java.io.File

object GestureRepository {

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
}
