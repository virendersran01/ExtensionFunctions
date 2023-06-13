package com.virtualstudios.extensionfunctions

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.net.NetworkRequest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

object ConnectedCompat {

    private val IMPL: ConnectedCompatImpl

    init {
        IMPL = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            MarshMallowImpl
        } else {
            BaseImpl
        }
    }

    fun isConnected(connectivityManager: ConnectivityManager) =
        IMPL.isConnected(connectivityManager)

    internal interface ConnectedCompatImpl {
        fun isConnected(connectivityManager: ConnectivityManager): Boolean
    }

    object BaseImpl : ConnectedCompatImpl {
        @Suppress("DEPRECATION")
        override fun isConnected(connectivityManager: ConnectivityManager): Boolean =
            connectivityManager.activeNetworkInfo?.isConnected ?: false

    }

    object MarshMallowImpl : ConnectedCompatImpl {
        @RequiresApi(Build.VERSION_CODES.M)
        override fun isConnected(connectivityManager: ConnectivityManager): Boolean =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                ?.hasCapability(NET_CAPABILITY_INTERNET) == true
    }
}

class NetworkMonitor @Inject constructor(
    private val connectivityManager: ConnectivityManager
) {

    val isConnected: Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                trySend(true)
            }

            override fun onLost(network: Network) {
                trySend(false)
                super.onLost(network)
            }
        }
        val request = NetworkRequest.Builder()
            .addCapability(NET_CAPABILITY_INTERNET)
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                }
            }
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()
        trySend(ConnectedCompat.isConnected(connectivityManager))
        connectivityManager.registerNetworkCallback(request, callback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
}

sealed class NetworkStatus {
    object Available : NetworkStatus()
    object Unavailable : NetworkStatus()
}

class NetworkStatusTracker(context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val networkStatus = callbackFlow<NetworkStatus> {
        val networkStatusCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onUnavailable() {
                trySend(NetworkStatus.Unavailable)
            }

            override fun onAvailable(network: Network) {
                trySend(NetworkStatus.Available)
            }

            override fun onLost(network: Network) {
                trySend(NetworkStatus.Unavailable)
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, networkStatusCallback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(networkStatusCallback)
        }
    }
}

sealed interface NetworkConnectionState {
    object Available : NetworkConnectionState
    object Unavailable : NetworkConnectionState
}

private fun networkCallback(callback: (NetworkConnectionState) -> Unit): ConnectivityManager.NetworkCallback {
    return object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            callback(NetworkConnectionState.Available)
        }

        override fun onLost(network: Network) {
            callback(NetworkConnectionState.Unavailable)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.M)
private fun getCurrentConnectivityState(connectivityManager: ConnectivityManager): NetworkConnectionState {
    val network = connectivityManager.activeNetwork

    val connected = connectivityManager.getNetworkCapabilities(network)
        ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false

    return if (connected) NetworkConnectionState.Available else NetworkConnectionState.Unavailable
}

@RequiresApi(Build.VERSION_CODES.M)
private fun Context.observeConnectivityAsFlow(): Flow<NetworkConnectionState> = callbackFlow {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val callback = networkCallback { connectionState ->
        trySend(connectionState)
    }

    val networkRequest = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .build()

    connectivityManager.registerNetworkCallback(networkRequest, callback)

    val currentState = getCurrentConnectivityState(connectivityManager)
    trySend(currentState)

    awaitClose {
        connectivityManager.unregisterNetworkCallback(callback)
    }
}

class NetworkObserver(context: Context) {
    private val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    @RequiresApi(Build.VERSION_CODES.N)
    fun observe() = callbackFlow {
        val callback = object: ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                launch { trySend(Status.Available) }
            }

            override fun onUnavailable() {
                super.onUnavailable()
                launch { trySend(Status.Unavailable) }
            }

            override fun onLosing(network: Network, maxMsToLive: Int) {
                super.onLosing(network, maxMsToLive)
                launch { trySend(Status.Losing) }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                launch { trySend(Status.Lost) }
            }
        }

        cm.registerDefaultNetworkCallback(callback)

        awaitClose { cm.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()

    enum class Status {
        Available,
        Unavailable,
        Losing,
        Lost
    }
}

/*
lifecycleScope.launchWhenStarted {
    networkObserver.observe().collectLatest {
        binding.tvConnectionStatus.text = "Connection status: ${it}"
    }
}*/


class NetworkMonitor2(context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    fun isNetworkAvailable(): Boolean {
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    fun registerNetworkCallback(networkCallback: ConnectivityManager.NetworkCallback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        } else {
            val builder = NetworkRequest.Builder()
            connectivityManager.registerNetworkCallback(builder.build(), networkCallback)
        }
    }

    fun unregisterNetworkCallback(networkCallback: ConnectivityManager.NetworkCallback) {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}

val networkCallback = object : ConnectivityManager.NetworkCallback() {

    override fun onAvailable(network: Network) {
        // Called when a network is available
    }

    override fun onLost(network: Network) {
        // Called when a network is lost
    }
}

/*val networkMonitor = NetworkMonitor2(context)
networkMonitor.registerNetworkCallback(networkCallback)

networkMonitor2.unregisterNetworkCallback(networkCallback)
*/

//Checking for internet connectivity using a BroadcastReceiver:

class ConnectivityReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        if (networkInfo != null && networkInfo.isConnected) {
            // internet connection is available
        } else {
            // internet connection is not available
        }
    }
}


//Using a callback interface to notify when internet connectivity changes:
interface ConnectivityListener {
    fun onConnectivityChanged(isConnected: Boolean)
}

class ConnectivityMonitor(private val context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val callbackList = mutableListOf<ConnectivityListener>()

    private val connectivityCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            notifyListeners(true)
        }

        override fun onLost(network: Network) {
            notifyListeners(false)
        }
    }

    fun registerListener(listener: ConnectivityListener) {
        callbackList.add(listener)
    }

    fun unregisterListener(listener: ConnectivityListener) {
        callbackList.remove(listener)
    }

    fun startMonitoring() {
        val builder = NetworkRequest.Builder()
        connectivityManager.registerNetworkCallback(builder.build(), connectivityCallback)
    }

    fun stopMonitoring() {
        connectivityManager.unregisterNetworkCallback(connectivityCallback)
    }

    private fun notifyListeners(isConnected: Boolean) {
        for (listener in callbackList) {
            listener.onConnectivityChanged(isConnected)
        }
    }
}

//Using a library like RxAndroid to monitor network connectivity:

/*
class MainActivity2 : AppCompatActivity() {

    private val connectivityObservable = ConnectivityObservable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        connectivityObservable.observe(this)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { isConnected ->
                if (isConnected) {
                    // internet connection is available
                } else {
                    // internet connection is not available
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        connectivityObservable.stopObserving(this)
    }
}*/
