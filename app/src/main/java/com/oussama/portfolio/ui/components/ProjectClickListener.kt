package com.oussama.portfolio.ui.components

import android.widget.ImageView

interface ProjectClickListener {
    fun onClick(projectTitle: String, imageView: ImageView)
}