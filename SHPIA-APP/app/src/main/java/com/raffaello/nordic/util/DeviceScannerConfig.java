package com.raffaello.nordic.util;

import android.os.ParcelUuid;

import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;
import no.nordicsemi.android.thingylib.utils.ThingyUtils;


public class DeviceScannerConfig {
    private static ScanSettings scanSettings = new ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY) // recommended for foreground
            .setReportDelay(20)
            .setUseHardwareBatchingIfSupported(false)
            .build();

    private static List<ScanFilter> filters = new ArrayList<>();


    private static void prepareFilters() {
        if(filters.isEmpty()) {
            filters.add(new ScanFilter.Builder().setServiceUuid(new ParcelUuid(ThingyUtils.THINGY_BASE_UUID)).build());
        }
    }

    public static List<ScanFilter> getFilters() {
        prepareFilters();
        return filters;
    }

    public static ScanSettings getScanSettings() {
        return scanSettings;
    }
}