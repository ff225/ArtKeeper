package com.example.artkeeper.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.artkeeper.R
import com.example.artkeeper.adapter.HomeAdapter
import com.example.artkeeper.databinding.FragmentMainBinding
import com.example.artkeeper.presentation.PendingRequestViewModel
import com.example.artkeeper.presentation.PendingRequestViewModelFactory
import com.example.artkeeper.utils.ArtKeeper

class PendingRequestFragment : Fragment(R.layout.fragment_main) {

    private val TAG = javaClass.simpleName

    private val viewModel: PendingRequestViewModel by viewModels {
        PendingRequestViewModelFactory(
            (requireActivity().application as ArtKeeper).userRepository,
        )
    }
    private lateinit var recyclerView: RecyclerView
    private var _binding: FragmentMainBinding? = null
    private val binding: FragmentMainBinding
        get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (!arguments?.getString("title").isNullOrEmpty())
            (requireActivity() as AppCompatActivity).supportActionBar?.title =
                arguments?.getString("title")
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.observePendingReq(arguments?.getStringArray("uidList")!!.toList())
        recyclerView = binding.recyclerViewHome
        val adapter = HomeAdapter(HomeAdapter.HomeListener { nickname, position ->
            findNavController().navigate(
                PendingRequestFragmentDirections.actionPendingRequestFragmentToVisitedUserProfileFragment(
                    nickname.uid
                )
            )
        })
        recyclerView.adapter = adapter

        binding.apply {
            searchTextView.visibility = View.GONE
            viewModel.pendingReqList.observe(viewLifecycleOwner) {
                Log.d(TAG, it.toString())
                adapter.submitList(it)
            }
        }
    }

}