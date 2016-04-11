package com.example.kakalauncher2.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

public class GuideViewPager extends ViewPager {

	private Bitmap bg;
	private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	
	public GuideViewPager(Context context) {
		super(context);
	}

	public GuideViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		if(this.bg != null) {
			int bgWidth = this.bg.getWidth();
			int bgHeight = this.bg.getHeight();
			
			int count = this.getAdapter().getCount();
			
			int subWidth = bgHeight * getWidth() / getHeight(); //bg在子View上显示的实际宽度
			int scrollX = this.getScrollX();  //手指滑动相对于屏幕上X的offset像数
			
			int bgOffset = scrollX * ((bgWidth - subWidth) / (count- 1)) / getWidth(); //手指滑动相对于bg上X的offset像数
			
			canvas.drawBitmap(this.bg, new Rect(bgOffset, 0, bgOffset+subWidth, bgHeight), 
					new Rect(scrollX, 0, scrollX+getWidth(), getHeight()), paint);
		} 
		super.dispatchDraw(canvas);
	}

	public void setBackground(Bitmap bitmap) {
		this.bg = bitmap;
		this.paint.setFilterBitmap(true); //抗锯齿
	}

}
