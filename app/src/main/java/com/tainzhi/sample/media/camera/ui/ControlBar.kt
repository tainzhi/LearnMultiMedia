package com.tainzhi.sample.media.camera.ui

import android.animation.AnimatorInflater
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.animation.doOnEnd
import com.tainzhi.sample.media.R
import com.tainzhi.sample.media.camera.SettingsActivity
import com.tainzhi.sample.media.camera.util.SettingsManager
import com.tainzhi.sample.media.databinding.ActivityCameraBinding

class ControlBar(val context: Context, val binding: ActivityCameraBinding, val onRatioUpdate: () -> Unit) {
    private lateinit var inflatedView: View
    private var previewAspectRatio = SettingsManager.getInstance()!!.getPreviewAspectRatio()
    private val vsControlBarRatio = binding.vsControlBarRatio
    private val btnRatio = binding.btnRatio.apply {
        setOnClickListener {
            AnimatorInflater.loadAnimator(context, R.animator.roate_x).apply {
                setTarget(binding.btnHdr)
                start()
            }
            AnimatorInflater.loadAnimator(context, R.animator.roate_x).apply {
                setTarget(binding.btnRatio)
                doOnEnd {
//                    binding.clGroupControlBar.visibility = View.INVISIBLE
                    changePreviewAspectRatio()
                }
                start()
            }
            AnimatorInflater.loadAnimator(context, R.animator.roate_x).apply {
                setTarget(binding.btnSettings)
                start()
            }
        }
    }
    private val tnSettings = binding.btnSettings.apply {
        setOnClickListener {
            context.startActivity(Intent(context, SettingsActivity::class.java))
        }
    }

    private fun changePreviewAspectRatio() {
        lateinit var selectedImageButton: ImageButton
        lateinit var ivRatioImageButtonList: Array<ImageButton>
        if (!this::inflatedView.isInitialized) {
            inflatedView = vsControlBarRatio.inflate()
        }
        val ivRatio1x1 = inflatedView.findViewById<AppCompatImageButton>(R.id.btn_ratio_1x1).apply {
            setOnClickListener {
                isSelected = true
                selectedImageButton.isSelected = false
                selectedImageButton = this
                previewAspectRatio = SettingsManager.PreviewAspectRatio.RATIO_1x1
                ivRatioImageButtonList.forEach { it.visibility = View.GONE }
                postChangePreviewAspectRatio()
            }
        }
        val ivRatio4x3 = inflatedView.findViewById<AppCompatImageButton>(R.id.btn_ratio_4x3).apply {
            setOnClickListener {
                isSelected = true
                selectedImageButton.isSelected = false
                selectedImageButton = this
                previewAspectRatio = SettingsManager.PreviewAspectRatio.RATIO_4x3
                ivRatioImageButtonList.forEach { it.visibility = View.GONE }
                postChangePreviewAspectRatio()
            }
        }
        val ivRatio16x9 = inflatedView.findViewById<AppCompatImageButton>(R.id.btn_ratio_16x9).apply {
            setOnClickListener {
                isSelected = true
                selectedImageButton.isSelected = false
                selectedImageButton = this
                previewAspectRatio = SettingsManager.PreviewAspectRatio.RATIO_16x9
                ivRatioImageButtonList.forEach { it.visibility = View.GONE }
                postChangePreviewAspectRatio()
            }
        }
        val ivRatioFull = inflatedView.findViewById<AppCompatImageButton>(R.id.btn_ratio_full).apply {
            setOnClickListener {
                isSelected = true
                selectedImageButton.isSelected = false
                selectedImageButton = this
                previewAspectRatio = SettingsManager.PreviewAspectRatio.RATIO_FULL
                ivRatioImageButtonList.forEach { it.visibility = View.GONE }
                postChangePreviewAspectRatio()
            }
        }
        ivRatioImageButtonList = arrayOf(ivRatio1x1, ivRatio4x3, ivRatio16x9, ivRatioFull)
        AnimatorInflater.loadAnimator(context, R.animator.revert_roate_x).apply {
            setTarget(ivRatio1x1)
            start()
        }
        AnimatorInflater.loadAnimator(context, R.animator.revert_roate_x).apply {
            setTarget(ivRatio4x3)
            start()
        }
        AnimatorInflater.loadAnimator(context, R.animator.revert_roate_x).apply {
            setTarget(ivRatio16x9)
            start()
        }
        AnimatorInflater.loadAnimator(context, R.animator.revert_roate_x).apply {
            setTarget(ivRatioFull)
            doOnEnd {
                for (imageButton in ivRatioImageButtonList) {
                    imageButton.visibility = View.VISIBLE
                }
            }
            start()
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
//        binding.clGroupControlBar.visibility = View.VISIBLE
        AnimatorInflater.loadAnimator(context, R.animator.revert_roate_x).apply {
            setTarget(binding.btnHdr)
            start()
        }
        AnimatorInflater.loadAnimator(context, R.animator.revert_roate_x).apply {
            setTarget(binding.btnRatio)
            doOnEnd {
//                binding.clGroupControlBar.visibility = View.INVISIBLE
            }
            start()
        }
        AnimatorInflater.loadAnimator(context, R.animator.revert_roate_x).apply {
            setTarget(binding.btnSettings)
            start()
        }
        onRatioUpdate.invoke()
    }
}
