package com.triviaclash.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.triviaclash.app.models.Quiz
import com.triviaclash.app.models.User
import com.triviaclash.app.repository.QuizRepository
import com.triviaclash.app.repository.UserRepository
import com.triviaclash.app.room.CachedQuiz
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val quizRepository: QuizRepository,
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile

    private val _recentQuizzes = MutableStateFlow<List<CachedQuiz>>(emptyList())
    val recentQuizzes: StateFlow<List<CachedQuiz>> = _recentQuizzes

    private val _dailyQuiz = MutableStateFlow<CachedQuiz?>(null)
    val dailyQuiz: StateFlow<CachedQuiz?> = _dailyQuiz

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadHomeData() {
        viewModelScope.launch {
            _isLoading.value = true
            val uid = userRepository.currentUser?.uid ?: return@launch
            _userProfile.value = userRepository.getUserProfile(uid)
            _dailyQuiz.value = quizRepository.getDailyQuiz()
            _isLoading.value = false
        }
    }

    fun syncQuizzes() {
        viewModelScope.launch {
            val quizzes = quizRepository.fetchQuizzesFromFirestore()
            quizRepository.syncQuizzesToLocal(quizzes)
        }
    }
}