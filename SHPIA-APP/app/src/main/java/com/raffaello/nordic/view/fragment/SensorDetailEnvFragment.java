package com.raffaello.nordic.view.fragment;

import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.raffaello.nordic.R;
import com.raffaello.nordic.model.NordicDevice;

import org.w3c.dom.Text;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import no.nordicsemi.android.thingylib.ThingyListener;
import no.nordicsemi.android.thingylib.ThingyListenerHelper;
import no.nordicsemi.android.thingylib.ThingySdkManager;
import no.nordicsemi.android.thingylib.utils.ThingyUtils;

public class SensorDetailEnvFragment extends Fragment {

    private ThingySdkManager thingySdkManager;
    private NordicDevice sensor;
    private BluetoothDevice device;
    private boolean isConnected;

    @BindView(R.id.line_chart_temperature)
    LineChart lineChartTemperature;

    @BindView(R.id.line_chart_humidity)
    LineChart lineChartHumidity;

    @BindView(R.id.line_chart_pressure)
    LineChart lineChartPressure;

    @BindView(R.id.temperature)
    TextView temperatureView;

    @BindView(R.id.pressure)
    TextView pressureView;

    @BindView(R.id.humidity)
    TextView humidityView;

    @BindView(R.id.eco2)
    TextView eco2View;

    @BindView(R.id.tvoc)
    TextView tvocView;

    public SensorDetailEnvFragment(NordicDevice sensor){
        this.sensor = sensor;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_sensor_detail_env, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //prepareTemperatureGraph();
        //preparePressureGraph();
        //prepareHumidityGraph();

        prepareGraph(lineChartTemperature, SensorType.TEMPERATURE ,-10f, 40f);
        prepareGraph(lineChartPressure, SensorType.PRESSURE,700f, 1100f);
        prepareGraph(lineChartHumidity, SensorType.HUMIDITY,0f, 100f);

    }

    private boolean checkConnection(){

        for(BluetoothDevice bdevice: thingySdkManager.getConnectedDevices()){
            if(bdevice.getAddress().equals(sensor.address)){
                device = bdevice;
                return true;
            }
        }

        return false;
    }

    @Override
    public void onPause() {
        super.onPause();
        ThingyListenerHelper.unregisterThingyListener(getContext(), thingyListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        thingySdkManager = ThingySdkManager.getInstance();
        isConnected = checkConnection();

        if(isConnected) {
            ThingyListenerHelper.registerThingyListener(getContext(), thingyListener, device);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ThingyListenerHelper.unregisterThingyListener(getContext(), thingyListener);
    }

    private ThingyListener thingyListener = new ThingyListener() {
        @Override
        public void onDeviceConnected(BluetoothDevice device, int connectionState) {

        }

        @Override
        public void onDeviceDisconnected(BluetoothDevice device, int connectionState) {

        }

        @Override
        public void onServiceDiscoveryCompleted(BluetoothDevice device) {

        }

        @Override
        public void onBatteryLevelChanged(BluetoothDevice bluetoothDevice, int batteryLevel) {

        }

        @Override
        public void onTemperatureValueChangedEvent(BluetoothDevice bluetoothDevice, String temperature) {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            addEntry(timestamp.toString(), Float.valueOf(temperature), SensorType.TEMPERATURE);
            temperatureView.setText(temperature);
        }

        @Override
        public void onPressureValueChangedEvent(BluetoothDevice bluetoothDevice, String pressure) {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            addEntry(timestamp.toString(), Float.valueOf(pressure), SensorType.PRESSURE);
            pressureView.setText(pressure);
        }

        @Override
        public void onHumidityValueChangedEvent(BluetoothDevice bluetoothDevice, String humidity) {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            addEntry(timestamp.toString(), Float.valueOf(humidity), SensorType.HUMIDITY);
            humidityView.setText(humidity);
        }

        @Override
        public void onAirQualityValueChangedEvent(BluetoothDevice bluetoothDevice, int eco2, int tvoc) {
            eco2View.setText(String.valueOf(eco2));
            tvocView.setText(String.valueOf(tvoc));
        }

        @Override
        public void onColorIntensityValueChangedEvent(BluetoothDevice bluetoothDevice, float red, float green, float blue, float alpha) {

        }

        @Override
        public void onButtonStateChangedEvent(BluetoothDevice bluetoothDevice, int buttonState) {

        }

        @Override
        public void onTapValueChangedEvent(BluetoothDevice bluetoothDevice, int direction, int count) {

        }

        @Override
        public void onOrientationValueChangedEvent(BluetoothDevice bluetoothDevice, int orientation) {

        }

        @Override
        public void onQuaternionValueChangedEvent(BluetoothDevice bluetoothDevice, float w, float x, float y, float z) {

        }

        @Override
        public void onPedometerValueChangedEvent(BluetoothDevice bluetoothDevice, int steps, long duration) {

        }

        @Override
        public void onAccelerometerValueChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {

        }

        @Override
        public void onGyroscopeValueChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {

        }

        @Override
        public void onCompassValueChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {

        }

        @Override
        public void onEulerAngleChangedEvent(BluetoothDevice bluetoothDevice, float roll, float pitch, float yaw) {

        }

        @Override
        public void onRotationMatrixValueChangedEvent(BluetoothDevice bluetoothDevice, byte[] matrix) {

        }

        @Override
        public void onHeadingValueChangedEvent(BluetoothDevice bluetoothDevice, float heading) {

        }

        @Override
        public void onGravityVectorChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {

        }

        @Override
        public void onSpeakerStatusValueChangedEvent(BluetoothDevice bluetoothDevice, int status) {

        }

        @Override
        public void onMicrophoneValueChangedEvent(BluetoothDevice bluetoothDevice, byte[] data) {

        }
    };

    // Graphs
    private void prepareGraph(LineChart chart, SensorType type ,float minY, float maxY){
        if (!chart.isEmpty()) {
            chart.getData().getXVals().clear();
            chart.clearValues();
        }
        chart.setTouchEnabled(true);
        chart.setVisibleXRangeMinimum(5);
        chart.setDragEnabled(true);
        chart.setPinchZoom(true);
        chart.setScaleEnabled(true);
        chart.setAutoScaleMinMaxEnabled(true);
        chart.setDrawGridBackground(false);
        chart.setBackgroundColor(Color.WHITE);

        LineData data = new LineData();
        data.setValueFormatter(new ChartValueFormatter());
        data.setValueTextColor(Color.WHITE);
        chart.setData(data);

        Legend legend = chart.getLegend();
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextColor(Color.BLACK);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawGridLines(false);
        xAxis.setAvoidFirstLastClipping(true);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawZeroLine(true);
        leftAxis.setTextColor(Color.BLACK);
        switch (type){
            case TEMPERATURE: leftAxis.setValueFormatter(new TemperatureYValueFormatter());
            default: leftAxis.setValueFormatter(new PressureChartYValueFormatter());
        }

        leftAxis.setDrawLabels(true);
        leftAxis.setAxisMinValue(minY);
        leftAxis.setAxisMaxValue(maxY);
        //leftAxis.setLabelCount(6, false);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);
    }

    private LineDataSet createDataSet(SensorType type){
        LineDataSet lineDataSet;
        switch (type){
            case TEMPERATURE: lineDataSet = new LineDataSet(null, "temperature"); break;
            case PRESSURE: lineDataSet = new LineDataSet(null, "pressure"); break;
            default: lineDataSet = new LineDataSet(null, "humidity"); break;
        }
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet.setColor(ContextCompat.getColor(requireContext(), R.color.teal_700));
        lineDataSet.setFillColor(ContextCompat.getColor(requireContext(), R.color.teal_200));
        lineDataSet.setHighLightColor(ContextCompat.getColor(requireContext(), R.color.teal_200));
        lineDataSet.setValueFormatter(new ChartValueFormatter());
        lineDataSet.setDrawValues(true);
        lineDataSet.setDrawCircles(true);
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setValueTextSize(6.5f);
        lineDataSet.setLineWidth(2.0f);
        return lineDataSet;
    }

    private void addEntry(final String timestamp, float value, SensorType type){
        final LineData data;
        final LineChart chart;
        switch (type){
            case TEMPERATURE: {
                chart = lineChartTemperature;
                data = lineChartTemperature.getData();
            } break;
            case PRESSURE: {
                chart = lineChartPressure;
                data = lineChartPressure.getLineData();
            } break;
            default: {
                chart = lineChartHumidity;
                data = lineChartHumidity.getLineData();
            } break;
        }

        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);

            if (set == null) {

                switch (type){
                    case TEMPERATURE: set = createDataSet(SensorType.TEMPERATURE); break;
                    case PRESSURE: set = createDataSet(SensorType.PRESSURE); break;
                    default: set = createDataSet(SensorType.HUMIDITY); break;
                }

                data.addDataSet(set);
            }
            data.addXValue(timestamp);
            final Entry entry = new Entry(value, set.getEntryCount());
            data.addEntry(entry, 0);
            final YAxis leftAxis = chart.getAxisLeft();

            if (value > leftAxis.getAxisMaximum()) {
                leftAxis.setAxisMaxValue(leftAxis.getAxisMaximum() + 20f);
            } else if (value < leftAxis.getAxisMinimum()) {
                leftAxis.setAxisMinValue(leftAxis.getAxisMinimum() - 20f);
            }

            chart.notifyDataSetChanged();
            chart.setVisibleXRangeMaximum(10);

            if (data.getXValCount() >= 10) {
                final int highestVisibleIndex = chart.getHighestVisibleXIndex();
                if ((data.getXValCount() - 10) < highestVisibleIndex) {
                    chart.moveViewToX(data.getXValCount() - 11);
                } else {
                    chart.invalidate();
                }
            } else {
                chart.invalidate();
            }
        }
    }

    // Graphs classes
    private enum SensorType{
        TEMPERATURE,
        PRESSURE,
        HUMIDITY
    }

    private class ChartValueFormatter implements ValueFormatter {
        private DecimalFormat mFormat;

        ChartValueFormatter() {
            mFormat = new DecimalFormat("##,##,#0.00");
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            return mFormat.format(value);
        }
    }

    private class TemperatureYValueFormatter implements YAxisValueFormatter {
        private DecimalFormat mFormat;

        TemperatureYValueFormatter() {
            mFormat = new DecimalFormat("##,##,#0.00");
        }

        @Override
        public String getFormattedValue(float value, YAxis yAxis) {
            return mFormat.format(value); //
        }
    }

    private class PressureChartYValueFormatter implements YAxisValueFormatter {
        private DecimalFormat mFormat;

        PressureChartYValueFormatter() {
            mFormat = new DecimalFormat("###,##0.00");
        }

        @Override
        public String getFormattedValue(float value, YAxis yAxis) {
            return mFormat.format(value);
        }
    }
}