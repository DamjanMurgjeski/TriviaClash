package com.triviaclash.app.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.triviaclash.app.R
import com.triviaclash.app.databinding.FragmentSettingsBinding
import androidx.navigation.fragment.findNavController
import java.util.Locale

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSettingsBinding.bind(view)

        setupCurrentState()
        setupClickListeners()
    }

    private fun setupCurrentState() {
        val isDarkMode = AppCompatDelegate.getDefaultNightMode() ==
                AppCompatDelegate.MODE_NIGHT_YES
        binding.switchDarkMode.isChecked = isDarkMode

        val prefs = requireActivity().getSharedPreferences("settings", 0)
        binding.switchNotifications.isChecked = prefs.getBoolean("notifications", true)

        val lang = prefs.getString("language", "en")
        binding.tvLanguage.text = if (lang == "mk") "Македонски" else "English"
    }

    private fun setupClickListeners() {
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            val prefs = requireActivity().getSharedPreferences("settings", 0)
            prefs.edit().putBoolean("notifications", isChecked).apply()
        }

        binding.layoutLanguage.setOnClickListener {
            val prefs = requireActivity().getSharedPreferences("settings", 0)
            val currentLang = prefs.getString("language", "en")
            val newLang = if (currentLang == "en") "mk" else "en"
            prefs.edit().putString("language", newLang).apply()
            binding.tvLanguage.text = if (newLang == "mk") "Македонски" else "English"

            val locale = Locale(newLang)
            Locale.setDefault(locale)
            val config = Configuration()
            config.setLocale(locale)
            requireActivity().resources.updateConfiguration(
                config,
                requireActivity().resources.displayMetrics
            )
            requireActivity().recreate()
        }

        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            findNavController().navigate(R.id.loginFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}