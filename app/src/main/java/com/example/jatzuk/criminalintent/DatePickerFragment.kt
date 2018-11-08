package com.example.jatzuk.criminalintent

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.widget.DatePicker
import java.util.*

class DatePickerFragment : DialogFragment() {
    companion object {
        private const val ARG_DATE = "date"
        const val EXTRA_DATE = "com.example.jatzuk.criminalintent.date"

        fun newInstance(date: Date): DatePickerFragment {
            return DatePickerFragment().apply {
                arguments = Bundle().apply { putSerializable(ARG_DATE, date) }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val date = arguments?.getSerializable(ARG_DATE) as Date
        val calendar = Calendar.getInstance()
        calendar.time = date

        val v = LayoutInflater.from(activity!!).inflate(R.layout.dialog_date, null)

        val datePicker = v.findViewById<DatePicker>(R.id.dialog_date_picker)
        datePicker.init(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH),
            null
        )

        return AlertDialog.Builder(activity!!)
            .setView(v)
            .setTitle(R.string.date_picker_title)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                sendResult(
                    Activity.RESULT_OK, GregorianCalendar(
                        datePicker.year,
                        datePicker.month,
                        datePicker.dayOfMonth
                    ).time
                )
            }
            .create()
    }

//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        val v = inflater.inflate(R.layout.dialog_date, container, false)
//
//        val date = arguments?.getSerializable(ARG_DATE) as Date
//        val calendar = Calendar.getInstance()
//        calendar.time = date
//
//        val datePicker = v.findViewById<DatePicker>(R.id.dialog_date_picker)
//        datePicker.init(
//            calendar.get(Calendar.YEAR),
//            calendar.get(Calendar.MONTH),
//            calendar.get(Calendar.DAY_OF_MONTH),
//            null
//        )
//
//        val buttonOk = v.findViewById<Button>(R.id.date_picker_ok_button)
//        buttonOk.setOnClickListener {
//            sendResult(
//                Activity.RESULT_OK,
//                GregorianCalendar(datePicker.year, datePicker.month, datePicker.dayOfMonth).time
//            )
//        }
//        return v
//    }

    private fun sendResult(resultCode: Int, date: Date) {
//        activity?.setResult(resultCode, Intent().putExtra(EXTRA_DATE, date))
//        activity?.finish()
        if (targetFragment == null) return
        targetFragment?.onActivityResult(targetRequestCode, resultCode, Intent().putExtra(EXTRA_DATE, date))
    }
}