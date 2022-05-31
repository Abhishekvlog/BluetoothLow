package com.dexter.bluetoothlow.adapter

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dexter.bluetoothlow.R

class BLListAdapter : ListAdapter<BluetoothDevice, BlListViewHolder>(BlDiffUtil()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout,parent,false)
        return BlListViewHolder(view)
    }

    override fun onBindViewHolder(holder: BlListViewHolder, position: Int) {
    }
}
class BlDiffUtil : DiffUtil.ItemCallback<BluetoothDevice>(){
    override fun areItemsTheSame(oldItem: BluetoothDevice, newItem: BluetoothDevice): Boolean {
        return oldItem.address == newItem.address
    }

    override fun areContentsTheSame(oldItem: BluetoothDevice, newItem: BluetoothDevice): Boolean {
        return areItemsTheSame(oldItem,newItem)
    }

}
class BlListViewHolder(view : View) : RecyclerView.ViewHolder(view){
}