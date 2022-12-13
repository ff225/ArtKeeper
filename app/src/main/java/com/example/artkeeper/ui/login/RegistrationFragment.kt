package com.example.artkeeper.ui.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.example.artkeeper.R
import com.example.artkeeper.databinding.FragmentRegistrationBinding
import com.example.artkeeper.presentation.ProfileViewModel
import com.example.artkeeper.presentation.ProfileViewModelFactory
import com.example.artkeeper.utils.ArtKeeper
import com.example.artkeeper.utils.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth

/**
 * TODO:
 *  - controllare la presenza di nickname sul db;
 *
 *
 */

class RegistrationFragment : Fragment() {
    companion object {
        const val TAG = "RegistrationFragment"
    }

    private val uid = FirebaseAuth.getInstance().currentUser!!.uid
    private var _binding: FragmentRegistrationBinding? = null
    private val binding: FragmentRegistrationBinding
        get() = _binding!!
    private var isRegistered = false

    private val viewModel by navGraphViewModels<ProfileViewModel>(R.id.profile) {
        ProfileViewModelFactory(
            (requireActivity().application as ArtKeeper).userRepository,
            (requireActivity().application as ArtKeeper).postRepository
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            MaterialAlertDialogBuilder(requireContext()).setTitle("Annullare registrazione?")
                .setPositiveButton(R.string.yes) { _, _ ->
                    deleteRegistration()
                }
                .setNegativeButton(R.string.no) { dialog, _ ->
                    dialog.cancel()
                }.show()
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.confirmButton.setOnClickListener {
            userRegistration()
        }
    }

    override fun onResume() {
        if (FirebaseAuth.getInstance().currentUser == null)
            findNavController().navigate(R.id.profile)
        super.onResume()
    }

    private fun userRegistration() {
        Log.d(TAG, "Confirm")
        if (!checkUserInfo()) {
            createUser()
            viewModel.insertUser(uid)
            findNavController().navigate(R.id.home)
            isRegistered = true
            Log.d(TAG, "user registered")
        } else
            Log.d(TAG, "registration failed")
    }

    private fun checkUserInfo(): Boolean {

        var hasError = false
        if (binding.textInputName.text.toString().trim().isEmpty()) {
            binding.textInputName.error = "Il campo è vuoto"
            hasError = true
        }
        if (binding.textInputLastname.text.toString().trim().isEmpty()) {
            binding.textInputLastname.error = "Il campo è vuoto"
            hasError = true
        }
        if (binding.textInputNickname.text.toString().trim().isEmpty()) {
            binding.textInputNickname.error = "Il campo è vuoto"
            hasError = true
        }
        return hasError
    }

    private fun createUser() {
        viewModel.apply {
            setName(binding.textInputName.text.toString().trim())
            setLastName(binding.textInputLastname.text.toString().trim())
            setNickName(binding.textInputNickname.text.toString().trim())

        }
    }

    private fun deleteRegistration() {
        viewModel.deleteRegistration().observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is Resource.Loading -> {
                    binding.apply {
                        textInputName.isEnabled = false
                        textInputLastname.isEnabled = false
                        textInputNickname.isEnabled = false
                        confirmButton.isEnabled = false
                    }
                    Toast.makeText(
                        requireContext(),
                        "Vuoi cancellare l'account?",
                        Toast.LENGTH_LONG
                    ).show()
                }
                is Resource.Success -> {
                    Toast.makeText(requireContext(), result.data, Toast.LENGTH_LONG)
                        .show()
                    findNavController().navigate(R.id.profile)
                }
                is Resource.Failure -> {
                    Toast.makeText(
                        requireContext(),
                        "Effettua il login per completare questa azione",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            }
        })
    }

    override fun onPause() {
        if (!isRegistered)
            FirebaseAuth.getInstance().currentUser?.delete()
        super.onPause()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        _binding = null
        super.onDestroy()
    }
}