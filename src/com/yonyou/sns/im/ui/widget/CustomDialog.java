package com.yonyou.sns.im.ui.widget;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.util.common.DensityUtils;

public class CustomDialog extends Dialog {

	public CustomDialog(Activity context, int theme) {
		super(context, theme);
	}

	public CustomDialog(Activity context) {
		super(context);
	}

	/**
	 * Helper class for creating a custom dialog
	 */
	public static class Builder {

		private Activity context;
		private String title;
		private String message;
		private String input;
		private String positiveButtonText;
		private String negativeButtonText;
		private View contentView;

		private DialogInterface.OnClickListener positiveButtonClickListener, negativeButtonClickListener;

		public Builder(Activity context) {
			this.context = context;
		}

		/**
		 * 获取消息
		 * @return
		 */
		public String getInput() {
			return this.input;
		}

		/**
		 * Set the Dialog message from String
		 * 
		 * @param title
		 * @return
		 */
		public Builder setMessage(String message) {
			this.message = message;
			return this;
		}

		/**
		 * Set the Dialog message from resource
		 * 
		 * @param title
		 * @return
		 */
		public Builder setMessage(int message) {
			this.message = (String) context.getText(message);
			return this;
		}

		/**
		 * Set the Dialog title from resource
		 * 
		 * @param title
		 * @return
		 */
		public Builder setTitle(int title) {
			this.title = (String) context.getText(title);
			return this;
		}

		/**
		 * Set the Dialog title from String
		 * 
		 * @param title
		 * @return
		 */
		public Builder setTitle(String title) {
			this.title = title;
			return this;
		}

		/**
		 * Set a custom content view for the Dialog. If a message is set, the
		 * contentView is not added to the Dialog...
		 * 
		 * @param v
		 * @return
		 */
		public Builder setView(View v) {
			this.contentView = v;
			return this;
		}

		/**
		 * Set the positive button resource and it"s listener
		 * 
		 * @param positiveButtonText
		 * @param listener
		 * @return
		 */
		public Builder setPositiveButton(int positiveButtonText, DialogInterface.OnClickListener listener) {
			this.positiveButtonText = (String) context.getText(positiveButtonText);
			this.positiveButtonClickListener = listener;
			return this;
		}

		/**
		 * Set the positive button text and it"s listener
		 * 
		 * @param positiveButtonText
		 * @param listener
		 * @return
		 */
		public Builder setPositiveButton(String positiveButtonText, DialogInterface.OnClickListener listener) {
			this.positiveButtonText = positiveButtonText;
			this.positiveButtonClickListener = listener;
			return this;
		}

		/**
		 * Set the negative button resource and it"s listener
		 * 
		 * @param negativeButtonText
		 * @param listener
		 * @return
		 */
		public Builder setNegativeButton(int negativeButtonText, DialogInterface.OnClickListener listener) {
			this.negativeButtonText = (String) context.getText(negativeButtonText);
			this.negativeButtonClickListener = listener;
			return this;
		}

		/**
		 * Set the negative button text and it"s listener
		 * 
		 * @param negativeButtonText
		 * @param listener
		 * @return
		 */
		public Builder setNegativeButton(String negativeButtonText, DialogInterface.OnClickListener listener) {
			this.negativeButtonText = negativeButtonText;
			this.negativeButtonClickListener = listener;
			return this;
		}

		/**
		 * Create the custom dialog
		 */
		public CustomDialog create() {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			// instantiate the dialog with the custom Theme
			final CustomDialog dialog = new CustomDialog(context, R.style.DialogStyle);
			View layout = inflater.inflate(R.layout.custom_dialog, null);
			dialog.addContentView(layout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			// set the dialog title
			((TextView) layout.findViewById(R.id.dialog_title)).setText(title);
			// set the confirm button
			if (positiveButtonText != null) {
				((TextView) layout.findViewById(R.id.dialog_right_btn)).setText(positiveButtonText);
				if (positiveButtonClickListener != null) {
					((TextView) layout.findViewById(R.id.dialog_right_btn))
							.setOnClickListener(new View.OnClickListener() {

								public void onClick(View v) {
									positiveButtonClickListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
									dialog.dismiss();
								}
							});
				}
			} else {
				// if no confirm button just set the visibility to GONE
				layout.findViewById(R.id.dialog_right_btn).setVisibility(View.GONE);
			}
			// set the cancel button
			if (negativeButtonText != null) {
				((TextView) layout.findViewById(R.id.dialog_left_btn)).setText(negativeButtonText);
				if (negativeButtonClickListener != null) {
					((TextView) layout.findViewById(R.id.dialog_left_btn))
							.setOnClickListener(new View.OnClickListener() {

								public void onClick(View v) {
									negativeButtonClickListener.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
									dialog.dismiss();
								}
							});
				}
			} else {
				// if no confirm button just set the visibility to GONE
				layout.findViewById(R.id.dialog_left_btn).setVisibility(View.GONE);
			}
			// set the content message
			if (message != null) {
				((TextView) layout.findViewById(R.id.dialog_text)).setText(message);
			} else if (contentView != null) {
				// if no message set
				// add the contentView to the dialog body
				layout.findViewById(R.id.dialog_text).setVisibility(View.GONE);
				((LinearLayout) layout.findViewById(R.id.content_view_root)).removeAllViews();
				((LinearLayout) layout.findViewById(R.id.content_view_root)).addView(contentView, new LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			}
			dialog.setContentView(layout);
			int screenW = DensityUtils.getScreenWidth(context);
			Window window = dialog.getWindow();
			WindowManager.LayoutParams lp = window.getAttributes();
			lp.width = (int) (0.8 * screenW);
			return dialog;
		}
		/**
		 * 创建带输入框的弹出框
		 * 
		 * @return
		 */
		public Builder setEditView(LayoutInflater inflater, String text) {
			View contenView = inflater.inflate(R.layout.common_alert_dialog_conten, null);
			final EditText input = (EditText) contenView.findViewById(R.id.editText);
			final ImageView deleteBtn = (ImageView) contenView.findViewById(R.id.edit_delete_btn);
			// 设置输入监听
			input.addTextChangedListener(new TextWatcher() {

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					Builder.this.input = input.getText().toString();
					if (Builder.this.input.length() > 0) {
						deleteBtn.setVisibility(View.VISIBLE);
					} else {
						deleteBtn.setVisibility(View.GONE);
					}
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {

				}

				@Override
				public void afterTextChanged(Editable s) {
				}
			});
			// 设置内容并移动光标
			input.setText(text);
			input.setSelection(text.length());
			// 设置点击删除监听
			deleteBtn.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// 删除输入框内容
					input.setText("");
				}
			});
			return this.setView(contenView);
		}
	}

}
