package com.example.jatzuk.criminalintent

import android.content.Context
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.*
import android.view.LayoutInflater
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import java.text.DateFormat
import java.util.*


class CrimeListFragment : Fragment() {
    interface Callback {
        fun onCrimeSelected(crime: Crime)
    }

    companion object {
        private const val SAVED_SUBTITLE_VISIBLE = "subtitle"
    }

    private lateinit var recyclerView: RecyclerView
    private var adapter: CrimeAdapter? = null
    private var pos = -1
    private var isSubtitleVisible = true
    private var callback: Callback? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        callback = context as Callback
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_crime_list, container, false)
        recyclerView = v.findViewById(R.id.crime_recycler_view)
        recyclerView.layoutAnimation = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_waterfall)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        itemTouchHelperAttachToRecyclerView()
        isSubtitleVisible = savedInstanceState?.getBoolean(SAVED_SUBTITLE_VISIBLE) ?: isSubtitleVisible
        updateUI()
        return v
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE, isSubtitleVisible)
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.fragment_crime_list, menu)

        val subtitle = menu?.findItem(R.id.show_subtitle)
        subtitle?.setTitle(
                if (isSubtitleVisible) R.string.show_subtitle
                else R.string.hide_subtitle
        )
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.new_crime -> {
                val crime = Crime()
                CrimeLab.addCrime(crime)
//                updateUI()
                callback?.onCrimeSelected(crime)
                true
            }
            R.id.show_subtitle -> {
                isSubtitleVisible = !isSubtitleVisible
                activity?.invalidateOptionsMenu()
                updateSubtitle()
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun updateUI() {
        val crimes = CrimeLab.getCrimes()
        view?.findViewById<ConstraintLayout>(R.id.empty_list_layout)?.visibility =
                if (crimes.isEmpty()) View.VISIBLE else View.INVISIBLE

        if (adapter == null) {
            adapter = CrimeAdapter(crimes)
            recyclerView.adapter = adapter
        } else {
            adapter?.crimes = crimes
            if (pos > -1 && pos > crimes.size - 1) adapter?.notifyItemChanged(pos)
            else adapter?.notifyDataSetChanged()
        }
        updateSubtitle()
    }

    private fun updateSubtitle(): Boolean {
        (activity as AppCompatActivity).supportActionBar?.subtitle =
                if (isSubtitleVisible) ""
                else {
                    val size = CrimeLab.getCrimes().size
                    resources.getQuantityString(R.plurals.subtitle_plural, size, size)
                }
        return true
    }

    private fun itemTouchHelperAttachToRecyclerView() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                adapter?.swap(viewHolder.adapterPosition, target.adapterPosition)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, position: Int) {
                val crime = adapter?.crimes!![viewHolder.adapterPosition]
                CrimeLab.removeCrime(crime.uuid)
                updateUI()
            }
        }).apply { attachToRecyclerView(recyclerView) }
    }

    private operator fun <E> List<E>.get(uuid: UUID): Int {
        val crimes = CrimeLab.getCrimes()
        for (i in 0 until crimes.size) if (crimes[i].uuid == uuid) return i
        return -1
    }

    private inner class CrimeHolder(layoutInflater: LayoutInflater, parent: ViewGroup) : RecyclerView.ViewHolder(layoutInflater.inflate(R.layout.list_item_crime, parent, false)) {
        private lateinit var crime: Crime
        private val titleTextView = itemView.findViewById<TextView>(R.id.crime_title)
        private val dateTextView = itemView.findViewById<TextView>(R.id.crime_date)
        private val solvedImageView = itemView.findViewById<ImageView>(R.id.crime_solved)
        private val constraintLayout = itemView.findViewById<ConstraintLayout>(R.id.constraintLayout)

        init {
            itemView.setOnClickListener {
                callback?.onCrimeSelected(crime)
                pos = adapterPosition
            }
        }

        fun bind(crime: Crime) {
            this.crime = crime
            titleTextView.text = crime.title
            var dateFormat = DateFormat.getDateInstance(DateFormat.FULL)
            val date = StringBuilder(dateFormat.format(crime.date).substringBefore(","))
            dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM)
            date.append(", ").append(dateFormat.format(crime.date))
            dateTextView.text = date
            solvedImageView.visibility = if (crime.isSolved) View.VISIBLE else View.GONE
            val speechText = if (crime.isSolved) getString(R.string.crime_solved_image_description) else getString(R.string.crime_not_solved_image_description)
            constraintLayout.contentDescription = crime.title + dateTextView.text + speechText
        }
    }

    private inner class CrimeAdapter(var crimes: List<Crime>) : RecyclerView.Adapter<CrimeHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = CrimeHolder(LayoutInflater.from(activity), parent)

        override fun getItemCount() = crimes.size

        override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
            holder.bind(crimes[position])
        }

        fun swap(firstPosition: Int, secondPosition: Int) {
            Collections.swap(crimes, firstPosition, secondPosition)
            notifyItemMoved(firstPosition, secondPosition)
        }
    }
}