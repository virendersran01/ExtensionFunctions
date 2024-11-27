package com.virtualstudios.extensionfunctions.core.presentation

import kotlinx.coroutines.flow.Flow

interface ConnectivityObserver {

    val isConnected: Flow<Boolean>
}