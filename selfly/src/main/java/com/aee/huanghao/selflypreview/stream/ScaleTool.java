package com.aee.huanghao.selflypreview.stream;

import android.graphics.Rect;

public class ScaleTool {
	public static Rect getScaledPosition(int frmW, int frmH, int wndW, int wndH) {
		int rectLeft = 0;
		int rectRigth = 0;
		int rectTop = 0;
		int rectBottom = 0;
		Rect rect = new Rect();

		//1280 720 2397 1440
//		Log.e("Aee", "frmW =="+frmW + "frmH =="+frmH+"wndW =="+wndW +"wndH =="+wndH );
//		Log.e("Aee", "wndW * frmH == "+wndW * frmH );
//		Log.e("Aee", "wndH * frmW == "+wndH * frmW);

		//1725840 1843200
		if (wndW * frmH < wndH * frmW) {
			// full filled with width
			rectLeft = 0;
			rectRigth = wndW;  //2397
			rectTop = (wndH - wndW * frmH / frmW) / 2; //46
			rectBottom = wndH - rectTop; //1394

//			Log.e("Aee", " rectTop =="+rectTop +"rectBottom =="+rectBottom );

		} else if (wndW * frmH > wndH * frmW) {
			// full filled with height
			rectLeft = (wndW - wndH * frmW / frmH) / 2;
			rectRigth = wndW - rectLeft;
			rectTop = 0;
			rectBottom = wndH;
		} else {
			// full filled with width and height
			rectLeft = 0;
			rectRigth = wndW;
			rectTop = 0;
			rectBottom = wndH;
		}
//0 46 2397 1394
		rect = new Rect(rectLeft, 0, wndW, wndH);
		return rect;
	}
}
