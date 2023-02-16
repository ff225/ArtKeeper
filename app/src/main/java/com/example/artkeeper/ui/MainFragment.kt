package com.example.artkeeper.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.artkeeper.R
import com.example.artkeeper.adapter.HomeAdapter
import com.example.artkeeper.databinding.FragmentMainBinding
import com.example.artkeeper.presentation.MainViewModel
import com.example.artkeeper.presentation.MainViewModelFactory
import com.example.artkeeper.utils.ArtKeeper
import com.example.artkeeper.utils.Constants.firebaseAuth
import com.example.artkeeper.utils.NotificationFollowingRequest
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainFragment : Fragment() {

    private val TAG: String = javaClass.simpleName

    private var _binding: FragmentMainBinding? = null
    private val binding: FragmentMainBinding
        get() = _binding!!
    private lateinit var recyclerView: RecyclerView


    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(
            (requireActivity().application as ArtKeeper).userRepository
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val serviceIntent = Intent(requireActivity(), NotificationFollowingRequest::class.java)
        if (firebaseAuth.currentUser == null) {
            requireActivity().stopService(serviceIntent)
            findNavController().navigate(MainFragmentDirections.actionMoveToLogin())
        }

        activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)!!.isGone = false
        _binding = FragmentMainBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        recyclerView = binding.recyclerViewHome
        val adapter = HomeAdapter(HomeAdapter.HomeListener { nickname, position ->
            findNavController().navigate(
                MainFragmentDirections.actionMainFragmentToVisitedUserProfileFragment(nickname.uid)
            )
        })
        recyclerView.adapter = adapter

        binding.searchTextView.onQueryTextChanged { queryString ->
            viewModel.searchQuery.value = queryString
        }

        viewModel.nickNameList.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        _binding = null
        super.onDestroy()
    }

    private inline fun SearchView.onQueryTextChanged(crossinline listener: (String) -> Unit) {
        this.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                listener(newText.orEmpty())
                return true
            }
        })
    }
}