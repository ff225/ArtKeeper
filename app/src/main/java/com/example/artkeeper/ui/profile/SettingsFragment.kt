package com.example.artkeeper.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.artkeeper.R
import com.example.artkeeper.databinding.FragmentSettingsBinding
import com.example.artkeeper.presentation.ProfileViewModel
import com.example.artkeeper.presentation.ProfileViewModelFactory
import com.example.artkeeper.utils.ArtKeeper
import com.example.artkeeper.utils.Resource
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
            showDialogAddChild()
        }

        binding.btnRmvSon.setOnClickListener {
            /**
             * radiogroup con i nomi dei figli
             * in viewModel utilizzare quello che è stato fatto per aggiungere
             */
            if (viewModel.user.value?.nChild != 0)
                showDialogRemoveChild()
            else
                Toast.makeText(
                    requireContext(),
                    "Nessun figlio da cancellare...",
                    Toast.LENGTH_LONG
                ).show()
        }
        binding.btnDeleteAccount.setOnClickListener {
            /**
             *  dialog: sei sicuro di voler cancellare l'account?
             *  si -> procedi con l'eliminazione (creare in viewModel il necessario)
             *  no -> chiudi il dialog
             */

            showDeleteAccount()
        }
        binding.btnLogout.setOnClickListener {
            AuthUI.getInstance().signOut(requireActivity()).addOnCompleteListener {
                if (it.isComplete)
                    findNavController().navigate(R.id.action_logout_move_to_home)
            }

        }
    }

    private fun showDialogAddChild() {
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

    private fun showDialogRemoveChild() {
        val childName = viewModel.user.value?.nameChild?.toTypedArray()
        Log.d("SettingsFragment", childName?.size.toString())
        lateinit var select: String
        var index = -1
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Chi vuoi cancellare?")
            .setSingleChoiceItems(childName, index) { _, which ->
                index = which
                select = childName?.get(which).toString()
            }
            .setPositiveButton("Cancella") { _, _ ->
                Toast.makeText(requireContext(), "Selected $select", Toast.LENGTH_LONG).show()
                viewModel.removeChild(index)
            }
            .setNegativeButton("Annulla") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    private fun showDeleteAccount() {
        viewModel.deleteAccount().observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is Resource.Loading -> {
                    Toast.makeText(
                        requireContext(),
                        "Vuoi cancellare l'account?",
                        Toast.LENGTH_LONG
                    ).show()
                }
                is Resource.Success -> {
                    Toast.makeText(requireContext(), result.data, Toast.LENGTH_LONG)
                        .show()
                    findNavController().navigate(R.id.action_logout_move_to_home)
                    requireActivity().viewModelStore.clear()
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

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

}