package com.spacca.app.data

import android.net.Uri
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.spacca.app.data.cache.AndroidContextHolder
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Holds the [ActivityResultLauncher] registered in [com.spacca.app.MainActivity] and bridges
 * the async photo-picker result back into a [kotlinx.coroutines] [suspendCancellableCoroutine].
 */
object ImagePickerHolder {
    private var launcher: androidx.activity.result.ActivityResultLauncher<PickVisualMediaRequest>? = null
    private var continuation: kotlinx.coroutines.CancellableContinuation<ByteArray?>? = null

    fun register(activity: androidx.activity.ComponentActivity) {
        launcher = activity.registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri -> onResult(uri) }
    }

    private fun onResult(uri: Uri?) {
        val cont = continuation ?: return
        continuation = null
        if (uri != null) {
            val ctx = AndroidContextHolder.context ?: run { cont.resume(null); return }
            val bytes = ctx.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            cont.resume(bytes)
        } else {
            cont.resume(null)
        }
    }

    suspend fun pickImage(): ByteArray? {
        return suspendCancellableCoroutine { cont ->
            continuation = cont
            val l = launcher
            if (l != null) {
                l.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            } else {
                cont.resume(null)
            }
        }
    }
}

actual suspend fun pickImage(): ByteArray? = ImagePickerHolder.pickImage()
