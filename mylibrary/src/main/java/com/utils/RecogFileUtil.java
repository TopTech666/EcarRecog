package com.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.util.Log;

import com.util.ImageUtils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

import static com.utils.Consts.platenum;


public class RecogFileUtil {
    private static final String TAG = RecogFileUtil.class.getSimpleName();
    private Context context;

    public RecogFileUtil(Context context) {
        this.context = context;
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * 保存二进制流到指定路径
     *
     * @param instream
     * @param filepath
     */
    public void saveFile(InputStream instream, String filepath) {
        if (!isExternalStorageWritable()) {
            Log.i(TAG, "SD卡不可用，保存失败");
            return;
        }

        File file = new File(filepath);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int cnt = 0;

            while ((cnt = instream.read(buffer)) != -1) {
                fos.write(buffer, 0, cnt);
            }

            instream.close();
            fos.close();

        } catch (FileNotFoundException e) {
            Log.i(TAG, e.getMessage());
        } catch (IOException e) {
            Log.i(TAG, e.getMessage());
        }
    }

    /**
     * Copy file
     *
     * @param from origin file path
     * @param to   target file path
     */
    public void copyFile(String from, String to) {
        if (!isExternalStorageWritable()) {
            Log.i(TAG, "SD卡不可用，保存失败");
            return;
        }

        File fileFrom = new File(from);
        File fileTo = new File(to);

        try {

            FileInputStream fis = new FileInputStream(fileFrom);
            FileOutputStream fos = new FileOutputStream(fileTo);
            byte[] buffer = new byte[1024];
            int cnt = 0;

            while ((cnt = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, cnt);
            }

            fis.close();
            fos.close();

        } catch (FileNotFoundException e) {
            Log.i(TAG, e.getMessage());
        } catch (IOException e) {
            Log.i(TAG, e.getMessage());
        }
    }

    /**
     * 保存 JSON 字符串到指定文件
     *
     * @param json
     * @param filepath
     */
    public boolean saveJson(String json, String filepath) {
        if (!isExternalStorageWritable()) {
            Log.i(TAG, "SD卡不可用，保存失败");
            return false;
        }

        File file = new File(filepath);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(json.getBytes());
            fos.close();

        } catch (FileNotFoundException e) {
            Log.i(TAG, e.getMessage());
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    /**
     * 删除指定的 JSON 文件
     *
     * @param filepath
     * @return
     */
    public boolean deleteJson(String filepath) {
        if (!isExternalStorageWritable()) {
            Log.i(TAG, "SD卡不可用，保存失败");
            return false;
        }

        File file = new File(filepath);

        if (file.exists()) {
            file.delete();
        }

        return false;
    }

    /**
     * 从指定文件读取 JSON 字符串
     *
     * @param filepath
     * @return
     */
    public String readJson(String filepath) {
        if (!isExternalStorageWritable()) {
            Log.i(TAG, "SD卡不可用，保存失败");
            return null;
        }

        File file = new File(filepath);
        StringBuilder sb = new StringBuilder();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(file)));
            String line = null;

            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            reader.close();

        } catch (FileNotFoundException e) {
            Log.i(TAG, e.getMessage());
        } catch (IOException e) {
            Log.i(TAG, e.getMessage());
        }

        return sb.toString();
    }

    /**
     * 保存图片到制定路径
     *
     * @param filepath
     * @param bitmap
     */
    public static void saveBitmap(String filepath, Bitmap bitmap) {
        if (TextUtils.isEmpty(filepath)) {
            Log.i(TAG, "SD卡不可用，保存失败");
            return;
        }

        if (bitmap == null) {
            return;
        }

        try {
            File file = new File(filepath);
            FileOutputStream outputstream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, outputstream);
            outputstream.flush();
            outputstream.close();
        } catch (FileNotFoundException e) {
            Log.i(TAG, e.getMessage());
        } catch (IOException e) {
            Log.i(TAG, e.getMessage());
        } catch (Exception e) {
            Log.i(TAG, e.getMessage());
        }
    }

    /**
     * 删除 Files 目录下所有的图片
     *
     * @return
     */
    public boolean cleanCache() {
        if (!isExternalStorageWritable()) {
            Log.i(TAG, "SD卡不可用，保存失败");
            return false;
        }

        File dir = context.getExternalFilesDir(null);

        if (dir != null) {
            for (File file : dir.listFiles()) {
                file.delete();
            }
        }

        return true;
    }

    /**
     * 计算 Files 目录下图片的大小
     *
     * @return
     */
    public String getCacheSize() {
        if (!isExternalStorageWritable()) {
            Log.i(TAG, "SD卡不可用，保存失败");
            return null;
        }

        long sum = 0;
        File dir = context.getExternalFilesDir(null);

        if (dir != null) {
            for (File file : dir.listFiles()) {
                sum += file.length();
            }
        }

        if (sum < 1024) {
            return sum + "字节";
        } else if (sum < 1024 * 1024) {
            return (sum / 1024) + "K";
        } else {
            return (sum / (1024 * 1024)) + "M";
        }
    }

    /**
     * 返回当前应用 SD 卡的绝对路径 like
     * /storage/sdcard0/Android/data/com.example.test/files
     */
    public String getAbsolutePath() {
        File root = context.getExternalFilesDir(null);

        if (root != null) {
            return root.getAbsolutePath();
        }

        return null;
    }

    /**
     * 返回当前应用的 SD卡缓存文件夹绝对路径 like
     * /storage/sdcard0/Android/data/com.example.test/cache
     */
    public String getCachePath() {
        File root = context.getExternalCacheDir();

        if (root != null) {
            return root.getAbsolutePath();
        }

        return null;
    }

    public boolean isBitmapExists(String filename) {
        File dir = context.getExternalFilesDir(null);
        File file = new File(dir, filename);

        return file.exists();
    }

    /**
     * 删除指定的 图片
     *
     * @param filepath
     * @return
     */
    public boolean deletepictur(String filepath) {
        if (!isExternalStorageWritable()) {
            return false;
        }

        File file = new File(filepath);

        if (file.exists()) {
            file.delete();
        }

        return false;
    }

    private static final File parentPath = Environment
            .getExternalStorageDirectory();
    private static String storagePath = "";
    private static final String DST_FOLDER_Sucess = "Take_Success";
    private static final String DST_FOLDER_Fail = "Take_Fail";

    /**
     * 初始化保存路径
     *
     * @return
     */
    public static String initPath(boolean isSuccess) {
        if (storagePath.equals("")) {
            storagePath = parentPath.getAbsolutePath() + "/PicDemo/" + (isSuccess ? DST_FOLDER_Sucess : DST_FOLDER_Fail)
                    + "/";
            File f = new File(storagePath);
            if (!f.exists()) {
                f.mkdirs();
            }
        }
        return storagePath;
    }

    /**
     * 初始化保存路径
     *
     * @return
     */
    public static String initPathFail() {
        if (storagePath.equals("")) {
            storagePath = parentPath.getAbsolutePath() + "/" + DST_FOLDER_Fail
                    + "/";
            File f = new File(storagePath);
            if (!f.exists()) {
                f.mkdirs();
            }
        }
        return storagePath;
    }

    public static boolean SaveImage(byte[] data, int width, int height,
                                    int index, String fileName, boolean isSucess) {

        // 1.采用NV21格式 YuvImage类进行保存 效率很高
        String dir = initPath(isSucess);

        File pictureFile = new File(dir + "/" + fileName + ".png");
        if (!pictureFile.exists()) {
            try {
                pictureFile.createNewFile();
                FileOutputStream filecon = new FileOutputStream(pictureFile);
                YuvImage image = new YuvImage(data, ImageFormat.NV21, width,
                        height, null);
                image.compressToJpeg(
                        new Rect(0, 0, image.getWidth(), image.getHeight()),
                        70, filecon); // 将NV21格式图片，以质量70压缩成Jpeg，并得到JPEG数据流
                filecon.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        return true;
    }

    /**
     * 保存Bitmap到sdcard
     *
     * @param b
     */
    public static boolean saveBitmap(Bitmap b, String fileName, boolean isSucess) {
        if (b == null) {
            return false;
        }
        String path = initPath(isSucess);
        String pngName = path + "/" + fileName + ".png";
        try {
            FileOutputStream fout = new FileOutputStream(pngName);
            BufferedOutputStream bos = new BufferedOutputStream(fout);
            b.compress(Bitmap.CompressFormat.PNG, 100, bos);
            bos.flush();
            bos.close();
            fout.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return true;
    }

    /**
     * @throws Exception
     * @功能：yuv转prg
     * @param：
     * @return：
     */
    public static int[] decodeYUV420SPrgb565(int[] rgb, byte[] yuv420sp,
                                             int width, int height) {
        final int frameSize = width * height;
        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0)
                    y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }
                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);
                if (r < 0)
                    r = 0;
                else if (r > 262143)
                    r = 262143;
                if (g < 0)
                    g = 0;
                else if (g > 262143)
                    g = 262143;
                if (b < 0)
                    b = 0;
                else if (b > 262143)
                    b = 262143;
                rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000)
                        | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
            }
        }
        return rgb;
    }

    /**
     * @throws Exception
     * @功能：保存图片
     * @param：quality：压缩率
     * @return：
     */
    public static boolean saveBitmap(byte[] yuv420, String fileName, int width,
                                     int height, int quality, boolean isSuccess) {
        boolean result = false;
        int[] bytes = new int[width * height];
        decodeYUV420SPrgb565(bytes, yuv420, width, height);
        Bitmap bitmap = Bitmap.createBitmap(bytes, width, height,
                Config.RGB_565);
        saveBitmap(bitmap, fileName, isSuccess);
        return result = true;
    }

    /*
     * 旋转图片
     *
     * @param angle
     *
     * @param bitmap
     *
     * @return Bitmap
     */
    public static Bitmap rotaingImageView(int angle, Bitmap bitmap) {
        // 旋转图片 动作
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
    }

    public static boolean SaveImage(byte[] data, int width, int height,
                                    int index, int nDegree) {

        int nImageWidth = width;
        int nImageHeight = height;

        String strPre = "C";

        // 1.采用NV21格式 YuvImage类进行保存 效率很高
        String fileName = strPre + nDegree + "_IMG_" + String.valueOf(index)
                + ".jpg";
        String fileDataName = strPre + nDegree + "_DATA_"
                + String.valueOf(index) + ".yuv";
        File sdRoot = Environment.getExternalStorageDirectory();
        String dir = "/photoss/";
        File mkDir = new File(sdRoot, dir);
        if (!mkDir.exists()) {
            mkDir.mkdirs();
        }

        File pictureFile = new File(sdRoot, dir + fileName);
        File dataFile = new File(sdRoot, dir + fileDataName);

        if (!pictureFile.exists()) {
            try {
                pictureFile.createNewFile();

                FileOutputStream filecon = new FileOutputStream(pictureFile);
                YuvImage image = new YuvImage(data, ImageFormat.NV21,
                        nImageWidth, nImageHeight, null);

                image.compressToJpeg(
                        new Rect(0, 0, image.getWidth(), image.getHeight()),
                        70, filecon); // 将NV21格式图片，以质量70压缩成Jpeg，并得到JPEG数据流

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!dataFile.exists()) {
            try {
                FileOutputStream fops = new FileOutputStream(dataFile);
                fops.write(data);
                fops.flush();
                fops.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    public static String input2Str(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] bs = new byte[1024];
        int i = 0;
        while ((i = in.read(bs)) != -1) {
            out.write(bs, 0, i);
        }
        in.close();
        out.close();

        byte[] bytesResult = out.toByteArray();
        String strResult = new String(bytesResult, "utf-8");
        return strResult;
    }

    public static String getCRC32(File mFile) {
        CRC32 crc32 = new CRC32();
        FileInputStream fileinputstream = null;
        CheckedInputStream checkedinputstream = null;
        String crc = null;
        try {
            fileinputstream = new FileInputStream(mFile);
            checkedinputstream = new CheckedInputStream(fileinputstream, crc32);
            byte[] buffer = new byte[4096];
            while (checkedinputstream.read(buffer) != -1) {
            }
            crc = String.valueOf(checkedinputstream.getChecksum().getValue());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileinputstream != null) {
                try {
                    fileinputstream.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
            if (checkedinputstream != null) {
                try {
                    checkedinputstream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return crc;
    }

    /**
     * 方法描述：读取文件内容
     * <p>
     *
     * @param
     * @return
     */
    public static String getString(String patch) {

        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(patch);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "";
        }
        InputStreamReader inputStreamReader = null;
        try {
            inputStreamReader = new InputStreamReader(inputStream, "gbk");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        BufferedReader reader = new BufferedReader(inputStreamReader);
        StringBuffer sb = new StringBuffer("");
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    /**
     * 方法描述：保存图片
     * <p>
     *
     * @param
     * @return
     */
    public static void writeModelFile(InputStream in, File path) throws IOException {
        int len = in.available();
        byte[] buffer = new byte[len];
        in.read(buffer);
        FileOutputStream fos = new FileOutputStream(path);
        fos.write(buffer);
        in.close();
        fos.flush();
        fos.close();
    }


    /**
     * 方法描述：保存bitmap
     * <p/>
     *
     * @param
     * @return
     */
    public static Bitmap saveBitmap() {
        if (Consts.orgdata == null) {
            return null;
        }
        Bitmap bitmap = null;
        try {
            bitmap = ImageUtils.GetBitmapFromYUV420SP(Consts.orgdata, Consts.orgw, Consts.orgh);

            if (TextUtils.isEmpty(Consts.platenum)) {
                Consts.platenum = "识别失败";
            }
            String fileName = strToDateLong(System.currentTimeMillis()) +
                    platenum.trim() + " " +
                    Consts.recogingDegger + "次 " +
                    Consts.speed + "秒" +
                    ".jpg";

            File imgFile = new File(Consts.IMAGGE_DIR, fileName);
            saveBitmap(imgFile.getPath(), bitmap);
            Consts.PIC_PATH = imgFile.getAbsolutePath();
//            if (bitmap != null && !bitmap.isRecycled()) {
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 40, new FileOutputStream(imgFile));
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 将长时间格式字符串转换为时间 yyyy-MM-dd HH:mm:ss
     *
     * @param strDate
     * @return
     */
    public static String strToDateLong(long strDate) {
        Date date = new Date(strDate);
        String strs = "";
        try {
//yyyy表示年MM表示月dd表示日
//yyyy-MM-dd是日期的格式，比如2015-12-12如果你要得到2015年12月12日就换成yyyy年MM月dd日
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日_HH时mm分ss秒_");
//进行格式化
            strs = sdf.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strs;
    }

    /****************************************
     * 方法描述：获取可用的sd卡
     *
     * @return
     ****************************************/
    public static String getSdPatch(Context activity) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().getAbsolutePath().toString();
        } else {
            String patch = null;
            if (TextUtils.isEmpty(patch = getCanUsePatch(activity))) {
                return activity.getCacheDir().getAbsolutePath();
            } else {
                return patch;
            }
        }
    }

    //获取当前可用的sd卡路径
    private static String getCanUsePatch(Context activity) {
        StorageManager mStorageManager = (StorageManager) activity
                .getSystemService(Activity.STORAGE_SERVICE);
        Method method = null;
        try {
            method = mStorageManager.getClass()
                    .getMethod("getVolumePaths");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        String[] paths = null;
        try {
            paths = (String[]) method.invoke(mStorageManager);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        if (paths == null || paths.length == 0) {
            return "";
        } else {
            for (int i = 0; i < paths.length; i++) {
                if (new File(paths[i]).canRead()) {
                    return paths[i];
                }
            }
        }
        return "";
    }


}
