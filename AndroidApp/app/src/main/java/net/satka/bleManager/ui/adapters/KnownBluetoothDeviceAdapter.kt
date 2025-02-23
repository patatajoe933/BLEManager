package net.satka.bleManager.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.recyclerview.widget.RecyclerView
import net.satka.bleManager.R
import net.satka.bleManager.databinding.ItemKnownBluetoothDeviceBinding
import net.satka.bleManager.ui.models.KnownBluetoothDeviceModel

class KnownBluetoothDeviceAdapter(private val devices: List<KnownBluetoothDeviceModel>) :
    RecyclerView.Adapter<KnownBluetoothDeviceAdapter.ViewHolder>() {
    var onItemClick: ((KnownBluetoothDeviceModel, Int) -> Unit)? = null
    var onItemCheckedChanged: ((KnownBluetoothDeviceModel, Int) -> Unit)? = null
    var onItemDeleteClick: ((KnownBluetoothDeviceModel, Int) -> Unit)? = null

    inner class ViewHolder(private val binding: ItemKnownBluetoothDeviceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.cardView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onItemClick?.invoke(devices[adapterPosition], adapterPosition)
                }
            }

            binding.trashIconButton.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onItemDeleteClick?.invoke(devices[adapterPosition], adapterPosition)
                }
            }

            binding.cardView.setOnLongClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    devices[adapterPosition].isChecked = !devices[adapterPosition].isChecked
                    binding.cardView.isChecked = devices[adapterPosition].isChecked
                    binding.trashIconButton.visibility =
                        if (binding.cardView.isChecked) View.GONE else View.VISIBLE
                    onItemCheckedChanged?.invoke(devices[adapterPosition], adapterPosition)
                }
                true
            }
        }

        fun bind(device: KnownBluetoothDeviceModel) {
            binding.deviceName.text =
                device.name ?: itemView.context.getString(R.string.unknown_name_device)
            binding.deviceName.isEnabled = device.isEnabled
            binding.deviceAddress.text = device.address
            binding.deviceAddress.isEnabled = device.isEnabled
            binding.deviceStatus.text = if (device.isEnabled) device.status else itemView.context.getString(
                R.string.pending_status
            )
            binding.deviceStatus.isEnabled = device.isEnabled
            binding.cardView.isChecked = device.isChecked
            binding.cardView.isEnabled = device.isEnabled
            binding.cardView.isClickable = device.isEnabled
            binding.trashIconButton.contentDescription =
                itemView.context.getString(R.string.delete_device, device.address)
            binding.trashIconButton.isEnabled = device.isEnabled
            binding.trashIconButton.visibility =
                if (binding.cardView.isChecked) View.GONE else View.VISIBLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemKnownBluetoothDeviceBinding.inflate(
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
