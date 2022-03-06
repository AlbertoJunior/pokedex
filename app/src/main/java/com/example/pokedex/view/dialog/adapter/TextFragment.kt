package com.example.pokedex.view.dialog.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.example.pokedex.core.textColorByText
import com.example.pokedex.databinding.FragmentTextBinding

class TextFragment : Fragment() {

    private lateinit var binding: FragmentTextBinding

    companion object {
        fun newInstance(text: String, color: String?) = TextFragment().apply {
            arguments = bundleOf(
                "text" to text,
                "color" to color
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTextBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            binding.tvText.text = it.getString("text")
            binding.tvText.textColorByText(it.getString("color"))
        }
    }
}