package com.parishod.watomatic.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.parishod.watomatic.R
import com.parishod.watomatic.activity.customapp.CustomAppsAdditionActivity
import com.parishod.watomatic.adapter.SupportedAppsAdapter
import com.parishod.watomatic.model.logs.App
import com.parishod.watomatic.model.utils.Constants
import com.parishod.watomatic.model.utils.DbUtils

import kotlinx.android.synthetic.main.fragment_enabled_apps.view.*

class EnabledAppsFragment: Fragment() {
    lateinit var fragmentView:View
    lateinit var dbUtils: DbUtils
    lateinit var supportedAppsAdapter: SupportedAppsAdapter
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragmentView = inflater.inflate(R.layout.fragment_enabled_apps, container, false)

        val layoutManager = LinearLayoutManager(context)

        dbUtils = DbUtils(context)
        supportedAppsAdapter = SupportedAppsAdapter(Constants.EnabledAppsDisplayType.VERTICAL, ArrayList<App>(dbUtils.supportedApps))
        fragmentView.supportedAppsList.layoutManager = layoutManager
        fragmentView.supportedAppsList.adapter = supportedAppsAdapter

        fragmentView.addCustomPackageButton.setOnClickListener {
            startActivity(Intent(activity, CustomAppsAdditionActivity::class.java))
        }
        return fragmentView
    }

    override fun onResume() {
        super.onResume()

        supportedAppsAdapter = SupportedAppsAdapter(Constants.EnabledAppsDisplayType.VERTICAL, ArrayList<App>(dbUtils.supportedApps))
        fragmentView.supportedAppsList.adapter = supportedAppsAdapter
    }
}