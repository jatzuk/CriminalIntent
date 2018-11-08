package com.example.jatzuk.criminalintent

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import java.util.*

class CrimePagerActivity : AppCompatActivity(), CrimeFragment.Callback {
    companion object {
        private const val EXTRA_CRIME_UUID = "com.example.jatzuk.criminalintent.crime_uuid"
        private const val MIN_SCALE = 0.85f
        private const val MIN_ALPHA = 0.5f

        fun newIntent(context: Context?, uuid: UUID): Intent {
            return Intent(context, CrimePagerActivity::class.java).putExtra(EXTRA_CRIME_UUID, uuid)
        }
    }

    private lateinit var viewPager: ViewPager
    private lateinit var crimes: List<Crime>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crime_pager)

        val uuid = intent.getSerializableExtra(EXTRA_CRIME_UUID) as UUID

        viewPager = findViewById(R.id.crime_view_pager)
        viewPager.setPageTransformer(true) { view, position ->
            view.apply {
                when {
                    position < -1 -> alpha = 0f
                    position <= 1 -> {
                        val scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position))
                        val vMargin = height * (1 - scaleFactor) / 2
                        val hMargin = width * (1 - scaleFactor) / 2
                        translationX = if (position < 0) hMargin - vMargin / 2
                        else hMargin + vMargin / 2
                        scaleX = scaleFactor
                        scaleY = scaleFactor
                        alpha = (MIN_ALPHA + (((scaleFactor - MIN_SCALE) / (1 - MIN_SCALE)) * (1 - MIN_ALPHA)))
                    }
                    else -> alpha = 0f
                }
            }
        }

        crimes = CrimeLab.getCrimes()
        val fm = supportFragmentManager
        viewPager.adapter = object : FragmentStatePagerAdapter(fm) {
            override fun getItem(position: Int) = CrimeFragment.newInstance(crimes[position].uuid)

            override fun getCount() = crimes.size
        }

        val firstBtn = findViewById<Button>(R.id.btn_first)
        firstBtn.setOnClickListener { viewPager.currentItem = 0 }

        val lastBtn = findViewById<Button>(R.id.btn_last)
        lastBtn.setOnClickListener { viewPager.currentItem = (viewPager.adapter as FragmentStatePagerAdapter).count }

        viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                firstBtn.isEnabled = position > 0
                lastBtn.isEnabled = position < crimes.size - 1
            }
        })

        for (i in 0 until crimes.size) {
            if (crimes[i].uuid == uuid) {
                viewPager.currentItem = i
                break
            }
        }
    }

    override fun onCrimeUpdated(crime: Crime) {}
}
