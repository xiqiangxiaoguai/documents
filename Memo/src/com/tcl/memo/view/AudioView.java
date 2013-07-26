package com.tcl.memo.view;

import java.io.File;
import java.io.IOException;

import com.tcl.memo.Constants;
import com.tcl.memo.R;
import com.tcl.memo.data.Note;
import com.tcl.memo.util.DateUtils;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class AudioView extends LinearLayout {
	public static final int STATE_START_RECORD = 1;
	public static final int STATE_STOP_RECORD = 2;
	public static final int STATE_CANCEL_RECORD = 3;
	public static final int STATE_START_PLAY_RECORD = 4;
	public static final int STATE_PAUSE_PLAY_RECORD = 5;
	public static final int STATE_RESUME_PLAY_RECORD = 6;
	public static final int STATE_STOP_PLAY_RECORD = 7;

	private static final int MSG_WHAT_SWITCH_RECORD_ICON = 1;
	private static final int MSG_WHAT_UPDATE_PLAY_PROGRESS = 2;

	private static final String TAG = AudioView.class.getSimpleName();

	private int mState = -1;

	private Note.Audio mNewAudio;
	private Note.Audio mInitAudio;

	private MediaPlayer mMediaPlayer;
	private MediaRecorder mMediaRecorder;

	private OnAudioChangeListener mOnAudioChangeListener;

	private Handler mHandler = new Handler() {
		public void handleMessage(Message message) {
			long uptimeMillis = SystemClock.uptimeMillis();

			switch (message.what) {
			case MSG_WHAT_SWITCH_RECORD_ICON: {
				((ImageView) findViewById(R.id.record_icon))
						.setImageResource(message.arg1 % 2 == 0 ? R.drawable.audio_start_record_02
								: R.drawable.audio_start_record_01);
				((TextView) findViewById(R.id.time))
						.setText(timeToString(message.arg1));
				break;
			}
			case MSG_WHAT_UPDATE_PLAY_PROGRESS: {
				TextView textView = (TextView)findViewById(R.id.start_time);
				ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
				if (message.arg1 > progressBar.getMax()) {
					textView.setText(timeToString(message.arg1));
					progressBar.setProgress(message.arg1);
					onStopPlayRecord();
					return;
				}
				textView.setText(timeToString(message.arg1));
				progressBar.setProgress(message.arg1);
				
				break;
			}
			}

			Message msg = new Message();
			msg.what = message.what;
			msg.arg1 = message.arg1 + 1;
			mHandler.sendMessageAtTime(msg, uptimeMillis + 1000);
		};
	};

	private OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.start_record: {
				onStartRecord();
				break;
			}
			case R.id.stop_record: {
				onStopRecord();
				break;
			}
			case R.id.cancel_record: {
				onCancelRecord();
				break;
			}
			case R.id.replace_record: {
				onReplaceRecord();
				break;
			}
			case R.id.delete_record: {
				onDeleteRecord();
				break;
			}
			case R.id.start_play: {
				onStartPlayRecord();
				break;
			}
			case R.id.pause_play: {
				onPausePlayRecord();
				break;
			}
			case R.id.resume_play: {
				onResumePlayRecord();
				break;
			}
			case R.id.stop_play: {
				onStopPlayRecord();
				break;
			}
			}
		}
	};

	public AudioView(Context context) {
		this(context, null);
	}

	public AudioView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AudioView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		addView(inflater.inflate(R.layout.audio_view, null));

		findViewById(R.id.start_record).setOnClickListener(mOnClickListener);
		findViewById(R.id.stop_record).setOnClickListener(mOnClickListener);
		findViewById(R.id.cancel_record).setOnClickListener(mOnClickListener);
		findViewById(R.id.replace_record).setOnClickListener(mOnClickListener);
		findViewById(R.id.delete_record).setOnClickListener(mOnClickListener);
		findViewById(R.id.start_play).setOnClickListener(mOnClickListener);
		findViewById(R.id.pause_play).setOnClickListener(mOnClickListener);
		findViewById(R.id.resume_play).setOnClickListener(mOnClickListener);
		findViewById(R.id.stop_play).setOnClickListener(mOnClickListener);

		((TextView) findViewById(R.id.time)).setText(timeToString(0));

		changeState(STATE_START_RECORD);
	}

	public void changeState(int state) {
		if (state != mState) {
			mState = state;
			onAudioStateChanged();
		}
	}

	protected void onAudioStateChanged() {
		switch (mState) {
		case STATE_START_RECORD: {
			findViewById(R.id.start_record).setVisibility(View.VISIBLE);
			findViewById(R.id.stop_cancel_layout).setVisibility(View.INVISIBLE);
			findViewById(R.id.start_play).setVisibility(View.INVISIBLE);
			findViewById(R.id.play_pause_resume_layout)
					.setVisibility(View.GONE);
			findViewById(R.id.time_layout).setVisibility(View.VISIBLE);
			findViewById(R.id.date).setVisibility(View.INVISIBLE);
			findViewById(R.id.progress_layout).setVisibility(View.INVISIBLE);
			findViewById(R.id.replace_delete_layout).setVisibility(View.GONE);
			((ImageView) findViewById(R.id.record_icon))
					.setImageResource(R.drawable.audio_start_record_01);
			break;
		}
		case STATE_STOP_RECORD:
		case STATE_CANCEL_RECORD: {
			findViewById(R.id.start_record).setVisibility(View.INVISIBLE);
			findViewById(R.id.stop_cancel_layout).setVisibility(View.VISIBLE);
			findViewById(R.id.start_play).setVisibility(View.INVISIBLE);
			findViewById(R.id.play_pause_resume_layout)
					.setVisibility(View.GONE);
			findViewById(R.id.time_layout).setVisibility(View.VISIBLE);
			findViewById(R.id.date).setVisibility(View.INVISIBLE);
			findViewById(R.id.progress_layout).setVisibility(View.INVISIBLE);
			findViewById(R.id.replace_delete_layout).setVisibility(View.GONE);
			break;
		}
		case STATE_START_PLAY_RECORD: {
			findViewById(R.id.start_record).setVisibility(View.INVISIBLE);
			findViewById(R.id.stop_cancel_layout).setVisibility(View.INVISIBLE);
			findViewById(R.id.start_play).setVisibility(View.VISIBLE);
			findViewById(R.id.play_pause_resume_layout)
					.setVisibility(View.GONE);
			findViewById(R.id.time_layout).setVisibility(View.INVISIBLE);
			findViewById(R.id.date).setVisibility(View.VISIBLE);
			findViewById(R.id.progress_layout).setVisibility(View.INVISIBLE);
			findViewById(R.id.replace_delete_layout)
					.setVisibility(View.VISIBLE);

			long createTime = System.currentTimeMillis();
			if (mNewAudio != null) {
				createTime = mNewAudio.mCreateTime;
			} else if (mInitAudio != null) {
				createTime = mInitAudio.mCreateTime;
			}
			((TextView) findViewById(R.id.date)).setText(DateUtils
					.formatTimeStampString(getContext(), createTime, true));
			break;
		}
		case STATE_PAUSE_PLAY_RECORD: {
			findViewById(R.id.start_record).setVisibility(View.INVISIBLE);
			findViewById(R.id.stop_cancel_layout).setVisibility(View.INVISIBLE);
			findViewById(R.id.start_play).setVisibility(View.INVISIBLE);
			findViewById(R.id.play_pause_resume_layout).setVisibility(
					View.VISIBLE);
			findViewById(R.id.stop_play).setVisibility(View.VISIBLE);
			findViewById(R.id.pause_play).setVisibility(View.VISIBLE);
			findViewById(R.id.resume_play).setVisibility(View.GONE);
			findViewById(R.id.time_layout).setVisibility(View.INVISIBLE);
			findViewById(R.id.date).setVisibility(View.INVISIBLE);
			findViewById(R.id.progress_layout).setVisibility(View.VISIBLE);
			findViewById(R.id.replace_delete_layout).setVisibility(View.GONE);
			break;
		}
		case STATE_RESUME_PLAY_RECORD: {
			findViewById(R.id.start_record).setVisibility(View.INVISIBLE);
			findViewById(R.id.stop_cancel_layout).setVisibility(View.INVISIBLE);
			findViewById(R.id.start_play).setVisibility(View.INVISIBLE);
			findViewById(R.id.play_pause_resume_layout).setVisibility(
					View.VISIBLE);
			findViewById(R.id.stop_play).setVisibility(View.VISIBLE);
			findViewById(R.id.pause_play).setVisibility(View.GONE);
			findViewById(R.id.resume_play).setVisibility(View.VISIBLE);
			findViewById(R.id.time_layout).setVisibility(View.INVISIBLE);
			findViewById(R.id.date).setVisibility(View.INVISIBLE);
			findViewById(R.id.progress_layout).setVisibility(View.VISIBLE);
			findViewById(R.id.replace_delete_layout).setVisibility(View.GONE);
			break;
		}
		case STATE_STOP_PLAY_RECORD: {
			findViewById(R.id.start_record).setVisibility(View.INVISIBLE);
			findViewById(R.id.stop_cancel_layout).setVisibility(View.INVISIBLE);
			findViewById(R.id.start_play).setVisibility(View.INVISIBLE);
			findViewById(R.id.play_pause_resume_layout).setVisibility(
					View.VISIBLE);
			findViewById(R.id.stop_play).setVisibility(View.VISIBLE);
			findViewById(R.id.pause_play).setVisibility(View.VISIBLE);
			findViewById(R.id.resume_play).setVisibility(View.GONE);
			findViewById(R.id.time_layout).setVisibility(View.INVISIBLE);
			findViewById(R.id.date).setVisibility(View.INVISIBLE);
			findViewById(R.id.progress_layout).setVisibility(View.VISIBLE);
			findViewById(R.id.replace_delete_layout).setVisibility(View.GONE);
			break;
		}
		}
	}
	
	public void stopRecord() {
		if(mMediaRecorder != null) {
			try {
				mMediaRecorder.stop();
				mMediaRecorder.release();
				mMediaRecorder = null;
			} catch(IllegalStateException e) {
				Log.d(TAG, e.toString(), e);
			}
		}
	}
	
	public void stopPlay() {
		if(mMediaPlayer != null) {
			try {
				if(mMediaPlayer.isPlaying()) {
					mMediaPlayer.stop();
				}
				mMediaPlayer.release();
				mMediaPlayer = null;
			} catch(IllegalStateException e) {
				Log.d(TAG, e.toString(), e);
			}
		}
	}

	protected void onStartRecord() {
		try {
			if (mNewAudio != null && mNewAudio.mUri != null) {
				File file = new File(mNewAudio.mUri);
				if (file.exists()) {
					file.delete();
				}
			}
	
			if (mNewAudio == null) {
				mNewAudio = new Note.Audio();
			}
			if (mNewAudio.mUri == null) {
				long timeMillis = System.currentTimeMillis();
				while (true) {
					mNewAudio.mUri = Constants.AUDIO_DIR + "/" + timeMillis
							+ ".3pg";
					try {
						File file = new File(mNewAudio.mUri);
						if (!file.exists()) {
							new File(file.getParent()).mkdirs();
							file.createNewFile();
							if (mOnAudioChangeListener != null) {
								mOnAudioChangeListener.onAudioChange(mNewAudio);
							}
							break;
						}
					} catch (IOException e) {
						Log.d(TAG, e.toString(), e);
					}
					timeMillis++;
				}
			}
	
			mMediaRecorder = new MediaRecorder();
			mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
			mMediaRecorder.setOutputFile(mNewAudio.mUri);

			mMediaRecorder.prepare();
			mMediaRecorder.start();
			
			changeState(STATE_STOP_RECORD);

			Message msg = new Message();
			msg.what = MSG_WHAT_SWITCH_RECORD_ICON;
			msg.arg1 = 0;
			mHandler.sendMessage(msg);
			return;
		} catch (IllegalStateException e) {
			Log.d(TAG, e.toString(), e);
		} catch (IOException e) {
			Log.d(TAG, e.toString(), e);
		}
		Toast.makeText(getContext(), R.string.record_error,
				Toast.LENGTH_SHORT).show();
	}

	protected void onStopRecord() {
		mNewAudio.mCreateTime = System.currentTimeMillis();
		((TextView) findViewById(R.id.date)).setText(DateUtils
				.formatTimeStampString(getContext(), mNewAudio.mCreateTime,
						true));
		mMediaRecorder.stop();
		mMediaRecorder.release();
		mMediaRecorder = null;
		
		mHandler.removeMessages(MSG_WHAT_SWITCH_RECORD_ICON);
		changeState(STATE_START_PLAY_RECORD);
	}

	protected void onCancelRecord() {
		mMediaRecorder.stop();
		mMediaRecorder.release();
		mMediaRecorder = null;
		
		mHandler.removeMessages(MSG_WHAT_SWITCH_RECORD_ICON);

		((TextView) findViewById(R.id.time)).setText(timeToString(0));
		changeState(STATE_START_RECORD);

		new File(mNewAudio.mUri).delete();
		mNewAudio.mUri = null;
		if (mOnAudioChangeListener != null) {
			mOnAudioChangeListener.onAudioChange(mNewAudio);
		}
	}

	protected void onReplaceRecord() {
		((TextView) findViewById(R.id.time)).setText(timeToString(0));
		changeState(STATE_START_RECORD);

		if (mNewAudio != null && mNewAudio.mUri != null) {
			new File(mNewAudio.mUri).delete();
			mNewAudio.mUri = null;
			if (mOnAudioChangeListener != null) {
				mOnAudioChangeListener.onAudioChange(mNewAudio);
			}
		} else if (mInitAudio != null && mInitAudio.mUri != null) {
			new File(mInitAudio.mUri).delete();
			mInitAudio.mUri = null;
			if (mOnAudioChangeListener != null) {
				mOnAudioChangeListener.onAudioChange(mInitAudio);
			}
		}
	}

	protected void onDeleteRecord() {
		if (mNewAudio != null && mNewAudio.mUri != null) {
			new File(mNewAudio.mUri).delete();
			mNewAudio.mUri = null;
			if (mOnAudioChangeListener != null) {
				mOnAudioChangeListener.onAudioChange(mNewAudio);
			}
		} else if (mInitAudio != null && mInitAudio.mUri != null) {
			new File(mInitAudio.mUri).delete();
			mInitAudio.mUri = null;
			if (mOnAudioChangeListener != null) {
				mOnAudioChangeListener.onAudioChange(mInitAudio);
			}
		}
	}

	protected void onStartPlayRecord() {
		mMediaPlayer = new MediaPlayer();
//		mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
//			@Override
//			public void onCompletion(MediaPlayer mp) {
//				onStopPlayRecord();
//			}
//		});
		try {
			if (mNewAudio != null && mNewAudio.mUri != null) {
				mMediaPlayer.setDataSource(mNewAudio.mUri);
			} else if (mInitAudio != null && mInitAudio.mUri != null) {
				mMediaPlayer.setDataSource(mInitAudio.mUri);
			}
			mMediaPlayer.prepare();

			int duration = mMediaPlayer.getDuration() / 1000;
			if (mMediaPlayer.getDuration() % 1000 != 0) {
				duration += 1;
			}

			ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
			progressBar.setMax(duration);
			progressBar.setProgress(0);
			((TextView) findViewById(R.id.start_time)).setText(timeToString(0));
			((TextView) findViewById(R.id.end_time))
					.setText(timeToString(duration));

			mMediaPlayer.start();
			changeState(STATE_PAUSE_PLAY_RECORD);

			Message msg = new Message();
			msg.what = MSG_WHAT_UPDATE_PLAY_PROGRESS;
			msg.arg1 = mMediaPlayer.getCurrentPosition() / 1000;
			mHandler.sendMessage(msg);
			return;
		} catch (IllegalArgumentException e) {
			Log.d(TAG, e.toString(), e);
		} catch (SecurityException e) {
			Log.d(TAG, e.toString(), e);
		} catch (IllegalStateException e) {
			Log.d(TAG, e.toString(), e);
		} catch (IOException e) {
			Log.d(TAG, e.toString(), e);
		}
		Toast.makeText(getContext(), R.string.play_record_error,
				Toast.LENGTH_SHORT).show();
	}

	protected void onPausePlayRecord() {
		if (mMediaPlayer.isPlaying()) {
			mMediaPlayer.pause();
		}
		mHandler.removeMessages(MSG_WHAT_UPDATE_PLAY_PROGRESS);
		changeState(STATE_RESUME_PLAY_RECORD);
	}

	protected void onResumePlayRecord() {
		if (!mMediaPlayer.isPlaying()) {
			mMediaPlayer.start();
		}
		changeState(STATE_PAUSE_PLAY_RECORD);

		Message msg = new Message();
		msg.what = MSG_WHAT_UPDATE_PLAY_PROGRESS;
		msg.arg1 = mMediaPlayer.getCurrentPosition() / 1000;
		mHandler.sendMessage(msg);
	}

	protected void onStopPlayRecord() {
		if(mMediaPlayer != null) {
			mMediaPlayer.stop();
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
		mHandler.removeMessages(MSG_WHAT_UPDATE_PLAY_PROGRESS);
		changeState(STATE_START_PLAY_RECORD);
	}

	public Note.Audio getAudio() {
		return mInitAudio;
	}

	public void setInitAudio(Note.Audio initAudio) {
		mInitAudio = initAudio;
		changeState((mInitAudio != null && mInitAudio.mUri != null) ? STATE_START_RECORD
				: STATE_START_PLAY_RECORD);
	}

	private String timeToString(int second) {
		int minite = second / 60;
		second = second % 60;

		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append(minite < 10 ? "0" : "").append(minite).append(":");
		strBuilder.append(second < 10 ? "0" : "").append(second);
		return strBuilder.toString();
	}
	
	public int getState() {
		return mState;
	}

	public void setmState(int state) {
		mState = state;
	}

	public OnAudioChangeListener getOnAudioChangeListener() {
		return mOnAudioChangeListener;
	}

	public void setOnAudioChangeListener(
			OnAudioChangeListener onAudioChangeListener) {
		mOnAudioChangeListener = onAudioChangeListener;
	}

	public static interface OnAudioChangeListener {
		void onAudioChange(Note.Audio audio);
	}
}