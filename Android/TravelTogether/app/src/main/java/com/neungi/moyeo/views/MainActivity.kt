package com.neungi.moyeo.views

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.location.LocationServices
import com.neungi.moyeo.R
import com.neungi.moyeo.config.BaseActivity
import com.neungi.moyeo.databinding.ActivityMainBinding
import com.neungi.moyeo.util.Permissions
import com.neungi.moyeo.views.aiplanning.viewmodel.AIPlanningViewModel
import com.neungi.moyeo.views.album.viewmodel.AlbumViewModel
import com.neungi.moyeo.views.auth.viewmodel.AuthViewModel
import com.neungi.moyeo.views.home.viewmodel.HomeViewModel
import com.neungi.moyeo.views.plan.scheduleviewmodel.ScheduleViewModel
import com.neungi.moyeo.views.plan.tripviewmodel.TripViewModel
import com.neungi.moyeo.views.setting.viewmodel.SettingViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.jar.Manifest

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    private val viewModel: MainViewModel by viewModels()
    private val aiPlanningViewModel: AIPlanningViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()
    private val tripViewModel: TripViewModel by viewModels()
    private val albumViewModel: AlbumViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    private val settingViewModel: SettingViewModel by viewModels()
    private lateinit var navController: NavController
    private val navHostFragment: NavHostFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.fcv_main) as NavHostFragment
    }

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.vm = viewModel
        binding.lifecycleOwner = this
        val deviceId = Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
        setBottomNavigationBar()

        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) && !hasPermission()) {
            requestNotificationPermission()
        }
        lifecycleScope.launch {
            viewModel.loadingState.collectLatest { isLoading ->
                Timber.d("Loading : "+isLoading.toString())
                if(isLoading) {
                    binding.lottieLoading.isClickable = true
                    binding.lottieLoading.isFocusable = true
                    binding.loadingAnimation.playAnimation()
                } else {
                    binding.lottieLoading.isClickable = false
                    binding.lottieLoading.isFocusable = false
                    binding.loadingAnimation.cancelAnimation()
                }

            }
        }


    }




    private fun setBottomNavigationBar() {
        navController = navHostFragment.navController
        navController.setGraph(R.navigation.nav_graph_main)
        with(binding.bnvMain) {
            setupWithNavController(navController)
            background = null
            menu.getItem(2).isEnabled = false
        }
        binding.fabOrder.setOnClickListener {
//            navController.popBackStack()
            if(viewModel.userLoginInfo.value==null){
                navController.navigate(R.id.fragment_login)
            }else {
                navController.navigate(R.id.fragment_select_period)
                binding.bnvMain.selectedItemId = 0
            }
        }
    }



    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun hasPermission(): Boolean {
        for (permission in Permissions.NOTIFICATION_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    private fun requestNotificationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
            NOTIFICATION_PERMISSION_REQUEST_CODE
        )
    }

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 100  // 임의의 고유 코드
    }

}