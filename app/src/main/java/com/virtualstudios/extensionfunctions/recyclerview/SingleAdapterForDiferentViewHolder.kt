package com.virtualstudios.extensionfunctions.recyclerview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.virtualstudios.extensionfunctions.R

class BaseAdapter1<Model : Any, ViewHolder : RecyclerView.ViewHolder>
    (
    private val onCreateViewHolder : (parent : ViewGroup, viewType : Int) -> ViewHolder,
    private val onBindViewHolder : (viewHolder : ViewHolder, position : Int, item : Model) -> Unit,
    private val differCallback  : DiffUtil.ItemCallback<Model>,
    private val onViewType : ((viewType : Int, item : List<Model>) -> Int)? = null,
    private val onDetachFromWindow : ((ViewHolder) -> Unit)? = null
) : RecyclerView.Adapter<ViewHolder>()
{
    var item = listOf<Model>()
    private var onGetItemViewType: ((position : Int) -> Int)? = null
    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = onCreateViewHolder.invoke(parent,viewType)

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        val item = differ.currentList[position]
        onBindViewHolder.invoke(holder,position, item)
    }

    override fun getItemCount(): Int = differ.currentList.size

    override fun getItemViewType(position: Int): Int
    {
        return if (onViewType != null)
        {
            onViewType.invoke(position, item)
        } else
        {
            val onGetItemViewType = onGetItemViewType
            onGetItemViewType?.invoke(position) ?: super.getItemViewType(position)
        }
    }

    override fun onViewDetachedFromWindow(holder: ViewHolder)
    {
        super.onViewDetachedFromWindow(holder)
        onDetachFromWindow?.invoke(holder)
    }
}

data class SingleAdapter(val id: String)

class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView)
{
    companion object
    {
        fun inflate(parent  : ViewGroup) : ViewHolder
        {
            return ViewHolder(
                LayoutInflater.from(parent.context)
                .inflate(R.layout.custom_dialog,parent,false)
            )
        }

        val differCallback = object : DiffUtil.ItemCallback<SingleAdapter>(){
            override fun areItemsTheSame(oldItem: SingleAdapter, newItem: SingleAdapter): Boolean
            {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: SingleAdapter, newItem: SingleAdapter): Boolean
            {
                return oldItem == newItem
            }
        }
    }

    fun bind(data: SingleAdapter)
    {
        with(itemView)
        {
            data.apply()
            {
               //set data here
            }
        }
    }

    fun setAction(action : (position : Int) -> Unit)
    {
        itemView.setOnClickListener()
        {
            action.invoke(adapterPosition)
        }

    }
}

val adapter = BaseAdapter1(
{ parent, _ -> ViewHolder.inflate(parent) },
{ viewHolder, _, item -> viewHolder.bind(item)
    viewHolder.setAction {
       //perform action.
    }
},
ViewHolder.differCallback
)