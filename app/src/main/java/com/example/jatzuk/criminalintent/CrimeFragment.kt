package com.example.jatzuk.criminalintent

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.app.ShareCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.*
import android.widget.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CrimeFragment : Fragment() {
    interface Callback {
        fun onCrimeUpdated(crime: Crime)
    }

    companion object {
        private const val ARG_CRIME_UUID = "crime_uuid"
        private const val DIALOG_DATE = "dialog_date"
        private const val DIALOG_TIME = "dialog_time"
        private const val DIALOG_PHOTO = "dialog_photo"

        private const val REQUEST_DATE = 0
        private const val REQUEST_TIME = 1
        private const val REQUEST_CONTACT = 2
        private const val REQUEST_PHOTO = 3

        var photoWidth = 0
            private set
        var photoHeight = 0
            private set

        fun newInstance(uuid: UUID): CrimeFragment {
            val args = Bundle()
            args.putSerializable(ARG_CRIME_UUID, uuid)
            val fragment = CrimeFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var crime: Crime
    private lateinit var title: EditText
    private lateinit var dateButton: Button
    private lateinit var timeButton: Button
    private lateinit var isSolved: CheckBox
    private lateinit var suspectButton: Button
    private lateinit var callButton: Button
    private lateinit var photoButton: ImageButton
    private lateinit var photoView: ImageView
    private lateinit var photoFile: File
    private var callback: Callback? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        callback = context as Callback
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uuid = arguments?.getSerializable(ARG_CRIME_UUID) as UUID
        crime = CrimeLab.getCrime(uuid) ?: Crime()
        photoFile = CrimeLab.getPhotoFile(crime)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_crime, container, false)

        title = v.findViewById(R.id.crime_title)
        title.setText(crime.title)
        title.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                crime.title = s.toString()
                updateCrime()
            }
        })

        dateButton = v.findViewById(R.id.crime_date)
        updateDate()
        dateButton.setOnClickListener {
            val dialog = DatePickerFragment.newInstance(crime.date)
            dialog.setTargetFragment(this, REQUEST_DATE)
            dialog.show(fragmentManager, DIALOG_DATE)
//            startActivityForResult(DatePickerActivity.newIntent(context!!, crime.date), REQUEST_DATE)
        }

        timeButton = v.findViewById(R.id.crime_time)
        updateTime()
        timeButton.setOnClickListener {
            val dialog = TimePickerFragment.newInstance(crime.date)
            dialog.setTargetFragment(this, REQUEST_TIME)
            dialog.show(fragmentManager, DIALOG_TIME)
        }

        isSolved = v.findViewById(R.id.crime_solved)
        isSolved.isChecked = crime.isSolved
        isSolved.setOnCheckedChangeListener { _, isChecked ->
            crime.isSolved = isChecked
            updateCrime()
        }

        val reportButton = v.findViewById<Button>(R.id.crime_report)
        reportButton.setOnClickListener {
            //            startActivity(Intent.createChooser(Intent().apply {
//                type = "text/plain"
//                putExtra(Intent.EXTRA_TEXT, getCrimeReport())
//                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject))
//            }, getString(R.string.send_crime_report)))
            startActivity(ShareCompat.IntentBuilder.from(activity)
                    .setType("text/plain")
                    .setChooserTitle(R.string.send_crime_report)
                    .setSubject(getString(R.string.crime_report_suspect))
                    .setText(getCrimeReport())
                    .createChooserIntent())
        }

        val pickContact = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        suspectButton = v.findViewById(R.id.crime_suspect)
        if (crime.suspect.isNotEmpty()) suspectButton.text = crime.suspect
        if (activity?.packageManager?.resolveActivity(pickContact, PackageManager.MATCH_DEFAULT_ONLY) == null) suspectButton.isEnabled = false
        suspectButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
                requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), REQUEST_CONTACT)
            else startActivityForResult(pickContact, REQUEST_CONTACT)
        }

        callButton = v.findViewById(R.id.suspect_call)
        if (crime.suspect.isEmpty()) callButton.isEnabled = false
        callButton.setOnClickListener { startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${crime.phoneNumber}"))) }

        val imageIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        photoButton = v.findViewById(R.id.crime_camera)
        photoButton.isEnabled = photoFile.path.isNotEmpty() && imageIntent.resolveActivity(activity!!.packageManager) != null
        photoButton.setOnClickListener {
            val uri = FileProvider.getUriForFile(activity!!, "com.example.jatzuk.criminalintent.fileprovider", photoFile)
            imageIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            for (a in activity!!.packageManager.queryIntentActivities(imageIntent, PackageManager.MATCH_DEFAULT_ONLY)) {
                activity?.grantUriPermission(a.activityInfo.packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
            startActivityForResult(imageIntent, REQUEST_PHOTO)
        }

        photoView = v.findViewById(R.id.crime_photo)
        photoView.setOnClickListener {
            if (photoFile.exists()) PhotoViewerFragment.newInstance(photoFile.path).show(fragmentManager, DIALOG_PHOTO)
        }

        val observer = photoView.viewTreeObserver
        if (observer!!.isAlive) {
            observer.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    photoView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    photoWidth = photoView.maxWidth
                    photoHeight = photoView.maxHeight
                    updatePhotoView()
                }
            })
        }

        return v
    }

    override fun onPause() {
        super.onPause()
        CrimeLab.updateCrime(crime)
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.fragment_crime, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.delete_crime -> {
                crime.title = ""
                updateCrime()
                activity?.finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) return
        if (requestCode == REQUEST_DATE) {
            crime.date = (data?.getSerializableExtra(DatePickerFragment.EXTRA_DATE) as Date)
            updateDate()
        } else if (requestCode == REQUEST_TIME) {
            updateTime(data)
        } else if (requestCode == REQUEST_CONTACT && data != null) {
            val contactUri = data.data!!
            val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts._ID)
            activity?.contentResolver?.query(contactUri, queryFields, null, null, null)?.use {
                if (it.count == 0) return
                it.moveToFirst()
                val suspect = it.getString(it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                val id = it.getString(it.getColumnIndex(ContactsContract.Contacts._ID))
                crime.suspect = suspect!!
                suspectButton.text = suspect
                crime.phoneNumber = getPhoneNumber(id)
            }
        } else if (requestCode == REQUEST_PHOTO) {
            activity?.revokeUriPermission(FileProvider.getUriForFile(activity!!, "com.example.jatzuk.criminalintent.fileprovider", photoFile), Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            updatePhotoView()
            photoView.postDelayed({ photoView.announceForAccessibility(getString(R.string.crime_photo_view_has_updated_announcement)) }, 100)
        }
        updateCrime()
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CONTACT -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startActivityForResult(Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), REQUEST_CONTACT)
                }
            }
        }
    }

    private fun updateCrime() {
        CrimeLab.updateCrime(crime)
        callback?.onCrimeUpdated(crime)
    }

    private fun updateDate() {
        val tm = SimpleDateFormat(getString(R.string.date_format), Locale.getDefault())
        dateButton.text = tm.format(crime.date)
    }

    private fun updateTime(data: Intent? = null) {
        val tm = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = (data?.getSerializableExtra(TimePickerFragment.EXTRA_TIME) as Date?)
        timeButton.text = tm.format(
                if (data != null) date
                else crime.date
        )
    }

    private fun getCrimeReport(): String {
        val dateFormat = getString(R.string.date_format)
        val dateString = DateFormat.format(dateFormat, crime.date).toString()
        val solvedString = if (crime.isSolved) getString(R.string.crime_report_solved) else getString(R.string.crime_report_unsolved)
        val suspect =
                if (crime.suspect.isEmpty()) getString(R.string.crime_report_no_suspect)
                else getString(R.string.crime_report_suspect, crime.suspect)
        return getString(R.string.crime_report, crime.title, dateString, solvedString, suspect)
    }

    private fun getPhoneNumber(id: String?): String {
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val queryFields = arrayOf(ContactsContract.Data.CONTACT_ID, ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.TYPE)
        val selectionClause = ContactsContract.Data.CONTACT_ID + " = ?"
        activity?.contentResolver?.query(uri, queryFields, selectionClause, arrayOf(id), null)?.use {
            if (it.count == 0) return ""
            while (it.moveToNext()) {
                val phoneType = it.getInt(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE))
                if (phoneType == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE) {
                    callButton.isEnabled = true
                    return it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA))
                }
            }
        }
        return ""
    }

    private fun updatePhotoView() {
        if (photoFile.path.isEmpty() || !photoFile.exists()) {
            photoView.setImageDrawable(null)
            photoView.contentDescription = getString(R.string.crime_photo_no_image_description)
        } else {
            photoView.setImageBitmap(getScaledBitmap(photoFile.path, photoWidth, photoHeight))
            photoView.contentDescription = getString(R.string.crime_photo_image_description)
        }
    }
}