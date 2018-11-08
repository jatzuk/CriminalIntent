package com.example.jatzuk.criminalintent

import android.view.View

class CrimeListActivity : SingleFragmentActivity(), CrimeListFragment.Callback,/* CrimeListFragment.Deleted,*/ CrimeFragment.Callback {
    override fun createFragment() = CrimeListFragment()

    override fun getLayoutResId() = R.layout.activity_masterdetail

    override fun onCrimeSelected(crime: Crime) {
        if (findViewById<View>(R.id.detail_fragment_container) == null) startActivity(CrimePagerActivity.newIntent(this, crime.uuid))
        else supportFragmentManager.beginTransaction().replace(R.id.detail_fragment_container, CrimeFragment.newInstance(crime.uuid)).commit()
    }

    override fun onCrimeUpdated(crime: Crime) {
        (supportFragmentManager.findFragmentById(R.id.fragment_container) as CrimeListFragment).updateUI()
    }
}