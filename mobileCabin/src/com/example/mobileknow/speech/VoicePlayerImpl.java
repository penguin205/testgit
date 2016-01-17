package com.example.mobileknow.speech;

import com.example.mobileknow.utils.Constants;
import com.iflytek.speech.SpeechConfig.RATE;
import com.iflytek.speech.SpeechError;
import com.iflytek.speech.SynthesizerPlayer;
import com.iflytek.speech.SynthesizerPlayerListener;

import android.content.Context;
import android.widget.Toast;

public class VoicePlayerImpl implements VoicePlayer{
	
	private Context context;
	private SynthesizerPlayer player;
	public VoicePlayerImpl(Context context) {
		this.context = context;
		player = SynthesizerPlayer.createSynthesizerPlayer(context, "appid="+ Constants.IFLYTEK_APPID +"");
	}
	
	@Override
	public void play(String text) {
		player.setSampleRate(RATE.rate16k);
		player.setSpeed(60);
		player.setBackgroundSound(null);
		player.setVolume(60);
		player.playText(text, null, playerListener);
	}
	
	private SynthesizerPlayerListener playerListener = new SynthesizerPlayerListener() {
		
		@Override
		public void onPlayResumed() {
		}
		
		@Override
		public void onPlayPercent(int percent,int beginPos,int endPos) {
		}
		
		@Override
		public void onPlayPaused() {
		}
		
		@Override
		public void onPlayBegin() {
		}
		
		@Override
		public void onEnd(SpeechError error) {
		}
		
		@Override
		public void onBufferPercent(int percent,int beginPos,int endPos) {
		}
	};
	
	@Override
	public void pause() {
		player.pause();
	}
	@Override
	public void resume() {
		player.resume();
	}
	@Override
	public void cancel() {
		player.cancel();
	}
	@Override
	public void destroy() {
		player.destory();
	}

	@Override
	public void setVoiceName(String name) {
		player.setVoiceName(name);
	}
	
}
