package lib.byhook.lv;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import lib.byhook.exlistview.R;
import lib.byhook.impl.PullMoreImpl;

/**
 * Created by byhook on 15-10-31.
 * mail : byhook@163.com
 */
public class ExFooter extends LinearLayout {

    private static final boolean DEBUG = true;

    private static final String TAG = "ExFooter";
    /**
     * 包裹的容器
     */
    private LinearLayout container;

    /**
     * 底部容器
     */
    private RelativeLayout footer;

    /**
     * 默认容器高度
     */
    private int mDefaultHeight;

    /**
     * 上拉加载更多
     */
    private PullMoreImpl mPullMoreImpl;

    /**
     * 文本内容
     */
    private TextView tv_content;

    public enum PullType{
        PULL_LOADING,  //正在加载状态
        PULL_COMPLETE  //完成状态
    }

    private PullType mPullType;

    public ExFooter(Context context) {
        super(context);
        initView();
    }

    public ExFooter(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ExFooter(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    private void initView() {
        inflate(getContext(), R.layout.lv_footer, this);

        container = (LinearLayout) findViewById(R.id.container);
        footer = (RelativeLayout) findViewById(R.id.footer);

        tv_content = (TextView) findViewById(R.id.tv_content);

        hideFooter();
    }

    /**
     * 初始化底部容器
     * @param width
     * @param height
     */
    public void initFooter(int width, int height) {
        mDefaultHeight = height/10;
        LinearLayout.LayoutParams lpHeader = (LayoutParams) footer.getLayoutParams();
        lpHeader.height = mDefaultHeight;
        footer.setLayoutParams(lpHeader);
    }

    /**
     * 是否允许下拉事件
     * @return
     */
    public boolean allow(){
        return mPullMoreImpl!=null;
    }

    /**
     * 手指滑动设置高度
     * @param height
     */
    public void setExHeight(int height){
        LayoutParams lp = (LayoutParams) container.getLayoutParams();
        lp.height += height;
        if(lp.height<=0){
            lp.height = 0;
        }else if(lp.height>mDefaultHeight){
            lp.height = mDefaultHeight;

        }
        container.setLayoutParams(lp);
    }

    /**
     * 显示底部容器
     */
    public void showFooter(){
        setExHeight(mDefaultHeight);
    }

    /**
     * 隐藏底部容器
     */
    public void hideFooter(){
        setExHeight(-mDefaultHeight);
    }

    /**
     * 加载更多
     * 回调接口
     */
    public void pullMore(){
        if (mPullType != PullType.PULL_LOADING) {
            //加载数据
            if (mPullMoreImpl != null) {
                tv_content.setText(R.string.footer_content_loading);
                mPullType = PullType.PULL_LOADING;
                mPullMoreImpl.onPullMore();
                if (DEBUG) Log.w(TAG, "Footer...onPullMore");
            }
        }
    }

    /**
     * 设置加载完成
     */
    public void setComplete(){
        mPullType = PullType.PULL_COMPLETE;
        tv_content.setText(R.string.footer_content_more);

        postDelayed(new Runnable() {
            @Override
            public void run() {
                hideFooter();
            }
        },500);
    }

    /**
     * 设置上拉加载更多监听器
     * @param mPullMoreImpl
     */
    public void setOnPullMoreListener(PullMoreImpl mPullMoreImpl){
        this.mPullMoreImpl = mPullMoreImpl;
    }

}
