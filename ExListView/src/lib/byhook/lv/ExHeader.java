package lib.byhook.lv;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;
import lib.byhook.exlistview.R;
import lib.byhook.impl.PullDownImpl;

/**
 * Created by byhook on 15-10-31.
 * mail : byhook@163.com
 */
public class ExHeader extends LinearLayout {

    /**
     * 包裹的容器
     */
    private LinearLayout container;

    /**
     * 头部内容
     */
    private RelativeLayout header;

    /**
     * 默认头部高度
     */
    private int mDefaultHeight;

    /**
     * 旋转图标
     */
    private ImageView iv_logo;

    /**
     * 内容
     */
    private TextView tv_content;

    /**
     * 滚动器
     */
    private Scroller mScroller;

    /**
     * 下拉加载更多
     */
    private PullDownImpl mPullDownImpl;

    /**
     * 手指是否触摸
     */
    private boolean mOnTouching;

    public enum PullType{
        PULL_IDLE,   //初始化状态
        PULL_READY,  //准备状态
        PULL_DOWN,   //下拉状态
        PULL_LOADING,  //正在加载状态
        PULL_COMPLETE  //完成状态
    }

    private PullType mPullType = PullType.PULL_READY;

    public ExHeader(Context context) {
        super(context);
        initView();
    }

    public ExHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ExHeader(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    /**
     * 初始化控件
     */
    private void initView() {
        inflate(getContext(), R.layout.lv_header, this);

        container = (LinearLayout) findViewById(R.id.container);
        header = (RelativeLayout) findViewById(R.id.header);

        tv_content = (TextView) findViewById(R.id.tv_content);

        mScroller = new Scroller(getContext(), new LinearInterpolator());  //OvershootInterpolator(0.75f)


        mDefaultHeight = 160;
        setExHeight(0);
    }

    /**
     * 初始化Header
     */
    public void initHeader(){
        //初始化Logo相对坐标
        iv_logo = (ImageView) findViewById(R.id.iv_logo);
        int marginLeft = getDisplayWidth()/6;
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) iv_logo.getLayoutParams();
        lp.setMargins(marginLeft, 0, 0, 0);
        iv_logo.setLayoutParams(lp);

    }

    /**
     * 获取默认屏幕宽度
     * @return
     */
    private int getDisplayWidth(){
        WindowManager windowManager = (WindowManager) getContext().getSystemService(
                Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    /**
     * 手指滑动设置高度
     * @param height
     */
    public void setExHeight(int height){
        LayoutParams lp = (LayoutParams) container.getLayoutParams();
        lp.height += height;
        if(lp.height<=0){
            //mPullType = PullType.PULL_IDLE;
            lp.height = 0;
        }else if(mPullType != PullType.PULL_COMPLETE && mPullType!= PullType.PULL_LOADING){
            //未完成下拉
            if(mPullType != PullType.PULL_READY && lp.height<mDefaultHeight){
                //回退
                mPullType = PullType.PULL_READY;
                //下拉刷新
                tv_content.setText(R.string.header_content_ready);
            }else if(mPullType != PullType.PULL_DOWN && lp.height>=mDefaultHeight){
                //旋转
                mPullType = PullType.PULL_DOWN;
                //释放立即刷新
                tv_content.setText(R.string.header_content_pulldown);
            }
        }
        container.setLayoutParams(lp);
    }

    /**
     * 获取高度
     * @return 容器高度
     */
    public int getExHeight(){
        return  container.getHeight();
    }

    /**
     * 回弹设置高度
     * @param height
     */
    public void setSmoothExHeight(int height){
        LayoutParams lp = (LayoutParams) container.getLayoutParams();
        lp.height = height;
        if(mScroller.isFinished()){
            if(lp.height<=0){
                mPullType = PullType.PULL_IDLE;
                lp.height = 0;
            }else if(mPullDownImpl!=null){   //lp.height==mDefaultHeight &&
                //正在加载数据
                mPullType = PullType.PULL_LOADING;
                mPullDownImpl.onPullDown();
            }
        }
        container.setLayoutParams(lp);
    }

    /**
     * 回滚Header位置
     */
    public void resetHeader() {
        int from = getExHeight();
        int to = -from;
        if(mPullType==PullType.PULL_DOWN || mPullType==PullType.PULL_LOADING){
            //正在更新
            to += mDefaultHeight;
            tv_content.setText(R.string.header_content_loading);
        }
        mScroller.startScroll(0, from, 0, to, 100);
        postInvalidate();
    }

    /**
     *
     * 设置刷新完成属性
     * @param str 提示文本
     */
    public void setComplete(final CharSequence str){
        mPullType = PullType.PULL_COMPLETE;
        tv_content.setText(str);

        if(!mOnTouching){
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    resetHeader();
                }
            },500);
        }

    }

    /**
     * 判断回调完成时候手指是否处于触摸状态
     * @param mOnTouching
     */
    public void setOnTouching(boolean mOnTouching) {
        this.mOnTouching = mOnTouching;
    }

    /**
     * 设置下拉刷新监听器
     * @param mPullDownImpl
     */
    public void setOnPullDownListener(PullDownImpl mPullDownImpl){
        this.mPullDownImpl = mPullDownImpl;
    }

    /**
     * 计算滚动距离
     */
    @Override
    public void computeScroll() {
        super.computeScroll();
        if(mScroller.computeScrollOffset()){
            setSmoothExHeight(mScroller.getCurrY());
            postInvalidate();
        }
    }

}
