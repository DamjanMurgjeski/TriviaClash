package com.triviaclash.app.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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