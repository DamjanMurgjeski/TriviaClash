package com.triviaclash.app.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.triviaclash.app.R
import com.triviaclash.app.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        setupUI()
        setupClickListeners()
    }

    private fun setupUI() {
        val user = FirebaseAuth.getInstance().currentUser
        binding.tvUsername.text = user?.displayName ?: "Player"
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