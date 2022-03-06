package com.example.pokedex.view.dialog.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class TextViewPager(
    fragment: Fragment,
    val list: List<FragmentDetail>
) : FragmentStateAdapter(fragment) {

    class FragmentDetail(val text: String, val color: String?)

    override fun getItemCount() = list.size

    override fun createFragment(position: Int) =
        TextFragment.newInstance(list[position].text, list[position].color)
}