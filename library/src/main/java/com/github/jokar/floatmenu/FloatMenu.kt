package com.github.jokar.floatmenu

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Point
import android.os.Build
import android.support.annotation.MenuRes
import android.support.annotation.NonNull
import android.support.v7.view.menu.MenuBuilder
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.PopupWindow
import android.widget.TextView

@SuppressLint("RestrictedApi")
class FloatMenu(private var context: Context) : PopupWindow(context) {

    private val ANCHORED_GRAVITY = Gravity.TOP or Gravity.START
    private val DEFAULT_ITEM_HEIGHT = 48
    private val DEFAULT_MENU_WIDTH = 150
    private val X_OFFSET = 10

    //
    private var mMenu: Menu = MenuBuilder(context)
    //view
    private var mRecyclerView: RecyclerView? = null
    private var mInflater: LayoutInflater = LayoutInflater.from(context)
    private var mMenuAdapter: MenuAdapter? = null
    var mOnMenuItemClickListener: OnMenuItemClickListener? = null
    //
    private val mMenuItems = ArrayList<MenuItem>()

    private var mScreenPoint: Point = DisplayUtil.getScreenMetrics(context)
    private var mMenuWidth: Int = DisplayUtil.dip2px(context, DEFAULT_MENU_WIDTH)

    init {
        contentView = mInflater.inflate(R.layout.layout_float_menu, null)
        mRecyclerView = contentView.findViewById(R.id.recyclerView)
        mRecyclerView?.layoutManager = LinearLayoutManager(context)
        mMenuAdapter = MenuAdapter()
        mRecyclerView?.adapter = mMenuAdapter
        //set option
        width = mMenuWidth
        setBackgroundDrawable(null)
        isOutsideTouchable = true
        //set shadow
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            elevation = 15f
        }
    }

    fun show(anchorView: View) {
        val location = IntArray(2)
        anchorView.getLocationInWindow(location)
        if (!tryShow(anchorView, 0, location[1])) {
            throw IllegalStateException("FloatMenu cannot be used without an anchor")
        }
    }

    fun show(anchorView: View, x: Int, y: Int) {
        if (!tryShow(anchorView, x, y)) {
            throw IllegalStateException("FloatMenu cannot be used without an anchor")
        }
    }

    private fun tryShow(anchorView: View?, x: Int, y: Int): Boolean {
        if (isShowing) {
            return true
        }

        if (anchorView == null) {
            return false
        }

        showPopup(anchorView, x, y)
        return true
    }

    private fun showPopup(anchorView: View, x: Int, y: Int) {
        if (!mMenu.hasVisibleItems()) {
            return
        }

        //set visible item data
        val size = mMenu.size()
        mMenuItems.clear()
        for (i in 0 until size) {
            val item = mMenu.getItem(i)
            if (item.isVisible) {
                mMenuItems.add(item)
            }
        }
        mMenuAdapter?.notifyDataSetChanged()
        //show
        val menuHeight = DisplayUtil.dip2px(context, DEFAULT_ITEM_HEIGHT * mMenuItems.size)
        if (x <= mScreenPoint.x / 2) {
            if (y + menuHeight < mScreenPoint.y) {
                animationStyle = R.style.Animation_top_left
                showAtLocation(anchorView, ANCHORED_GRAVITY, x + X_OFFSET, y)
            } else {
                animationStyle = R.style.Animation_bottom_left
                showAtLocation(anchorView, ANCHORED_GRAVITY, x + X_OFFSET, y - menuHeight)
            }
        } else {
            if (y + menuHeight < mScreenPoint.y) {
                animationStyle = R.style.Animation_top_right
                showAtLocation(anchorView, ANCHORED_GRAVITY, x - mMenuWidth - X_OFFSET, y)
            } else {
                animationStyle = R.style.Animation_bottom_right
                showAtLocation(anchorView, ANCHORED_GRAVITY, x - mMenuWidth + X_OFFSET, y - menuHeight)
            }
        }
    }

    override fun dismiss() {
        super.dismiss()
        mMenuItems.clear()
    }

    fun getMenu():Menu{
        return mMenu
    }

    @NonNull
    private fun getMenuInflater(): MenuInflater {
        return MenuInflater(context)
    }

    fun inflate(@MenuRes menuRes: Int) {
        getMenuInflater().inflate(menuRes, mMenu)
    }

    inner class MenuAdapter : RecyclerView.Adapter<ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
                ViewHolder(mInflater.inflate(R.layout.item_float_menu, parent,
                        false))

        override fun getItemCount(): Int = mMenuItems.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.mTvValue.text = mMenuItems[position].title
            holder.mTvValue.setOnClickListener {
                mOnMenuItemClickListener?.onMenuItemClick(mMenuItems[position])
                dismiss()
            }
        }
    }
}

interface OnMenuItemClickListener {
    fun onMenuItemClick(item: MenuItem)
}

class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var mTvValue: TextView = itemView.findViewById(R.id.tv_value)
}

object DisplayUtil {

    fun getScreenMetrics(context: Context): Point {
        val dm = context.resources.displayMetrics
        val screenWidth = dm.widthPixels
        val screenHeight = dm.heightPixels
        return Point(screenWidth, screenHeight)
    }

    fun dip2px(context: Context, dipValue: Int): Int {
        val scale = context.resources.displayMetrics.density
        return (dipValue * scale + 0.5f).toInt()
    }
}