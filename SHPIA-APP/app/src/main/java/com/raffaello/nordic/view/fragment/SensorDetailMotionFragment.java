package com.raffaello.nordic.view.fragment;

import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.raffaello.nordic.model.Device;
import com.raffaello.nordic.service.DataCollectorService;
import com.raffaello.nordic.util.BeaconScanner;
import com.raffaello.nordic.util.Renderer;
import com.raffaello.nordic.util.ServiceUtils;
import com.raffaello.nordic.view.activity.MainActivity;

import org.altbeacon.beacon.Beacon;
import org.rajawali3d.surface.RajawaliSurfaceView;

import java.text.DecimalFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.thingylib.ThingyListener;
import no.nordicsemi.android.thingylib.ThingyListenerHelper;
import no.nordicsemi.android.thingylib.ThingySdkManager;
import no.nordicsemi.android.thingylib.utils.ThingyUtils;

public class SensorDetailMotionFragment extends Fragment {

    private ThingySdkManager thingySdkManager;
    private Device sensor;
    private BluetoothDevice device;
    private boolean isConnected = false;
    private BeaconScanner scanner;

    private Renderer renderer;

    @BindView(R.id.line_chart_gravity)
    LineChart lineChartGravityVector;

    @BindView(R.id.line_acc_gravity)
    LineChart lineAccGravityVector;

    @BindView(R.id.heading)
    TextView headingView;

    @BindView(R.id.heading_direction)
    TextView headingDirectionView;

    @BindView(R.id.orientation)
    TextView orientationView;

    @BindView(R.id.portrait_image)
    ImageView portraitImage;

    @BindView(R.id.rajwali_surface)
    RajawaliSurfaceView surfaceView;

    public SensorDetailMotionFragment(Device sensor){
        this.sensor = sensor;
        scanner=BeaconScanner.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sensor_detail_motion, container, false);
        ButterKnife.bind(this, view);

        // 3D model
        renderer = new Renderer(getContext());
        surfaceView.setSurfaceRenderer(renderer);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prepareVectorChart(lineChartGravityVector, -10f, 10f);
        prepareVectorChart(lineAccGravityVector, -3f, 3f);

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
    public void onResume() {
        super.onResume();

        if(sensor.description.startsWith("Nordic")){
            thingySdkManager = ThingySdkManager.getInstance();
            isConnected = checkConnection();

            if(isConnected) {
                ThingyListenerHelper.registerThingyListener(getContext(), thingyListener, device);
                renderer.setConnectionState(isConnected);
                renderer.setNotificationEnabled(true);
            }
        }
        else{
            if(ServiceUtils.isRunning(DataCollectorService.class, MainActivity.getAppContext()))//if I am collecting data
                scanner.setFragment2(sensor.address,this);
        }

        if (surfaceView != null) {
            surfaceView.onResume();
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if (surfaceView != null) {
            surfaceView.onPause();
        }
        ThingyListenerHelper.unregisterThingyListener(getContext(), thingyListener);
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

        }

        @Override
        public void onPressureValueChangedEvent(BluetoothDevice bluetoothDevice, String pressure) {

        }

        @Override
        public void onHumidityValueChangedEvent(BluetoothDevice bluetoothDevice, String humidity) {

        }

        @Override
        public void onAirQualityValueChangedEvent(BluetoothDevice bluetoothDevice, int eco2, int tvoc) {

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
            portraitImage.setPivotX(portraitImage.getWidth() / 2.0f);
            portraitImage.setPivotY(portraitImage.getHeight() / 2.0f);
            portraitImage.setRotation(0);

            switch (orientation) {
                case ThingyUtils.PORTRAIT_TYPE:
                    portraitImage.setRotation(0);

                    orientationView.setText(ThingyUtils.PORTRAIT);
                    break;
                case ThingyUtils.LANDSCAPE_TYPE:
                    portraitImage.setRotation(90);

                    orientationView.setText(ThingyUtils.LANDSCAPE);
                    break;
                case ThingyUtils.REVERSE_PORTRAIT_TYPE:
                    portraitImage.setRotation(-180);
                    orientationView.setText(ThingyUtils.REVERSE_PORTRAIT);
                    break;
                case ThingyUtils.REVERSE_LANDSCAPE_TYPE:
                    portraitImage.setRotation(-90);

                    orientationView.setText(ThingyUtils.REVERSE_LANDSCAPE);
                    break;
            }

        }

        @Override
        public void onQuaternionValueChangedEvent(BluetoothDevice bluetoothDevice, float w, float x, float y, float z) {
            if (surfaceView!= null) {
                renderer.setQuaternions(x, y, z, w);
            }
        }

        @Override
        public void onPedometerValueChangedEvent(BluetoothDevice bluetoothDevice, int steps, long duration) {

        }

        @Override
        public void onAccelerometerValueChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {
            addVectorEntry(x, y, z, lineAccGravityVector);
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
            if (heading >= 0 && heading <= 10) {
                headingDirectionView.setText("NORTH");
            } else if (heading >= 35 && heading <= 55) {
                headingDirectionView.setText("N. East");
            } else if (heading >= 80 && heading <= 100) {
                headingDirectionView.setText("EAST");
            } else if (heading >= 125 && heading <= 145) {
                headingDirectionView.setText("S. EAST");
            } else if (heading >= 170 && heading <= 190) {
                headingDirectionView.setText("SOUTH");
            } else if (heading >= 215 && heading <= 235) {
                headingDirectionView.setText("S. WEST");
            } else if (heading >= 260 && heading <= 280) {
                headingDirectionView.setText("WEST");
            } else if (heading >= 305 && heading <= 325) {
                headingDirectionView.setText("N. WEST");
            } else if (heading >= 350 && heading <= 359) {
                headingDirectionView.setText("NORTH");
            }

            headingView.setText(heading + "°");
        }

        @Override
        public void onGravityVectorChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {
            addVectorEntry(x, y, z, lineChartGravityVector);
        }

        @Override
        public void onSpeakerStatusValueChangedEvent(BluetoothDevice bluetoothDevice, int status) {

        }

        @Override
        public void onMicrophoneValueChangedEvent(BluetoothDevice bluetoothDevice, byte[] data) {

        }
    };

    // Graph
    private void prepareVectorChart(LineChart chart, float minY, float maxY) {
        if (!chart.isEmpty()) {
            chart.clearValues();
        }
        chart.setTouchEnabled(true);
        chart.setVisibleXRangeMinimum(5);
        chart.setVisibleXRangeMaximum(5);
        chart.setDragEnabled(true);
        chart.setPinchZoom(true);
        chart.setScaleEnabled(true);
        chart.setAutoScaleMinMaxEnabled(true);
        chart.setDrawGridBackground(false);
        chart.setBackgroundColor(Color.WHITE);

        LineData data = new LineData();
        data.setValueFormatter(new VectorChartValueFormatter());
        data.setValueTextColor(Color.WHITE);
        chart.setData(data);

        Legend legend = chart.getLegend();
        legend.setEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawGridLines(false);
        xAxis.setAvoidFirstLastClipping(true);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setValueFormatter(new VectorYValueFormatter());
        leftAxis.setDrawLabels(true);
        leftAxis.setAxisMinValue(minY);
        leftAxis.setAxisMaxValue(maxY);
        leftAxis.setLabelCount(3, false); //
        leftAxis.setDrawZeroLine(true);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);
    }

    private LineDataSet[] createVectorDataSet() {
        final LineDataSet[] lineDataSets = new LineDataSet[3];
        LineDataSet lineDataSetX = new LineDataSet(null, "X");
        lineDataSetX.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSetX.setColor(ContextCompat.getColor(requireContext(), R.color.teal_700));
        lineDataSetX.setHighLightColor(ContextCompat.getColor(requireContext(), R.color.teal_200));
        lineDataSetX.setValueFormatter(new VectorChartValueFormatter());
        lineDataSetX.setDrawValues(true);
        lineDataSetX.setDrawCircles(true);
        lineDataSetX.setDrawCircleHole(false);
        lineDataSetX.setValueTextSize(6.5f);
        lineDataSetX.setLineWidth(2.0f);
        lineDataSets[0] = lineDataSetX;

        LineDataSet lineDataSetY = new LineDataSet(null, "Y");
        lineDataSetY.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSetY.setColor(ContextCompat.getColor(requireContext(), R.color.teal_700));
        lineDataSetY.setHighLightColor(ContextCompat.getColor(requireContext(), R.color.teal_200));
        lineDataSetY.setValueFormatter(new VectorChartValueFormatter());
        lineDataSetY.setDrawValues(true);
        lineDataSetY.setDrawCircles(true);
        lineDataSetY.setDrawCircleHole(false);
        lineDataSetY.setValueTextSize(6.5f);
        lineDataSetY.setLineWidth(2.0f);
        lineDataSets[1] = lineDataSetY;

        LineDataSet lineDataSetZ = new LineDataSet(null, "Z");
        lineDataSetZ.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSetZ.setColor(ContextCompat.getColor(requireContext(), R.color.teal_700));
        lineDataSetZ.setHighLightColor(ContextCompat.getColor(requireContext(), R.color.teal_200));
        lineDataSetZ.setValueFormatter(new VectorChartValueFormatter());
        lineDataSetZ.setDrawValues(true);
        lineDataSetZ.setDrawCircles(true);
        lineDataSetZ.setDrawCircleHole(false);
        lineDataSetZ.setValueTextSize(6.5f);
        lineDataSetZ.setLineWidth(2.0f);
        lineDataSets[2] = lineDataSetZ;
        return lineDataSets;
    }

    private void addVectorEntry(final float gravityVectorX, final float gravityVectorY, final float gravityVectorZ, LineChart chart) {
        LineData data = chart.getData();

        if (data != null) {
            ILineDataSet setX = data.getDataSetByIndex(0);
            ILineDataSet setY = data.getDataSetByIndex(1);
            ILineDataSet setZ = data.getDataSetByIndex(2);

            if (setX == null || setY == null || setZ == null) {
                final LineDataSet[] dataSets = createVectorDataSet();
                setX = dataSets[0];
                setY = dataSets[1];
                setZ = dataSets[2];
                data.addDataSet(setX);
                data.addDataSet(setY);
                data.addDataSet(setZ);
            }

            data.addXValue(ThingyUtils.TIME_FORMAT_PEDOMETER.format(new Date()));
            data.addEntry(new Entry(gravityVectorX, setX.getEntryCount()), 0);
            data.addEntry(new Entry(gravityVectorY, setY.getEntryCount()), 1);
            data.addEntry(new Entry(gravityVectorZ, setZ.getEntryCount()), 2);

            chart.notifyDataSetChanged();
            chart.setVisibleXRangeMaximum(10);
            chart.moveViewToX(data.getXValCount() - 11);
        }
    }

    // Graph classes
    class VectorYValueFormatter implements YAxisValueFormatter {
        private DecimalFormat mFormat;

        VectorYValueFormatter() {
            mFormat = new DecimalFormat("##,##,#0.00");
        }

        @Override
        public String getFormattedValue(float value, YAxis yAxis) {
            return mFormat.format(value); //
        }
    }

    class VectorChartValueFormatter implements ValueFormatter {
        private DecimalFormat mFormat;

        VectorChartValueFormatter() {
            mFormat = new DecimalFormat("#0.00");
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            return mFormat.format(value);
        }
    }

    //methods for beacon
    public void setvectorAcc(Beacon beacon){
        addVectorEntry(scanner.getAcc(beacon)[0], scanner.getAcc(beacon)[1], scanner.getAcc(beacon)[2], lineAccGravityVector);
    }

}