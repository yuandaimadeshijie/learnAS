package com.yonyou.sns.im.util;

import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.widget.ImageView;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.entity.message.YMMessage;
import com.yonyou.sns.im.log.YMLogger;

public class RecordManager {

	private static final String TAG = "RecordManager";

	private static RecordManager instance = new RecordManager();

	private RecordManager() {

	}

	public static synchronized RecordManager getInstance() {
		return instance;
	}

	/** 用于语音播放 */
	private MediaPlayer mPlayer = new MediaPlayer();

	private ImageView image;

	private AnimationDrawable drawable;

	private int direction;

	public void play(String path, ImageView image, int direction) {
		if (mPlayer.isPlaying()) {
			mPlayer.stop();
			stopDrawable();
		}

		this.image = image;
		this.direction = direction;
		// 设置录音动画
		this.image.setImageResource(direction == YMMessage.DIRECTION_SEND ? R.drawable.audio_animation_play_right
				: R.drawable.audio_animation_play_left);
		this.drawable = (AnimationDrawable) image.getDrawable();

		try {
			// 启动录音动画
			startDrawable();
			mPlayer.reset();
			mPlayer.setDataSource(path);
			mPlayer.prepare();
			mPlayer.start();

			mPlayer.setOnCompletionListener(new OnCompletionListener() {

				@Override
				public void onCompletion(MediaPlayer mp) {
					stopDrawable();
				}

			});
		} catch (Exception e) {
			YMLogger.e(TAG, "play faild!", e);
			stopDrawable();
		}
	}

	/**
	 * 停止录音
	 */
	public void stop() {
		mPlayer.stop();
	}

	private void startDrawable() {
		this.image.setImageResource(direction == YMMessage.DIRECTION_SEND ? R.drawable.audio_animation_play_right
				: R.drawable.audio_animation_play_left);
		this.drawable.start();
	}

	private void stopDrawable() {
		this.drawable.stop();
		this.image.setImageResource(direction == YMMessage.DIRECTION_SEND ? R.drawable.im_audio_play_right_3
				: R.drawable.im_audio_play_left_3);
	}

}
