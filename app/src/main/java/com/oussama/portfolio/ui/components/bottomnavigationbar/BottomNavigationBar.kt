package com.oussama.portfolio.ui.components.bottomnavigationbar

import android.animation.LayoutTransition
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.MenuInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.oussama.portfolio.BaseApplication
import com.oussama.portfolio.R
import com.oussama.portfolio.databinding.BottomNavigationBarItemBinding
import com.oussama.portfolio.ui.components.drawables.GlitchDrawable
import com.oussama.portfolio.ui.components.textshufflerview.TextShufflerView

class BottomNavigationBar : LinearLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER
        layoutTransition = LayoutTransition()
        clipChildren = false
        val attributes =
            context.obtainStyledAttributes(attrs, R.styleable.BottomNavigationBar, defStyleAttr, 0)
        activeItemTintColor = attributes.getColor(
            R.styleable.BottomNavigationBar_activeItemColor,
            activeItemTintColor
        )
        if(!isInEditMode)
            activeItemTintColor = BaseApplication.INSTANCE.colorCombination[0]

        inactiveItemTintColor = attributes.getColor(
            R.styleable.BottomNavigationBar_inactiveItemColor,
            inactiveItemTintColor
        )
        val menuResId = attributes.getResourceId(R.styleable.BottomNavigationBar_menu, 0)
        if (menuResId != 0) {

            val menuInflater = MenuInflater(context)
            val menu = PopupMenu(context, null).menu
            menuInflater.inflate(menuResId, menu)

            for (i in 0 until menu.size()) {
                val menuItem = menu.getItem(i)
                val id = menuItem.itemId
                val title = menuItem.title
                val icon = menuItem.icon
                items.add(BottomNavigationItem(id, title.toString(), icon))
            }
        }
        attributes.recycle()
        createMenu()
    }


    private var items: ArrayList<BottomNavigationItem> = arrayListOf()
    private var activeItemIndex: Int = 0
    private var activeItemTintColor: Int = Color(0xFF61dca3).toArgb()
    private var inactiveItemTintColor: Int = Color(0xFF979998).toArgb()


    private var bottomNavigationInterface: BottomNavigationInterface? = null

    fun setBottomNavigationClickListener(listener: BottomNavigationInterface) {
        bottomNavigationInterface = listener
    }


    private fun createMenu() {

        val inflater = LayoutInflater.from(context)

        items.forEachIndexed { index, bottomNavigationItem ->
            val binding = BottomNavigationBarItemBinding.inflate(inflater, this, false)
            val glitchDrawable =
                GlitchDrawable(bottomNavigationItem.drawable, activeItemTintColor)
            binding.itemTitle.setText(bottomNavigationItem.title)
            glitchDrawable.setTint(inactiveItemTintColor)
            binding.itemDrawable.setImageDrawable(glitchDrawable)
            binding.itemContainer.setOnClickListener {
                performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                if(activeItemIndex == index){
                    binding.itemTitle.shuffleText(reset = true, withDelay = false)
                    glitchDrawable.startGlitchAnimation()
                    return@setOnClickListener
                }
                hidePreviousItem()
                showActiveItem(index)
                bottomNavigationInterface?.onBottomNavigationItemClick(bottomNavigationItem.id)
            }
            val params = LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            params.weight = 0.33f
            binding.root.layoutParams = params
            addView(binding.root)
            binding.itemTitle.visibility = GONE
        }
        setActiveItem(0)
    }

    private fun showActiveItem(index: Int) {
        activeItemIndex = index
        val glitchDrawable =
            getChildAt(activeItemIndex).findViewById<ImageView>(R.id.itemDrawable).drawable as GlitchDrawable
        glitchDrawable.setTint(activeItemTintColor)
        val textShufflerView = getChildAt(activeItemIndex).findViewById<TextShufflerView>(R.id.itemTitle)
        if (textShufflerView.visibility != VISIBLE)
            textShufflerView.visibility = VISIBLE
        postDelayed({
            textShufflerView.shuffleText(reset = true, withDelay = false)
            /*postDelayed({
                textShufflerView.shuffleText(reset = true, withDelay = false)
            }, 200)*/
            glitchDrawable.startGlitchAnimation()
        }, 200)

    }

    private fun hidePreviousItem() {
        (getChildAt(activeItemIndex).findViewById<ImageView>(R.id.itemDrawable).drawable as GlitchDrawable).setTint(
            inactiveItemTintColor
        )
        if (getChildAt(activeItemIndex).findViewById<TextShufflerView>(R.id.itemTitle).visibility != GONE)
            getChildAt(activeItemIndex).findViewById<TextShufflerView>(R.id.itemTitle).visibility = GONE
        getChildAt(activeItemIndex).background = null
    }

    fun setActiveItem(position: Int) {
        hidePreviousItem()
        showActiveItem(position)
    }

}