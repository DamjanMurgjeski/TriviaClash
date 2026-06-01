package com.triviaclash.app.fragments

import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.triviaclash.app.R
import com.triviaclash.app.databinding.FragmentQuizBinding
import com.triviaclash.app.models.Question
import com.triviaclash.app.repository.UserRepository
import kotlinx.coroutines.launch

class QuizFragment : Fragment(R.layout.fragment_quiz) {

    private var _binding: FragmentQuizBinding? = null
    private val binding get() = _binding!!
    private lateinit var analytics: FirebaseAnalytics
    private val userRepository = UserRepository()

    private val questions = mutableListOf<Question>()
    private var currentIndex = 0
    private var score = 0
    private var correctAnswers = 0
    private var timer: CountDownTimer? = null
    private var currentCategory = "science"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentQuizBinding.bind(view)
        analytics = FirebaseAnalytics.getInstance(requireContext())

        loadQuestionsFromFirestore()
    }

    private fun loadQuestionsFromFirestore() {
        currentCategory = arguments?.getString("category") ?: "science"
        val quizId = "quiz_$currentCategory"

        val analyticsBundle = Bundle()
        analyticsBundle.putString("category", currentCategory)
        analytics.logEvent("quiz_started", analyticsBundle)

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
            saveResultsAndNavigate()
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

    private fun saveResultsAndNavigate() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val xpEarned = correctAnswers * 10
            val coinsEarned = correctAnswers * 5

            val analyticsBundle = Bundle()
            analyticsBundle.putInt("final_score", score)
            analyticsBundle.putInt("correct_answers", correctAnswers)
            analyticsBundle.putInt("total_questions", questions.size)
            analytics.logEvent("quiz_completed", analyticsBundle)

            lifecycleScope.launch {
                userRepository.updateUserStats(
                    uid = uid,
                    xpToAdd = xpEarned,
                    coinsToAdd = coinsEarned,
                    score = score,
                    correct = correctAnswers,
                    total = questions.size,
                    quizTitle = "${currentCategory.replaceFirstChar { it.uppercase() }} Quiz",
                    category = currentCategory
                )
            }
        }

        val bundle = Bundle()
        bundle.putInt("score", score)
        bundle.putInt("correctAnswers", correctAnswers)
        bundle.putInt("totalQuestions", questions.size)
        findNavController().navigate(R.id.action_quiz_to_results, bundle)
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

                // Промени боја кога останува малку време
                when {
                    secondsLeft <= 5 -> binding.tvTimer.setTextColor(Color.RED)
                    secondsLeft <= 10 -> binding.tvTimer.setTextColor(Color.YELLOW)
                    else -> binding.tvTimer.setTextColor(Color.parseColor("#FF6D00"))
                }
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
        val timeBonus = binding.progressTimer.progress * 2

        val analyticsBundle = Bundle()
        analyticsBundle.putString("answer_selected", selected)
        analyticsBundle.putBoolean("is_correct", selected == correct)
        analytics.logEvent("quiz_answer", analyticsBundle)

        if (selected == correct) {
            score += 100 + timeBonus
            correctAnswers++
            highlightButton(selected, Color.parseColor("#4CAF50"))

            if (timeBonus > 30) {
                Toast.makeText(
                    requireContext(),
                    "⚡ Speed Bonus! +$timeBonus pts",
                    Toast.LENGTH_SHORT
                ).show()
            }
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