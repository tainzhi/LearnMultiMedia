package com.tainzhi.sample.media.camera.ui

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.widget.AppCompatImageButton
import com.tainzhi.sample.media.R
import com.tainzhi.sample.media.camera.SettingsActivity
import com.tainzhi.sample.media.camera.util.SettingsManager
import com.tainzhi.sample.media.databinding.ActivityCameraBinding

class ControlBar(val context: Context, val binding: ActivityCameraBinding, private val onRatioUpdate: () -> Unit) {
    private lateinit var inflatedView: View
    private var previewAspectRatio = SettingsManager.getInstance()!!.getPreviewAspectRatio()
    private val btnRatio = binding.btnRatio.apply {
        updateControlBarRatioIcon(previewAspectRatio)
        setOnClickListener {
//            AnimatorInflater.loadAnimator(context, R.animator.roate_x).apply {
//                setTarget(binding.clControlBarLevel1Menu)
//                doOnEnd {
//                    binding.clControlBarLevel1Menu.visibility = View.INVISIBLE
//                    changePreviewAspectRatio()
//                }
//                start()
//            }
            binding.clControlBarLevel1Menu.animate().alpha(0.5f)
                .withEndAction {
                    binding.clControlBarLevel1Menu.visibility = View.INVISIBLE
                    changePreviewAspectRatio()
                    inflatedView.animate()
                        .alpha(1f)
                        .withEndAction {
                             inflatedView.visibility = View.VISIBLE
                        }
                        .start()
                }
                .start()
        }
    }
    private val btnSettings = binding.btnSettings.apply {
        setOnClickListener {
            context.startActivity(Intent(context, SettingsActivity::class.java))
        }
    }

    fun rotate(angle: Int) {
        for (view in arrayOf(btnSettings, btnRatio, binding.btnHdr)) {
            view.animate()
                .setDuration(800)
                .rotation(angle.toFloat())
                .start()
        }
    }

    private fun changePreviewAspectRatio() {
        lateinit var selectedImageButton: ImageButton
        if (!this::inflatedView.isInitialized) {
            inflatedView = binding.vsControlBarRatio.inflate()
        }
        val ivRatio1x1 = inflatedView.findViewById<AppCompatImageButton>(R.id.btn_ratio_1x1).apply {
            setOnClickListener {
                isSelected = true
                selectedImageButton.isSelected = false
                selectedImageButton = this
                previewAspectRatio = SettingsManager.PreviewAspectRatio.RATIO_1x1
                postChangePreviewAspectRatio()
            }
        }
        val ivRatio4x3 = inflatedView.findViewById<AppCompatImageButton>(R.id.btn_ratio_4x3).apply {
            setOnClickListener {
                isSelected = true
                selectedImageButton.isSelected = false
                selectedImageButton = this
                previewAspectRatio = SettingsManager.PreviewAspectRatio.RATIO_4x3
                postChangePreviewAspectRatio()
            }
        }
        val ivRatio16x9 = inflatedView.findViewById<AppCompatImageButton>(R.id.btn_ratio_16x9).apply {
            setOnClickListener {
                isSelected = true
                selectedImageButton.isSelected = false
                selectedImageButton = this
                previewAspectRatio = SettingsManager.PreviewAspectRatio.RATIO_16x9
                postChangePreviewAspectRatio()
            }
        }
        val ivRatioFull = inflatedView.findViewById<AppCompatImageButton>(R.id.btn_ratio_full).apply {
            setOnClickListener {
                isSelected = true
                selectedImageButton.isSelected = false
                selectedImageButton = this
                previewAspectRatio = SettingsManager.PreviewAspectRatio.RATIO_FULL
                postChangePreviewAspectRatio()
            }
        }
        when (previewAspectRatio) {
            SettingsManager.PreviewAspectRatio.RATIO_1x1 -> {
                ivRatio1x1.isSelected = true
                selectedImageButton = ivRatio1x1
            }

            SettingsManager.PreviewAspectRatio.RATIO_4x3 -> {
                ivRatio4x3.isSelected = true
                selectedImageButton = ivRatio4x3
            }

            SettingsManager.PreviewAspectRatio.RATIO_16x9 -> {
                ivRatio16x9.isSelected = true
                selectedImageButton = ivRatio16x9
            }

            SettingsManager.PreviewAspectRatio.RATIO_FULL -> {
                ivRatioFull.isSelected = true
                selectedImageButton = ivRatioFull
            }
        }
    }

    private fun postChangePreviewAspectRatio() {
        SettingsManager.getInstance()!!.setPreviewRatio(previewAspectRatio)
        updateControlBarRatioIcon(previewAspectRatio)
        inflatedView.animate().alpha(0.5f)
            .withEndAction {
                inflatedView.visibility = View.INVISIBLE
                changePreviewAspectRatio()
                binding.clControlBarLevel1Menu.animate()
                    .alpha(1f)
                    .withEndAction {
                         binding.clControlBarLevel1Menu.visibility = View.VISIBLE
                    }
                    .start()
            }
            .start()
        onRatioUpdate.invoke()
    }

    private fun updateControlBarRatioIcon(previewAspectRatio: SettingsManager.PreviewAspectRatio) {
        binding.btnRatio.setImageDrawable(context.resources.getDrawable(
            when (previewAspectRatio) {
                SettingsManager.PreviewAspectRatio.RATIO_1x1 -> R.drawable.ic_ratio_1x1
                SettingsManager.PreviewAspectRatio.RATIO_4x3 -> R.drawable.ic_ratio_4x3
                SettingsManager.PreviewAspectRatio.RATIO_16x9 -> R.drawable.ic_ratio_16x9
                else -> R.drawable.ic_ratio_full
            }))
    }
}
