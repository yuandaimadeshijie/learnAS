package com.yonyou.sns.im.ui.widget.recoredview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/**
 * 曲线绘制，通过振幅绘图
 * 传进来的振幅为声贝大小，范围为0~100
 * 
 * @author wudl
 * 
 */
public class WaveLine extends View {

	/** 振幅 */
	private float amplitude = 100;
	/** 每条线的间隔 */
	private int space = 10;
	/** 画笔 */
	private Paint paint = generatePaint();

	private static Paint generatePaint() {
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setStrokeWidth(3f);
		paint.setColor(Color.WHITE);
		paint.setAntiAlias(true);
		return paint;
	}

	public WaveLine(Context context) {
		super(context);
	}

	public WaveLine(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public float getAmplitude() {
		return amplitude;
	}

	public void setAmplitude(float amplitude) {
		this.amplitude = amplitude;
	}

	@Override
	protected void onDraw(Canvas c) {
		drawLines(c);
		super.onDraw(c);
	}

	/**
	 * 画一组抛物线线
	 * 
	 * @param c
	 */
	private void drawLines(Canvas c) {
		// 最高振幅
		float max = (float) (amplitude*3.5);
		// 设置线的间距
		int i=space;
		while (max > 0) {
			// 绘制向上的曲线
			drawWaveLine(c, true, max);
			// 绘制向下的曲线
			drawWaveLine(c, false, max);
			max = max -i;
			i+=i/10;
		}
		// 当声音为0时,画一条直线
		drawWaveLine(c, false, 0);
	}

	/**
	 * 画一条线
	 * 
	 * @param c
	 */
	private void drawWaveLine(Canvas c, boolean isTop, float amplitude) {
		// 获取坐标
		Rect rect = new Rect(0, 0, WaveLine.this.getWidth(), WaveLine.this.getHeight());
		// 曲线的路径
		Path path = new Path();
		// 抛物线的第一个点
		PointF firstPoint = new PointF(rect.left, (rect.top + rect.bottom) / 2);
		// 抛物线的第二个点
		PointF lastPoint = new PointF(rect.right, (rect.top + rect.bottom) / 2);
		// 中点
		PointF midTopPoint = CalculateTopPoint(firstPoint, lastPoint, amplitude, isTop);
		// 1/4直线中点
		PointF midPoint1 = CalculateTopPoint(firstPoint, midTopPoint, 0, isTop);
		midPoint1 = CalculateTopPoint(firstPoint, midPoint1, -amplitude / 5, isTop);
		// 3/4直线中点
		PointF midPoint2 = CalculateTopPoint(midTopPoint, lastPoint, 0, isTop);
		midPoint2 = CalculateTopPoint(midPoint2, lastPoint, -amplitude / 5, isTop);
		// 创建控制点
		PointF[] adjustedPoints = { firstPoint, midPoint1, midTopPoint, midPoint2, lastPoint };
		path = drawPath(path, adjustedPoints);
		// 绘制的贝塞尔曲线
		c.drawPath(path, paint);
	}

	/**
	 * 绘制路径
	 * 
	 * @param path
	 * @param adjustedPoints
	 * @return
	 */
	public Path drawPath(Path path, PointF adjustedPoints[]) {
		path.moveTo(adjustedPoints[0].x, adjustedPoints[0].y);
		int pointSize = adjustedPoints.length;

		for (int i = 0; i < adjustedPoints.length - 1; i++) {
			float pointX = (adjustedPoints[i].x + adjustedPoints[i + 1].x) / 2;
			float pointY = (adjustedPoints[i].y + adjustedPoints[i + 1].y) / 2;

			float controlX = adjustedPoints[i].x;
			float controlY = adjustedPoints[i].y;

			path.quadTo(controlX, controlY, pointX, pointY);
		}
		path.quadTo(adjustedPoints[pointSize - 1].x, adjustedPoints[pointSize - 1].y, adjustedPoints[pointSize - 1].x,
				adjustedPoints[pointSize - 1].y);
		return path;
	}

	/**
	 * 通过俩点和振幅计算抛物线顶点坐标 y-y1=k(x-x1)
	 * 
	 * @param point1
	 *            第一个点
	 * @param point2
	 *            第二个点
	 * @param amplitude
	 *            振幅
	 * @return
	 */
	private PointF CalculateTopPoint(PointF p1, PointF p2, float amplitude, boolean isTop) {
		// 计算俩点中点坐标
		float x = (p1.x + p2.x) / 2;
		float y = (p1.y + p2.y) / 2;
		// 计算俩点间的斜率
		float k;
		if (p2.y == p1.y)
			k = 0;
		else
			k = (p2.y - p1.y) / (p2.x - p1.x);
		// 计算抛物线顶点坐标的y值
		float topY;
		if (isTop) {
			topY = y + amplitude;
		} else {
			topY = y - amplitude;
		}
		// 通过中垂线计算 抛物线顶点坐标的x值
		float topX = x - k * (topY - y);
		// 初始化点
		PointF point = new PointF(topX, topY);
		return point;
	}

	@Override
	public void invalidate() {
		super.invalidate();
	}

}
