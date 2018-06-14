package recognition.ecarrecog;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.RequiresApi;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.Helper.ComRecogHelper;
import com.Helper.RecogResult;
import com.etop.plate.PlateAPI;
import com.mine.recog.R;
import com.utils.NavigationBarHeightUtils;
import com.utils.PLViewfinderView;
import com.utils.StreamEmpowerFileUtils;
import com.utils.UserIdUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * @author huiliu
 */
public class CarActivity extends Activity implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private static final String PATH = Environment.getExternalStorageDirectory() + "/alpha/Plate/";
    private Camera mCamera;
    private SurfaceView mSurfaceView;
    private RelativeLayout mainRl;
    private SurfaceHolder surfaceHolder;
    private PlateAPI plApi = null;
    private Bitmap bitmap;
    private int preWidth = 0;
    private int preHeight = 0;
    private int screenWidth;
    private int screenHeight;
    private Vibrator mVibrator;
    private PLViewfinderView myView;
    private boolean isFatty = false;
    private boolean bInitKernal = false;
    private AlertDialog alertDialog = null;
    private int[] m_ROI = {0, 0, 0, 0};
    private boolean isROI = false;
    private ImageButton ibnBack;
    private ImageButton ibnFlash;
    private boolean baddView = false;
    private Camera.Parameters params;
    private ComRecogHelper recogHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 设置全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 屏幕常亮
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main_etop);
        findView();
    }


    /******************* 更变内容 *******************/

    private void findView() {
        mSurfaceView = (SurfaceView) findViewById(R.id.etop_sv_main);
        mainRl = (RelativeLayout) findViewById(R.id.etop_rl_main);
        ibnBack = (ImageButton) findViewById(R.id.etop_ibn_back);
        ibnFlash = (ImageButton) findViewById(R.id.etop_ibn_flash);


        try {
            StreamEmpowerFileUtils.copyDataBase(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 竖屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //获取设置的配置信息
        Configuration cf = this.getResources().getConfiguration();
        int noriention = cf.orientation;

        if (noriention == Configuration.ORIENTATION_PORTRAIT) {
            initOCRKernal();//初始化识别核心
        }
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        // 屏幕宽度（像素）
        screenWidth = metric.widthPixels;
        // 屏幕高度（像素）
        screenHeight = metric.heightPixels;
        if (screenWidth * 3 == screenHeight * 4) {
            isFatty = true;
        }


        surfaceHolder = mSurfaceView.getHolder();
        surfaceHolder.addCallback(CarActivity.this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        if (alertDialog == null) {
            alertDialog = new Builder(this).create();
        }

        File file = new File(PATH);
        if (!file.exists() && !file.isDirectory()) {
            file.mkdirs();
        }
        mOnClick();
    }

    private void mOnClick() {
        ibnBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ibnFlash.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                    String mess = "当前设备不支持闪光灯";
                    Toast.makeText(getApplicationContext(), mess, Toast.LENGTH_SHORT).show();
                } else {
                    if (mCamera != null) {
                        params = mCamera.getParameters();
                        String flashMode = params.getFlashMode();
                        if (flashMode.equals(Camera.Parameters.FLASH_MODE_TORCH)) {
                            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                            params.setExposureCompensation(0);
                        } else {
                            // 闪光灯常亮
                            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                            params.setExposureCompensation(-1);
                        }
                        try {
                            mCamera.setParameters(params);
                        } catch (Exception e) {
                            String mess = "当前设备不支持闪光灯";
                            Toast.makeText(getApplicationContext(), mess, Toast.LENGTH_SHORT).show();
                        }
                        mCamera.startPreview();
                    }
                }
            }
        });
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        initOCRKernal();//如果初始化核心失败，重新初始化
        if (alertDialog == null) {
            alertDialog = new Builder(this).create();
        }
        if (mCamera == null) {
            try {
                /******************* 更变内容 *******************/
                mCamera = Camera.open();
                //获取maxZoom和countZoom数值
                /******************* 更变内容 *******************/
            } catch (Exception e) {
                e.printStackTrace();
                if (mCamera != null) {
                    mCamera.release();
                    mCamera = null;
                }
                String mess = getResources().getString(R.string.toast_camera);
                Toast.makeText(this, mess, Toast.LENGTH_SHORT).show();
                return;
            }
        }
        initCamera(holder);
    }


    /******************* 更变内容 *******************/

    @Override
    public void surfaceChanged(final SurfaceHolder holder, int format, int width, int height) {
        if (mCamera != null) {
            mCamera.autoFocus(new AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    if (success) {
                        synchronized (camera) {
                            new Thread() {
                                @Override
                                public void run() {
                                    initCamera(holder);
                                    super.run();
                                }
                            }.start();
                        }
                        mCamera.cancelAutoFocus();// 只有加上了这一句，才会自动对焦。
                    }
                }
            });
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        releaseCamera();//相机资源释放
        //卸载识别核心
        if (plApi != null) {
            plApi.ETUnInitPlateKernal();
            plApi = null;
        }
    }


    /********************初始化识别核心**********************/
    private void initOCRKernal() {
//        recogHelper = ComRecogHelper.getDefault(this,
//                true, "粤", false);
//        if (plApi == null) {
//            plApi = new PlateAPI();
//            String cacheDir = (this.getExternalCacheDir()).getPath();
//            String userIdPath = cacheDir + "/" + UserIdUtils.UserID + ".lic";
//            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//            int nRet = plApi.ETInitPlateKernal("", userIdPath, UserIdUtils.UserID, 0x06, 0x02, telephonyManager, this);
//            if (nRet != 0) {
//                Toast.makeText(getApplicationContext(), "激活失败", Toast.LENGTH_SHORT).show();
//                System.out.print("nRet=" + nRet);
//                bInitKernal = false;
//            } else {
//                System.out.print("nRet=" + nRet);
//                bInitKernal = true;
//            }
//        }
    }

    @TargetApi(14)
    private void initCamera(SurfaceHolder holder) {
        params = mCamera.getParameters();
        List<Size> list = params.getSupportedPreviewSizes();
        Size size;
        int length = list.size();
        Size tmpsize = list.get(0);
        int navigationBarHeight = NavigationBarHeightUtils.getNavigationBarHeight(this);
        tmpsize = getOptimalPreviewSize(list, screenHeight + navigationBarHeight, screenWidth);

        int previewWidth = list.get(0).width;
        int previewheight = list.get(0).height;
        previewWidth = tmpsize.width;
        previewheight = tmpsize.height;
        int second_previewWidth = 0;
        int second_previewheight = 0;

        if (length == 1) {
            preWidth = previewWidth;
            preHeight = previewheight;
        } else {
            second_previewWidth = previewWidth;
            second_previewheight = previewheight;
            for (int i = 0; i < length; i++) {
                size = list.get(i);
                if (size.height > 700) {
                    if (size.width * previewheight == size.height * previewWidth && size.height < second_previewheight) {
                        second_previewWidth = size.width;
                        second_previewheight = size.height;
                    }
                }
            }
            preWidth = second_previewWidth;
            preHeight = second_previewheight;
        }
        if (!isROI) {
            int t;
            int b;
            int l;
            int r;
            l = screenHeight / 5;
            r = screenHeight * 3 / 5;
            t = 4;
            b = screenWidth - 4;
            double proportion = (double) screenHeight / (double) preWidth;
            l = (int) (l / proportion);
            t = 0;
            r = (int) (r / proportion);
            b = preHeight;
            int borders[] = {l, t, r, b};
            m_ROI[0] = l;
            m_ROI[1] = t;
            m_ROI[2] = r;
            m_ROI[3] = b;
            recogHelper.getApi().ETSetPlateROI(borders, preWidth, preHeight);
            isROI = true;
        }
//        if (!baddView) {
//            if (isFatty) {
//                myView = new PLViewfinderView(this, screenWidth, screenHeight, isFatty);
//            } else {
//                myView = new PLViewfinderView(this, screenWidth, screenHeight);
//            }
//            mainRl.addView(myView);
//            baddView = true;
//        }

        params.setPreviewSize(preWidth, preHeight);
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        try {
            mCamera.setPreviewDisplay(surfaceHolder);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        mCamera.setPreviewCallback(this);
        mCamera.setParameters(params);
        mCamera.setDisplayOrientation(90);
        if ("500CT".equals(Build.MODEL)) {
            mCamera.setDisplayOrientation(270);
        }
        try {
            mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();
    }

  boolean  isSuccess ;

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

        recogHelper.getCarnum(data, camera, new RecogResult() {
            @Override
            public void recogSuccess(String carPlate, byte[] picData) {
//                isSuccess = true;
                //识别一次
                setResult(RESULT_OK);
                CarActivity.this.finish();

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
                Toast.makeText(CarActivity.this, "获取权限失败！", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
        });
//        params = camera.getParameters();
//        int buffl = 256;
//        char recogval[] = new char[buffl];
//        Long timeStart = System.currentTimeMillis();
//        try {
//
//            if (!alertDialog.isShowing()) {
//                int pLineWarp[] = new int[800 * 45];
//                int nv21Width = params.getPreviewSize().width;
//                int nv21Height = params.getPreviewSize().height;
//                int r = plApi.RecognizePlateNV21(data, 1, nv21Width, nv21Height, recogval, buffl, pLineWarp);
//                Long timeEnd = System.currentTimeMillis();
//                if (r == 0) {
//                    // 震动
//                    String plateNo = plApi.GetRecogResult(0);
//                    String plateColor = plApi.GetRecogResult(1);
//                    mVibrator = (Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE);
//                    mVibrator.vibrate(50);
//
//                    Intent intent = new Intent();
//                    if (!TextUtils.isEmpty(plateNo)) {
//                        intent.putExtra("carResult", plateNo);
//                    } else {
//                        intent.putExtra("carResult", "");
//                    }
//                    setResult(RESULT_OK, intent);
//                    finish();
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (alertDialog != null) {
            alertDialog.cancel();
            alertDialog.dismiss();
        }
        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }

        releaseCamera();//释放相机资源

        /************释放识别核心************/
        if (plApi != null) {
            plApi.ETUnInitPlateKernal();
            plApi = null;
        }
    }

    /************相机用完资源释放*************/
    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }


    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) {
            return null;
        }

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;
        int nminwh = (w > h ? h : w);
        int nthresh = nminwh >= 700 ? 700 : nminwh;
        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (size.height < nthresh) {
                continue;
            }
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) {
                continue;
            }
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (size.height < nthresh) {
                    continue;
                }
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                } else if (Math.abs(size.height - targetHeight) == minDiff) {
                    optimalSize = size;
                }
            }
        }
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                } else if (Math.abs(size.height - targetHeight) == minDiff) {
                    optimalSize = size;
                }
            }
        }
        return optimalSize;
    }

}


