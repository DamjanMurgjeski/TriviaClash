package com.triviaclash.app.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.triviaclash.app.R
import com.triviaclash.app.databinding.FragmentProfileBinding

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)

        setupUI()
        setupClickListeners()
        loadUserStats()
    }

    override fun onResume() {
        super.onResume()
        loadUserStats()
    }

    private fun setupUI() {
        val user = FirebaseAuth.getInstance().currentUser
        binding.tvUsername.text = user?.displayName ?: user?.email?.substringBefore("@") ?: "Player"
        binding.tvEmail.text = user?.email ?: "Guest"
    }

    private fun loadUserStats() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val totalGames = doc.getLong("totalGames")?.toInt() ?: 0
                    val highestScore = doc.getLong("highestScore")?.toInt() ?: 0
                    val xp = doc.getLong("xp")?.toInt() ?: 0
                    val level = doc.getLong("level")?.toInt() ?: 1

                    binding.tvTotalGames.text = totalGames.toString()
                    binding.tvHighScore.text = highestScore.toString()
                    binding.tvWinRate.text = if (totalGames > 0) "${(xp / totalGames)}%" else "0%"
                    binding.tvLevel.text = "Level $level"
                }
            }
    }

    private fun setupClickListeners() {
        binding.btnAchievements.setOnClickListener {
            findNavController().navigate(R.id.achievementsFragment)
        }

        binding.btnMatchHistory.setOnClickListener {
            findNavController().navigate(R.id.matchHistoryFragment)
        }

        binding.btnSettings.setOnClickListener {
            findNavController().navigate(R.id.settingsFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}