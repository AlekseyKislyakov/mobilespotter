package com.example.mobile_spotter.presentation.base

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile_spotter.data.entities.LoadableResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

abstract class BaseViewModel: ViewModel() {

    protected fun <T> loadData(
        block: suspend () -> T,
    ): Flow<LoadableResult<T>> = flow {
        try {
            emit(LoadableResult.loading())
            emit(LoadableResult.success(block()))
        } catch (error: Throwable) {
            emit(LoadableResult.failure<T>(error))
        }
    }

    protected fun <T> loadData(
        block: Flow<T>,
    ): Flow<LoadableResult<T>> = flow {
        try {
            emit(LoadableResult.loading())
            block.collect {
                emit(LoadableResult.success(it))
            }
        } catch (error: Throwable) {
            emit(LoadableResult.failure<T>(error))
        }
    }

    protected fun <T> MutableLiveData<LoadableResult<T>>.launchLoadData(
        block: suspend () -> T,
    ): Job = viewModelScope.launch {
        loadData(block).collect { result -> this@launchLoadData.postValue(result) }
    }

    protected fun <T> MutableLiveData<LoadableResult<T>>.launchLoadData(
        block: Flow<T>,
    ): Job = viewModelScope.launch {
        loadData(block).collect { result -> this@launchLoadData.postValue(result) }
    }

//    protected fun <T : Any> MutableLiveData<PagingData<T>>.launchPagingData(
//        block: () -> Flow<PagingData<T>>
//    ): Job = viewModelScope.launch {
//        block()
//            .cachedIn(viewModelScope)
//            .collectLatest { this@launchPagingData.postValue(it) }
//    }
//
//    protected fun MutableLiveData<LoadableResult<Empty>>.bindPagingState(loadState: CombinedLoadStates) {
//        when (loadState.source.refresh) {
//            // Only show the list if refresh succeeds.
//            is LoadState.NotLoading -> this.postValue(LoadableResult.success(Empty()))
//            // Show loading spinner during initial load or refresh.
//            is LoadState.Loading -> this.postValue(LoadableResult.loading())
//            // Show the retry state if initial load or refresh fails.
//            is LoadState.Error -> this.postValue(LoadableResult.failure((loadState.source.refresh as LoadState.Error).error))
//        }
//    }
}
