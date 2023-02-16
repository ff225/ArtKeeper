package com.example.artkeeper.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ShareCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.artkeeper.R
import com.example.artkeeper.adapter.PostAdapter
import com.example.artkeeper.databinding.FragmentProfileBinding
import com.example.artkeeper.presentation.VisitedUserProfileViewModel
import com.example.artkeeper.presentation.VisitedUserProfileViewModelFactory
import com.example.artkeeper.utils.ArtKeeper
import com.example.artkeeper.utils.Constants
import com.example.artkeeper.utils.Resource
import com.example.artkeeper.utils.ShareAction

class VisitedUserProfileFragment : Fragment(R.layout.fragment_profile) {

    private val TAG = javaClass.simpleName
    private var _binding: FragmentProfileBinding? = null
    private val binding: FragmentProfileBinding
        get() = _binding!!

    private lateinit var recyclerView: RecyclerView
    private val viewModel: VisitedUserProfileViewModel by viewModels {
        VisitedUserProfileViewModelFactory(
            (requireActivity().application as ArtKeeper).userRepository,
            (requireActivity().application as ArtKeeper).postRepository,
            (requireActivity().application as ArtKeeper).workManager
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d("VisitedUserProfileFragment", arguments?.getString("uidRequest").toString())

        binding.apply {
            buttonFollower.visibility = View.GONE
            buttonNotification.visibility = View.GONE
        }

        val adapter = PostAdapter(PostAdapter.PostListener { post, position, option ->
            Log.d(TAG, "position: $position")

            if (option == "share") {
                val path = ShareAction().getTmpFileUri(requireContext(), post.imagePath)
                val shareIntent =
                    ShareCompat.IntentBuilder(requireActivity()).apply {
                        setType("image/jpg")
                        setText(post.description.toString())
                        post.sketchedBy?.let {
                            setText("Disegnato da: $it \n${post.description.toString()}")
                        }
                        addStream(path)

                    }.createChooserIntent()
                startActivity(shareIntent)
            }
        })
        recyclerView = binding.recyclerViewProfile
        recyclerView.adapter = adapter
        adapter.menu = R.menu.options_menu_home
        listener()
        userObserver(adapter)

    }


    private fun listener() {
        binding.apply {
            acceptFollowinReq.apply {
                setOnClickListener {
                    Log.d(TAG, arguments?.getString("uidRequest")!!)
                    viewModel.acceptRequest(arguments?.getString("uidRequest")!!, true)
                    llPendingReq.visibility = View.GONE
                    btnSettings.visibility = View.VISIBLE
                    btnSettings.text = getString(R.string.stop_following)
                }
            }
            deleteFollowingReq.apply {
                setOnClickListener {
                    Log.d(TAG, arguments?.getString("uidRequest")!!)
                    viewModel.acceptRequest(arguments?.getString("uidRequest")!!, false)
                    llPendingReq.visibility = View.GONE
                    btnSettings.visibility = View.VISIBLE
                    btnSettings.text = getString(R.string.follow)
                }
            }
            btnSettings.setOnClickListener {
                binding.btnSettings.apply {
                    if (text == getString(R.string.follow)) {
                        viewModel.sendRequest(arguments?.getString("uidRequest")!!, true)
                        text = getString(R.string.stop_following)
                    } else {
                        viewModel.sendRequest(arguments?.getString("uidRequest")!!, false)
                        viewModel.acceptRequest(arguments?.getString("uidRequest")!!, false)
                        text = getString(R.string.follow)
                    }
                }
            }
        }
    }

    private fun userObserver(adapter: PostAdapter) {
        viewModel.getInfoUser(arguments?.getString("uidRequest").toString())
            .observe(viewLifecycleOwner) { result ->
                when (result) {
                    is Resource.Loading -> {
                        binding.tvNPost.visibility = View.INVISIBLE
                        binding.btnSettings.visibility = View.INVISIBLE
                        binding.progressBar2.visibility = View.VISIBLE
                    }
                    is Resource.Success -> {
                        val userOnline = result.data
                        adapter.nickName = userOnline.nickName.toString()
                        binding.apply {
                            tvName.text = userOnline.firstName
                            tvLastName.text = userOnline.lastName
                            tvUsername.text = userOnline.nickName

                            Log.d(TAG, userOnline.photoUser!!.toUri().toString())
                            Glide.with(imageProfile.context)
                                .load(userOnline.photoUser!!.toUri())
                                .format(DecodeFormat.PREFER_RGB_565)
                                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                .error(R.drawable.ic_baseline_settings_24)
                                .into(imageProfile)

                            if (userOnline.uid.equals(Constants.firebaseAuth.uid)) {
                                btnSettings.visibility = View.GONE
                                showPost(adapter)
                            } else {
                                isPendingRequestTo()
                                isPendingRequestFrom()
                                seeVisitedPost(adapter)
                            }

                        }

                    }
                    is Resource.Failure -> {
                        Log.d(TAG, "failure")
                        binding.tvNPost.visibility = View.INVISIBLE
                        binding.btnSettings.visibility = View.INVISIBLE
                        binding.progressBar2.visibility = View.VISIBLE
                    }
                }
            }
    }

    private fun isPendingRequestTo() {
        viewModel.pendingRequestTo.observe(viewLifecycleOwner) { listRequest ->
            Log.d(
                "$TAG, pendingRequestTo",
                listRequest.contains(arguments?.getString("uidRequest")).toString()
            )
            if (listRequest.contains(arguments?.getString("uidRequest")))
                binding.apply {
                    btnSettings.visibility = View.VISIBLE
                    btnSettings.text = getString(R.string.stop_following)
                    llPendingReq.visibility = View.GONE
                }
            else {
                binding.btnSettings.visibility = View.VISIBLE
                binding.btnSettings.text = getString(R.string.follow)
            }
        }
    }

    private fun isPendingRequestFrom() {
        viewModel.pendingRequestFrom.observe(viewLifecycleOwner) { listRequest ->
            Log.d(
                "$TAG, pendingRequestFrom",
                listRequest.contains(arguments?.getString("uidRequest")).toString()
            )
            if (listRequest.contains(arguments?.getString("uidRequest")))
                binding.apply {
                    btnSettings.visibility = View.GONE
                    llPendingReq.visibility = View.VISIBLE
                }
        }
    }

    private fun showPost(adapter: PostAdapter) {
        viewModel.getPostUser(arguments?.getString("uidRequest").toString())
            .observe(viewLifecycleOwner) { result ->
                when (result) {
                    is Resource.Loading -> {
                        binding.progressBar2.visibility = View.VISIBLE
                    }
                    is Resource.Success -> {
                        binding.recyclerViewProfile.visibility = View.VISIBLE
                        binding.progressBar2.visibility = View.GONE
                        binding.tvNPost.visibility = View.VISIBLE
                        binding.tvNPost.text =
                            getString(R.string.num_post, result.data.size.toString())
                        adapter.submitList(result.data)
                    }
                    is Resource.Failure -> {
                        Log.d(TAG, result.exception.toString())
                    }
                }
            }
    }

    private fun seeVisitedPost(adapter: PostAdapter) {
        viewModel.followers.observe(viewLifecycleOwner) { followers ->
            Log.d(
                "$TAG, seeVisitedPost",
                followers.contains(arguments?.getString("uidRequest")).toString()
            )
            if (followers.contains(arguments?.getString("uidRequest"))) {
                binding.apply {
                    btnSettings.apply {
                        text =
                            getString(R.string.stop_following)
                        visibility = View.VISIBLE
                    }
                    showPost(adapter)
                }

            } else {
                binding.apply {
                    recyclerViewProfile.visibility = View.INVISIBLE
                    progressBar2.visibility = View.GONE
                    tvNPost.visibility = View.GONE
                }

            }
        }
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

}