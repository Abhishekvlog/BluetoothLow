package com.dexter.bluetoothlow

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_layout.view.*

class RecyclerViewAdapter(val list: List<BluetoothDevice>) : RecyclerView.Adapter<RecyclerViewViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout,parent,false)
        return RecyclerViewViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerViewViewHolder, position: Int) {
        holder.setData(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }
}
class RecyclerViewViewHolder(val item : View) : RecyclerView.ViewHolder(item){
    @SuppressLint("MissingPermission")
    fun setData(bluetoothDevice: BluetoothDevice) {
        item.name_view.text = bluetoothDevice.name
    }

}