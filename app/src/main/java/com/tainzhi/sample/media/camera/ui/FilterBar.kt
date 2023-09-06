package com.tainzhi.sample.media.camera.ui

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.QuickViewHolder
import com.tainzhi.sample.media.R
import com.tainzhi.sample.media.databinding.ActivityCameraBinding

class FilterBar(val context: Context, val binding: ActivityCameraBinding) {
    private lateinit var inflatedView: View
    private val types =
        listOf("YUV1", "YUV2", "LUT3", "LUT4", "LUT5")
    private val filterView = binding.filter.apply {
        setOnClickListener {
            show()
        }
    }

    fun show() {
        inflatedView = binding.vsFilter.inflate()
        inflatedView.findViewById<RecyclerView>(R.id.filter_recylerview).run {
            LinearSnapHelper().attachToRecyclerView(this)
            addItemDecoration(object :
                DividerItemDecoration(context, DividerItemDecoration.HORIZONTAL) {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    val edgeMargin =
                        ((context as Activity).windowManager.currentWindowMetrics.bounds.width()
                                - context.resources.getDimension(R.dimen.camera_filter_item_width)) / 2
                    val position = parent.getChildAdapterPosition(view)
                    // center first item
                    if (position == 0) {
                        outRect.set(edgeMargin.toInt(), 0, 5, 0);
                        // center last item
                    } else if (position == state.itemCount - 1) {
                        outRect.set(0, 0, edgeMargin.toInt(), 0);
                    } else {
                        outRect.set(0, 0, 5, 0);
                    }
                }
            })
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = FilterAdapter().apply {
                setOnItemClickListener { _, _, position ->
                }
                submitList(types)
            }

        }
    }

    fun hide() {
        inflatedView.visibility = View.GONE
    }
}

class FilterAdapter() : BaseQuickAdapter<String, QuickViewHolder>() {
    override fun onBindViewHolder(holder: QuickViewHolder, position: Int, item: String?) {
        holder.setText(R.id.tv_item_filter_type, item)
    }

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): QuickViewHolder {
        return QuickViewHolder(R.layout.item_filter, parent)
    }
}

data class FilterItem(val type: String)