package com.sysu.bbs.argo.api;

import java.util.HashMap;

import android.content.Context;

public final class API {
	public final static String entry = "http://bbs.sysu.edu.cn";
	Context context;
	HashMap<String, String> param;
	public API(Context c,HashMap<String, String> p) {
		context = c;
		param = p;
	}
	public static final class GET {
		public static final String AJAX_SECTION = entry + "/ajax/section/";
		
		public static final String AJAX_USER_FAV = entry + "/ajax/user/fav/";
		public static final String AJAX_USER_QUERY = entry + "/ajax/user/query/";
		
		public static final String AJAX_BOARD_ALL = entry + "/ajax/board/all/";
		public static final String AJAX_BOARD_ALLS = entry + "/ajax/board/alls/";
		public static final String AJAX_BOARD_GET = entry + "/ajax/board/get/";
		public static final String AJAX_BOARD_GETBYSEC = entry + "/ajax/board/getbysec/";
		
		public static final String AJAX_POST_LIST = entry + "/ajax/post/list/";
		public static final String AJAX_POST_GET = entry + "/ajax/post/get/";
		public static final String AJAX_POST_TOPICLIST = entry + "/ajax/post/topiclist/";
		
		public static final String AJAX_MAIL_MAILBOX = entry + "/ajax/mail/mailbox/";
		public static final String AJAX_MAIL_LIST = entry + "/ajax/mail/list/";
		public static final String AJAX_MAIL_GET = entry + "/ajax/mail/get/";
		
		public static final String AJAX_ANN = entry + "/ajax/ann/";
		public static final String AJAX_ANC = entry + "/ajax/anc/";
		
		public static final String AJAX_COMM_TOPTEN = entry + "/ajax/comm/topten/";
				
	}
	public static final class POST {
		public static final String AJAX_LOGIN = entry + "/ajax/login/";
		public static final String AJAX_LOGOUT = entry + "/ajax/logout/";
		public static final String AJAX_USER_ADDFAV = entry + "/ajax/user/addfav/";
		public static final String AJAX_USER_DELFAV = entry + "/ajax/user/delfav";
		public static final String AJAX_POST_ADD = entry + "/ajax/post/add/";
		public static final String AJAX_POST_DEL = entry + "/ajax/post/del/";
		public static final String AJAX_MAIL_SEND = entry + "/ajax/mail/send/";
		public static final String AJAX_BOARD_CLEAR = entry + "/ajax/board/clear/";
	}
}
