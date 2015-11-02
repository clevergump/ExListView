package sample.byhook.exlistview;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import lib.byhook.impl.PullDownImpl;
import lib.byhook.impl.PullMoreImpl;
import lib.byhook.lv.ExListView;

public class MainActivity extends Activity implements PullDownImpl, PullMoreImpl {

    private ExListView lv_home_ex;

	private List<String> exStr;

	private ArrayAdapter<String> adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		lv_home_ex = (ExListView) findViewById(R.id.lv_home_ex);

		exStr = new ArrayList<String>();
		for(int i=0;i<7;i++){
			exStr.add(""+i);
		}

		adapter  = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,exStr);
		lv_home_ex.setAdapter(adapter);

		lv_home_ex.setOnPullDownListener(this);
		lv_home_ex.setOnPullMoreListener(this);

	}

	public void onPullUp() {
		Toast.makeText(this,"PullUp",Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onPullDown() {
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i<3;i++){
					exStr.add(0,""+i);
				}
				adapter.notifyDataSetChanged();
                lv_home_ex.setPullDownComplete("加载完成");
			}
		},1000);
	}

	@Override
	public void onPullMore() {
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
//				for (int i = 0; i<5;i++){
//					exStr.add("II"+i);
//				}
//				adapter.notifyDataSetChanged();
				lv_home_ex.setPullMoreComplete();
			}
		},3000);
	}
}
