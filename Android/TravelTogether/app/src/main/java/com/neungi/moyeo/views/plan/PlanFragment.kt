package com.neungi.moyeo.views.plan

import android.app.AlertDialog
import com.neungi.moyeo.views.plan.adapter.TripAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.neungi.moyeo.R
import com.neungi.moyeo.config.BaseFragment
import com.neungi.moyeo.databinding.DialogAddTripBinding
import com.neungi.moyeo.databinding.FragmentPlanBinding
import com.neungi.moyeo.views.MainViewModel
import com.neungi.moyeo.views.aiplanning.viewmodel.AIPlanningViewModel
import com.neungi.moyeo.views.plan.tripviewmodel.TripUiEvent
import com.neungi.moyeo.views.plan.tripviewmodel.TripViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate

@AndroidEntryPoint
class PlanFragment : BaseFragment<FragmentPlanBinding>(R.layout.fragment_plan) {

    private val tripViewModel: TripViewModel by activityViewModels()
    private val mainViewModel : MainViewModel by activityViewModels()
    private val aiPlaningViewModel: AIPlanningViewModel by activityViewModels()
    private lateinit var tripAdapter: TripAdapter
    private var flag = false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.vm = tripViewModel
        setupRecyclerView()
        binding.onAddClick = View.OnClickListener {
            showCalendarDialog()
        }
        lifecycleScope.launch {
            mainViewModel.userLoginInfo.collect{
                if (it != null) {
                    tripViewModel.getTrips("7")
                }
            }
        }
        lifecycleScope.launch {
            tripViewModel.trips.collectLatest {
                arguments?.let { argument ->
                    val tripId = argument.getInt("tripId", -1)
                    if (!flag && (tripId != -1)) {
                        tripViewModel.getTrip(tripId)
                    }
                }
            }
        }

        lifecycleScope.launch {
            tripViewModel.selectedTrip.collectLatest { trip ->
                val tripId = trip?.id ?: -1
                if (!flag && (tripId != -1)) {
                    flag = true
                    val bundle = Bundle().apply {
                        putInt("tripId", trip?.id ?: -1)
                    }
                    findNavController().navigateSafely(R.id.action_plan_to_planDetail, bundle)
                }
            }
        }

    }

    private fun handleUiEvent(event: TripUiEvent) {
        when (event) {
            is TripUiEvent -> {

            }
        }
    }


    override fun onResume() {
        super.onResume()
        mainViewModel.setBnvState(true)
    }

    private fun setupRecyclerView() {
        tripAdapter = TripAdapter(
            onItemClick = { trip ->
                tripViewModel.initTrip(trip)
                Timber.d(trip.title)
                findNavController().navigateSafely(R.id.action_plan_to_planDetail)
            },
            onDeleteClick = { tripId ->
                // 삭제 처리 로직
                tripViewModel
                println("Delete trip with ID: $tripId")
            }
        )
        binding.recyclerViewTrips.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewTrips.adapter = tripAdapter
    }

    private fun showCalendarDialog() {
        val dialogBinding = DialogAddTripBinding.inflate(LayoutInflater.from(context))
        dialogBinding.apply {
            vm = aiPlaningViewModel
        }
        val calendar = dialogBinding.calendarView
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("여행시작일과 종료일을 선택해주세요") // 다이어로그 타이틀 설정
            .setView(dialogBinding.root)
            .setPositiveButton("추가하기") { _, _ ->
                // OK 버튼 클릭 시 동작
                Timber.d(calendar.selectedStartDate.toString()+" "+calendar.selectedEndDate)
            }
            .setNegativeButton("취소하기", null)
            .create()

        dialog.show()
    }

    companion object {

        fun newInstance(tripId: Int) = PlanFragment().apply {
            arguments = Bundle().apply {
                putInt("tripId", tripId)
            }
        }
    }
}