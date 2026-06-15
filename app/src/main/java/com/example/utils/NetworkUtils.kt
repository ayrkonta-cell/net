package com.example.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import java.net.Inet4Address
import java.net.NetworkInterface
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object NetworkUtils {

    fun getLocalIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address is Inet4Address) {
                        val host = address.hostAddress
                        if (!host.isNullOrEmpty()) return host
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return "N/A"
    }

    enum class ConnectionType {
        WIFI, CELLULAR, ETHERNET, OFFLINE
    }

    fun getConnectionType(context: Context): ConnectionType {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return ConnectionType.OFFLINE
        
        val activeNetwork = connectivityManager.activeNetwork ?: return ConnectionType.OFFLINE
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return ConnectionType.OFFLINE

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionType.CELLULAR
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> ConnectionType.ETHERNET
            else -> ConnectionType.OFFLINE
        }
    }

    fun getNetworkDetails(context: Context): Map<String, String> {
        val details = mutableMapOf<String, String>()
        details["Local IP"] = getLocalIpAddress()
        
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (connectivityManager != null) {
            val activeNetwork = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            if (activeNetwork != null && capabilities != null) {
                val downSpeed = capabilities.linkDownstreamBandwidthKbps / 1000.0 // in Mbps
                if (downSpeed > 0) {
                    details["Link Speed (Down)"] = String.format("%.1f Mbps", downSpeed)
                }
                
                val linkProperties = connectivityManager.getLinkProperties(activeNetwork)
                val dnsList = linkProperties?.dnsServers?.mapNotNull { it.hostAddress }
                if (!dnsList.isNullOrEmpty()) {
                    details["DNS Servers"] = dnsList.joinToString(", ")
                }
            }
        }
        return details
    }

    suspend fun getPublicIpAddress(): String = withContext(Dispatchers.IO) {
        val client = OkHttpClient.Builder()
            .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
            .build()
        val request = Request.Builder()
            .url("https://api.ipify.org")
            .build()
        
        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    response.body?.string()?.trim() ?: "Unknown"
                } else {
                    "Error (${response.code})"
                }
            }
        } catch (e: Exception) {
            "Offline / Unreachable"
        }
    }
}
