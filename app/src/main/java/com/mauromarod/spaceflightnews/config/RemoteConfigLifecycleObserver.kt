package com.mauromarod.spaceflightnews.config

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.mauromarod.spaceflightnews.core.domain.repository.RemoteConfigRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class RemoteConfigLifecycleObserver(
    private val remoteConfigRepository: RemoteConfigRepository,
    private val scope: CoroutineScope,
) : DefaultLifecycleObserver {

    override fun onResume(owner: LifecycleOwner) {
        scope.launch { remoteConfigRepository.fetchAndActivate() }
    }
}
