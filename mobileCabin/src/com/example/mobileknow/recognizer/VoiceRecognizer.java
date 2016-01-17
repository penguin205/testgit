package com.example.mobileknow.recognizer;

import java.util.ArrayList;

import com.example.mobileknow.utils.BroadcastHelper;
import com.example.mobileknow.utils.Constants;
import com.iflytek.speech.RecognizerResult;
import com.iflytek.speech.SpeechError;
import com.iflytek.speech.SpeechConfig.RATE;
import com.iflytek.ui.RecognizerDialog;
import com.iflytek.ui.RecognizerDialogListener;

import android.content.Context;
import android.util.Log;

public class VoiceRecognizer {
	
	protected static final String TAG = "VoiceRecognizer";
	private Context context;

	private RecognizerDialog dialog;
	private StringBuilder sb = new StringBuilder();
	
	public VoiceRecognizer(Context context) {
		this.context = context;
		dialog = new RecognizerDialog(context, "appid="+Constants.IFLYTEK_APPID);
		dialog.setListener(recognizerListener);
	}
	public void start() {
		dialog.setEngine("sms", null, null);
		dialog.setSampleRate(RATE.rate16k);
		dialog.show();
	}

	private RecognizerDialogListener recognizerListener = new RecognizerDialogListener() {
		
		@Override
		public void onResults(ArrayList<RecognizerResult> results, boolean isLast) {
			
			for(RecognizerResult result : results) {
				sb.append(result.text);
			}
		}
		
		@Override
		public void onEnd(SpeechError error) {
			if(error == null) {
				Log.d(TAG, "识别结果：" + sb.toString());
				BroadcastHelper.sendBroadCast(context, Constants.VOICERECOGNIZER_STRING_ACTION, 
						Constants.VOICERECOGNIZER_STRING_ACTION_KEY, sb.toString());
				sb.setLength(0);
				dialog.dismiss();
			} else {
				int errorCode = error.getErrorCode();
				String errorDesc = error.getErrorDesc();
				Log.d(TAG, "errorCode :" + errorCode + " errorDesc :" + errorDesc);
			}
		}
	};
}
