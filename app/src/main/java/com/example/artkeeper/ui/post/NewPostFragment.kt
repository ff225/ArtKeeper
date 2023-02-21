package com.example.artkeeper.ui.post

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.isEmpty
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.bumptech.glide.Glide
import com.example.artkeeper.BuildConfig
import com.example.artkeeper.R
import com.example.artkeeper.adapter.ImageFiltersAdapter
import com.example.artkeeper.data.model.ImageFilter
import com.example.artkeeper.databinding.FragmentNewPostBinding
import com.example.artkeeper.presentation.PostViewModel
import com.example.artkeeper.presentation.PostViewModelFactory
import com.example.artkeeper.utils.ArtKeeper
import com.example.artkeeper.utils.ImageFilterListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import jp.co.cyberagent.android.gpuimage.GPUImage
import java.io.File


class NewPostFragment : Fragment(R.layout.fragment_new_post), ImageFilterListener {

    private val TAG = javaClass.simpleName

    enum class Source {
        CAMERA, GALLERY
    }

    private var imageFromUser: Uri? = null
    private lateinit var gpuImage: GPUImage
    private lateinit var originalBitmap: Bitmap
    private val filteredBitmap = MutableLiveData<Bitmap>()
    private var _binding: FragmentNewPostBinding? = null
    private val binding
        get() = _binding!!


    private val viewModel: PostViewModel by viewModels {
        PostViewModelFactory(
            (requireActivity().application as ArtKeeper).userRepository,
            (requireActivity().application as ArtKeeper).editImageRepository,
            (requireActivity().application as ArtKeeper).workManager
        )
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentNewPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listeners()
        setupObservers()
    }

    private fun listeners() {
        binding.apply {
            pickFromGallery.setOnClickListener { takePhoto(Source.GALLERY) }

            pickFromCamera.setOnClickListener { takePhoto(Source.CAMERA) }

            shareButton.setOnClickListener {
                filteredBitmap.value?.let {
                    viewModel.saveFilteredImage(it)
                } ?: Toast.makeText(
                    requireContext(),
                    getText(R.string.error_photo_post),
                    Toast.LENGTH_LONG
                ).show()
            }

            cancelButton.setOnClickListener { cancelAction() }

            imageViewPost.setOnLongClickListener {
                imageViewPost.setImageBitmap(originalBitmap)
                return@setOnLongClickListener false
            }

            imageViewPost.setOnClickListener {
                imageViewPost.setImageBitmap(filteredBitmap.value)
            }

        }
    }

    private fun setupObservers() {
        viewModel.user?.observe(viewLifecycleOwner) { user ->
            user?.let {
                Log.d(TAG, user.nickName)

                Log.d(TAG, user.nameChild?.isNotEmpty().toString())

                binding.radioGroup.removeAllViews()
                if (binding.radioGroup.isEmpty() && user.nameChild!!.isNotEmpty()) {
                    binding.textviewAddChild.visibility = View.VISIBLE
                    binding.radioGroup.visibility = View.VISIBLE
                    for (item in user.nameChild) {
                        val radioButton = RadioButton(requireContext())
                        radioButton.apply {
                            layoutParams = LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                            id = user.nameChild.indexOf(item)
                            text = item
                        }
                        binding.radioGroup.addView(radioButton)
                    }
                } else {
                    binding.textviewAddChild.visibility = View.GONE
                    binding.radioGroup.visibility = View.GONE
                }
            }

        }

        viewModel.imagePreviewUiState.observe(viewLifecycleOwner) {
            val dataState = it ?: return@observe

            dataState.bitmap?.let { bitmap ->
                originalBitmap = bitmap
                filteredBitmap.value = bitmap

                with(originalBitmap) {
                    gpuImage.setImage(this)
                    viewModel.loadImageFilters(this)
                }
            } ?: kotlin.run {
                dataState.error?.let { error ->
                    Log.d(TAG, error)
                }
            }
        }

        viewModel.imageFiltersUiState.observe(viewLifecycleOwner) {
            val imageFilterDataState = it ?: return@observe
            imageFilterDataState.imageFilters?.let { imageFilters ->
                ImageFiltersAdapter(imageFilters, this).also { adapter ->
                    binding.filtersRecyclerView.visibility = View.VISIBLE
                    binding.filtersRecyclerView.adapter = adapter
                }
            } ?: kotlin.run {
                imageFilterDataState.error?.let { error ->
                    Log.d(TAG, error)
                }
            }
        }

        filteredBitmap.observe(viewLifecycleOwner) {
            binding.imageViewPost.visibility = View.VISIBLE
            Glide.with(binding.imageViewPost.context)
                .load(it)
                .into(binding.imageViewPost)
            binding.llPickPhoto.visibility = View.GONE
        }

        viewModel.saveFilteredImageDataState.observe(viewLifecycleOwner) {
            val saveFilteredImageDataState = it ?: return@observe
            saveFilteredImageDataState.uri?.let { uri ->
                imageFromUser = uri
                shareAction()
            } ?: kotlin.run {
                saveFilteredImageDataState.error?.let { error ->
                    Log.d(TAG, error)
                }
            }
        }

        viewModel.description.observe(viewLifecycleOwner) { text ->
            binding.textInputDescription.setText(text)
        }
    }

    private fun savePost(): Observer<List<WorkInfo>> {
        return Observer { listOfWorkInfo ->
            if (listOfWorkInfo.isNullOrEmpty())
                return@Observer
            val workInfo = listOfWorkInfo[listOfWorkInfo.lastIndex]
            Log.d(TAG, workInfo.state.toString())
            Log.d(TAG, listOfWorkInfo[0].state.toString())

            if (workInfo.state.isFinished) {
                if (WorkInfo.State.FAILED == workInfo.state)
                    Toast.makeText(
                        requireContext(),
                        getText(R.string.failed_sharing),
                        Toast.LENGTH_LONG
                    ).show()
                else {
                    Toast.makeText(
                        requireContext(),
                        getText(R.string.success_sharing),
                        Toast.LENGTH_LONG
                    )
                        .show()
                    findNavController().navigate(NewPostFragmentDirections.actionMoveToHome())
                }
            } else {
                binding.apply {
                    progressBarSharing.visibility = View.VISIBLE
                    filtersRecyclerView.visibility = View.GONE
                    cancelButton.isEnabled = false
                    shareButton.isEnabled = false
                    radioGroup.isEnabled = false
                    textInputDescription.isEnabled = false
                }
                (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(
                    false
                )
                activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)?.visibility =
                    View.INVISIBLE

                requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
                    cancelAction()
                }
            }
        }
    }

    private val getPhotoFromPhotoPicker =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                Log.d(TAG, "in PhotoPicker selected URI: $uri")
                gpuImage = GPUImage(requireContext())
                viewModel.prepareImagePreview(uri)
            } else {
                Log.d(TAG, "in PhotoPicker no media selected")
            }
        }

    private val getPhotoFromCamera =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
            if (isSuccess) {
                imageFromUser.let {
                    gpuImage = GPUImage(requireContext())
                    Log.d(TAG, it?.path.toString())
                    viewModel.prepareImagePreview(it!!)
                }
            } else
                imageFromUser = null

        }

    private fun takePhoto(photoFrom: Source) {
        when (photoFrom) {
            Source.GALLERY -> {
                Log.d(TAG, "Pick photo from gallery")
                getPhotoFromPhotoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
            else -> {
                Log.d(TAG, "Pick photo from camera")
                lifecycleScope.launchWhenStarted {
                    getTmpFileUri().let { uri ->
                        imageFromUser = uri
                        getPhotoFromCamera.launch(uri)
                    }

                }
            }
        }
    }

    private fun getTmpFileUri(): Uri {
        val tmpFile =
            File.createTempFile("tmp_image_file", ".jpg", requireContext().cacheDir).apply {
                createNewFile()
                deleteOnExit()
            }

        return FileProvider.getUriForFile(
            requireContext(),
            "${BuildConfig.APPLICATION_ID}.file_provider",
            tmpFile
        )
    }

    private fun cancelAction() {

        if (viewModel.checkPost())
            MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.cancel_post)
                .setNegativeButton(R.string.no) { dialog, _ ->
                    dialog.cancel()
                }
                .setPositiveButton(R.string.yes) { dialog, _ ->
                    findNavController().navigate(R.id.action_move_to_home)
                    binding.apply {
                        imageViewPost.visibility = View.GONE
                        textInputDescription.isFocusable = false
                        textInputDescription.isFocusableInTouchMode = true
                        radioGroup.clearCheck()
                    }
                    WorkManager.getInstance(requireContext()).cancelAllWork()
                    viewModel.reset()
                    dialog.dismiss()
                }.show()
        else
            findNavController().navigate(R.id.action_move_to_home)


    }

    private fun shareAction() {

        Log.d(TAG, "condivido il post...")
        Log.d(
            TAG,
            binding.radioGroup.findViewById<RadioButton>(binding.radioGroup.checkedRadioButtonId)?.text.toString()
        )
        Log.d(
            TAG,
            (binding.radioGroup.checkedRadioButtonId).toString()
        )

        viewModel.apply {
            setImageUri(imageFromUser!!)
            setDescription(binding.textInputDescription.text.toString().trim())
            setChildName(binding.radioGroup.findViewById<RadioButton>(binding.radioGroup.checkedRadioButtonId)?.text as String?)
            insert()
            savePostRemoteWorksInfo.observe(viewLifecycleOwner, savePost())
        }
    }

    override fun onFilterSelected(imageFilter: ImageFilter) {
        with(imageFilter) {
            with(gpuImage) {
                setFilter(filter)
                filteredBitmap.value = bitmapWithFilterApplied
            }
        }
    }

    override fun onDestroy() {
        _binding = null
        Log.d(TAG, "onDestroy")
        super.onDestroy()
    }
}