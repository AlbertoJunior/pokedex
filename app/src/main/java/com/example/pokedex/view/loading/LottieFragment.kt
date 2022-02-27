package com.example.pokedex.view.loading

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.pokedex.databinding.FragmentWalkingBinding

class LottieFragment : Fragment() {
    private lateinit var binding: FragmentWalkingBinding

    companion object {
        fun newInstance() = LottieFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWalkingBinding.inflate(inflater, container, false)
        return binding.root
    }

}