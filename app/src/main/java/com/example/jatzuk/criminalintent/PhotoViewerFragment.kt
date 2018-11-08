package com.example.jatzuk.criminalintent

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.widget.ImageView

class PhotoViewerFragment : DialogFragment() {
    companion object {
        private const val ARG_PATH = "path"

        fun newInstance(path: String): PhotoViewerFragment {
            return PhotoViewerFragment().apply {
                arguments = Bundle().apply { putSerializable(ARG_PATH, path) }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val v = LayoutInflater.from(activity!!).inflate(R.layout.dialog_photo, null)
        v.findViewById<ImageView>(R.id.dialog_photo_viewer).setImageBitmap(getScaledBitmap(arguments?.getSerializable(ARG_PATH) as String, CrimeFragment.photoWidth, CrimeFragment.photoHeight))

        return AlertDialog.Builder(activity!!)
                .setView(v)
                .create()
    }
}
