package com.example.imagecropdrawandtexteditor.editimage.adapter

import com.example.imagecropdrawandtexteditor.editimage.fragment.PaintFragment
import com.example.imagecropdrawandtexteditor.editimage.adapter.ColorListAdapter.IColorListAction
import androidx.recyclerview.widget.RecyclerView
import com.example.imagecropdrawandtexteditor.R
import com.example.imagecropdrawandtexteditor.editimage.adapter.ColorListAdapter
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import com.example.imagecropdrawandtexteditor.editimage.adapter.ColorListAdapter.ColorViewHolder
import com.example.imagecropdrawandtexteditor.editimage.adapter.ColorListAdapter.MoreViewHolder


class ColorListAdapter(
    private val mContext: PaintFragment,
    private val colorsData: IntArray,
    private val mCallback: IColorListAction?
) : RecyclerView.Adapter<RecyclerView.ViewHolder?>() { // end class
    interface IColorListAction {
        fun onColorSelected(position: Int, color: Int)
        fun onMoreSelected(position: Int)
    }

    inner class ColorViewHolder(itemView: View?) : RecyclerView.ViewHolder(
        itemView!!) {
        var colorPanelView: View

        init {
            colorPanelView = itemView!!.findViewById(R.id.color_panel_view)
        }
    } // end inner class

    inner class MoreViewHolder(itemView: View?) : RecyclerView.ViewHolder(
        itemView!!) {
        var moreBtn: View

        init {
            moreBtn = itemView!!.findViewById(R.id.color_panel_more)
        }
    } //end inner class

    override fun getItemCount(): Int {
        return colorsData.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (colorsData.size == position) TYPE_MORE else TYPE_COLOR
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var v: View? = null
        var viewHolder: RecyclerView.ViewHolder? = null
        if (viewType == TYPE_COLOR) {
            v = LayoutInflater.from(parent.context).inflate(
                R.layout.view_color_panel, parent, false)
            viewHolder = ColorViewHolder(v)
        } else if (viewType == TYPE_MORE) {
            v = LayoutInflater.from(parent.context)
                .inflate(R.layout.view_color_more_panel, parent, false)
            viewHolder = MoreViewHolder(v)
        }
        return viewHolder!!
    }



    private fun onBindColorViewHolder(holder: ColorViewHolder?, position: Int) {
        holder!!.colorPanelView.setBackgroundColor(colorsData[position])
        holder.colorPanelView.setOnClickListener {
            mCallback?.onColorSelected(position,
                colorsData[position])
        }
    }

    private fun onBindColorMoreViewHolder(holder: MoreViewHolder?, position: Int) {
        holder!!.moreBtn.setOnClickListener { mCallback?.onMoreSelected(position) }
    }

    companion object {
        const val TYPE_COLOR = 1
        const val TYPE_MORE = 2
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val type = getItemViewType(position)
        if (type == TYPE_COLOR) {
            onBindColorViewHolder(holder as ColorViewHolder?, position)
        } else if (type == TYPE_MORE) {
            onBindColorMoreViewHolder(holder as MoreViewHolder?, position)
        }
    }
}
