package com.triviaclash.app.models

data class Question(
    val id: String = "",
    val quizId: String = "",
    val questionText: String = "",
    val optionA: String = "",
    val optionB: String = "",
    val optionC: String = "",
    val optionD: String = "",
    val correctAnswer: String = "",
    val explanation: String = ""
)