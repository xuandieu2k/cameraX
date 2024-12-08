package vn.xdeuhug.camerax.base

import android.annotation.SuppressLint
import androidx.recyclerview.widget.RecyclerView

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 26 / 10 / 2024
 */

abstract class AppAdapter<T, VH : RecyclerView.ViewHolder> : RecyclerView.Adapter<VH>() {

    private var listData: List<T> = listOf()

    override fun onBindViewHolder(holder: VH, position: Int) {
        onBindData(holder, listData[position], position)
    }

    fun updateAllItems() {
        for (position in getData().indices) {
            notifyItemChanged(position)
        }
    }

    fun getData(): List<T> = listData

    @SuppressLint("NotifyDataSetChanged")
    fun setData(newData: List<T>) {
        /*val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize() = listData.size

            override fun getNewListSize() = newData.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return listData[oldItemPosition] == newData[newItemPosition]
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return listData[oldItemPosition] == newData[newItemPosition]
            }
        }

        val diffResult = DiffUtil.calculateDiff(diffCallback)
        listData = newData
        diffResult.dispatchUpdatesTo(this)*/
        listData = newData
        notifyDataSetChanged()
    }

    protected fun getItem(position: Int): T {
        return listData[position]
    }

    override fun getItemCount(): Int {
        return listData.size
    }

    abstract fun onBindData(holder: VH, item: T, position: Int)
}

