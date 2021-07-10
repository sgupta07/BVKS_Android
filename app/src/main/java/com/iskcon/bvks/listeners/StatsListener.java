package com.iskcon.bvks.listeners;

import androidx.annotation.NonNull;

import com.iskcon.bvks.manager.StatsManager;
import com.iskcon.bvks.model.ListenRecord;

import java.util.List;

public interface StatsListener {
    void statsUpdated(@NonNull List<ListenRecord> statsList, StatsManager.StatsType statsType);
    void onError(String msg);

}
