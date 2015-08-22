package com.yonyou.sns.im.ui.widget.recoredview;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.UUID;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.log.YMLogger;
import com.yonyou.sns.im.util.common.TimeUtil;
import com.yonyou.sns.im.util.common.YMStorageUtil;

/**
 * 波浪录音组件
 * 使用方法：
 * waveRecoredFrame.run()启动
 * waveRecoredFrame.release()回收
 * RecoredListener 可以注册自己的代码，在录音成功和取消录音时写入自己的代码
 * @author wudl
 * @date 2014年12月16日
 * @version V1.0
 */
public class WaveRecoredFrame extends FrameLayout implements OnClickListener,OnTouchListener {

	/** 更新分贝曲线 */
	public static final int UPDATE_DB_LINE = 0;
	/** context*/
	private Context context;
	/** 录音frame */
	private View mRecordFrame;
	/** 录音按钮 */
	private ImageButton mRecordButton;
	/** 录音按钮边框 */
	private ImageView mRecordButtonBorder;
	/** 录音text */
	private TextView mRecordText;
	/** 隐藏按钮 */
	private ImageButton mRecordDismiss;
	/** 录音线 */
	private WaveLine mRecordAnim;
	/** 录音时间 */
	private TextView mRecordTime;
	/** 开始录音时间 */
	private long startRecordTime = 0;
	/** 去除声音噪声的基准值 */
	private int BASE = 600;
	/** 取样时间的间隔 */
	private int SPACE = 100;
	/** 录音*/
	private MediaRecorder mRecorder;
	/** 录音的存储目录*/
	private String audioFilePath;
	/** 当前录音存储的路径*/
	private String currentAudioPath;
	/** 录音监听*/
	private RecoredListener listener;
	/** handler*/
	private WaveHandler handler = new WaveHandler(this);
	/** 获取分贝刷新ui线程 */
	private Runnable mUpdateMicStatusTimer = new Runnable() {

		public void run() {
			updateMicStatus();
		}
	};

	public WaveRecoredFrame(Context context) {
		super(context);
		this.context = context;
		init();
	}

	public WaveRecoredFrame(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		init();
	}

	/**
	 * 初始化组件
	 */
	private void init() {
		// 根视图
		View root = LayoutInflater.from(context).inflate(R.layout.wave_recored_view, this);
		initRecordFrame(root);
	}

	/**
	 * 初始化界面控件
	 * @param root
	 */
	private void initRecordFrame(View root) {
		// 录音相关
		mRecordFrame = root.findViewById(R.id.chat_record_frame);
		mRecordButton = (ImageButton) root.findViewById(R.id.chat_record_frame_btn);
		mRecordButtonBorder = (ImageView) root.findViewById(R.id.chat_record_frame_btnborder);
		mRecordText = (TextView) root.findViewById(R.id.chat_record_frame_btntext);
		mRecordAnim = (WaveLine) root.findViewById(R.id.chat_record_frame_anim);
		mRecordTime = (TextView) root.findViewById(R.id.chat_record_frame_time);
		mRecordDismiss = (ImageButton) root.findViewById(R.id.chat_record_frame_dismiss_btn);
		// 设置录音点击事件
		mRecordButton.setOnClickListener(this);
		// 点击录音取消按钮监听
		mRecordDismiss.setOnClickListener(this);
		mRecordFrame.setOnTouchListener(this);
	}

	/** 开始录音 */
	private void startVoice() {
		// 没在录音的时候启动录音
		if (mRecorder == null) {
			mRecordText.setText(getResources().getString(R.string.chat_record_button_working));
			// 显示旋转按钮
			mRecordButtonBorder.setVisibility(View.VISIBLE);
			// 录音动画
			Animation anim = AnimationUtils.loadAnimation(context, R.anim.record_button_border);
			// 匀速效果
			LinearInterpolator lir = new LinearInterpolator();
			anim.setInterpolator(lir);
			mRecordButtonBorder.startAnimation(anim);
			// 设置录音保存路径
			if (TextUtils.isEmpty(audioFilePath)) {
				currentAudioPath = YMStorageUtil.getAudioPath(context) + File.separator + UUID.randomUUID().toString()
						+ ".amr";
			} else {
				currentAudioPath = audioFilePath + File.separator + UUID.randomUUID().toString() + ".amr";
			}
			try {
				mRecorder = new MediaRecorder();
				mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
				mRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
				mRecorder.setOutputFile(currentAudioPath);
				mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
				mRecorder.setAudioEncodingBitRate(16);
				mRecorder.setAudioSamplingRate(4000);
				mRecorder.prepare();
			} catch (Exception e) {
				YMLogger.e("MediaRecorder run failed!");
			}
			mRecorder.start();
			// 获取录音开始时间
			startRecordTime = System.currentTimeMillis();
			// 监听音量大小
			updateMicStatus();
		}
	}

	/** 停止录音 */
	private void stopVoice() {
		if (mRecorder != null) {
			// 停止录音并释放
			mRecorder.stop();
			mRecorder.release();
			mRecorder = null;
		}
		// 隐藏frame
		mRecordFrame.setVisibility(View.GONE);
		// 移除计时线程
		if (mUpdateMicStatusTimer != null) {
			handler.removeCallbacks(mUpdateMicStatusTimer);
		}
	}

	/**
	 * 更新话筒状态 分贝是相对响度 分贝的计算公式K=20lg(Vo/Vi)
	 */
	private void updateMicStatus() {
		if (mRecorder != null) {
			int ratio = mRecorder.getMaxAmplitude() / BASE;
			// 分贝
			int db = 0;
			if (ratio > 1)
				db = (int) (20 * Math.log10(ratio));
			handler.obtainMessage(UPDATE_DB_LINE, db).sendToTarget();
			// 每间隔0.1秒采集一次数据
			handler.postDelayed(mUpdateMicStatusTimer, SPACE);
		}
	}

	/**
	 * 设置录音存储位置
	 * @param audioFilePath
	 */
	public void setAudioFilePath(String audioFilePath) {
		this.audioFilePath = audioFilePath;
	}

	/**
	 * 设置监听
	 * @param listener
	 */
	public void setListener(RecoredListener listener) {
		this.listener = listener;
	}

	/**
	 * 获取当前存储的录音路径
	 * @return
	 */
	public String getCurrentAudioPath() {
		return currentAudioPath;
	}

	/**
	 * 释放资源
	 */
	public void release() {
		stopVoice();
	}

	/**
	 * 运行
	 */
	public void run() {
		mRecordFrame.setVisibility(View.VISIBLE);
		startVoice();
	}

	/**
	 * 主线程处理
	 * @author wudl
	 * @date 2014年12月16日
	 * @version V1.0
	 */
	private static class WaveHandler extends Handler {

		/** 若引用*/
		WeakReference<WaveRecoredFrame> reference;

		public WaveHandler(WaveRecoredFrame frame) {
			reference = new WeakReference<WaveRecoredFrame>(frame);
		}

		public WaveRecoredFrame getFrame() {
			return reference.get();
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case UPDATE_DB_LINE:
				// 更新线
				getFrame().mRecordAnim.setAmplitude((int) msg.obj *100/35);
				// 重绘
				getFrame().mRecordAnim.invalidate();
				// 更新时间
				long time = System.currentTimeMillis();
				getFrame().mRecordTime.setText(TimeUtil.getMinAndSec(time - getFrame().startRecordTime));
				break;
			default:
				break;
			}

		}
	}

	/**
	 * 录音监听
	 * @author wudl
	 * @date 2014年12月16日
	 * @version V1.0
	 */
	public interface RecoredListener {

		/**
		 * 成功录音
		 */
		public void recoredSuccess();

		/**
		 * 录音失败
		 */
		public void recoredCancel();
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.chat_record_frame_btn) {
			// 录音成功
			stopVoice();
			if (listener != null) {
				listener.recoredSuccess();
			}
		} else if (v.getId() == R.id.chat_record_frame_dismiss_btn) {
			// 取消录音
			stopVoice();
			if (listener != null) {
				listener.recoredCancel();
			}
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// 让其他层获取不到焦点
		return true;
	}
}
