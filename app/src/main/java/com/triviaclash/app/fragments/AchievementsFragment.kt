package com.triviaclash.app.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.triviaclash.app.R
import com.triviaclash.app.databinding.FragmentAchievementsBinding

class AchievementsFragment : Fragment(R.layout.fragment_achievements) {

    private var _binding: FragmentAchievementsBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAchievementsBinding.bind(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}