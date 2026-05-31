package com.triviaclash.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.triviaclash.app.R
import com.triviaclash.app.databinding.FragmentAchievementsBinding

class AchievementsFragment : Fragment(R.layout.fragment_achievements) {

    private var _binding: FragmentAchievementsBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAchievementsBinding.bind(view)

        binding.rvAchievements.layoutManager = LinearLayoutManager(requireContext())
        loadAchievements()
    }

    private fun loadAchievements() {
        FirebaseFirestore.getInstance()
            .collection("achievements")
            .get()
            .addOnSuccessListener { result ->
                val achievements = result.documents.map { doc ->
                    AchievementItem(
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        emoji = doc.getString("emoji") ?: "🏆",
                        xpReward = doc.getLong("xpReward")?.toInt() ?: 0
                    )
                }
                binding.rvAchievements.adapter = AchievementsAdapter(achievements)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class AchievementItem(
    val title: String,
    val description: String,
    val emoji: String,
    val xpReward: Int
)

class AchievementsAdapter(private val achievements: List<AchievementItem>) :
    RecyclerView.Adapter<AchievementsAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvEmoji: TextView = view.findViewById(R.id.tvEmoji)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)
        val tvXpReward: TextView = view.findViewById(R.id.tvXpReward)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_achievement, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val achievement = achievements[position]
        holder.tvEmoji.text = achievement.emoji
        holder.tvTitle.text = achievement.title
        holder.tvDescription.text = achievement.description
        holder.tvXpReward.text = "+${achievement.xpReward} XP"
    }

    override fun getItemCount() = achievements.size
}