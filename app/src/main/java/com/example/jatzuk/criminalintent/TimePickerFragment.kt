package com.example.jatzuk.criminalintent

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.widget.TimePicker
import java.util.*

class TimePickerFragment : DialogFragment() {
    companion object {
        private const val ARG_TIME = "time"
        const val EXTRA_TIME = "com.example.jatzuk.criminalintent.time"

        fun newInstance(date: Date): TimePickerFragment {
            return TimePickerFragment().apply { arguments = Bundle().apply { putSerializable(ARG_TIME, date) } }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val date = arguments?.getSerializable(ARG_TIME) as Date
        val calendar = Calendar.getInstance()
        calendar.time = date

        val v = LayoutInflater.from(activity!!).inflate(R.layout.dialog_time, null)

        val timePicker = v.findViewById<TimePicker>(R.id.dialog_time_picker)
        timePicker.hour = calendar[Calendar.HOUR_OF_DAY]
        timePicker.minute = calendar[Calendar.MINUTE]

        return AlertDialog.Builder(activity!!)
            .setView(v)
            .setTitle(R.string.time_picker_title)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                calendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
                calendar.set(Calendar.MINUTE, timePicker.minute)
                sendResult(Activity.RESULT_OK, calendar.time)
            }
            .create()
    }

    private fun sendResult(resultCode: Int, date: Date) {
        if (targetFragment == null) return
        targetFragment?.onActivityResult(targetRequestCode, resultCode, Intent().putExtra(EXTRA_TIME, date))
    }
}