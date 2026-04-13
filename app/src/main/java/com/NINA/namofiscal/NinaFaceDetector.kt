package com.nina.namofiscal

import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

class NinaFaceDetector(private val onResult: (FaceState) -> Unit) {

    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .build()

    private val detector = FaceDetection.getClient(options)

    enum class FaceState {
        OLHANDO,        // Rosto detectado e olhos abertos
        ESCONDIDO,      // Nenhum rosto detectado
        DESVIANDO_OLHAR, // Rosto lá, mas olhos fechados ou virado
        SORRINDO_SAFADO  // Detectou sorriso (perigoso com a Nina!)
    }

    fun analisarImagem(image: InputImage) {
        detector.process(image)
            .addOnSuccessListener { faces ->
                if (faces.isEmpty()) {
                    onResult(FaceState.ESCONDIDO)
                    return@addOnSuccessListener
                }

                for (face in faces) {
                    val sorrindo = (face.smilingProbability ?: 0f) > 0.7f
                    val olhoEsquerdoAberto = (face.leftEyeOpenProbability ?: 0f) > 0.4f
                    val olhoDireitoAberto = (face.rightEyeOpenProbability ?: 0f) > 0.4f

                    when {
                        sorrindo -> onResult(FaceState.SORRINDO_SAFADO)
                        olhoEsquerdoAberto && olhoDireitoAberto -> onResult(FaceState.OLHANDO)
                        else -> onResult(FaceState.DESVIANDO_OLHAR)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("NINA_FACE", "Erro ao detectar rosto: ${e.message}")
            }
    }
}
