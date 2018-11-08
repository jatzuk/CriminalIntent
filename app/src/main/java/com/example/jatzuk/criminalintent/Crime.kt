package com.example.jatzuk.criminalintent

import java.util.*

class Crime(val uuid: UUID = UUID.randomUUID(), var title: String = "", var date: Date = Date(), var isSolved: Boolean = false) {
    var suspect = ""
    var phoneNumber = ""

    fun getPhotoFileName() = "IMG_$uuid.jpg"
}