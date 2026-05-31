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
import com.google.firebase.firestore.Query
import com.triviaclash.app.R
import com.triviaclash.app.databinding.FragmentLeaderboardBinding

class LeaderboardFragment : Fragment(R.layout.fragment_leaderboard) {

    private var _binding: FragmentLeaderboardBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLeaderboardBinding.bind(view)

        binding.rvLeaderboard.layoutManager = LinearLayoutManager(requireContext())
        loadGlobalLeaderboard()

        binding.tabLayout.addOnTabSelectedListener(object :
            com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> loadGlobalLeaderboard()
                    1 -> loadWeeklyLeaderboard()
                }
            }
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })
    }

    private fun loadGlobalLeaderboard() {
        FirebaseFirestore.getInstance()
            .collection("leaderboard")
            .orderBy("xp", Query.Direction.DESCENDING)
            .limit(20)
            .get()
            .addOnSuccessListener { result ->
                val entries = result.documents.mapIndexed { index, doc ->
                    LeaderboardItem(
                        rank = index + 1,
                        username = doc.getString("username") ?: "Player",
                        xp = doc.getLong("xp")?.toInt() ?: 0,
                        level = doc.getLong("level")?.toInt() ?: 1
                    )
                }
                binding.rvLeaderboard.adapter = LeaderboardAdapter(entries)
            }
    }

    private fun loadWeeklyLeaderboard() {
        FirebaseFirestore.getInstance()
            .collection("leaderboard")
            .orderBy("weeklyScore", Query.Direction.DESCENDING)
            .limit(20)
            .get()
            .addOnSuccessListener { result ->
                val entries = result.documents.mapIndexed { index, doc ->
                    LeaderboardItem(
                        rank = index + 1,
                        username = doc.getString("username") ?: "Player",
                        xp = doc.getLong("weeklyScore")?.toInt() ?: 0,
                        level = doc.getLong("level")?.toInt() ?: 1
                    )
                }
                binding.rvLeaderboard.adapter = LeaderboardAdapter(entries)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class LeaderboardItem(
    val rank: Int,
    val username: String,
    val xp: Int,
    val level: Int
)

class LeaderboardAdapter(private val items: List<LeaderboardItem>) :
    RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvRank: TextView = view.findViewById(R.id.tvRank)
        val tvUsername: TextView = view.findViewById(R.id.tvUsername)
        val tvLevel: TextView = view.findViewById(R.id.tvLevel)
        val tvXP: TextView = view.findViewById(R.id.tvXP)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leaderboard, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvRank.text = when (item.rank) {
            1 -> "🥇"
            2 -> "🥈"
            3 -> "🥉"
            else -> "#${item.rank}"
        }
        holder.tvUsername.text = item.username
        holder.tvLevel.text = "Level ${item.level}"
        holder.tvXP.text = "${item.xp} XP"
    }

    override fun getItemCount() = items.size
}