package com.example.artkeeper.presentation

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.*
import androidx.work.*
import com.example.artkeeper.data.model.ImageFilter
import com.example.artkeeper.data.model.PostToRemote
import com.example.artkeeper.data.model.User
import com.example.artkeeper.data.repository.EditImageRepository
import com.example.artkeeper.data.repository.UserRepository
import com.example.artkeeper.utils.Constants.firebaseAuth
import com.example.artkeeper.workers.GetLatestPost
import com.example.artkeeper.workers.SavePostRemote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit


class PostViewModel(
    userRepo: UserRepository,
    private val editImageRepository: EditImageRepository,
    private val workManager: WorkManager
) :
    ViewModel() {

    private val uid: String = firebaseAuth.uid.toString()
    private var _user: LiveData<User>? = userRepo.getUserLocal(uid)?.asLiveData()
    val user: LiveData<User>? = _user

    val savePostRemoteWorksInfo: LiveData<List<WorkInfo>> =
        workManager.getWorkInfosForUniqueWorkLiveData("savePostRequest")

    // region:: set informazioni post
    private var _imageUri: MutableLiveData<Uri?> = MutableLiveData(null)
    private var _childName: MutableLiveData<String?> = MutableLiveData(null)
    private var _description: MutableLiveData<String?> = MutableLiveData(null)
    val description: LiveData<String?>
        get() = _description

    private var _timestamp: Long = 0L

    fun setImageUri(imageUri: Uri) {
        _imageUri.value = imageUri
    }

    fun setDescription(description: String) {
        _description.value = description.trim()
    }

    fun setChildName(name: String?) {
        _childName.value = name
    }

    private fun getTimestamp(): Long {
        val calendar = Calendar.getInstance()
        return Date(calendar.time.time).time

    }
    //endregion


    // region:: Image preview
    private val _imagePreviewDataState = MutableLiveData<ImagePreviewState>()
    val imagePreviewUiState: LiveData<ImagePreviewState> get() = _imagePreviewDataState

    fun prepareImagePreview(imageUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                emitImagePreviewState(true)
                editImageRepository.prepareImagePreview(imageUri)
            }.onSuccess {
                emitImagePreviewState(bitmap = it)
            }.onFailure {
                emitImagePreviewState(error = it.message)
            }
        }
    }

    private fun emitImagePreviewState(
        isLoading: Boolean = false,
        bitmap: Bitmap? = null,
        error: String? = null
    ) {
        val dataState = ImagePreviewState(isLoading, bitmap, error)
        _imagePreviewDataState.postValue(dataState)
    }

    data class ImagePreviewState(val isLoading: Boolean, val bitmap: Bitmap?, val error: String?)

    // endregion


    // region:: Carica filtri immagine
    private val _imageFiltersDataState = MutableLiveData<ImageFiltersDataState>()
    val imageFiltersUiState get() = _imageFiltersDataState


    fun loadImageFilters(originalImage: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                emitImageFiltersUiState(isLoading = true)
                editImageRepository.getImageFilters(getPreviewImage(originalImage))
            }.onSuccess { imageFilters ->
                emitImageFiltersUiState(imageFilters = imageFilters)
            }.onFailure {
                emitImageFiltersUiState(error = it.message)
            }
        }
    }

    private fun getPreviewImage(originalImage: Bitmap): Bitmap {
        return kotlin.runCatching {
            val previewWidth = 80
            val previewHeight = originalImage.height * previewWidth / originalImage.width
            Bitmap.createScaledBitmap(originalImage, previewWidth, previewHeight, false)

        }.getOrDefault(originalImage)
    }

    private fun emitImageFiltersUiState(
        isLoading: Boolean = false,
        imageFilters: List<ImageFilter>? = null,
        error: String? = null
    ) {
        val dataState = ImageFiltersDataState(isLoading, imageFilters, error)
        _imageFiltersDataState.postValue(dataState)
    }

    data class ImageFiltersDataState(
        val isLoading: Boolean,
        val imageFilters: List<ImageFilter>?,
        val error: String?
    )

    // endregion


    // region:: Salva immagine
    data class SaveFilteredImageDataState(
        val isLoading: Boolean,
        val uri: Uri?,
        val error: String?
    )

    private val _saveFilteredImageDataState = MutableLiveData<SaveFilteredImageDataState>()
    val saveFilteredImageDataState: LiveData<SaveFilteredImageDataState>
        get() = _saveFilteredImageDataState

    private fun emitSaveFilteredImage(
        isLoading: Boolean = false,
        uri: Uri? = null,
        error: String? = null
    ) {
        val dataState = SaveFilteredImageDataState(isLoading, uri, error)
        _saveFilteredImageDataState.postValue(dataState)
    }

    fun saveFilteredImage(filteredBitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                emitSaveFilteredImage(isLoading = true)
                editImageRepository.saveFilteredImage(filteredBitmap)
            }.onSuccess {
                emitSaveFilteredImage(uri = it)
            }.onFailure {
                emitSaveFilteredImage(error = it.message)
            }
        }
    }
    // endregion


    //region:: inserisci nuovo post
    fun insert() {
        savePostWork()
        reset()
    }

    private fun createPostRemote(): PostToRemote {
        _timestamp = getTimestamp()
        return PostToRemote(
            _imageUri.value.toString(),
            _childName.value,
            _description.value,
            _timestamp.toString()
        )
    }

    private fun savePostWork() {
        val constraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val postRemote = createPostRemote()
        val savePostRequest =
            OneTimeWorkRequestBuilder<SavePostRemote>().setInputData(
                workDataOf(
                    "uid" to uid,
                    "imagePath" to postRemote.imagePath,
                    "childName" to postRemote.sketchedBy,
                    "description" to postRemote.description,
                    "timestamp" to postRemote.postTimestamp
                )
            )
                .setConstraints(constraint).setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

        workManager.beginUniqueWork(
            "savePostRequest",
            ExistingWorkPolicy.KEEP,
            savePostRequest
        ).then(getLatestPost())
            .enqueue()
    }

    private fun getLatestPost(): OneTimeWorkRequest {
        val constraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        return OneTimeWorkRequestBuilder<GetLatestPost>().setInputData(workDataOf("uid" to uid))
            .setConstraints(constraint)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            ).addTag("getLatestPostWorker").build()
    }
    //endregion


    fun checkPost(): Boolean {
        if (_imageUri.value == null)
            return false

        return true
    }


    fun reset() {
        _imageUri.value = null
        _description.value = null
        _childName.value = null
        _timestamp = 0L
    }

}

@Suppress("UNCHECKED_CAST")
class PostViewModelFactory(
    private val userRepo: UserRepository,
    private val editImageRepository: EditImageRepository,
    private val workManager: WorkManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PostViewModel(userRepo, editImageRepository, workManager) as T
    }
}