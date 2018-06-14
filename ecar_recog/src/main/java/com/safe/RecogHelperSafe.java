package com.safe;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.hardware.Camera;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.Helper.RecogResult;
import com.LPR;
import com.etop.plate.PlateAPI;
import com.utils.RecogEncryptionUtil;
import com.utils.RecogSpUtil;
import com.utils.CityCodeUtil;
import com.utils.RecogConsts;
import com.utils.RecogFileUtil;
import com.utils.StreamEmpowerFileUtils;
import com.utils.UserIdUtils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import static com.Helper.ComRecogHelper.isPic;

public class RecogHelperSafe {
    private static final int DEFAULT_SCOPE = 85; //合格分数
    protected static RecogHelperSafe recogHelper;
    public static Application mContext;
    boolean isEcarRecog;//是否是亿车识别

    public Random random;

    //isEcarRecog true 亿车识别  false 安荣识别
    public RecogHelperSafe(String cityName, boolean isEcarRecog,
                           int screenHeigth,
                           int screenWidth,
                           int preHeigth,
                           int preWidth) {
        init(cityName, isEcarRecog, screenHeigth, screenWidth, preHeigth, preWidth);
    }

    //isEcarRecog true 亿车识别  false 安荣识别
    public RecogHelperSafe(String cityName) {
        init(cityName, false, 0, 0, 0, 0);
    }


    //isInitConfig 是否初始化参数   相机页面一定要设为true否则无法识别
    //cityName  默认的第一个汉字 如：粤
    //screenHeigth  screenWidth  屏幕长款
    // preHeigth   preWidth  预览页面长款
    public static synchronized RecogHelperSafe getDefault(Application context,
                                                          boolean isInitConfig,
                                                          String cityName,
                                                          boolean isEcarRecog,
                                                          int screenHeigth,
                                                          int screenWidth,
                                                          int preHeigth,
                                                          int preWidth
    ) {
        synchronized (RecogHelperSafe.class) {
            if (isInitConfig)//初始化参数
                initConfig();
            mContext = (mContext == null ? context : mContext);
            return recogHelper == null ? recogHelper = new RecogHelperSafe(cityName,
                    isEcarRecog, screenHeigth, screenWidth, preHeigth, preWidth) : recogHelper;
        }
    }


    //初始化参数
    public static void initConfig() {
        RecogConsts.recogingDegger = 0;
        time = System.currentTimeMillis();
        bestScope = 0;
        tempCarnum = "";
    }

    //初始化
    public boolean init(String cityName,
                        boolean isEcarRecog,
                        int screenHeigth,
                        int screenWidth,
                        int preHeigth,
                        int preWidth) {
        this.isEcarRecog = isEcarRecog;
        spUtil = new RecogSpUtil(mContext, RecogConsts.SP_PERMITION);

        if (isEcarRecog) {
            return initEcarRecog(cityName);
        } else {
            return initAnrongRecog(screenHeigth, screenWidth, preHeigth, preWidth);
        }

    }

    PlateAPI plApi;

    //安荣识别
    private boolean initAnrongRecog(int screenHeigth,
                                    int screenWidth,
                                    int preHeigth,
                                    int preWidth) {


        random = new Random();
        //初始化识别算法
        String sdDir = RecogFileUtil.getSdPatch(mContext);
        if (sdDir == null) {
            Toast.makeText(mContext, "找不到存储路径", Toast.LENGTH_LONG).show();
            return false;
        }
        String ImgPath = sdDir + "/mTest/Img/";
        RecogConsts.IMAGGE_DIR = ImgPath;
        String modelPath = sdDir + "/mTest/data/";

        File f1 = new File(ImgPath);
        File f2 = new File(modelPath);
        if (!f1.exists()) {
            f1.mkdirs();
        }
        if (!f2.exists()) {
            f2.mkdirs();
        }
        boolean bInitKernal = true;


        if (plApi == null) {
            plApi = new PlateAPI();
            String cacheDir = (mContext.getExternalCacheDir()).getPath();
            String userIdPath = cacheDir + "/" + UserIdUtils.UserID + ".lic";
            TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            int nRet = plApi.ETInitPlateKernal("", userIdPath, UserIdUtils.UserID, 0x06, 0x02, telephonyManager, mContext);
            if (nRet != 0) {
                Toast.makeText(mContext, "激活失败", Toast.LENGTH_SHORT).show();
                System.out.print("nRet=" + nRet);
                bInitKernal = false;
            } else {
                System.out.print("nRet=" + nRet);
                bInitKernal = true;
            }
        }

        int[] m_ROI = {0, 0, 0, 0};
        int t;
        int b;
        int l;
        int r;
        l = screenHeigth / 5;
        r = screenHeigth * 3 / 5;
        t = 4;
        b = screenWidth - 4;
        double proportion = (double) screenHeigth / (double) preWidth;
        l = (int) (l / proportion);
        t = 0;
        r = (int) (r / proportion);
        b = preHeigth;
        int borders[] = {l, t, r, b};
        m_ROI[0] = l;
        m_ROI[1] = t;
        m_ROI[2] = r;
        m_ROI[3] = b;
        plApi.ETSetPlateROI(borders, preWidth, preHeigth);
        return bInitKernal;
    }

    //初始化亿车识别
    private boolean initEcarRecog(String cityName) {
        random = new Random();
        //初始化识别算法
        String sdDir = RecogFileUtil.getSdPatch(mContext);
        if (sdDir == null) {
            Toast.makeText(mContext, "找不到存储路径", Toast.LENGTH_LONG).show();
            return false;
        }
        String ImgPath = sdDir + "/mTest/Img/";
        RecogConsts.IMAGGE_DIR = ImgPath;
        String modelPath = sdDir + "/mTest/data/";

        File f1 = new File(ImgPath);
        File f2 = new File(modelPath);
        if (!f1.exists()) {
            f1.mkdirs();
        }
        if (!f2.exists()) {
            f2.mkdirs();
        }

        try {
            Log.d("tagutil", String.format("current time is in java : %d", System.currentTimeMillis()));
            LPR.init(modelPath.getBytes("GBK"), CityCodeUtil.getCityCode(cityName, mContext), mContext);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;

        }
        return true;
    }


    RecogSpUtil spUtil;

    //是否获取过权限
    public boolean isCheckPermition() {
        if (!RecogConsts.IS_CHECK_PERMITION) {
            savePermitionInfo(true);
        } else {
            savePermitionInfo(false);
        }
        return (boolean) spUtil.getData(RecogConsts.IS_GETEDPERMITION, Boolean.class, false);
    }

    //保存权限状态     flag：true  已获取
    public void savePermitionInfo(boolean flag) {
        if (spUtil == null) {
            spUtil = new RecogSpUtil(mContext, RecogConsts.SP_PERMITION);
        }
        spUtil.save(RecogConsts.IS_GETEDPERMITION, flag);
    }


    public static String tempCarnum = "";
    public static long time;  //开始识别时间
    public final static int MAX_DEGGER = 3;//最高前后比对次数
    public static final int MATCHING_LENG = 4; //匹配车牌长度


    public static int bestScope;//上一次的识别率

    public synchronized void getCarnum(final byte[] data, final Camera camera, final RecogResult recogToken) {
        getCarnum(data, camera.getParameters().getPreviewSize().width, camera.getParameters().getPreviewSize().height, recogToken);
    }

    public synchronized void getCarnum(final byte[] data, int width, int height, final RecogResult recogToken) {
//        if (!permitionCheck(recogToken)) {
//            return;
//        }
        if (isEcarRecog) {
            getByEcarPlate(data, width, height, recogToken);
        } else {
            getByAnronPlate(data, width, height, recogToken);
        }
    }

    private void getByAnronPlate(byte[] data, int width, int height, RecogResult recogToken) {


        int buffl = 256;
        char recogval[] = new char[buffl];
        synchronized (recogHelper.getClass()) {
            if (data != null) {
                RecogConsts.orgdata = data;
                //加密

                String platenum = "";
                try {
                    int pLineWarp[] = new int[800 * 45];
                    int result = plApi.RecognizePlateNV21(data, 1, width, height, recogval, buffl, pLineWarp);
                    if (result == 0) {
                        // 震动
                        platenum = plApi.GetRecogResult(0);
                    }
                    if (!TextUtils.isEmpty(platenum))
                        Log.d(this.getClass().getSimpleName(), "***************** plate=" + platenum);
                } catch (Exception e) {
                    e.printStackTrace();
                }


//                Log.d("number1", TextUtils.isEmpty(platenum) ? "返回空  " : platenum + "正确率="+scope);

                //第一次不往下走
//                if (!isPic
//                        && TextUtils.isEmpty(tempCarnum.trim())
//                        && isNumber(platenum)) {
//                    tempCarnum = platenum.trim();
//                }
//
                boolean getedSuccess;//车牌是否获取成功
                getedSuccess = isNumber(platenum);

                if (getedSuccess) {
                    RecogConsts.orgw = width;
                    RecogConsts.orgh = height;
                    RecogConsts.speed = (System.currentTimeMillis() - time) / 1000.0f;
                    time = System.currentTimeMillis();
                    RecogConsts.platenum = platenum;
                    recogToken.recogSuccess(platenum, data);
                    tempCarnum = "";
                } else {
                    RecogConsts.orgdata = null;
                    RecogConsts.orgw = 0;
                    RecogConsts.orgh = 0;
                    recogToken.recogFail();
                    tempCarnum = isNumber(platenum) ? platenum : tempCarnum;//初始化中间车牌
                }
            }
        }
    }

    private void getByEcarPlate(byte[] data, int width, int height, RecogResult recogToken) {
        synchronized (recogHelper.getClass()) {
            if (data != null) {
                RecogConsts.orgdata = data;
                //加密
                int location = random.nextInt(RecogEncryptionUtil.getLocation(RecogEncryptionUtil.MYRECOG_SCOPE));
                if (data.length > location) {
                    RecogEncryptionUtil.setPoint(data, location);
                } else {
                    recogToken.recogFail();
                    return;
                }

                String platenum = "";
                int scope = 0;
                try {
                    LPR.locate(width, height, data); //0-255
                    platenum = new String(LPR.getplate(0), "GBK").trim();
                    scope = LPR.getplatescore(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }


//                Log.d("number1", TextUtils.isEmpty(platenum) ? "返回空  " : platenum + "正确率="+scope);

                //第一次不往下走
                if (!isPic
                        && TextUtils.isEmpty(tempCarnum.trim())
                        && isNumber(platenum)) {
                    tempCarnum = platenum.trim();
                    if (scope < 80) {
                        recogToken.recogFail();
                        Log.d("number2", "第一次获取 tempnum=" + tempCarnum);
                        return;
                    }

                }

                boolean getedSuccess;//车牌是否获取成功
                if (isPic) {
                    getedSuccess = isNumber(platenum);
                } else {
                    getedSuccess = isNumber(platenum) && (isScopeOk(scope) || tempCarnum.equals(platenum));
                    Log.d("number2", getedSuccess ? platenum : "返回空" + "\nplatenum=" + platenum + "\ntempnum=" + tempCarnum);
                }

                if (getedSuccess) {
                    RecogConsts.orgw = width;
                    RecogConsts.orgh = height;
                    RecogConsts.speed = (System.currentTimeMillis() - time) / 1000.0f;
                    time = System.currentTimeMillis();
                    RecogConsts.platenum = platenum;
                    recogToken.recogSuccess(platenum, data);
                    tempCarnum = "";
                } else {
                    RecogConsts.orgdata = null;
                    RecogConsts.orgw = 0;
                    RecogConsts.orgh = 0;
                    recogToken.recogFail();
                    tempCarnum = isNumber(platenum) ? platenum : tempCarnum;//初始化中间车牌
                }
            }
        }
    }


    /****************************************
     方法描述：使用时间限制来获取车牌
     @param  maxTime 识别时长 单位秒
     @return
     ****************************************/

    public synchronized void getCarnumByTime(final byte[] data, final Camera camera, final RecogResult recogToken, int maxTime) {
//        if (!permitionCheck(recogToken)) {
//            return;
//        }
        synchronized (recogHelper.getClass()) {

            if (data != null) {
                RecogConsts.orgdata = data;
                //加密
                int width = camera.getParameters().getPreviewSize().width;
                int height = camera.getParameters().getPreviewSize().height;
                int location = random.nextInt(RecogEncryptionUtil.getLocation(RecogEncryptionUtil.MYRECOG_SCOPE));
                if (data.length > location) {
                    RecogEncryptionUtil.setPoint(data, location);
                } else {
                    recogToken.recogFail();
                    return;
                }

                String platenum = "";
                try {
                    LPR.locate(width, height, data); //0-255
                    platenum = new String(LPR.getplate(0), "GBK").trim();

                } catch (Exception e) {
                    e.printStackTrace();
                }


//                Log.d("number1", TextUtils.isEmpty(platenum) ? "返回空  " : platenum + "正确率="+scope);

                //第一次不往下走
                if (!isPic
                        && TextUtils.isEmpty(tempCarnum.trim())
                        && isNumber(platenum)) {
                    tempCarnum = platenum.trim();
                    recogToken.recogFail();
                    Log.d("number2", "第一次获取 tempnum=" + tempCarnum);
                    return;
                }

                boolean getedSuccess;//车牌是否获取成功
                if (isPic) {
                    getedSuccess = isNumber(platenum);
                } else {
                    getedSuccess = ((isNumber(platenum) && tempCarnum.equals(platenum)))
                            || (!TextUtils.isEmpty(platenum) && (System.currentTimeMillis() - time) / 1000 >= maxTime);   //超过时间限制
                    Log.d("number2", getedSuccess ? platenum : "返回空" + "\nplatenum=" + platenum + "\ntempnum=" + tempCarnum);
                }

                if (getedSuccess) {
                    RecogConsts.orgw = width;
                    RecogConsts.orgh = height;
                    RecogConsts.speed = (System.currentTimeMillis() - time) / 1000.0f;
                    time = System.currentTimeMillis();
                    RecogConsts.platenum = platenum;
                    recogToken.recogSuccess(platenum, data);
                    tempCarnum = "";

                } else {
                    RecogConsts.orgdata = null;
                    RecogConsts.orgw = 0;
                    RecogConsts.orgh = 0;
                    recogToken.recogFail();
                    tempCarnum = isNumber(platenum) ? platenum : tempCarnum;//初始化中间车牌
                    Log.d("number3", "匹配" + (!TextUtils.isEmpty(platenum) ? platenum.substring(0, MATCHING_LENG) : "") + "  " + tempCarnum);
                }
            }
        }
    }

    //判断是否是车牌
    public boolean isNumber(String platenum) {
        return !TextUtils.isEmpty(platenum) && platenum.trim().length() > 3;
    }

    //销毁
    public void destory() {
        if (plApi != null)
            plApi.ETUnInitPlateKernal();

    }

    //成绩是否达标
    public boolean isScopeOk(int scope) {
        return scope > DEFAULT_SCOPE;
    }

    public Camera.Parameters offFlash(Camera.Parameters parameters) {

        if (parameters != null) {
            if (parameters == null) return parameters;
            String flashMode = parameters.getFlashMode();
            if (flashMode.equals(Camera.Parameters.FLASH_MODE_TORCH)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                parameters.setExposureCompensation(0);
            }
        }
        return parameters;
    }

    public Camera.Parameters openFlash(Camera.Parameters parameters) {
        if (parameters != null) {
            String flashMode = parameters.getFlashMode();
            if (!flashMode.equals(Camera.Parameters.FLASH_MODE_TORCH)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                parameters.setExposureCompensation(0);
            }
        }
        return parameters;
    }

    /**
     * 方法描述：检查权限
     * <p/>
     *
     * @param
     * @return
     */
    @SuppressLint("MissingPermission")
    public boolean permitionCheck(RecogResult recogToken) {
        if (RecogConsts.IS_CHECK_PERMITION) {
            //从sp文件确认
            //文件比对
            if ((boolean) spUtil.getData(RecogConsts.IS_GETEDPERMITION, Boolean.class, false)) {  //获取过权限则不验证
                recogToken.permitionSuccess();
                RecogConsts.IS_CHECK_PERMITION = false;
                return true;
            }
            String sdDir = RecogFileUtil.getSdPatch(mContext);
            TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Service.TELEPHONY_SERVICE);
            try {
                String imeiSha = RecogEncryptionUtil.getSHA(tm.getDeviceId());
                String fileSha = RecogFileUtil.getString(sdDir + RecogConsts.SPATH);
                if (TextUtils.isEmpty(imeiSha) || TextUtils.isEmpty(fileSha) || !imeiSha.equals(fileSha)) {
                    recogToken.permitionFail();
                    savePermitionInfo(false);
                } else {
                    recogToken.permitionSuccess();
                    savePermitionInfo(true);
                }
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                recogToken.permitionFail();
                savePermitionInfo(false);
            }

        } else {
            recogToken.permitionSuccess();
            return true;
        }
        return false;
    }

    public PlateAPI getApi() {
        return plApi;
    }
}
