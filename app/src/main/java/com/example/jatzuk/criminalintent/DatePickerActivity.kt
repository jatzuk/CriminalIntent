//package com.example.jatzuk.criminalintent
//
//import android.content.Context
//import android.content.Intent
//import android.support.v4.app.Fragment
//import java.util.*
//
//class DatePickerActivity : SingleFragmentActivity() {
//    companion object {
//        private const val EXTRA_CRIME_DATE = "com.example.jatzuk.criminalintent.crime_date"
//
//        fun newIntent(context: Context, date: Date): Intent {
//            return Intent(context, DatePickerActivity::class.java).putExtra(EXTRA_CRIME_DATE, date)
//        }
//    }
//
//    override fun createFragment(): Fragment {
//        val date = intent.getSerializableExtra(EXTRA_CRIME_DATE) as Date
//        return DatePickerFragment.newInstance(date)
//    }
//
//}