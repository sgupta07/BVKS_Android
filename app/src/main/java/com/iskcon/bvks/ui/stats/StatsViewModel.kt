package com.iskcon.bvks.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


class StatsViewModel : ViewModel() {
}


class StatsViewModelFactory() : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatsViewModel::class.java)) {
            return StatsViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}