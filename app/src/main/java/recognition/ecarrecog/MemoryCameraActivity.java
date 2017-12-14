package recognition.ecarrecog;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.Helper.ComRecogHelper;
import com.Helper.RecogResult;
import com.ecaray.wintonlib.WintonRecogManager;
import com.ecaray.wintonlib.helper.RecogniteHelper4WT;
import com.mine.recog.R;
import com.utils.Consts;
import com.utils.MemoryUtil;
import com.utils.RecogFileUtil;
import com.utils.ViewfinderView;

import java.util.List;



/**
 * ===============================================
 * <p>
 * 项目名称: parkBees-pda
 * <p>
 * 包: com.chmtech.parkbees.activity
 * <p>
 * 类名称: MemoryCameraActivity
 * <p>
 * 类描述:拍照识别界面
 * <p>
 * 创建人:
 * <p>
 * 创建时间: 2015-9-7 上午11:45:42
 * <p>
 * 修改人:
 * <p>
 * 修改时间: 2015-9-7 上午11:45:42
 * <p>
 * 修改备注:
 * <p>
 * 版本:
 * <p>
 * ===============================================
 */

public class MemoryCameraActivity extends Activity implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private Camera mCamera;
    private SurfaceView surfaceView;
    private ViewfinderView myview;
    private RelativeLayout re;
    private SurfaceHolder holder;
    private int width, height;
    private int preWidth = 0;
    private int preHeight = 0;
    private boolean isFatty = false;
    private int rotation = 90;

    private boolean isSuccess; //是否获取成功
    private Camera.Parameters parameters;
    private ComRecogHelper recogHelper;
    WintonRecogManager wintonHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recogHelper = ComRecogHelper.getDefault(getApplication(), true, "粤",false);
        Consts.IS_WENTONG=false;
        if (Consts.IS_WENTONG) {
            wintonHelper = WintonRecogManager.getInstance();
            wintonHelper.setStop(false);
            wintonHelper.bind(MemoryCameraActivity.this);
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);//
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_carmera);
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        width = metric.widthPixels; //
        height = metric.heightPixels; //
        findView();
        if (width * 3 == height * 4) {
            isFatty = true;
        }
        if (rotation == 90 || rotation == 270) {
            myview = new ViewfinderView(MemoryCameraActivity.this, width, height, false);
        } else {
            myview = new ViewfinderView(MemoryCameraActivity.this, width, height, true);
        }
        findViewById(R.id.memory).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mCamera!=null)
                    mCamera.autoFocus(new AutoFocusCallback() {
                        @Override
                        public void onAutoFocus(boolean b, Camera camera) {

                        }
                    });
            }
        });


    }


    /**
     * 文通获取车牌号后的回调类
     */
    class Geted implements RecogniteHelper4WT.OnResult {


        @Override
        public void onGeted(String fileName, String carPlate) {
            wintonHelper.setStop(true);
            Consts.platenum = carPlate;

//            Intent intent = new Intent(MemoryCameraActivity.this, TestActivity.class);
//            startActivity(intent);
            setResult(RESULT_OK);
            MemoryCameraActivity.this.finish();

        }

        @Override
        public void recogFail() {

        }

        @Override
        public String saveImage(byte[] data) {
            Consts.orgdata = data;
            return "";
        }
    }


    private void findView() {
        findViewById(R.id.btn_save).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Consts.orgdata == null || mCamera == null) {
                    Toast.makeText(MemoryCameraActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
                    return;
                }
                Consts.orgw = mCamera.getParameters().getPreviewSize().width;
                Consts.orgh = mCamera.getParameters().getPreviewSize().height;
                Toast.makeText(MemoryCameraActivity.this, "正在保存~", Toast.LENGTH_SHORT).show();
                RecogFileUtil.saveBitmap();
                Toast.makeText(MemoryCameraActivity.this, "保存成功！", Toast.LENGTH_SHORT).show();
            }
        });

        surfaceView = (SurfaceView) findViewById(R.id.surfaceViwe_video);
        surfaceView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mCamera.autoFocus(new AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean b, Camera camera) {

                    }
                });
            }
        });
        re = (RelativeLayout) findViewById(R.id.memory);
        // setButton();
        holder = surfaceView.getHolder();
        holder.addCallback(MemoryCameraActivity.this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        if (ComRecogHelper.isPic) {
            findViewById(R.id.memory).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Consts.orgdata == null || mCamera == null) {
                        Toast.makeText(MemoryCameraActivity.this, "识别失败，请再按一次", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    surfaceView.setClickable(false);
                    recogHelper.getCarnum(Consts.orgdata, mCamera, new RecogResult() {
                        @Override
                        public void recogSuccess(String carPlate, byte[] picData) {
//                            Intent intent = new Intent(MemoryCameraActivity.this, TestActivity.class);
//                            startActivity(intent);
                            MemoryCameraActivity.this.finish();
                        }

                        @Override
                        public void recogFail() {
                        }

                        @Override
                        public void permitionSuccess() {

                        }

                        @Override
                        public void permitionFail() {
                            Toast.makeText(MemoryCameraActivity.this, "获取权限失败！", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                }
            });
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        recogHelper.offFlash(parameters);
        Consts.IS_WENTONG=true;

    }

    /**
     * 方法描述：相机滞空
     * <p>
     *
     * @param
     * @return
     */
    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.cancelAutoFocus();
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            if (Consts.IS_WENTONG&&wintonHelper!=null)
                wintonHelper.unBind(this, Consts.IS_WENTONG);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        initCamera(holder, rotation);

    }

    @Override
    public void surfaceChanged(final SurfaceHolder holder, int format, int width, int height) {

    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                releaseCamera();
            }
        }).start();


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        releaseCamera();

    }

    long time;

    @Override
    public void onPreviewFrame(byte[] data, final Camera camera) {

        if (ComRecogHelper.isPic || isSuccess) {
            return;
        }
        if (Consts.IS_WENTONG) {
            if (wintonHelper != null && System.currentTimeMillis() - time > 500) {
                time = System.currentTimeMillis();
                wintonHelper.useWTRecognitionByData(this, data, new Geted(), preWidth, preHeight);
            }
        } else {
            recogHelper.getCarnum(data, camera, new RecogResult() {
                @Override
                public void recogSuccess(String carPlate, byte[] picData) {
                    isSuccess = true;
                    //识别一次
                    setResult(RESULT_OK);
                    MemoryCameraActivity.this.finish();

                    //无限次识别
//                recogHelper=RecogHelperSafe.getDefault(MemoryCameraActivity.this,true);
//                Toast.makeText(MemoryCameraActivi`ty.this, "车牌号 ="+carPlate, Toast.LENGTH_SHORT).show();

                }

                @Override
                public void recogFail() {
                }

                @Override
                public void permitionSuccess() {

                }

                @Override
                public void permitionFail() {
                    Toast.makeText(MemoryCameraActivity.this, "获取权限失败！", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                }
            });
            Log.i("recog=", "可用内存=" + MemoryUtil.getmem_UNUSED(this) + "\n总内存=" + MemoryUtil.getmem_TOLAL());
        }
    }


    /**
     * @throws
     * @Title: initCamera
     */
    @TargetApi(14)
    private void initCamera(SurfaceHolder holder, int r) {
        if (mCamera == null) {
            try {
                mCamera = Camera.open();
                mCamera.setPreviewDisplay(holder);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        parameters = mCamera.getParameters();
        List<Camera.Size> list = parameters.getSupportedPreviewSizes();
        Camera.Size size;
        int length = list.size();
        int previewWidth = 480;
        int previewheight = 640;
        int second_previewWidth;
        int second_previewheight;
        if (length == 1) {
            size = list.get(0);
            previewWidth = size.width;
            previewheight = size.height;
        } else {
            for (int i = 0; i < length; i++) {
                size = list.get(i);
                if (isFatty) {
                    if (size.height <= 960 || size.width <= 1280) {
                        second_previewWidth = size.width;
                        second_previewheight = size.height;
                        previewWidth = second_previewWidth;
                        previewheight = second_previewheight;
                    }
                } else {
                    if (size.height <= 960 || size.width <= 1280) {
                        second_previewWidth = size.width;
                        second_previewheight = size.height;
                        if (previewWidth <= second_previewWidth) {
                            previewWidth = second_previewWidth;
                            previewheight = second_previewheight;
                        }
                    }
                }
            }
        }
        preWidth = previewWidth;
        preHeight = previewheight;
        Consts.orgw = preWidth;
        Consts.orgh = preHeight;
        parameters.setPictureFormat(PixelFormat.JPEG);
        parameters.setPreviewSize(preWidth, preHeight);
        recogHelper.openFlash(parameters);
        parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_CLOUDY_DAYLIGHT);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        mCamera.setParameters(parameters);
        if (rotation == 90 || rotation == 270) {
            if (width < 1080) {
                mCamera.stopPreview();
            }
        } else {
            if (height < 1080) {
                mCamera.stopPreview();
            }
        }

        mCamera.setDisplayOrientation(r);
        mCamera.setPreviewCallback(this);
        mCamera.startPreview();

        re.removeView(myview);
        re.addView(myview);
        
        mCamera.autoFocus(new AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean b, Camera camera) {

            }
        });
    }


}
