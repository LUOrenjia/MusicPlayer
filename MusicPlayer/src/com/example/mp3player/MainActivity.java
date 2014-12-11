package com.example.mp3player;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private Button buttonPro;
	private Button buttonStart;
	private Button buttonLast;
	private static int flag=0; //用于标记播放/停止按钮的状态的变量，当flag为0时，播放器为停止播放状态；当flag为1时，播放器为播放状态。
	private MediaPlayer mp = new MediaPlayer();
	private ListView listview;//用于显示歌曲列表
	private SeekBar sb = null;//进度条
	public  Map<String,String> Mp3Map = null;//用于保存歌曲名和歌曲路径
	public List<String>  Mp3NameList = null;//用于保存歌曲名
	public String playing = null; //当前正在播放的音乐名称
	public Handler hander = new Handler();
	private long exitTime = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		//读写歌曲列表文件相关IO流的定义
		File savePath = new File("/sdcard/data/list.txt");//歌曲列表的路径
		File mp3Path = new File("/sdcard/Music/");//扫描音乐的目录
		FileWriter fw =null;
		FileReader fr = null;
		BufferedWriter bw = null;
		BufferedReader br = null;
		
		if(!savePath.exists()){
			try {
				savePath.createNewFile();
				fw = new FileWriter(savePath);
				bw = new BufferedWriter(fw);
				save(mp3Path,bw);
			} catch (IOException e) {
				e.printStackTrace();
			}
			finally{
				try {
					bw.close();
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		try {
			fr = new FileReader(savePath);
			br = new BufferedReader(fr);
			Mp3Map = getData_Map(br);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}		
		try {
			fr = new FileReader(savePath);
			br = new BufferedReader(fr);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Mp3NameList = getData(br);
		listview = (ListView)findViewById(R.id.listView1);
		listview.setAdapter(new ArrayAdapter<String>(MainActivity.this, R.layout.array_item, Mp3NameList));


		listview.setOnItemClickListener(new mp3onClickLinser());
		
		sb = (SeekBar)findViewById(R.id.seekBar1);
		buttonPro = (Button)findViewById(R.id.button_pro);
		buttonStart = (Button)findViewById(R.id.button_start);
		buttonLast = (Button)findViewById(R.id.button_last);
		buttonPro.setOnTouchListener(new MP3BtuuonLinsner());
		buttonStart.setOnTouchListener(new MP3BtuuonLinsner());
		buttonLast.setOnTouchListener(new MP3BtuuonLinsner());
		
		sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				if(fromUser==true){
					mp.seekTo(progress);
				}
			}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub	
			}	
		});
		mp.setOnCompletionListener(new OnCompletionListener() {	
			@Override
			public void onCompletion(MediaPlayer mp) {
				// TODO Auto-generated method stub
				if(Mp3Map!=null){
					if(mp.isPlaying()){
						mp.reset();
					}
					try {
						playing = Mp3NameList.get((Mp3NameList.indexOf(playing)+1));
						hander.removeCallbacks(setSeekBarThread);
						mp.reset();
						mp.setDataSource(Mp3Map.get(playing));
						mp.prepare();
						mp.start();
						sb.setMax(mp.getDuration());
						hander.post(setSeekBarThread);
						//buttonStart.setBackgroundResource(R.drawable.start_down); //播放音乐，修改播放键的图标显示
						//playing = playing;
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (SecurityException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalStateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
				}
			}
			
		});
	}
	
	
	
	Runnable setSeekBarThread = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			sb.setProgress(mp.getCurrentPosition());
			System.out.println("test!!!!!!!!!!!!!");
			hander.postDelayed(setSeekBarThread,100);
		}
	};
	public class MP3BtuuonLinsner implements OnTouchListener
	{
		public String Temp = null; 
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if(event.getAction() == MotionEvent.ACTION_DOWN){
				if(((Button)v).getId()==R.id.button_pro){        //”上一首“按键
					v.setBackgroundResource(R.drawable.pro_down); 
				}else if(((Button)v).getId()==R.id.button_start){//”播放/暂停“按键
					if(flag==0){
						v.setBackgroundResource(R.drawable.start_down);
						
					}else{
						v.setBackgroundResource(R.drawable.start_up);
					}			
				}else if(((Button)v).getId()==R.id.button_last){//”下一首“按键
					v.setBackgroundResource(R.drawable.last_down);
				}			
			}else if(event.getAction() == MotionEvent.ACTION_UP){
				if(((Button)v).getId()==R.id.button_pro){//”上一首“按键
					v.setBackgroundResource(R.drawable.pro_up);
					if(Mp3Map!=null){
						if(mp.isPlaying()){
							mp.reset();
						}
						try {
							Temp = Mp3NameList.get((Mp3NameList.indexOf(playing)-1));
							hander.removeCallbacks(setSeekBarThread);
							mp.reset();
							mp.setDataSource(Mp3Map.get(Temp));
							mp.prepare();
							mp.start();
							sb.setMax(mp.getDuration());
							hander.post(setSeekBarThread);
							buttonStart.setBackgroundResource(R.drawable.start_down); //播放音乐，修改播放键的图标显示
							playing = Temp;
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (SecurityException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalStateException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
					
					
				}else if(((Button)v).getId()==R.id.button_start){//”播放/暂停“按键
					if(flag==1){
						v.setBackgroundResource(R.drawable.start_down);
						mp.start();
						flag=0;
					}else{
						v.setBackgroundResource(R.drawable.start_up);
						mp.pause();
						flag=1;
					}
				}else if(((Button)v).getId()==R.id.button_last){//”下一首“按键
					v.setBackgroundResource(R.drawable.last_up);
					if(Mp3Map!=null){
						if(mp.isPlaying()){
							mp.reset();
						}
						try {
							Temp = Mp3NameList.get((Mp3NameList.indexOf(playing)+1));
							//System.out.println("22222 "+Temp);
							hander.removeCallbacks(setSeekBarThread);
							mp.reset();
							mp.setDataSource(Mp3Map.get(Temp));
							mp.prepare();
							mp.start();
							sb.setMax(mp.getDuration());
							hander.post(setSeekBarThread);
							buttonStart.setBackgroundResource(R.drawable.start_down); //播放音乐，修改播放键的图标显示
							playing = Temp;
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (SecurityException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalStateException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					
					}
				}	
			}
			return false;
		}
		
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void save(File path,BufferedWriter bw){
		if(path.exists()){
			if(path.isDirectory()){
				File[] fileList = path.listFiles();
				for(File f : fileList){
					if(f.isFile()){
						if(f.getName().endsWith(".mp3")){
							try {
								bw.write(f.getAbsolutePath()+"\n");
								bw.flush();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}	
					}else{
						save(f,bw);
					}
				}
			}
		}else{
			Toast.makeText(MainActivity.this, "打开文件失败！", Toast.LENGTH_SHORT).show();
		}
	}
		
	private List<String> getData(BufferedReader br){
		List<String> data = new ArrayList<String>();
		String buf = null;
		String[] str = null;
		try {
			while((buf = br.readLine())!=null){
				str = buf.split("/");
				data.add(str[str.length-1]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
 		return data;
	}
	
	
	public Map<String,String> getData_Map(BufferedReader br){
		Map<String,String> data = new HashMap<String,String>();
		String buf = null;
		String[] str = null;
		try {
			while((buf = br.readLine())!=null){
				str = buf.split("/");
				data.put(str[str.length-1],buf);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return data;
	}
	public class mp3onClickLinser implements OnItemClickListener{
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {	
			try {
//				if(mp.isPlaying()){
//					mp.reset();
//				}	
				hander.removeCallbacks(setSeekBarThread);
				//arg1.setBackgroundColor(Color.YELLOW);
				mp.reset();
				mp.setDataSource(Mp3Map.get(((TextView)arg1).getText()));
				mp.prepare();
				mp.start();
				sb.setMax(mp.getDuration());
				hander.post(setSeekBarThread);
				buttonStart.setBackgroundResource(R.drawable.start_down); //播放音乐，修改播放键的图标显示
				playing = (String) ((TextView)arg1).getText();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			mp.setOnCompletionListener(new OnCompletionListener() {				
				@Override
				public void onCompletion(MediaPlayer mp) {
					// TODO Auto-generated method stub
					mp.release();
				}
			});
		}
		
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){   
	        if((System.currentTimeMillis()-exitTime) > 2000){  
	            Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();                                
	            exitTime = System.currentTimeMillis();   
	        } else {
	            finish();
	            System.exit(0);
	        }
	        return true;   
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
}
