package com.example.mobileknow.speech;

public interface VoicePlayer {
	public void play(String text);
	public void pause();
	public void resume();
	public void cancel();
	public void destroy();
	public void setVoiceName(String name);
}
