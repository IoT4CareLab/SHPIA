package com.raffaello.nordic.util;

import android.content.Context;
import android.view.MotionEvent;

import com.raffaello.nordic.R;

import org.rajawali3d.Object3D;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.lights.PointLight;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.loader.ParsingException;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.renderer.RajawaliRenderer;

public class Renderer extends RajawaliRenderer {

    private Context mContext;

    private Object3D mObjectModel;

    private double x, y, z, w;

    private boolean mIsConnected = false;
    private boolean mIsNotificationEnabled = false;

    public Renderer(Context context) {
        super(context);
        this.mContext = context;
        setFrameRate(60);
    }

    public void setConnectionState(final boolean flag) {
        mIsConnected = flag;
    }

    public void setNotificationEnabled(final boolean flag) {
        mIsNotificationEnabled = flag;
    }

    public void setQuaternions(final double x, final double y, final double z, final double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    @Override
    protected void initScene() {
        DirectionalLight directionalLight1 = new DirectionalLight(-1f, -1f, -1.0f);
        directionalLight1.setColor(1.0f, 1.0f, 1.0f);
        directionalLight1.setPower(10);
        //getCurrentScene().addLight(directionalLight1);

        PointLight pl1 = new PointLight();
        pl1.setPosition(0.6, 0.9, 10.4);
        pl1.setRotation(-45, 20, 45);
        pl1.setColor(1.0f, 1.0f, 1.0f);
        pl1.setPower(60);
        //getCurrentScene().addLight(pl1);

        PointLight pl2 = new PointLight();
        pl2.setPosition(-2.5, 3.9, 10.4);
        pl2.setRotation(-45, -20, 45);
        pl1.setColor(1.0f, 1.0f, 1.0f);
        pl2.setPower(20);
        //getCurrentScene().addLight(pl2);

        getCurrentScene().setBackgroundColor(0xffffff);

        try {

            LoaderOBJ objParser = new LoaderOBJ(mContext.getResources(), mTextureManager, R.raw.thingymodel_obj);
            objParser.parse();
            mObjectModel = objParser.getParsedObject();

            getCurrentScene().addChild(mObjectModel);
        } catch (ParsingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {

    }

    @Override
    public void onTouchEvent(MotionEvent event) {

    }

    @Override
    protected void onRender(long ellapsedRealtime, double deltaTime) {
        super.onRender(ellapsedRealtime, deltaTime);
        if (mIsConnected && mIsNotificationEnabled) {
            Quaternion q = mObjectModel.getOrientation();
            q.setAll(w, -y, -x, z);
            mObjectModel.setOrientation(q);
        } else {
            Quaternion q = mObjectModel.getOrientation();
            q.setAll(1, 0, 0, 0);
            mObjectModel.setOrientation(q);
        }
    }
}