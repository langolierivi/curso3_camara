package com.esprinet.curso3

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.esprinet.curso3.ui.theme.Curso3Theme
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Button
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.viewinterop.AndroidView
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissions()
        setContent {
            CameraPreviewScreen()
        }

    }

    fun permissions() {
        //Comprueba que tenga los permisos
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.CAMERA
            )
            != PackageManager.PERMISSION_GRANTED
        ) {


            //// El permiso no esta aprobado.
            // Mostramos una explicación de porque pedimos el permiso
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    android.Manifest.permission.CAMERA
                )
            ) {
                //Podemos mostrar una ventana explicativa para el permiso.


            } else {
                // Nosotros hacemos la petición de los permisos.
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        android.Manifest.permission.CAMERA,
                    ),
                    0
                )
            }
        }
        //Si tenemos los permisos no hacemos nada
        else {
            //
        }

    }


}

@Composable
fun CameraPreviewScreen() {
    val lensFacing = CameraSelector.LENS_FACING_BACK
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val preview = Preview.Builder().build()
    val previewView = remember {
        PreviewView(context)
    }
    val cameraxSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
    val imageCapture = remember {
        ImageCapture.Builder().build()
    }
    LaunchedEffect(lensFacing) {
        val cameraProvider = context.getCameraProvider()
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(lifecycleOwner, cameraxSelector, preview,imageCapture)
        preview.setSurfaceProvider(previewView.surfaceProvider)
    }
    Box{
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        Button(onClick = { captureImages(imageCapture,context) }) {
            Text(text = "Haz foto")

        }

    }

}

//Creamos un nuevo metodo para la clase Contexto
private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
    suspendCoroutine { continuation ->
        ProcessCameraProvider.getInstance(this).also { cameraProvider ->
            cameraProvider.addListener({
                continuation.resume(cameraProvider.get())
            }, ContextCompat.getMainExecutor(this))
        }
    }

//Creamos la funcion que nos genera el fichero de la imagen
private fun captureImages(imageCapture: ImageCapture,context: Context){
    var fileimg=File.createTempFile("imagen",".jpg")
    val outputOptions=ImageCapture.OutputFileOptions.Builder(
        fileimg).build()
    imageCapture.takePicture(outputOptions,ContextCompat.getMainExecutor(context),object:ImageCapture.OnImageSavedCallback{
        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
            Toast.makeText(context,"Guardado correctamente",Toast.LENGTH_SHORT).show()
        }

        override fun onError(exception: ImageCaptureException) {
            Toast.makeText(context,"Ha habido un error al guardar"+exception.message.toString(),Toast.LENGTH_SHORT).show()
            Log.e("error",exception.message.toString())

        }

    })
}

