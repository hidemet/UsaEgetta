package com.example.hammami.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hammami.R
import com.example.hammami.databinding.ItemActiveCouponBinding
import com.example.hammami.domain.model.Coupon
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class ActiveCouponAdapter(
    private val onCouponClicked: (String) -> Unit
) : ListAdapter<Coupon, ActiveCouponAdapter.ViewHolder>(CouponDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemActiveCouponBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onCouponClicked)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemActiveCouponBinding,
        private val onCouponClicked: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(coupon: Coupon) {
            with(binding) {
                setupCouponInfo(coupon)
                setupClickListener(coupon.code)
            }
        }

        private fun ItemActiveCouponBinding.setupCouponInfo(coupon: Coupon) {
            couponCode.text = coupon.code
            couponValue.text = root.context.getString(
                R.string.coupon_value_format,
                coupon.value
            )
            couponExpiration.text = root.context.getString(
                R.string.coupon_expiration_format,
                formatDate(coupon.expirationDate)
            )
        }

        private fun ItemActiveCouponBinding.setupClickListener(code: String) {
            copyButton.setOnClickListener { onCouponClicked(code) }
        }

        private fun formatDate(timestamp: Timestamp): String =
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(timestamp.toDate())
    }

    private class CouponDiffCallback : DiffUtil.ItemCallback<Coupon>() {
        override fun areItemsTheSame(oldItem: Coupon, newItem: Coupon): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Coupon, newItem: Coupon): Boolean =
            oldItem == newItem
    }
}