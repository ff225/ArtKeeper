package com.example.artkeeper.ui.profile

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.artkeeper.R
import com.example.artkeeper.adapter.PostAdapter
import com.example.artkeeper.data.model.Post
import com.example.artkeeper.databinding.FragmentProfileBinding
import com.example.artkeeper.presentation.ProfileViewModel
import com.example.artkeeper.presentation.ProfileViewModelFactory
import com.example.artkeeper.utils.ArtKeeper
import com.example.artkeeper.utils.Resource


class ProfileFragment : Fragment() {

    private val TAG: String = javaClass.simpleName
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView
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
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = binding.recyclerViewProfile

        val adapter = PostAdapter()

        /*val adapter = PostAdapter(object : PostAdapter.OptionsMenuClickListener {
            override fun onOptionsMenuClicked(post: Post, position: Int) {
                performOptionsMenuClick(post, position)
            }

        })

*/
        recyclerView.adapter = adapter
        viewModel.getUserPost().observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Loading -> {
                    binding.apply {
                        progressBar2.visibility = View.VISIBLE
                        tvNPost.visibility = View.INVISIBLE
                    }
                }
                is Resource.Success -> {
                    binding.apply {
                        tvNPost.visibility = View.VISIBLE
                        tvNPost.text = getString(R.string.num_post, it.data.size.toString())
                        progressBar2.visibility = View.GONE
                    }
                    adapter.submitList(it.data)
                    adapter.nickName = viewModel.user.value!!.nickName
                }
                is Resource.Failure -> {}
            }
        })
        viewModel.user.observe(viewLifecycleOwner) {
            val imageUri: Uri = viewModel.image!!
            Glide.with(binding.imageProfile.context)
                .load(imageUri)
                .into(binding.imageProfile)
            binding.tvName.text = it?.firstName
            binding.tvLastName.text = it?.lastName
            binding.tvUsername.text = it?.nickName
        }

        /* viewModel.numPost.observe(viewLifecycleOwner) {
             binding.tvNPost.text = getString(R.string.num_post, it.toString())
         }*/
/*
        viewModel.postUser.observe(viewLifecycleOwner) { post ->
            post.let {
                adapter.submitList(post)
            }
        }
*/
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
                        Log.d(TAG, "in cancelButton, cancello post: ${post.id}")
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