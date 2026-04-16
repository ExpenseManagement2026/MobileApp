package com.example.mobileapp.Presentation.add

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapp.R

class CategoryAdapter(
    private var list: List<CategoryModel>,
    private val onClick: (CategoryModel) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    private var selectedPos = -1

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val layout: LinearLayout = view.findViewById(R.id.layoutCategory)
        val name: TextView = view.findViewById(R.id.tvName)
        val icon: ImageView = view.findViewById(R.id.ivIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.name.text = item.name
        holder.icon.setImageResource(item.icon)

        if (selectedPos == position) {
            holder.layout.setBackgroundResource(R.drawable.bg_item_selected)
        } else {
            holder.layout.setBackgroundResource(R.drawable.bg_item_normal)
        }

        holder.itemView.setOnClickListener {
            val oldPos = selectedPos
            selectedPos = holder.adapterPosition
            notifyItemChanged(oldPos)
            notifyItemChanged(selectedPos)
            onClick(item)
        }
    }

    override fun getItemCount() = list.size
}