package com.notepod.android.ui

import androidx.lifecycle.*
import com.notepod.android.data.FlatNode
import com.notepod.android.data.Note
import com.notepod.android.data.NoteRepository
import kotlinx.coroutines.launch

class MainViewModel(private val repo: NoteRepository) : ViewModel() {

    private val _flatTree = MutableLiveData<List<FlatNode>>()
    val flatTree: LiveData<List<FlatNode>> = _flatTree

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    init {
        refreshTree()
    }

    fun refreshTree() {
        viewModelScope.launch {
            _loading.value = true
            _flatTree.value = repo.getFlatTree()
            _loading.value = false
        }
    }

    fun addNote(parentId: String?, title: String) {
        viewModelScope.launch {
            repo.addNote(parentId, title)
            refreshTree()
        }
    }

    fun deleteNote(id: String) {
        viewModelScope.launch {
            repo.deleteSubtree(id)
            refreshTree()
        }
    }

    fun toggleExpand(note: Note) {
        viewModelScope.launch {
            repo.toggleExpand(note)
            refreshTree()
        }
    }

    fun saveNote(id: String, title: String, content: String) {
        viewModelScope.launch {
            val existing = repo.getNoteById(id) ?: return@launch
            repo.updateNote(existing.copy(title = title, content = content))
            refreshTree()
        }
    }
}

class MainViewModelFactory(private val repo: NoteRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return MainViewModel(repo) as T
    }
}
