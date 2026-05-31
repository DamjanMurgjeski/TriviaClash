package com.triviaclash.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.triviaclash.app.R
import com.triviaclash.app.databinding.FragmentMatchHistoryBinding

class MatchHistoryFragment : Fragment(R.layout.fragment_match_history) {

    private var _binding: FragmentMatchHistoryBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMatchHistoryBinding.bind(view)

        binding.rvMatchHistory.layoutManager = LinearLayoutManager(requireContext())
        loadMatchHistory()
    }

    private fun loadMatchHistory() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .collection("match_history")
            .orderBy("playedAt", Query.Direction.DESCENDING)
            .limit(20)
            .get()
            .addOnSuccessListener { result ->
                val matches = result.documents.map { doc ->
                    MatchItem(
                        quizTitle = doc.getString("quizTitle") ?: "Quiz",
                        category = doc.getString("category") ?: "",
                        score = doc.getLong("score")?.toInt() ?: 0,
                        correctAnswers = doc.getLong("correctAnswers")?.toInt() ?: 0,
                        totalQuestions = doc.getLong("totalQuestions")?.toInt() ?: 0
                    )
                }
                binding.rvMatchHistory.adapter = MatchHistoryAdapter(matches)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class MatchItem(
    val quizTitle: String,
    val category: String,
    val score: Int,
    val correctAnswers: Int,
    val totalQuestions: Int
)

class MatchHistoryAdapter(private val matches: List<MatchItem>) :
    RecyclerView.Adapter<MatchHistoryAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvQuizTitle: TextView = view.findViewById(R.id.tvQuizTitle)
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvScore: TextView = view.findViewById(R.id.tvScore)
        val tvCorrect: TextView = view.findViewById(R.id.tvCorrect)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_match_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val match = matches[position]
        holder.tvQuizTitle.text = match.quizTitle
        holder.tvCategory.text = match.category
        holder.tvScore.text = match.score.toString()
        holder.tvCorrect.text = "${match.correctAnswers}/${match.totalQuestions}"
    }

    override fun getItemCount() = matches.size
}