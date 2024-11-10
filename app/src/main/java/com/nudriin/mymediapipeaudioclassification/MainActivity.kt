package com.nudriin.mymediapipeaudioclassification

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.mediapipe.tasks.components.containers.Classifications
import com.nudriin.mymediapipeaudioclassification.databinding.ActivityMainBinding
import java.text.NumberFormat

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var  audioClassifierHelper: AudioClassifierHelper
    private var isRecording = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeAudioClassifierHelper()
    }

    private fun initializeAudioClassifierHelper() {
        audioClassifierHelper = AudioClassifierHelper(
            context = this,
            classifierListener = object : AudioClassifierHelper.ClassifierListener {
                override fun onResults(results: List<Classifications>, inferenceTime: Long) {
                    runOnUiThread {
                        results.let { it ->
                            if(it.isNotEmpty() && it[0].categories().isNotEmpty()) {
                                println(it)
                                val sortedCategories =
                                    it[0].categories().sortedByDescending { it?.score() }
                                val displayResult =
                                    sortedCategories.joinToString("\n") {
                                        "${it.categoryName()} " + NumberFormat.getPercentInstance()
                                            .format(it.score()).trim()
                                    }
                                binding.tvResult.text = displayResult
                            } else {
                                binding.tvResult.text = ""
                            }
                        }
                    }
                }

                override fun onError(error: String) {
                    Toast.makeText(this@MainActivity, error, Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    private fun setOnClickListener() {
        binding.btnStart.setOnClickListener {
            audioClassifierHelper.startAudioClassification()
            isRecording = true
            updateButtonStates()
        }
        binding.btnStop.setOnClickListener {
            audioClassifierHelper.stopAudioClassification()
            isRecording = false
            updateButtonStates()
        }
    }

    private fun updateButtonStates() {
        binding.btnStart.isEnabled = !isRecording
        binding.btnStop.isEnabled = isRecording
    }

    override fun onResume() {
        super.onResume()
        if (isRecording) {
            audioClassifierHelper.startAudioClassification()
        }
    }

    override fun onPause() {
        super.onPause()
        if (::audioClassifierHelper.isInitialized) {
            audioClassifierHelper.stopAudioClassification()
        }
    }
}