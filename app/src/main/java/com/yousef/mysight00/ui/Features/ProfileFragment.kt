package com.yousef.mysight00.ui.Features

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yousef.mysight00.R
import com.yousef.mysight00.adapter.ProfileAdapter
import com.yousef.mysight00.model.ProfileItem

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val items = listOf(
            ProfileItem(R.drawable.ic_favorite, "Favourites"),
            ProfileItem(R.drawable.ic_download, "Downloads"),
            ProfileItem(R.drawable.ic_language, "Languages"),
            ProfileItem(R.drawable.ic_location, "Location"),
            ProfileItem(R.drawable.ic_clear_cache, "Clear Cache"),
            ProfileItem(R.drawable.ic_clear_history, "Clear History"),
            ProfileItem(R.drawable.ic_logout, "Log Out")
        )

        val adapter = ProfileAdapter(items)
        recyclerView.adapter = adapter

        return view
    }
}
