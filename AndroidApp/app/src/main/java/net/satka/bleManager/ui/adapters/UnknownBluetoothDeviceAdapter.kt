package net.satka.bleManager.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import net.satka.bleManager.R
import net.satka.bleManager.databinding.ItemUnknownBluetoothEviceBinding
import net.satka.bleManager.ui.models.UnknownBluetoothDeviceModel

class UnknownBluetoothDeviceAdapter(private val devices: List<UnknownBluetoothDeviceModel>) :
    RecyclerView.Adapter<UnknownBluetoothDeviceAdapter.ViewHolder>() {
    var onItemClick: ((UnknownBluetoothDeviceModel, Int) -> Unit)? = null

    inner class ViewHolder(private val binding: ItemUnknownBluetoothEviceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.cardView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onItemClick?.invoke(devices[adapterPosition], adapterPosition)
                }
            }
        }

        fun bind(device: UnknownBluetoothDeviceModel) {
            binding.deviceName.text = device.name ?: itemView.context.getString(R.string.unknown_name_device)
            binding.deviceAddress.text = device.address
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemUnknownBluetoothEviceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val device = devices[position]
        holder.bind(device)
    }

    override fun getItemCount(): Int {
        return devices.size
    }
}
