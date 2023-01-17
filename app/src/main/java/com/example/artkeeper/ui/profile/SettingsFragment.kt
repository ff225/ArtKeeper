package com.example.artkeeper.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.work.WorkInfo
import com.example.artkeeper.R
import com.example.artkeeper.databinding.FragmentSettingsBinding
import com.example.artkeeper.presentation.ProfileViewModel
import com.example.artkeeper.presentation.ProfileViewModelFactory
import com.example.artkeeper.utils.ArtKeeper
import com.firebase.ui.auth.AuthUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding: FragmentSettingsBinding
        get() = _binding!!

    private val viewModel by navGraphViewModels<ProfileViewModel>(R.id.profile) {
        ProfileViewModelFactory(
            (requireActivity().application as ArtKeeper).userRepository,
            (requireActivity().application as ArtKeeper).postRepository,
            (requireActivity().application as ArtKeeper).workManager
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
            findNavController().navigate(R.id.action_settingsFragment_to_updateInfoFragment)
        }

        binding.btnAddSon.setOnClickListener {
            showDialogAddChild()
        }

        viewModel.user.observe(viewLifecycleOwner) { user ->
            binding.btnRmvSon.setOnClickListener {
                /**
                 * radiogroup con i nomi dei figli
                 * in viewModel utilizzare quello che è stato fatto per aggiungere
                 */
                if (user.nameChild != null && user.nChild != 0)
                    showDialogRemoveChild(user.nameChild.toTypedArray())
                else
                    Toast.makeText(
                        requireContext(),
                        "Nessun figlio da cancellare...",
                        Toast.LENGTH_LONG
                    ).show()
            }
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
            viewModel.deleteLocalAccount()
            viewModel.logoutUserWorkInfo.observe(viewLifecycleOwner, logout())
        }
        /*AuthUI.getInstance().signOut(requireActivity()).addOnCompleteListener {

            if (it.isSuccessful) {

            }
        }*/

    }

    private fun showDialogAddChild() {
        val inflater = requireActivity().layoutInflater
        val viewDialog = inflater.inflate(R.layout.dialog_addson, null)

        MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.add_son)
            .setView(viewDialog)
            .setNegativeButton(R.string.delete) { dialog, _ ->
                dialog.cancel()
            }
            .setPositiveButton(R.string.confirm) { dialog, _ ->
                val text =
                    viewDialog.findViewById<EditText>(R.id.text_input_name).text.toString().trim()
                if (text.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        "Devi inserire un nome valido...",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    viewModel.addChild(text)
                    dialog.dismiss()
                }
            }.show()
    }

    // TODO: fix after cache
    private fun showDialogRemoveChild(childName: Array<String>?) {

        Log.d("SettingsFragment", childName?.size.toString())
        var select: String? = null
        var index = -1
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Chi vuoi cancellare?")
            .setSingleChoiceItems(childName, index) { _, which ->
                index = which
                select = childName?.get(which).toString()
            }
            .setPositiveButton(R.string.confirm) { _, _ ->
                Toast.makeText(requireContext(), "Selected $select", Toast.LENGTH_LONG).show()
                viewModel.removeChild(index)
            }
            .setNegativeButton(R.string.delete) { dialog, _ ->
                dialog.cancel()
            }
            .show()

    }

    private fun showDeleteAccount() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Sicuro di voler procedere?")
            .setPositiveButton(R.string.confirm) { _, _ ->
                //deleteAccount()
                viewModel.deleteRemoteAccount()
                viewModel.deleteRemoteUserWorksInfos.observe(viewLifecycleOwner, deleteAccount())
            }
            .setNegativeButton(R.string.delete) { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    private fun logout(): Observer<List<WorkInfo>> {
        return Observer { listOfWorkInfo ->
            if (listOfWorkInfo.isNullOrEmpty())
                return@Observer
            val workInfo = listOfWorkInfo[listOfWorkInfo.lastIndex]
            if (workInfo.state.isFinished) {
                AuthUI.getInstance().signOut(requireContext()).addOnCompleteListener {
                    if (it.isSuccessful) {
                        findNavController().navigate(R.id.action_logout_move_to_home)
                    }
                }

            }

        }
    }


    private fun deleteAccount(): Observer<List<WorkInfo>> {
        return Observer { listOfWorkInfo ->
            if (listOfWorkInfo.isNullOrEmpty()) {
                return@Observer
            }
            //val workInfo = listOfWorkInfo[2]
            val workInfo = listOfWorkInfo[listOfWorkInfo.lastIndex]
            Log.d("SettingsFragment", workInfo.toString())
            if (workInfo.state.isFinished) {
                AuthUI.getInstance().signOut(requireContext()).addOnCompleteListener {
                    if (it.isSuccessful)
                        findNavController().navigate(R.id.action_logout_move_to_home)
                }
            } else {
                binding.apply {
                    progressBar.visibility = View.VISIBLE
                    btnChangeInfo.isEnabled = false
                    btnChangeImgProfile.isEnabled = false
                    btnAddSon.isEnabled = false
                    btnRmvSon.isEnabled = false
                    btnChangeImgProfile.isEnabled = false
                    btnDeleteAccount.isEnabled = false
                    btnLogout.isEnabled = false

                }
                (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(
                    false
                )
                activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)?.visibility =
                    View.INVISIBLE

                requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
                }
            }

        }
        /*
        viewModel.deleteAccount().observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is Resource.Loading -> {
                    binding.apply {
                        progressBar.visibility = View.VISIBLE
                        btnChangeInfo.isEnabled = false
                        btnChangeImgProfile.isEnabled = false
                        btnAddSon.isEnabled = false
                        btnRmvSon.isEnabled = false
                        btnChangeImgProfile.isEnabled = false
                        btnDeleteAccount.isEnabled = false
                        btnLogout.isEnabled = false

                    }
                    (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(
                        false
                    )
                    activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)?.visibility =
                        View.INVISIBLE

                    Toast.makeText(
                        requireContext(),
                        "Operazione in corso... verrai reindirizzato alla pagina di login.",
                        Toast.LENGTH_SHORT
                    ).show()
                    requireActivity().onBackPressedDispatcher.addCallback(this) {
                    }
                }
                is Resource.Success -> {
                    Toast.makeText(requireContext(), result.data, Toast.LENGTH_LONG)
                        .show()
                    findNavController().navigate(R.id.action_logout_move_to_home)
                    activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)?.visibility =
                        View.VISIBLE
                    requireActivity().viewModelStore.clear()
                }
                is Resource.Failure -> {
                    (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(
                        true
                    )
                    activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)?.visibility =
                        View.INVISIBLE
                    binding.apply {
                        progressBar.visibility = View.GONE
                        btnChangeInfo.isEnabled = true
                        btnChangeImgProfile.isEnabled = true
                        btnAddSon.isEnabled = true
                        btnRmvSon.isEnabled = true
                        btnChangeImgProfile.isEnabled = true
                        btnDeleteAccount.isEnabled = true
                        btnLogout.isEnabled = true

                    }
                    Toast.makeText(
                        requireContext(),
                        "Impossibile cancellare l'account.\n" +
                                "Riesegui il login per completare questa azione",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            }
        })

         */
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

}