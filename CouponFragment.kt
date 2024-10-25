package com.example.hammami.presentation.ui.fragments.userProfile.coupon


import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hammami.R
import com.example.hammami.databinding.FragmentCouponBinding
import com.example.hammami.presentation.ui.adapters.ActiveCouponAdapter
import com.example.hammami.presentation.ui.fragments.BaseFragment
import com.example.hammami.presentation.ui.fragments.userProfile.coupon.CouponViewModel.*

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CouponFragment : BaseFragment() {
    private var _binding: FragmentCouponBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CouponViewModel by viewModels()
    private val activeCouponsAdapter by lazy { createActiveCouponsAdapter() }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCouponBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setupUI() {
        setupAppBar()
        setupRecyclerView()
        setupFab()
        setupInitialState()

    }


    override fun observeFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { observeState() }
                launch { observeEvents() }
            }
        }
    }

    private suspend fun observeState() {
        viewModel.state.collect { state ->
            updateUI(state)
        }
    }

    private suspend fun observeEvents() {
        viewModel.uiEvent.collect { event ->
            handleEvent(event)
        }
    }

    private fun updateUI(state: CouponState) = with(binding) {
        progressIndicator.isVisible = state.isLoading
        updatePointsDisplay(state.userPoints)
        updateCouponsList(state)
        updateFabState(state.canRedeemCoupons)
    }

    private fun updatePointsDisplay(points: Int) = with(binding) {
        tvPoints.text = getString(R.string.points, points)
    }

    private fun updateCouponsList(state: CouponState) = with(binding) {
        activeCouponsTitle.isVisible = state.hasActiveCoupons
        rvActiveCoupons.isVisible = state.hasActiveCoupons
        noActiveCoupons.isVisible = !state.hasActiveCoupons && !state.isLoading

        activeCouponsAdapter.submitList(state.activeCoupons)
    }

    private fun updateFabState(enabled: Boolean) = with(binding) {
        fabRedeemCoupon.isEnabled = enabled
    }


    private fun setupFab() = with(binding) {
        fabRedeemCoupon.setOnClickListener { showCouponBottomSheet() }
    }

    private fun showCouponBottomSheet() {
        CouponBottomSheetDialogFragment.newInstance()
            .show(childFragmentManager, "coupon_bottom_sheet")
    }


    private fun setupInitialState() = with(binding) {
        progressIndicator.isVisible = true
        activeCouponsTitle.isVisible = false
        rvActiveCoupons.isVisible = false
        noActiveCoupons.isVisible = false
    }


    private fun setupAppBar() {
        binding.topAppBar.setNavigationOnClickListener { onBackClick() }
    }

    private fun createActiveCouponsAdapter() = ActiveCouponAdapter { code ->
        viewModel.copyCouponToClipboard(code)
    }

    private fun setupRecyclerView() = with(binding) {
        rvActiveCoupons.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = activeCouponsAdapter
            isNestedScrollingEnabled = false
            addItemDecoration(createItemDecoration())
        }
    }

    private fun createItemDecoration() = object : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            outRect.bottom = resources.getDimensionPixelSize(R.dimen.spacing_small)
        }
    }


    private fun handleEvent(event: UiEvent) {
        when (event) {
            is UiEvent.ShowUserMessage -> showSnackbar(event.message)
        }
    }


    override fun onResume() {
        super.onResume()
        Log.d("CouponFragment", "onResume")
        viewModel.loadData()
    }

    override fun onPause() {
        super.onPause()
        Log.d("CouponFragment", "onPause")
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
