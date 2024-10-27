package com.example.hammami.presentation.ui.adapters


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hammami.R
import com.example.hammami.databinding.ItemAvailableCouponBinding
import com.example.hammami.domain.model.AvailableCoupon

class AvailableCouponAdapter(
    private val onCouponSelected: (AvailableCoupon) -> Unit
) : ListAdapter<AvailableCoupon, AvailableCouponAdapter.ViewHolder>(CouponDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAvailableCouponBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onCouponSelected)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemAvailableCouponBinding,
        private val onCouponSelected: (AvailableCoupon) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(coupon: AvailableCoupon) {
            with(binding) {
                setupCouponInfo(coupon)
                setupClickListener(coupon)
            }
        }

        private fun ItemAvailableCouponBinding.setupCouponInfo(coupon: AvailableCoupon) {
            couponValue.text = root.context.getString(
                R.string.coupon_value_format,
                coupon.value
            )
            couponPoints.text = root.context.getString(
                R.string.points_required_format,
                coupon.requiredPoints
            )
        }

        private fun ItemAvailableCouponBinding.setupClickListener(coupon: AvailableCoupon) {
            root.setOnClickListener {
                if (coupon.isEnabled) {
                    onCouponSelected(coupon)
                }
            }
        }
    }

    private class CouponDiffCallback : DiffUtil.ItemCallback<AvailableCoupon>() {
        override fun areItemsTheSame(oldItem: AvailableCoupon, newItem: AvailableCoupon): Boolean =
            oldItem.value == newItem.value &&
                    oldItem.requiredPoints == newItem.requiredPoints

        override fun areContentsTheSame(oldItem: AvailableCoupon, newItem: AvailableCoupon): Boolean =
            oldItem == newItem
    }
}