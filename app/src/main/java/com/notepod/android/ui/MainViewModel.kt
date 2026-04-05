package com.notepod.android.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.notepod.android.data.FlatNode
import com.notepod.android.data.Note
import com.notepod.android.data.NoteRepository
import kotlinx.coroutines.launch

class MainViewModel(private val repo: NoteRepository) : ViewModel() {

    private val _tree = MutableLiveData<List<FlatNode>>(emptyList())
    val flatTree: LiveData<List<FlatNode>> = _tree

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    init { refreshTree() }

    fun refreshTree() {
        viewModelScope.launch {
            _loading.value = true
            _tree.value = repo.getFlatTree()
            _loading.value = false
        }
    }

    fun addNote(parentId: String?, title: String) {
        viewModelScope.launch { repo.addNote(parentId, title); refreshTree() }
    }

    fun deleteNote(id: String) {
        viewModelScope.launch { repo.deleteSubtree(id); refreshTree() }
    }

    fun toggleExpand(note: Note) {
        viewModelScope.launch { repo.toggleExpand(note); refreshTree() }
    }

    fun saveNote(id: String, title: String, content: String) {
        viewModelScope.launch {
            val n = repo.getNoteById(id) ?: return@launch
            repo.updateNote(n.copy(title = title, content = content))
            refreshTree()
        }
    }
}

class MainViewModelFactory(private val repo: NoteRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = MainViewModel(repo) as T
}
