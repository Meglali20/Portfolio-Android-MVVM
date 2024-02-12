package com.oussama.portfolio.ui.adapters

import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.oussama.portfolio.BaseApplication
import com.oussama.portfolio.data.models.MediaModel
import com.oussama.portfolio.databinding.ItemMediaBinding
import com.oussama.portfolio.ui.adapters.viewholders.MediaViewHolder


class MediaAdapter : RecyclerView.Adapter<MediaViewHolder>() {
    private val mediaList: ArrayList<MediaModel> = ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val binding =
            ItemMediaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MediaViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return mediaList.size
    }

    fun addMedias(mediasToAdd: List<MediaModel>) {
        mediaList.addAll(mediasToAdd)
        notifyItemRangeInserted(if (itemCount == 0) 0 else itemCount, mediasToAdd.size)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val mediaModel = mediaList[holder.adapterPosition]
        Glide.with(holder.binding.imageView)
            .load(mediaModel.imageUrl)
            .placeholder(ColorDrawable(BaseApplication.INSTANCE.colorCombination.random()))
            .into(holder.binding.imageView)

    }
}