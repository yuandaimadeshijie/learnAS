package com.yonyou.sns.im.entity;

import java.util.Locale;

import android.text.TextUtils;

import com.yonyou.sns.im.core.YYIMChatManager;
import com.yonyou.sns.im.util.common.PinYinUtil;

public class YMLetterRoster {

	/** 首字母*/
	private Character firstLetter;
	/** 联系人*/
	private YMRoster roster;
	/** 联系人 名片*/
	private YMVCard rosterVcard;

	public YMLetterRoster(YMRoster roster) {
		// name
		String name = TextUtils.isEmpty(roster.getAlias()) ? roster.getUserName() : roster.getAlias();
		// 通过name获得拼音
		String convertName = PinYinUtil.converterFromPinYinSpell(name);
		// 首字母
		String letterStr = null;
		if (!TextUtils.isEmpty(convertName)) {
			letterStr = convertName.substring(0, 1);
		}
		if (TextUtils.isEmpty(letterStr) || !letterStr.matches("^[a-zA-Z]*")) {
			letterStr = "#";
		}
		this.roster = roster;
		this.rosterVcard = YYIMChatManager.getInstance().queryVCard(roster.getJid());
		this.firstLetter = letterStr.toUpperCase(Locale.getDefault()).charAt(0);
	}

	/**
	 * @return the firstLetter
	 */
	public Character getFirstLetter() {
		return firstLetter;
	}

	/**
	 * @param firstLetter the firstLetter to set
	 */
	public void setFirstLetter(Character firstLetter) {
		this.firstLetter = firstLetter;
	}

	/**
	 * @return the roster
	 */
	public YMRoster getRoster() {
		return roster;
	}

	/**
	 * @param roster the roster to set
	 */
	public void setRoster(YMRoster roster) {
		this.roster = roster;
	}

	/**
	 * @return
	 */
	public YMVCard getRosterVcard() {
		return rosterVcard;
	}

	/**
	 * @param rosterVcard the rosterVcard to set
	 */
	public void setRosterVcard(YMVCard rosterVcard) {
		this.rosterVcard = rosterVcard;
	}
}
