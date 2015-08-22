package com.yonyou.sns.im.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.base.BaseActivity;
import com.yonyou.sns.im.core.YYIMSessionManager;
import com.yonyou.sns.im.ui.widget.GuideFragment;
import com.yonyou.sns.im.util.inject.InjectView;

/**
 * 入口页
 *
 * @author litfb
 * @date 2014年9月15日
 * @version 1.0
 */
public class SplashActivity extends BaseActivity implements OnClickListener {

	/** 向导Dialog */
	@InjectView(id = R.id.view_guide)
	private View guideView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {

				// 引导
				if (isShowGuide()) {
					showGuideFragment();
				} else {
					jump();
				}

			}
		}, 2000);
	}

	private void jump() {
		// 用户名
		String account = YYIMSessionManager.getInstance().getAccount();
		// 密码
		String password = YYIMSessionManager.getInstance().getPassword();
		if (!TextUtils.isEmpty(account) && !TextUtils.isEmpty(password) && password.length() == 32) {
			startActivity(new Intent(SplashActivity.this, MainActivity.class));
			finish();
		} else {
			startActivity(new Intent(SplashActivity.this, LoginActivity.class));
			overridePendingTransition(R.anim.anim_fade_in, R.anim.activity_hold);
			finish();
		}
	}

	private void showGuideFragment() {
		GuideFragment guideFragment = (GuideFragment) getSupportFragmentManager().findFragmentByTag(GuideFragment.TAG);
		if (guideFragment == null) {
			guideFragment = new GuideFragment();
			// 设fragment属性
			guideFragment.setActivity(this);
			guideFragment.setOnClickListener(SplashActivity.this);
		}

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.setCustomAnimations(R.anim.anim_fade_in, R.anim.anim_fade_out);
		ft.add(R.id.view_guide, guideFragment);
		ft.commit();
	}

	/**
	 * 是否显示引导页
	 * 
	 * @return
	 */
	private boolean isShowGuide() {
		// // 获得版本号
		// int codeVersion =
		// getBaseApplication().getPreference().getInt(YMConfigConstants.APP_VERSION,
		// 0);
		// if (codeVersion == 0 || codeVersion != CommonConstants.CODE_VERSION)
		// {
		// return true;
		// }
		return true;
	}

	@Override
	public void onClick(View v) {
		jump();
	}

}
