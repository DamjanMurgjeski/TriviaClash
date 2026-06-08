package com.triviaclash.app.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.triviaclash.app.R
import com.triviaclash.app.databinding.FragmentProfileBinding
import java.io.File

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private var currentPhotoUri: Uri? = null

    // Gallery picker
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { saveAndDisplayImage(it) }
    }

    // Camera launcher
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            currentPhotoUri?.let { saveAndDisplayImage(it) }
        }
    }

    // Camera permission
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) openCamera()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)

        setupUI()
        setupClickListeners()
        loadUserStats()
        loadSavedProfileImage()
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

    private fun loadSavedProfileImage() {
        val prefs = requireContext().getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
        val savedPath = prefs.getString("profile_image_path", null)
        if (savedPath != null) {
            Glide.with(this)
                .load(File(savedPath))
                .circleCrop()
                .placeholder(R.mipmap.ic_launcher_round)
                .into(binding.ivProfileImage)
        }
    }

    private fun saveAndDisplayImage(uri: Uri) {
        val fileName = "profile_${System.currentTimeMillis()}.jpg"
        val destFile = File(requireContext().filesDir, fileName)

        requireContext().contentResolver.openInputStream(uri)?.use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        val prefs = requireContext().getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("profile_image_path", destFile.absolutePath).apply()

        Glide.with(this)
            .load(destFile)
            .circleCrop()
            .placeholder(R.mipmap.ic_launcher_round)
            .into(binding.ivProfileImage)
    }

    private fun showImagePickerDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Change Profile Photo")
            .setItems(arrayOf("Camera", "Gallery")) { _, which ->
                when (which) {
                    0 -> checkCameraPermissionAndOpen()
                    1 -> galleryLauncher.launch("image/*")
                }
            }
            .show()
    }

    private fun checkCameraPermissionAndOpen() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> openCamera()
            else -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun openCamera() {
        val photoFile = File(
            requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "profile_${System.currentTimeMillis()}.jpg"
        )
        currentPhotoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            photoFile
        )
        cameraLauncher.launch(currentPhotoUri)
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
                    val level = doc.getLong("level")?.toInt() ?: 1
                    binding.tvTotalGames.text = totalGames.toString()
                    binding.tvHighScore.text = highestScore.toString()
                    val totalCorrect = doc.getLong("totalCorrect")?.toInt() ?: 0
                    val totalQuestions = doc.getLong("totalQuestions")?.toInt() ?: 0
                    binding.tvWinRate.text = if (totalQuestions > 0) "${(totalCorrect * 100 / totalQuestions)}%" else "0%"
                    binding.tvLevel.text = "Level $level"
                }
            }
    }

    private fun setupClickListeners() {
        binding.btnChangePhoto.setOnClickListener {
            showImagePickerDialog()
        }

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