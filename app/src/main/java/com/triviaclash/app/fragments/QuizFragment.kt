package com.triviaclash.app.fragments

import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.triviaclash.app.R
import com.triviaclash.app.databinding.FragmentQuizBinding
import com.triviaclash.app.models.Question

class QuizFragment : Fragment(R.layout.fragment_quiz) {

    private var _binding: FragmentQuizBinding? = null
    private val binding get() = _binding!!

    private val questions = mutableListOf<Question>()
    private var currentIndex = 0
    private var score = 0
    private var correctAnswers = 0
    private var timer: CountDownTimer? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentQuizBinding.bind(view)

        loadQuestionsFromFirestore()
    }

    private fun loadQuestionsFromFirestore() {
        val category = arguments?.getString("category") ?: "science"
        val quizId = "quiz_$category"

        val db = FirebaseFirestore.getInstance()
        db.collection("questions")
            .whereEqualTo("quizId", quizId)
            .get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    val question = Question(
                        id = doc.getString("id") ?: "",
                        quizId = doc.getString("quizId") ?: "",
                        questionText = doc.getString("questionText") ?: "",
                        optionA = doc.getString("optionA") ?: "",
                        optionB = doc.getString("optionB") ?: "",
                        optionC = doc.getString("optionC") ?: "",
                        optionD = doc.getString("optionD") ?: "",
                        correctAnswer = doc.getString("correctAnswer") ?: ""
                    )
                    questions.add(question)
                }
                if (questions.isNotEmpty()) {
                    displayQuestion()
                } else {
                    loadDemoQuestions()
                }
            }
            .addOnFailureListener {
                loadDemoQuestions()
            }
    }

    private fun loadDemoQuestions() {
        questions.add(Question("1", "demo", "What is 2+2?", "3", "4", "5", "6", "B"))
        questions.add(Question("2", "demo", "Capital of France?", "London", "Paris", "Berlin", "Madrid", "B"))
        displayQuestion()
    }

    private fun displayQuestion() {
        if (currentIndex >= questions.size) {
            val bundle = Bundle()
            bundle.putInt("score", score)
            bundle.putInt("correctAnswers", correctAnswers)
            bundle.putInt("totalQuestions", questions.size)
            findNavController().navigate(R.id.action_quiz_to_results, bundle)
            return
        }

        val question = questions[currentIndex]
        binding.tvQuestionNumber.text = "Question ${currentIndex + 1}/${questions.size}"
        binding.tvQuestion.text = question.questionText
        binding.btnAnswerA.text = "A. ${question.optionA}"
        binding.btnAnswerB.text = "B. ${question.optionB}"
        binding.btnAnswerC.text = "C. ${question.optionC}"
        binding.btnAnswerD.text = "D. ${question.optionD}"
        binding.tvScore.text = "Score: $score"

        resetButtonColors()
        enableAnswerButtons()
        startTimer()

        binding.btnAnswerA.setOnClickListener { checkAnswer("A") }
        binding.btnAnswerB.setOnClickListener { checkAnswer("B") }
        binding.btnAnswerC.setOnClickListener { checkAnswer("C") }
        binding.btnAnswerD.setOnClickListener { checkAnswer("D") }
    }

    private fun startTimer() {
        timer?.cancel()
        binding.progressTimer.max = 30
        binding.progressTimer.progress = 30

        timer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = (millisUntilFinished / 1000).toInt()
                binding.tvTimer.text = secondsLeft.toString()
                binding.progressTimer.progress = secondsLeft
            }

            override fun onFinish() {
                binding.tvTimer.text = "0"
                disableAnswerButtons()
                binding.root.postDelayed({
                    currentIndex++
                    displayQuestion()
                }, 1500)
            }
        }.start()
    }

    private fun checkAnswer(selected: String) {
        timer?.cancel()
        disableAnswerButtons()

        val correct = questions[currentIndex].correctAnswer

        if (selected == correct) {
            score += 100
            correctAnswers++
            highlightButton(selected, Color.parseColor("#4CAF50"))
        } else {
            highlightButton(selected, Color.parseColor("#F44336"))
            highlightButton(correct, Color.parseColor("#4CAF50"))
        }

        binding.tvScore.text = "Score: $score"

        binding.root.postDelayed({
            currentIndex++
            displayQuestion()
        }, 1500)
    }

    private fun highlightButton(answer: String, color: Int) {
        when (answer) {
            "A" -> binding.btnAnswerA.setBackgroundColor(color)
            "B" -> binding.btnAnswerB.setBackgroundColor(color)
            "C" -> binding.btnAnswerC.setBackgroundColor(color)
            "D" -> binding.btnAnswerD.setBackgroundColor(color)
        }
    }

    private fun resetButtonColors() {
        val purple = Color.parseColor("#7C4DFF")
        binding.btnAnswerA.setBackgroundColor(purple)
        binding.btnAnswerB.setBackgroundColor(purple)
        binding.btnAnswerC.setBackgroundColor(purple)
        binding.btnAnswerD.setBackgroundColor(purple)
    }

    private fun enableAnswerButtons() {
        binding.btnAnswerA.isEnabled = true
        binding.btnAnswerB.isEnabled = true
        binding.btnAnswerC.isEnabled = true
        binding.btnAnswerD.isEnabled = true
    }

    private fun disableAnswerButtons() {
        binding.btnAnswerA.isEnabled = false
        binding.btnAnswerB.isEnabled = false
        binding.btnAnswerC.isEnabled = false
        binding.btnAnswerD.isEnabled = false
    }

    override fun onDestroyView() {
        timer?.cancel()
        super.onDestroyView()
        _binding = null
    }
}