package com.example.artkeeper.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.RecyclerView
import com.example.artkeeper.R
import com.example.artkeeper.adapter.PostAdapter
import com.example.artkeeper.data.model.Post
import com.example.artkeeper.databinding.FragmentProfileBinding
import com.example.artkeeper.presentation.ProfileViewModel
import com.example.artkeeper.presentation.ProfileViewModelFactory
import com.example.artkeeper.utils.ArtKeeper


class ProfileFragment : Fragment() {

    companion object {
        const val TAG = "ProfileFragment"
    }

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView
    private val viewModel: ProfileViewModel by navGraphViewModels(R.id.profile) {
        ProfileViewModelFactory(
            (activity?.application as ArtKeeper).userRepository,
            (activity?.application as ArtKeeper).postRepository
        )
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = binding.recyclerViewProfile
        val adapter = PostAdapter(object : PostAdapter.OptionsMenuClickListener {
            override fun onOptionsMenuClicked(post: Post, position: Int) {
                performOptionsMenuClick(post, position)
            }

        })
        recyclerView.adapter = adapter

        viewModel.user.observe(viewLifecycleOwner) {
            binding.tvName.text = it?.firstName
            binding.tvLastName.text = it?.lastName
            binding.tvUsername.text = it?.nickName
        }

        viewModel.numPost.observe(viewLifecycleOwner) {
            binding.tvNPost.text = getString(R.string.num_post, it.toString())
        }

        viewModel.postUser.observe(viewLifecycleOwner) { post ->
            post.let {
                adapter.submitList(post)
            }
        }

        binding.btnSettings.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_settingsFragment)
        }
    }

    private fun performOptionsMenuClick(post: Post, position: Int) {

        val popupMenu = PopupMenu(
            requireContext(),
            binding.recyclerViewProfile[position].findViewById(R.id.textViewOptions)
        )

        popupMenu.inflate(R.menu.options_menu_profile)

        popupMenu.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem?): Boolean {
                when (item?.itemId) {
                    R.id.cancel_button -> {
                        Log.d(TAG, "cancello post...${post.id}")
                        viewModel.deletePost(post)
                        binding.recyclerViewProfile.adapter?.notifyItemRemoved(position)
                        // here are the logic to delete an item from the list
                        return true
                    }
                }
                return false
            }
        })
        popupMenu.show()
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}