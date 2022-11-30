package com.example.artkeeper.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.artkeeper.R
import com.example.artkeeper.databinding.FragmentSettingsBinding
import com.example.artkeeper.presentation.ProfileViewModel
import com.example.artkeeper.presentation.ProfileViewModelFactory
import com.example.artkeeper.utils.ArtKeeper
import com.firebase.ui.auth.AuthUI
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding: FragmentSettingsBinding
        get() = _binding!!

    private val viewModel: ProfileViewModel by activityViewModels {
        ProfileViewModelFactory(
            (activity?.application as ArtKeeper).userRepository,
            (activity?.application as ArtKeeper).postRepository
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.btnChangeInfo.setOnClickListener {
            //navigate to updateinfo
            findNavController().navigate(R.id.action_settingsFragment_to_updateInfoFragment)
        }

        binding.btnAddSon.setOnClickListener {
            val inflater = requireActivity().layoutInflater
            val viewDialog = inflater.inflate(R.layout.dialog_addson, null)

            MaterialAlertDialogBuilder(requireContext()).setTitle("Nome figlio")
                .setView(viewDialog)
                .setNegativeButton(R.string.no) { dialog, _ ->
                    dialog.cancel()
                }
                .setPositiveButton(R.string.yes) { dialog, _ ->
                    val text = viewDialog.findViewById<EditText>(R.id.text_input_name)
                    if (text.text.toString() == "") {
                        Toast.makeText(
                            requireContext(),
                            "Devi inserire un nome valido...",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        viewModel.addChild(text.text.toString())
                        dialog.dismiss()
                    }
                }.show()
        }
        binding.btnLogout.setOnClickListener {
            AuthUI.getInstance().signOut(requireActivity()).addOnCompleteListener {
                if (it.isComplete)
                    findNavController().navigate(R.id.action_logout_move_to_home)
            }

        }
    }
}