package com.triviaclash.app.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.triviaclash.app.R
import com.triviaclash.app.databinding.FragmentResultsBinding

class ResultsFragment : Fragment(R.layout.fragment_results) {

    private var _binding: FragmentResultsBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentResultsBinding.bind(view)

        setupUI()
        setupClickListeners()
    }

    private fun setupUI() {
        binding.tvScore.text = "850"
        binding.tvCorrectAnswers.text = "8/10"
        binding.tvXPEarned.text = "+80"
        binding.tvCoinsEarned.text = "+40"
    }

    private fun setupClickListeners() {
        binding.btnPlayAgain.setOnClickListener {
            findNavController().navigate(R.id.action_results_to_categories)
        }

        binding.btnBackHome.setOnClickListener {
            findNavController().navigate(R.id.action_results_to_home)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}