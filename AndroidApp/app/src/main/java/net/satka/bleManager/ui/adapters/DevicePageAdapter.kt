package net.satka.bleManager.ui.adapters
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import net.satka.bleManager.ui.fragments.DynamicFragment
import java.util.UUID

class DevicePageAdapter(activity: FragmentActivity, private val uuids : List<UUID>) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = uuids.size

    override fun createFragment(position: Int): Fragment {
       return DynamicFragment.newInstance(uuids[position].toString())
    }
}