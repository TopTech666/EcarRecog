package com;

import android.content.Context;

public class LPR {
	static {
		System.loadLibrary("LPR");
	}

	//城市cityCode code
	public static native boolean init(byte[] path, int cityCode, Context context);
	//识别  wd 摄像头识别区的宽度  ht 摄像头识别区的高度     imgData 图片的data值
	public static native int[] locate(int wd, int ht, byte[] imgData);
	//获取车牌号
	public static native byte[] getplate(int index);
	//可信度
	public static native int getplatescore(int index);


	
}
