package com.spacca.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.darwin.NSObject
import kotlin.coroutines.resume

actual suspend fun pickImage(): ByteArray? {
    val rootVC = PlatformContextHolder.rootViewController ?: return null
    return withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { cont ->
            val config = PHPickerConfiguration()
            config.filter = PHPickerFilter.imagesFilter
            config.selectionLimit = 1

            val picker = PHPickerViewController(configuration = config)
            picker.delegate = IosPickerDelegate(cont)

            rootVC.presentViewController(picker, true, null)
        }
    }
}

private class IosPickerDelegate(
    private val cont: kotlinx.coroutines.CancellableContinuation<ByteArray?>
) : NSObject(), PHPickerViewControllerDelegateProtocol {

    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
        picker.dismissViewControllerAnimated(true, null)

        val result = didFinishPicking.firstOrNull() as? PHPickerResult
        val provider = result?.itemProvider

        if (provider != null && provider.hasItemConformingToTypeIdentifier("public.image")) {
            provider.loadDataRepresentationForTypeIdentifier("public.image") { data: NSData?, _ ->
                if (data != null) {
                    val bytes = ByteArray(data.length.toInt()) { data[it.toULong()].toByte() }
                    cont.resume(bytes)
                } else {
                    cont.resume(null)
                }
            }
        } else {
            cont.resume(null)
        }
    }
}
