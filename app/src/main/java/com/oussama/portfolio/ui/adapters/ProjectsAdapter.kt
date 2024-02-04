package com.oussama.portfolio.ui.adapters

import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.oussama.portfolio.BaseApplication
import com.oussama.portfolio.data.models.ProjectModel
import com.oussama.portfolio.databinding.ItemProjectBinding
import com.oussama.portfolio.ui.adapters.viewholders.ProjectViewHolder
import com.oussama.portfolio.ui.components.ProjectClickListener
import com.oussama.portfolio.ui.components.ScalePressEffect

class ProjectsAdapter : RecyclerView.Adapter<ProjectViewHolder>() {

    private val projectsList: ArrayList<ProjectModel> = ArrayList()
    private var projectClickListener: ProjectClickListener? = null
    private var recyclerView: RecyclerView? = null

    private val handler = Handler(Looper.getMainLooper())

    fun setProjectClickListener(projectClickListener: ProjectClickListener) {
        this.projectClickListener = projectClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val binding =
            ItemProjectBinding.inflate(LayoutInflater.from(parent.context), parent, false);
        return ProjectViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return projectsList.size
    }

    fun addProjects(projectsToAdd: List<ProjectModel>?) {
        if (projectsToAdd == null)
            return
        projectsList.addAll(projectsToAdd)
        notifyItemRangeInserted(if (itemCount == 0) 0 else itemCount, projectsToAdd.size)
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        val projectModel = projectsList[holder.adapterPosition]
        handler.postDelayed({
            holder.binding.projectTitle.setText(
                projectModel.title,
                autoAnimate = true,
                animateWithDelay = false
            )
        }, 200)

        holder.itemView.setOnTouchListener(ScalePressEffect)
        holder.itemView.setOnClickListener {
            projectClickListener?.onClick(projectModel.title, holder.binding.projectIcon)
        }

        Glide.with(holder.binding.projectIcon).load(projectModel.icon)
            .diskCacheStrategy(DiskCacheStrategy.ALL).into(holder.binding.projectIcon)
        Glide.with(holder.binding.bannerImage).load(projectModel.bannerImage)
            .placeholder(ColorDrawable(BaseApplication.INSTANCE.colorCombination.random()))
            .diskCacheStrategy(DiskCacheStrategy.ALL).into(holder.binding.bannerImage)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    fun addRecyclerViewScrollListener() {
        recyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                for (i in firstVisibleItemPosition..lastVisibleItemPosition) {
                    //val view = recyclerView.layoutManager?.findViewByPosition(i);
                    val viewHolder =
                        recyclerView.findViewHolderForAdapterPosition(i) as ProjectViewHolder
                    viewHolder.binding.projectTitle.onScroll()
                }
            }
        })
    }

    fun onParentScrolled() {
        val layoutManager = recyclerView?.layoutManager as LinearLayoutManager
        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
        val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
        for (i in firstVisibleItemPosition..lastVisibleItemPosition) {
            //val view = recyclerView.layoutManager?.findViewByPosition(i);
            val viewHolder =
                recyclerView?.findViewHolderForAdapterPosition(i) as ProjectViewHolder
            viewHolder.binding.projectTitle.onScroll()
        }
    }


}