package com.example.smsManager.utils;

import android.net.Uri;

public class Sms {
	public static final Uri CONVERSATION_URI = Uri.parse("content://sms/conversations");
	public static final Uri SMS_URI = Uri.parse("content://sms");
	// SMS type : receive
	public static final int SMS_RECEIVE = 1;
	// SMS type : send
	public static final int SMS_SEND = 2;
	// SMS type : draft
	public static final int SMS_DUSTBIN = 3;
	
	public static final Uri SMS_INBOX = Uri.parse("content://sms/inbox");
	public static final Uri SMS_SENT = Uri.parse("content://sms/sent");
	public static final Uri SMS_DRAFT = Uri.parse("content://sms/draft");
	public static final Uri SMS_OUTBOX = Uri.parse("content://sms/outbox");
	
	public static final Uri GROUPS_QUERY_URI = Uri.parse("content://com.example.smsManager.provider.GroupContentProvider/groups");
	public static final Uri GROUPS_INSERT_URI = Uri.parse("content://com.example.smsManager.provider.GroupContentProvider/groups/insert");
	public static final Uri GROUPS_UPDATE_URI = Uri.parse("content://com.example.smsManager.provider.GroupContentProvider/groups/update");
	public static final Uri GROUPS_SINGLE_DELETE_URI = Uri.parse("content://com.example.smsManager.provider.GroupContentProvider/groups/delete/#");
	public static final Uri GROUPS_ALL_DELETE_URI = Uri.parse("content://com.example.smsManager.provider.GroupContentProvider/groups/all_delete");
	public static final Uri THREAD_GROUP_QUERY_URI = Uri.parse("content://com.example.smsManager.provider.GroupContentProvider/thread_group");
	public static final Uri THREAD_GROUP_INSERT = Uri.parse("content://com.example.smsManager.provider.GroupContentProvider/thread_group/insert");
	

}
