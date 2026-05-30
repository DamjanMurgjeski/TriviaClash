package com.triviaclash.app.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.triviaclash.app.R
import com.triviaclash.app.databinding.FragmentQuizBinding
import com.triviaclash.app.models.Question
import com.triviaclash.app.viewmodel.QuizState
import com.triviaclash.app.viewmodel.QuizViewModel
import kotlinx.coroutines.launch

class QuizFragment : Fragment(R.layout.fragment_quiz) {

    private var _binding: FragmentQuizBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentQuizBinding.bind(view)

        setupClickListeners()
        observeViewModel()
        loadDemoQuestions()
    }

    private fun loadDemoQuestions() {
        val demoQuestion = Question(
            id = "1",
            quizId = "demo",
            questionText = "What is the capital of France?",
            optionA = "London",
            optionB = "Paris",
            optionC = "Berlin",
            optionD = "Madrid",
            correctAnswer = "B"
        )
        displayQuestion(demoQuestion)
    }

    private fun displayQuestion(question: Question) {
        binding.tvQuestion.text = question.questionText
        binding.btnAnswerA.text = "A. ${question.optionA}"
        binding.btnAnswerB.text = "B. ${question.optionB}"
        binding.btnAnswerC.text = "C. ${question.optionC}"
        binding.btnAnswerD.text = "D. ${question.optionD}"
    }

    private fun setupClickListeners() {
        binding.btnAnswerA.setOnClickListener { checkAnswer("A") }
        binding.btnAnswerB.setOnClickListener { checkAnswer("B") }
        binding.btnAnswerC.setOnClickListener { checkAnswer("C") }
        binding.btnAnswerD.setOnClickListener { checkAnswer("D") }
    }

    private fun checkAnswer(answer: String) {
        disableAnswerButtons()
        if (answer == "B") {
            binding.btnAnswerB.setBackgroundColor(Color.parseColor("#4CAF50"))
        } else {
            when (answer) {
                "A" -> binding.btnAnswerA.setBackgroundColor(Color.parseColor("#F44336"))
                "C" -> binding.btnAnswerC.setBackgroundColor(Color.parseColor("#F44336"))
                "D" -> binding.btnAnswerD.setBackgroundColor(Color.parseColor("#F44336"))
            }
            binding.btnAnswerB.setBackgroundColor(Color.parseColor("#4CAF50"))
        }

        binding.root.postDelayed({
            findNavController().navigate(R.id.action_quiz_to_results)
        }, 1500)
    }

    private fun disableAnswerButtons() {
        binding.btnAnswerA.isEnabled = false
        binding.btnAnswerB.isEnabled = false
        binding.btnAnswerC.isEnabled = false
        binding.btnAnswerD.isEnabled = false
    }

    private fun observeViewModel() {}

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}