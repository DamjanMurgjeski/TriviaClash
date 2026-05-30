package com.triviaclash.app.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.triviaclash.app.R
import com.triviaclash.app.adapters.CategoryAdapter
import com.triviaclash.app.databinding.FragmentCategoryBinding
import com.triviaclash.app.models.Category

class CategoryFragment : Fragment(R.layout.fragment_category) {

    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCategoryBinding.bind(view)

        setupCategories()
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
            findNavController().navigate(R.id.action_category_to_quiz)
        }

        binding.rvCategories.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}