package kr.co.hoon.a180605audio;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    List<String> files;
    int playing;
    MediaPlayer mediaPlayer;
    TextView title;
    Button playbtn;
    SeekBar seekbar;
    boolean isPlaying;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        files = new ArrayList<>();
        files.add("http://182.222.11.113:8080/files/audio/a.mp3");
        files.add("http://182.222.11.113:8080/files/audio/b.mp3");
        files.add("http://182.222.11.113:8080/files/audio/c.mp3");
        files.add("http://182.222.11.113:8080/files/audio/d.mp3");

        playing = 0;
        mediaPlayer = new MediaPlayer();
        title = findViewById(R.id.title);
        playbtn = findViewById(R.id.playbtn);
        seekbar = findViewById(R.id.seekbar);

        isPlaying = false;

        // 이벤트 핸들러 등록
        // 노래 재생이 완료되면 호출되는 핸들러
        mediaPlayer.setOnCompletionListener(onCompletionListener);
        // 노래 재생중 위치 변경이 일어났을 때 호출되는 핸들러
        mediaPlayer.setOnSeekCompleteListener(onSeekCompleteListener);
        // 시크바에서 시크바의 값이 변경됐을 때 호출되는 핸들러
        seekbar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        // 핸들러 호출
        handler.sendEmptyMessageDelayed(0,200);
        // 버튼들의 이벤트핸들러
        playbtn.setOnClickListener(onClickListener);
        findViewById(R.id.stopbtn).setOnClickListener(onClickListener);
        findViewById(R.id.prevbtn).setOnClickListener(onClickListener);
        findViewById(R.id.nextbtn).setOnClickListener(onClickListener);

        // 첫번째 노래 준비
        loadMedia(playing);
    }

    // 액티비티가 종료될 때 호출되는 메소드
    @Override
    protected void onDestroy() {
        // 상위클래스의 메소드를 재정의할 때 상위클래스의 메소드가 추상메소드가 아니라면 반드시 호출해줘야함
        super.onDestroy();
        // mediaPlayer가 null 이 아니라면 메모리 해제
        // mediaPlayer는 시스템자원이라서 모든 애플리케이션이 공유하기 때문
        // 다른 애플리케이션이 사용할 수 있도록 정리해줘야함
        if(mediaPlayer != null){
            mediaPlayer.release();
        }
    }

    // 노래 재생준비를 하는 메소드
    private void loadMedia(int playing){
        try {
            // 재생할 노래 설정
            mediaPlayer.setDataSource(this, Uri.parse(files.get(playing)));
//            Log.e("uri", Uri.parse(files.get(playing)).toString());
            // 텍스트뷰에 제목 설정
            title.setText(files.get(playing));
            mediaPlayer.prepare();
            // 재생 시간을 seekbar의 최대값으로 설정
            seekbar.setMax(mediaPlayer.getDuration());
        }catch(Exception e){}
    }

    // 버튼의 클릭이벤트 객체
    Button.OnClickListener onClickListener = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.playbtn:
                    if(isPlaying == false){
                        mediaPlayer.start();
                        playbtn.setText("||");
                        isPlaying = true;
                    }else{
                        mediaPlayer.pause();
                        playbtn.setText("▶");
                        isPlaying = false;
                    }
                    break;
                case R.id.stopbtn:
                    mediaPlayer.stop();
                    playbtn.setText("▶");
                    seekbar.setProgress(0);
                    isPlaying = false;
                    try{
                        mediaPlayer.prepare();
                    }catch(Exception e){}
                    break;
                case R.id.prevbtn:
                    playing = (playing == 0 ? files.size()-1 : playing-1);
                    mediaPlayer.reset();
                    loadMedia(playing);
                    mediaPlayer.start();
                    playbtn.setText("||");
                    break;
                case R.id.nextbtn:
                    playing = (playing == files.size()-1 ? 0 : playing+1);
                    mediaPlayer.reset();
                    loadMedia(playing);
                    mediaPlayer.start();
                    playbtn.setText("||");
                    break;
            }
        }
    };

    // 재생이 종료됐을 때 이벤트
    MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            playing = (playing == files.size()-1 ? 0 : playing+1);
            mediaPlayer.reset();
            loadMedia(playing);
            mediaPlayer.start();

        }
    };

    // 노래 재생중 위치변경이 일어났을 때 이벤트
    MediaPlayer.OnSeekCompleteListener onSeekCompleteListener = new MediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(MediaPlayer mp) {
            if(isPlaying){
                mediaPlayer.start();
            }
        }
    };

    // 노래 재생위치가 이동됐을 때 이벤트
    SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(fromUser){
                mediaPlayer.seekTo(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            isPlaying = mediaPlayer.isPlaying();
            if(isPlaying){
                mediaPlayer.pause();
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(mediaPlayer != null && mediaPlayer.isPlaying()){
                seekbar.setProgress(mediaPlayer.getCurrentPosition());
            }
            // 0.2초마다 seekbar 갱신
            handler.sendEmptyMessageDelayed(0,200);
        }
    };
}
