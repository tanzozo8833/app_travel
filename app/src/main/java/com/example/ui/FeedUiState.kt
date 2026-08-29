package com.example.ui

import com.example.data.model.Post

sealed interface FeedUiState {
    /**
     * Feed is loading for the first time or refreshing without cache.
     */
    object Loading : FeedUiState

    /**
     * Feed has successfully loaded posts from Firestore server or is showing cached posts with offline status.
     */
    data class Success(
        val posts: List<Post>,
        val isOffline: Boolean = false,
        val hasMore: Boolean = true
    ) : FeedUiState

    /**
     * Feed is empty (no posts published yet by community).
     */
    data class Empty(
        val isOffline: Boolean = false
    ) : FeedUiState

    /**
     * Error loading feed with no cached posts available.
     */
    data class Error(
        val message: String,
        val cachedPosts: List<Post> = emptyList(),
        val isOffline: Boolean = false
    ) : FeedUiState
}
