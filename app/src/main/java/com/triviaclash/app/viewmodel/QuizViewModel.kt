package com.triviaclash.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.triviaclash.app.models.Question
import com.triviaclash.app.repository.QuizRepository
import com.triviaclash.app.repository.UserRepository
import com.triviaclash.app.room.RecentMatch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class QuizViewModel(
    private val quizRepository: QuizRepository,
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions: StateFlow<List<Question>> = _questions

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score

    private val _timeLeft = MutableStateFlow(30)
    val timeLeft: StateFlow<Int> = _timeLeft

    private val _quizState = MutableStateFlow<QuizState>(QuizState.Loading)
    val quizState: StateFlow<QuizState> = _quizState

    private val _correctAnswers = MutableStateFlow(0)
    val correctAnswers: StateFlow<Int> = _correctAnswers

    private var timerJob: Job? = null

    fun loadQuestions(quizId: String) {
        viewModelScope.launch {
            _quizState.value = QuizState.Loading
            val questionList = quizRepository.fetchQuestionsByQuizId(quizId)
            _questions.value = questionList
            _quizState.value = QuizState.Playing
            startTimer()
        }
    }

    fun submitAnswer(selectedAnswer: String) {
        timerJob?.cancel()
        val current = _questions.value[_currentIndex.value]
        if (selectedAnswer == current.correctAnswer) {
            _score.value += 100
            _correctAnswers.value += 1
            _quizState.value = QuizState.CorrectAnswer
        } else {
            _quizState.value = QuizState.WrongAnswer(current.correctAnswer)
        }
    }

    fun nextQuestion() {
        val nextIndex = _currentIndex.value + 1
        if (nextIndex >= _questions.value.size) {
            _quizState.value = QuizState.Finished
        } else {
            _currentIndex.value = nextIndex
            _timeLeft.value = 30
            _quizState.value = QuizState.Playing
            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            for (i in 30 downTo 0) {
                _timeLeft.value = i
                delay(1000)
            }
            _quizState.value = QuizState.TimeUp
        }
    }

    fun saveMatchResult(quizId: String, quizTitle: String, category: String) {
        viewModelScope.launch {
            val xpEarned = _correctAnswers.value * 10
            val coinsEarned = _correctAnswers.value * 5
            val uid = userRepository.currentUser?.uid ?: return@launch
            userRepository.updateUserXP(uid, xpEarned, coinsEarned)
        }
    }
}

sealed class QuizState {
    object Loading : QuizState()
    object Playing : QuizState()
    object CorrectAnswer : QuizState()
    data class WrongAnswer(val correctAnswer: String) : QuizState()
    object TimeUp : QuizState()
    object Finished : QuizState()
}