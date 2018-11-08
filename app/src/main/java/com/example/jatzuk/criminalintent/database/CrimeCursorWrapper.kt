package com.example.jatzuk.criminalintent.database

import android.database.Cursor
import android.database.CursorWrapper
import com.example.jatzuk.criminalintent.Crime
import java.util.*

class CrimeCursorWrapper(cursor: Cursor) : CursorWrapper(cursor) {
    fun getCrime(): Crime {
        val uuidString = getString(getColumnIndex(Cols.UUID))
        val title = getString(getColumnIndex(Cols.TITLE))
        val date = getLong(getColumnIndex(Cols.DATE))
        val isSolved = getInt(getColumnIndex(Cols.SOLVED))
        val crime = Crime(UUID.fromString(uuidString), title, Date(date), isSolved != 0)
        crime.suspect = getString(getColumnIndex(Cols.SUSPECT))
        return crime
    }
}