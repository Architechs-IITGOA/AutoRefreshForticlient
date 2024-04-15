package com.iitgoapapaharpic.fortinetconnect

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
import androidx.appcompat.app.AppCompatActivity
import com.iitgoapapaharpic.fortinetconnect.databinding.ActivityAskPermissionsBinding


class AskPermissions : AppCompatActivity() {
    private lateinit var binding: ActivityAskPermissionsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAskPermissionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.giveProtectedAppPermission.setOnClickListener {
            requestIgnoreBatteryOptimizations()
        }

        binding.giveAutostartPermission.setOnClickListener {
            give_autostart_permission()
        }



        binding.understood.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun requestIgnoreBatteryOptimizations() {
        val powerManager = applicationContext.getSystemService(POWER_SERVICE) as PowerManager
        if (!powerManager.isIgnoringBatteryOptimizations(applicationContext.packageName)) {
            val intent = Intent(ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.data = Uri.parse("package:${applicationContext.packageName}")
            startActivity(intent)
        }
    }

    private fun give_autostart_permission(){
        val manufacturer = Build.MANUFACTURER
        try {
            val intent = Intent()
            if ("xiaomi".equals(manufacturer, ignoreCase = true)) {
                intent.setComponent(
                    ComponentName(
                        "com.miui.securitycenter",
                        "com.miui.permcenter.autostart.AutoStartManagementActivity"
                    )
                )
            } else if ("oppo".equals(manufacturer, ignoreCase = true)) {
                intent.setComponent(
                    ComponentName(
                        "com.coloros.safecenter",
                        "com.coloros.safecenter.permission.startup.StartupAppListActivity"
                    )
                )
            } else if ("vivo".equals(manufacturer, ignoreCase = true)) {
                intent.setComponent(
                    ComponentName(
                        "com.vivo.permissionmanager",
                        "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
                    )
                )
            } else if ("Letv".equals(manufacturer, ignoreCase = true)) {
                intent.setComponent(
                    ComponentName(
                        "com.letv.android.letvsafe",
                        "com.letv.android.letvsafe.AutobootManageActivity"
                    )
                )
            } else if ("Honor".equals(manufacturer, ignoreCase = true)) {
                intent.setComponent(
                    ComponentName(
                        "com.huawei.systemmanager",
                        "com.huawei.systemmanager.optimize.process.ProtectActivity"
                    )
                )
            }
            val list =
                packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            if (list.size > 0) {
                startActivity(intent)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
}