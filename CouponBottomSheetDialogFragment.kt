package com.example.hammami.presentation.ui.fragments.userProfile.coupon

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hammami.R
import com.example.hammami.databinding.BottomSheetAvailableCouponsBinding
import com.example.hammami.domain.model.AvailableCoupon
import com.example.hammami.domain.model.Coupon
import com.example.hammami.presentation.ui.adapters.AvailableCouponAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CouponBottomSheetDialogFragment : BottomSheetDialogFragment() {
    private var _binding: BottomSheetAvailableCouponsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CouponViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAvailableCouponsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        setupAvailableCouponsRecyclerView()
        showAvailableCouponsLayout()
    }

    private fun setupAvailableCouponsRecyclerView() {
        val adapter = AvailableCouponAdapter { availableCoupon ->
            showConfirmationLayout(availableCoupon)
        }

        binding.couponListLayout.rvAvailableCoupons.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
        }

        // Osserva gli available coupons
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                adapter.submitList(state.availableCoupons)
            }
        }
    }


    private fun showAvailableCouponsLayout() {
        binding.apply {
            couponListLayout.root.isVisible = true
            confirmationLayout.root.isVisible = false
            generatedCouponLayout.root.isVisible = false
        }
    }

    private fun showConfirmationLayout(coupon: AvailableCoupon) {
        binding.apply {
            couponListLayout.root.isVisible = false
            confirmationLayout.root.isVisible = true
            generatedCouponLayout.root.isVisible = false

            with(confirmationLayout) {
                confirmationText.text = getString(
                    R.string.confirm_redemption_message,
                    coupon.value,
                    coupon.requiredPoints
                )

                buttonConfirm.setOnClickListener {
                    viewModel.onCouponSelected(coupon.value)
                }

                buttonCancel.setOnClickListener {
                    showAvailableCouponsLayout()
                }
            }
        }
    }

    private fun showGeneratedCouponLayout(coupon: Coupon) {
        binding.apply {
            couponListLayout.root.isVisible = false
            confirmationLayout.root.isVisible = false
            generatedCouponLayout.root.isVisible = true

            with(generatedCouponLayout) {
                textFieldCouponCode.setText(coupon.code)
                textFieldCouponCode.setOnClickListener {
                    viewModel.copyCouponToClipboard(coupon.code)
                }

                buttonClose.setOnClickListener {
                    dismiss()
                }
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiEvent.collect { event ->
                when (event) {
                    is CouponViewModel.UiEvent.CouponGenerated -> {
                        showGeneratedCouponLayout(event.coupon)
                    }
                    is CouponViewModel.UiEvent.ShowError -> {
                        showSnackbar(event.message.asString(requireContext()))
                        dismiss()
                    }
                    is CouponViewModel.UiEvent.ShowMessage -> {
                        showSnackbar(event.message.asString(requireContext()))
                    }
                }
            }
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = CouponBottomSheetDialogFragment()
    }
}