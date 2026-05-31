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
        val score = arguments?.getInt("score") ?: 0
        val correctAnswers = arguments?.getInt("correctAnswers") ?: 0
        val totalQuestions = arguments?.getInt("totalQuestions") ?: 0
        val xpEarned = correctAnswers * 10
        val coinsEarned = correctAnswers * 5

        binding.tvScore.text = score.toString()
        binding.tvCorrectAnswers.text = "$correctAnswers/$totalQuestions"
        binding.tvXPEarned.text = "+$xpEarned"
        binding.tvCoinsEarned.text = "+$coinsEarned"

        binding.tvEmoji.text = when {
            correctAnswers == totalQuestions -> "🏆"
            correctAnswers >= totalQuestions / 2 -> "😊"
            else -> "😢"
        }
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