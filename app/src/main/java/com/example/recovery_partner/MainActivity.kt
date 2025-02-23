package com.example.recovery_partner

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.rememberImagePainter
import com.example.recovery_partner.ui.theme.Recovery_PartnerTheme
import io.livekit.android.compose.local.RoomScope
import io.livekit.android.compose.state.rememberTracks
import io.livekit.android.compose.ui.VideoTrackView
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
const val wsURL ="ws://10.0.2.2:7880"


interface ApiService {
    @GET("getToken")
    fun getToken(): Call<TokenResponse>
}

data class TokenResponse(val token: String)

object RetrofitClient {
    private const val BASE_URL ="http://10.0.2.2:5000"
// Change if needed

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

class MainActivity : ComponentActivity() {
    private var token by mutableStateOf<String?>(null)
    private var isJoined by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissions {
            fetchToken()
        }
    }

    private fun fetchToken() {
        RetrofitClient.apiService.getToken().enqueue(object : Callback<TokenResponse> {
            override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                if (response.isSuccessful) {
                    token = response.body()?.token
                } else {
                    Log.e("MainActivity", "Failed to fetch token: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                Log.e("MainActivity", "API call failed: ${t.message}")
            }
        })
    }

    private fun requestPermissions(onGranted: () -> Unit) {
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions.all { it.value }) {
                onGranted()
            } else {
                Log.e("MainActivity", "Permissions denied")
            }
        }

        requestPermissionLauncher.launch(
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO,Manifest.permission.INTERNET)
        )
    }

    override fun onStart() {
        super.onStart()
        setContent {
            Recovery_PartnerTheme {
                if (!isJoined) {
                    CameraPreviewScreen(onJoinClicked = { isJoined = true })
                } else {
                    RoomScreen(token)
                }
            }
        }
    }
}

@Composable
fun CameraPreviewScreen(onJoinClicked: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }
            },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            update = { previewView ->
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build()
                    preview.setSurfaceProvider(previewView.surfaceProvider)

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_FRONT_CAMERA,
                            preview
                        )
                    } catch (e: Exception) {
                        Log.e("CameraPreview", "Use case binding failed", e)
                    }
                }, ContextCompat.getMainExecutor(context))
            }
        )

        Button(
            onClick = onJoinClicked,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Join Room")
        }
    }
}

@Composable
fun RoomScreen(token: String?) {
    if (token == null) {
        Text("Fetching Token...", modifier = Modifier.padding(16.dp))
        return
    }

    var connectionError by remember { mutableStateOf<String?>(null) }

    RoomScope(
        url = wsURL,
        token = token,
        audio = true,
        video = true,
        connect = true,
        // Remove onDisconnect since it's not a valid parameter
        onConnected = {
            Log.d("RoomScreen", "Connected to room")
        },

    ) {
        val trackRefs = rememberTracks()

        Column(modifier = Modifier.fillMaxSize()) {
            connectionError?.let { error ->
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            if (trackRefs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(modifier = Modifier.padding(8.dp))
                        Text(
                            "Waiting for participants...",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(trackRefs.size) { index ->
                        VideoTrackView(
                            trackReference = trackRefs[index],
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun PreviewUI() {
    Recovery_PartnerTheme {
        CameraPreviewScreen(onJoinClicked = {})
    }
}
