package com.triviaclash.app.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.triviaclash.app.R
import com.triviaclash.app.adapters.CategoryAdapter
import com.triviaclash.app.databinding.FragmentHomeBinding
import com.triviaclash.app.models.Category

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        setupUI()
        setupCategories()
        setupClickListeners()
        loadUserData()
        loadRecentQuizzes()
    }

    override fun onResume() {
        super.onResume()
        loadUserData()
        loadRecentQuizzes()
    }

    private fun setupUI() {
        val user = FirebaseAuth.getInstance().currentUser
        binding.tvUsername.text = user?.displayName ?: user?.email?.substringBefore("@") ?: "Player"
    }

    private fun loadUserData() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val xp = doc.getLong("xp")?.toInt() ?: 0
                    val level = doc.getLong("level")?.toInt() ?: 1
                    val coins = doc.getLong("coins")?.toInt() ?: 0
                    val streak = doc.getLong("streak")?.toInt() ?: 0

                    binding.tvLevel.text = "Level $level"
                    binding.tvXP.text = "$xp XP"
                    binding.progressXP.progress = xp % 500
                    binding.tvCoins.text = coins.toString()
                    binding.tvStreak.text = "${streak}🔥"
                }
            }
    }

    private fun loadRecentQuizzes() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .collection("match_history")
            .orderBy("playedAt", Query.Direction.DESCENDING)
            .limit(5)
            .get()
            .addOnSuccessListener { result ->
                val recentList = result.documents.map { doc ->
                    RecentQuizItem(
                        title = doc.getString("quizTitle") ?: "Quiz",
                        category = doc.getString("category") ?: "",
                        score = doc.getLong("score")?.toInt() ?: 0,
                        correctAnswers = doc.getLong("correctAnswers")?.toInt() ?: 0,
                        totalQuestions = doc.getLong("totalQuestions")?.toInt() ?: 0
                    )
                }
                binding.rvRecentQuizzes.layoutManager = LinearLayoutManager(requireContext())
                binding.rvRecentQuizzes.adapter = RecentQuizAdapter(recentList)
            }
    }

    private fun setupCategories() {
        val categories = listOf(
            Category("sports", "Sports", "⚽", Color.parseColor("#E91E63")),
            Category("movies", "Movies", "🎬", Color.parseColor("#9C27B0")),
            Category("programming", "Programming", "💻", Color.parseColor("#2196F3")),
            Category("gaming", "Gaming", "🎮", Color.parseColor("#4CAF50")),
            Category("geography", "Geography", "🌍", Color.parseColor("#FF9800")),
            Category("science", "Science", "🔬", Color.parseColor("#00BCD4"))
        )

        val adapter = CategoryAdapter(categories) { category ->
            val bundle = Bundle()
            bundle.putString("category", category.id)
            findNavController().navigate(R.id.action_home_to_quiz, bundle)
        }

        binding.rvCategories.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        binding.rvCategories.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.btnPlayDaily.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_categories)
        }

        binding.ivProfile.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_profile)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class RecentQuizItem(
    val title: String,
    val category: String,
    val score: Int,
    val correctAnswers: Int,
    val totalQuestions: Int
)

class RecentQuizAdapter(private val items: List<RecentQuizItem>) :
    RecyclerView.Adapter<RecentQuizAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvQuizTitle)
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
        val item = items[position]
        holder.tvTitle.text = item.title
        holder.tvCategory.text = item.category
        holder.tvScore.text = item.score.toString()
        holder.tvCorrect.text = "${item.correctAnswers}/${item.totalQuestions}"
    }

    override fun getItemCount() = items.size
}