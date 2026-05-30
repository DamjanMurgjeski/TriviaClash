package com.triviaclash.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.triviaclash.app.models.LeaderboardEntry
import com.triviaclash.app.repository.LeaderboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LeaderboardViewModel(
    private val leaderboardRepository: LeaderboardRepository = LeaderboardRepository()
) : ViewModel() {

    private val _globalLeaderboard = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val globalLeaderboard: StateFlow<List<LeaderboardEntry>> = _globalLeaderboard

    private val _weeklyLeaderboard = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val weeklyLeaderboard: StateFlow<List<LeaderboardEntry>> = _weeklyLeaderboard

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadGlobalLeaderboard() {
        viewModelScope.launch {
            _isLoading.value = true
            _globalLeaderboard.value = leaderboardRepository.getGlobalLeaderboard()
            _isLoading.value = false
        }
    }

    fun loadWeeklyLeaderboard() {
        viewModelScope.launch {
            _isLoading.value = true
            _weeklyLeaderboard.value = leaderboardRepository.getWeeklyLeaderboard()
            _isLoading.value = false
        }
    }
}