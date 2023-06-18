package com.virtualstudios.extensionfunctions.recyclerview

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

class BaseViewHolder<T> internal constructor(private val binding: ViewBinding, private val expression:(T, ViewBinding)->Unit)
    :RecyclerView.ViewHolder(binding.root){
    fun bind(item:T){
        expression(item,binding)
    }
}

class BaseAdapter<T>: RecyclerView.Adapter<BaseViewHolder<T>>(){
    var listOfItems:MutableList<T>? = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var expressionViewHolderBinding: ((T,ViewBinding) -> Unit)? = null
    var expressionOnCreateViewHolder:((ViewGroup)->ViewBinding)? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<T> {
        return expressionOnCreateViewHolder?.let { it(parent) }?.let { BaseViewHolder(it, expressionViewHolderBinding!!) }!!
    }

    override fun onBindViewHolder(holder: BaseViewHolder<T>, position: Int) {
        holder.bind(listOfItems!![position])
    }

    override fun getItemCount(): Int {
        return listOfItems!!.size
    }

    fun clearList() {
        listOfItems?.clear()
        notifyDataSetChanged()
    }

    fun addItem(item:MutableList<T>){
        notifyDataSetChanged()
    }
}

data class Person(val name: String, val age: Int)


/*
private fun populateRecycler() {

private val mAdapter = BaseAdapter<Person>()

    // Clear the existing list in the adapter
    mAdapter.clearList()

    // Add the list of Person objects to the adapter
    mAdapter.listOfItems = personList.toMutableList()

    // Customize the binding logic
    mAdapter.expressionViewHolderBinding = { person, viewBinding ->
        val itemBinding = viewBinding as ItemPersonBinding
        itemBinding.nameTextView.text = person.name
        itemBinding.ageTextView.text = person.age.toString()
    }

    // Customize the ViewHolder creation logic
    mAdapter.expressionOnCreateViewHolder = { viewGroup ->
        // Inflate the item layout using ViewBinding
        ItemPersonBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
    }

    // Finally, put the adapter to the RecyclerView
    binding.recycler.apply {
        layoutManager = LinearLayoutManager(requireContext())
        adapter = mAdapter
    }
}*/
