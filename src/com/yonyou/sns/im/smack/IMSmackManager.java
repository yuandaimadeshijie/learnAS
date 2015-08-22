package com.yonyou.sns.im.smack;

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.search.ReportedData;
import org.jivesoftware.smackx.search.ReportedData.Row;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.jivesoftware.smackx.xdata.Form;

import android.database.Cursor;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.core.XMPPManager;
import com.yonyou.sns.im.core.YYIMCallBack;
import com.yonyou.sns.im.core.YYIMChat;
import com.yonyou.sns.im.core.YYIMChatManager;
import com.yonyou.sns.im.core.YYIMDBHandler;
import com.yonyou.sns.im.core.YYIMRosterManager;
import com.yonyou.sns.im.core.YYIMSessionManager;
import com.yonyou.sns.im.db.OrgStructProvider;
import com.yonyou.sns.im.entity.SearchValue;
import com.yonyou.sns.im.entity.YMOrgStruct;
import com.yonyou.sns.im.entity.YMRoom;
import com.yonyou.sns.im.entity.YMRoster;
import com.yonyou.sns.im.entity.message.YMMessage;
import com.yonyou.sns.im.exception.YMErrorConsts;
import com.yonyou.sns.im.exception.YMException;
import com.yonyou.sns.im.log.YMLogger;
import com.yonyou.sns.im.smack.packet.SnsOrgStructPacket;
import com.yonyou.sns.im.smack.provider.SnsOrgStructProvider;

public class IMSmackManager {

	private static IMSmackManager instance = new IMSmackManager();

	private IMSmackManager() {
		// 注册数据表
		YYIMDBHandler.getInstance().addTable(OrgStructProvider.AUTHORITY, new OrgStructProvider());
	}

	public synchronized static IMSmackManager getInstance() {
		return instance;
	}

	public void init() {
		// 组织结构
		ProviderManager.addIQProvider("query", "http://jabber.org/protocol/org", new SnsOrgStructProvider());
	}

	/**
	 * 获取组织结构的根节点
	 * @param yyimCallBack
	 * @return
	 */
	public void getRoot(YYIMCallBack yyimCallBack) {
		final List<YMOrgStruct> list = queryStruct(YMOrgStruct.PID_ROOT);
		if (list != null && list.size() > 0) {
			yyimCallBack.onSuccess(list);
		} else {
			getOrgStruct(YMOrgStruct.PID_ROOT, yyimCallBack);
		}
	}

	/**
	 * 通过父节点获取组织结构
	 * 
	 * @param pid
	 * @return
	 */
	public void getOrgStruct(final String pid, final YYIMCallBack yyimCallBack) {
		new Thread() {

			@Override
			public void run() {
				super.run();
				// 组织结构包
				SnsOrgStructPacket packet = new SnsOrgStructPacket();
				// 组织结构列表
				List<YMOrgStruct> entities = new ArrayList<YMOrgStruct>();
				// 设置类型为get
				packet.setType(Type.GET);
				// 目标jid
				String to = pid + "@org." + YYIMChatManager.getInstance().getYmSettings().getImServerName();
				packet.setTo(to);
				// 发送包
				try {
					packet = (SnsOrgStructPacket) XMPPManager.getInstance().getConnection()
							.createPacketCollectorAndSend(packet).nextResultOrThrow();
					// 封装数据
					for (SnsOrgStructPacket.Item item : packet.getItems()) {
						YMOrgStruct entity = new YMOrgStruct(item, pid);
						entities.add(entity);
						// 加入数据库
						insertOrUpdateStruct(entity);
					}
					if (yyimCallBack != null) {
						yyimCallBack.onSuccess(queryStruct(pid));
					}
				}catch(NotConnectedException e){
					YMLogger.d(e);
					if (yyimCallBack != null) {
						 yyimCallBack.onError(YMErrorConsts.ERROR_AUTHORIZATION, "未连接");
					}
				}catch (Exception e) {
					YMLogger.d(e);
					if (yyimCallBack != null) {
						 yyimCallBack.onError(YMErrorConsts.EXCEPTION_UNKNOWN, e.getMessage());
					}
				}
			}
		}.start();
	}

	/**
	 * 更新组织结构
	 * 
	 * @param entity
	 */
	public void insertOrUpdateStruct(YMOrgStruct entity) {
		// selection
		String selection = YMOrgStruct.JID + "=?";
		// 根据jid查
		Cursor cursor = YYIMDBHandler.getInstance().query(OrgStructProvider.CONTENT_URI, YMOrgStruct.REQUIRED_COLUMNS,
				selection, new String[] { entity.getJid() }, YMOrgStruct.JID);

		if (cursor != null && cursor.getCount() > 0) {
			// 更新
			YYIMDBHandler.getInstance().update(OrgStructProvider.CONTENT_URI, entity.toContentValues(), selection, null);
		} else {
			// 新增
			YYIMDBHandler.getInstance().insert(OrgStructProvider.CONTENT_URI, entity.toContentValues());
		}
		cursor.close();
	}

	/**
	 * 通过pid查询组织结构
	 * 
	 * @param pid
	 * @return
	 */
	public List<YMOrgStruct> queryStruct(String pid) {
		// selection
		String selection = YMOrgStruct.USER_JID + "=? and " + YMOrgStruct.PID + "=?";
		// 根据jid查
		Cursor cursor = YYIMDBHandler.getInstance().query(OrgStructProvider.CONTENT_URI, YMOrgStruct.ALL_COLUMNS, selection,
				new String[] { YYIMSessionManager.getInstance().getUserJid(), pid }, YMOrgStruct.NAME);
		// list
		List<YMOrgStruct> entities = new ArrayList<YMOrgStruct>();
		while (cursor.moveToNext()) {
			YMOrgStruct entity = new YMOrgStruct(cursor);
			entities.add(entity);
		}
		return entities;
	}

	/**
	 * 通过jid和key查询组织结构
	 * @param key
	 * @return
	 */
	public List<YMOrgStruct> queryStructByKey(String key) {
		// selection
		String selection = YMOrgStruct.USER_JID + "=? and " + YMOrgStruct.NAME + " like \"%" + key + "%\" or "
				+ YMOrgStruct.JID + " like \"%" + key + "%\"";
		// 查询
		Cursor cursor = YYIMDBHandler.getInstance().query(OrgStructProvider.CONTENT_URI, YMOrgStruct.ALL_COLUMNS, selection,
				new String[] { YYIMSessionManager.getInstance().getUserJid() }, YMOrgStruct.NAME);
		// list
		List<YMOrgStruct> entities = new ArrayList<YMOrgStruct>();
		while (cursor.moveToNext()) {
			YMOrgStruct entity = new YMOrgStruct(cursor);
			entities.add(entity);
		}
		return entities;
	}

	/**
	 * 查询用户
	 * 
	 * @param user
	 * @return
	 */
	public void searchRosters(final String key, final YYIMCallBack yyimCallBack) {
		new Thread() {

			@Override
			public void run() {
				super.run();
				// result
				List<SearchValue> users = new ArrayList<SearchValue>();
				try {
					// search result
					ReportedData data = null;
					// UserSearchManager
					UserSearchManager usm = new UserSearchManager(XMPPManager.getInstance().getConnection());
					String serverName = YYIMChatManager.getInstance().getYmSettings().getImServerName();
					// searchForm
					Form searchForm = usm.getSearchForm("search." + serverName);
					// answerForm
					Form answerForm = searchForm.createAnswerForm();
					answerForm.setAnswer("Username", true);
					answerForm.setAnswer("Name", true);
					answerForm.setAnswer("search", key);
					// get result
					data = usm.getSearchResults(answerForm, "search." + serverName);
					// column:jid,Username,Name,Email
					List<Row> dataList = data.getRows();
					if (dataList == null || dataList.size() <= 0) {
						throw new YMException();
					}
					Roster roster = XMPPManager.getInstance().getConnection().getRoster();
					// 遍历结果
					for (Row row : dataList) {
						SearchValue item = new SearchValue(row);
						// 结果实体
						if (roster != null && roster.getEntry(item.getJid()) != null) {
							item.setFriend(true);
						} else {
							item.setFriend(false);
						}
						users.add(item);
					}
					if (yyimCallBack != null) {
						yyimCallBack.onSuccess(users);
					}
				} catch (NoResponseException e) {
					YMLogger.d(e);
					if (yyimCallBack != null) {
						yyimCallBack.onError(YMErrorConsts.EXCEPTION_NORESPONSE, "服务器未响应");
					}
				} catch (NotConnectedException e) {
					YMLogger.d(e);
					if (yyimCallBack != null) {
						yyimCallBack.onError(YMErrorConsts.ERROR_AUTHORIZATION, "未连接");
					}
				} catch (XMPPErrorException e) {
					YMLogger.d(e);
					if (yyimCallBack != null) {
						yyimCallBack.onError(IMErrorConsts.EXCEPTION_SEARCH_ROSTER, e.getMessage());
					}
				} catch(YMException e){
					YMLogger.d(e);
					if (yyimCallBack != null) {
						yyimCallBack.onError(IMErrorConsts.EXCEPTION_SEARCH_EMPTY, "未查到数据");
					}
				}
				catch (Exception e) {
					YMLogger.d(e);
					if (yyimCallBack != null) {
						yyimCallBack.onError(YMErrorConsts.EXCEPTION_UNKNOWN, e.getMessage());
					}
				}
			}
		}.start();
	}

	/**
	 * 前端搜索
	 * 
	 * @param key
	 */
	public List<SearchValue> searchForWord(String key) {
		List<SearchValue> mSearchResultList = new ArrayList<SearchValue>();
		// 查询联系人
		List<YMRoster> rosters = YYIMRosterManager.getInstance().getRostersByKey(key);
		if (rosters.size() > 0) {
			// 添加一个模块标志
			SearchValue result = new SearchValue(getString(R.string.search_roster_text));
			mSearchResultList.add(result);
			// 添加模块数据
			for (YMRoster ymRoster : rosters) {
				mSearchResultList.add(new SearchValue(ymRoster));
			}
		}
		// 查询聊天记录
		List<YMMessage> messages = YYIMChatManager.getInstance().getMessagesByKey(key);
		if (messages.size() > 0) {
			// 添加一个模块标志
			SearchValue result = new SearchValue(getString(R.string.search_chat_text));
			mSearchResultList.add(result);
			// 添加模块数据
			for (YMMessage ymMessage : messages) {
				mSearchResultList.add(new SearchValue(ymMessage));
			}
		}
		// 查询房间
		List<YMRoom> rooms = YYIMChatManager.getInstance().getRoomsByKey(key);
		if (rooms.size() > 0) {
			// 添加一个模块标志
			SearchValue result = new SearchValue(getString(R.string.search_group_text));
			mSearchResultList.add(result);
			// 添加模块数据
			for (YMRoom ymRoom : rooms) {
				mSearchResultList.add(new SearchValue(ymRoom));
			}
		}
		return mSearchResultList;
	}

	private String getString(int resId) {
		return YYIMChat.getInstance().getAppContext().getResources().getString(resId);
	}

}
