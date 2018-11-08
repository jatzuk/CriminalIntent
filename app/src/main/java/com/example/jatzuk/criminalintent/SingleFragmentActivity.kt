package com.example.jatzuk.criminalintent

import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity

abstract class SingleFragmentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutResId())

        val fm = supportFragmentManager
        with(fm.findFragmentById(R.id.fragment_container)) {
            if (this == null) fm.beginTransaction().add(R.id.fragment_container, createFragment()).commit()
        }
    }

    protected abstract fun createFragment(): Fragment

    @LayoutRes
    protected open fun getLayoutResId() = R.layout.activity_fragment
}