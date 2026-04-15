package com.kado.app.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.kado.app.util.topViewController
import kotlin.coroutines.resume
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UniformTypeIdentifiers.UTTypeData
import platform.darwin.NSObject
import platform.posix.memcpy

// Strong reference to prevent GC while picker is active.
// UIDocumentPickerViewController.delegate is a weak property in UIKit.
private var retainedDelegate: NSObject? = null

@Composable
actual fun rememberApkgPickerLauncher(onResult: (ByteArray?) -> Unit): () -> Unit {
    val scope = rememberCoroutineScope()

    return remember {
        {
            scope.launch {
                val bytes = pickFile()
                onResult(bytes)
            }
        }
    }
}

private suspend fun pickFile(): ByteArray? = withContext(Dispatchers.Main) {
    suspendCancellableCoroutine { continuation ->
        val picker = UIDocumentPickerViewController(
            forOpeningContentTypes = listOf(UTTypeData)
        )

        val delegate = object : NSObject(), UIDocumentPickerDelegateProtocol {
            override fun documentPicker(
                controller: UIDocumentPickerViewController,
                didPickDocumentsAtURLs: List<*>
            ) {
                retainedDelegate = null
                val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
                if (url == null) {
                    if (continuation.isActive) continuation.resume(null)
                    return
                }
                val accessing = url.startAccessingSecurityScopedResource()
                try {
                    val data = NSData.dataWithContentsOfURL(url)
                    val bytes = data?.toByteArray()
                    if (continuation.isActive) continuation.resume(bytes)
                } finally {
                    if (accessing) url.stopAccessingSecurityScopedResource()
                }
            }

            override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
                retainedDelegate = null
                if (continuation.isActive) continuation.resume(null)
            }
        }

        retainedDelegate = delegate
        picker.delegate = delegate
        picker.allowsMultipleSelection = false

        val rootViewController = topViewController()
        if (rootViewController == null) {
            retainedDelegate = null
            if (continuation.isActive) continuation.resume(null)
            return@suspendCancellableCoroutine
        }
        rootViewController.presentViewController(picker, animated = true, completion = null)

        continuation.invokeOnCancellation {
            retainedDelegate = null
            picker.dismissViewControllerAnimated(false, completion = null)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    val bytes = ByteArray(size)
    if (size > 0) {
        bytes.usePinned { pinned ->
            memcpy(pinned.addressOf(0), this.bytes, length)
        }
    }
    return bytes
}
