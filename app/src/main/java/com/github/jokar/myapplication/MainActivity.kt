package com.github.jokar.myapplication

import android.graphics.Point
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.Toast
import com.github.jokar.floatmenu.FloatMenu
import com.github.jokar.floatmenu.OnMenuItemClickListener
import com.github.jokar.floatmenu.ViewHolder
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var mFloatMenu: FloatMenu
    private val mPoint = Point()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mFloatMenu = FloatMenu(this)
        mFloatMenu.inflate(R.menu.menu_chat)
        mFloatMenu.mOnMenuItemClickListener = object : OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem) {
                Toast.makeText(applicationContext, item.title, Toast.LENGTH_SHORT).show()
            }
        }
        //
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        var mainAdapter = MainAdapter()
        mainAdapter.onItemClickListener = object : OnItemClickListener {
            override fun itemClick(view: View, position: Int) {
                mFloatMenu.show(view, mPoint.x, mPoint.y)
            }
        }
        recyclerView.adapter = mainAdapter
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            mPoint.x = ev.rawX.toInt()
            mPoint.y = ev.rawY.toInt()
        }
        return super.dispatchTouchEvent(ev)
    }
}

class MainAdapter : RecyclerView.Adapter<ViewHolder>() {
    var onItemClickListener: OnItemClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_float_menu,
                    parent, false))

    override fun getItemCount(): Int = 20

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mTvValue.text = "$position"
        holder.mTvValue.setOnClickListener {
            onItemClickListener?.itemClick(holder.mTvValue, holder.layoutPosition)
        }
    }
}

interface OnItemClickListener {
    fun itemClick(view: View, position: Int)
}