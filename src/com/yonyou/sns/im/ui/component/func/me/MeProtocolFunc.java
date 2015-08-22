package com.yonyou.sns.im.ui.component.func.me;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout.LayoutParams;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.activity.ChatActivity;
import com.yonyou.sns.im.core.YYIMSessionManager;
import com.yonyou.sns.im.ui.component.func.BaseFunc;

/**
 * 与web端通信
 * @author wudl
 *
 */
public class MeProtocolFunc extends BaseFunc {

	public MeProtocolFunc(Activity activity) {
		super(activity);
	}

	@Override
	public int getFuncId() {
		return R.id.me_func_protocal;
	}

	@Override
	public int getFuncIcon() {
		return R.drawable.my_computer;
	}

	@Override
	public int getFuncName() {
		return R.string.me_func_protocal;
	}

	@Override
	public void onclick() {
		Intent intent = new Intent(getActivity(), ChatActivity.class);
		String currentUserJid = YYIMSessionManager.getInstance().getUserJid();
		String targetResouse = getActivity().getResources().getString(R.string.web_jid_resouse);
		intent.putExtra(ChatActivity.EXTRA_ROOM_JID, currentUserJid + "/" + targetResouse);
		getActivity().startActivity(intent);
	}

	@Override
	public View initFuncView(boolean isSeparator, Object... params) {
		View funcView = super.initFuncView(isSeparator, params);
		LayoutParams params2 = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params2.bottomMargin = (int) getActivity().getResources().getDimension(R.dimen.me_vertical_margin);
		funcView.setLayoutParams(params2);
		return funcView;
	}
}
