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
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerEditedImage
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.darwin.NSObject
import platform.posix.memcpy

// Strong reference to prevent GC while picker is active.
// UIImagePickerController.delegate is a weak property in UIKit.
private var retainedImageDelegate: NSObject? = null

@Composable
actual fun rememberImagePickerLauncher(onResult: (ByteArray?) -> Unit): () -> Unit {
    val scope = rememberCoroutineScope()

    return remember {
        {
            scope.launch {
                val bytes = pickImage()
                onResult(bytes)
            }
        }
    }
}

private suspend fun pickImage(): ByteArray? = withContext(Dispatchers.Main) {
    suspendCancellableCoroutine { continuation ->
        val picker = UIImagePickerController()
        picker.sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary

        val delegate = object :
            NSObject(),
            UIImagePickerControllerDelegateProtocol,
            UINavigationControllerDelegateProtocol {
            override fun imagePickerController(
                picker: UIImagePickerController,
                didFinishPickingMediaWithInfo: Map<Any?, *>
            ) {
                retainedImageDelegate = null
                picker.dismissViewControllerAnimated(true, completion = null)
                val image = (
                    didFinishPickingMediaWithInfo[UIImagePickerControllerEditedImage]
                        ?: didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage]
                    ) as? UIImage
                if (image == null) {
                    if (continuation.isActive) continuation.resume(null)
                    return
                }
                val data = UIImageJPEGRepresentation(image, 0.85)
                val bytes = data?.toByteArray()
                if (continuation.isActive) continuation.resume(bytes)
            }

            override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
                retainedImageDelegate = null
                picker.dismissViewControllerAnimated(true, completion = null)
                if (continuation.isActive) continuation.resume(null)
            }
        }

        retainedImageDelegate = delegate
        picker.delegate = delegate

        val rootViewController = topViewController()
        if (rootViewController == null) {
            retainedImageDelegate = null
            if (continuation.isActive) continuation.resume(null)
            return@suspendCancellableCoroutine
        }
        rootViewController.presentViewController(picker, animated = true, completion = null)

        continuation.invokeOnCancellation {
            retainedImageDelegate = null
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
