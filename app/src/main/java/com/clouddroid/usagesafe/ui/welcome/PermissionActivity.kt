package com.clouddroid.usagesafe.ui.welcome

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import androidx.lifecycle.ViewModelProviders
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_permission.*
import org.jetbrains.anko.longToast


class PermissionActivity : BaseActivity() {

    private var lockAvd: AnimatedVectorDrawableCompat? = null
    private var safeAvd: AnimatedVectorDrawableCompat? = null
    private lateinit var viewModel: PermissionActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)
        initViewModel()
        initAvDs()
        setOnClickListeners()
        scheduleAnimation()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this, viewModelFactory)[PermissionActivityViewModel::class.java]
    }

    private fun initAvDs() {
        lockAvd = AnimatedVectorDrawableCompat.create(this, R.drawable.avd_lock)
        lockAvd?.registerAnimationCallback(object : Animatable2Compat.AnimationCallback() {
            override fun onAnimationEnd(drawable: Drawable?) {
                playSafeShake()
            }
        })
        safeAvd = AnimatedVectorDrawableCompat.create(this, R.drawable.avd_safe)
    }

    private fun setOnClickListeners() {
        permissionButton.setOnClickListener {
            viewModel.startWatchingForPermissionChanges()
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            startActivity(intent)
            longToast(getString(R.string.activity_permission_find_app))
        }

        safeIV.setOnClickListener {
            playLockRotation()
        }
    }

    private fun scheduleAnimation() {
        val handler = Handler()
        handler.postDelayed({
            playLockRotation()
        }, 500)
    }

    private fun playLockRotation() {
        safeIV.setImageDrawable(lockAvd)
        lockAvd?.start()
    }

    private fun playSafeShake() {
        safeIV.setImageDrawable(safeAvd)
        safeAvd?.start()
    }
}