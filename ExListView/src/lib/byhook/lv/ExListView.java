package lib.byhook.lv;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Scroller;

import lib.byhook.impl.PullDownImpl;
import lib.byhook.impl.PullMoreImpl;

/**
 * Created by byhook on 15-10-31.
 * mail : byhook@163.com
 */
public class ExListView extends ListView {

    /**
     * 是否调试
     */
    private static final boolean DEBUG = true;
    private static final String TAG = "ExListView";

    /**
     * 头部
     */
    private ExHeader exHeader;

    /**
     * 底部
     */
    private ExFooter exFooter;

    /**
     * 触摸点X,Y
     */
    private float mXPoint;
    private float mYPoint;

    /**
     * 阻尼系数
     */
    private static final float ELASTICITY_COEFFICIENT = 0.65f;

    /**
     * 无效触点
     */
    private static final int INVALID_POINTER = -1;

    /**
     * 活动点
     */
    private int mActivePointerId = INVALID_POINTER;

    /**
     * Item个数
     */
    private int mItemCount;

    /**
     * 下拉接口
     */
    private PullDownImpl mPullDownImpl;

    /**
     * 上拉接口
     */
    private PullMoreImpl mPullMoreImpl;


    public ExListView(Context context) {
        super(context);
        initView();
    }

    public ExListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ExListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    /**
     * 初始化控件
     */
    private void initView(){
        exHeader = new ExHeader(getContext());
        addHeaderView(exHeader);
        exHeader.setVisibility(View.GONE);
        exHeader.initHeader();


        exFooter = new ExFooter(getContext());
        addFooterView(exFooter);
        exFooter.setVisibility(View.GONE);
    }

    /**
     * 避免多指异常
     * @param ev
     */
    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mYPoint = ev.getY(newPointerIndex);
            mActivePointerId = ev.getPointerId(newPointerIndex);
        }
    }

    /**
     * 获取内容
     * @param adapter
     */
    @Override
    public void setAdapter(ListAdapter adapter) {
        super.setAdapter(adapter);
        this.mItemCount = adapter.getCount();
    }

    /**
     * 是否在最顶部
     * @return
     */
    private boolean isOnTop(){
        return getFirstVisiblePosition()==0;
    }

    /**
     * 是否在最底部
     * @return
     */
    private boolean isOnBottom(){
        return getLastVisiblePosition()==mItemCount+1;
    }

    /**
     * 禁用越界滚动
     * @param scrollX
     * @param scrollY
     * @param clampedX
     * @param clampedY
     */
    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        //super.onOverScrolled(scrollX*5, scrollY*5, clampedX, clampedY);
    }

    /**
     * 拦截事件
     * @param ev
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mXPoint = ev.getX();
                mYPoint = ev.getY();
                mActivePointerId = ev.getPointerId(0);
                break;
            case MotionEvent.ACTION_MOVE:
                if(Math.abs(ev.getY() - mYPoint) > 5){

                    return true;
                }
                break;
            case MotionEvent.ACTION_CANCEL:

        }
        return super.onInterceptTouchEvent(ev);
    }

    /**
     * 处理事件
     * @param ev
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_POINTER_DOWN:
                final int index = ev.getActionIndex();
                mYPoint = (int) ev.getY(index);
                mActivePointerId = ev.getPointerId(index);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                if (mActivePointerId != INVALID_POINTER) {
                    mYPoint = (int) ev.getY(ev.findPointerIndex(mActivePointerId));
                }
                break;
            case MotionEvent.ACTION_DOWN:
                exHeader.setOnTouching(true);
                mYPoint = (int) ev.getY();
                mActivePointerId = ev.getPointerId(0);
                break;
            case MotionEvent.ACTION_MOVE:
                final int activePointerIndex = ev.findPointerIndex(mActivePointerId);
                if (activePointerIndex == INVALID_POINTER) {
                    break;
                }
                final float pointY = ev.getY(activePointerIndex);

                float deltaY = (pointY - mYPoint);
                mYPoint = pointY;

                if(isOnTop()){
                    //位于最顶部
                    if(deltaY>0 || exHeader.getExHeight()>0) {
                        deltaY *= ELASTICITY_COEFFICIENT;
                        //smoothScrollBy(0, (int) deltaY);
                        exHeader.setExHeight((int) deltaY);
                        setSelection(0);
                    }
                }else{
                    //最顶部消失
                    if(exFooter.getVisibility()!=View.VISIBLE){
                        exFooter.setVisibility(View.VISIBLE);
                        if(DEBUG) Log.w(TAG,"Footer...Visible");
                    }else if(isOnBottom() && deltaY<0){
                        //if(DEBUG) Log.w(TAG,"Footer...Pull");
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                exHeader.setOnTouching(false);
                mActivePointerId = INVALID_POINTER;
                if(exHeader.getExHeight()>0){
                    exHeader.resetHeader();
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    /**
     * 监听下拉事件
     */
    public void setOnPullDownListener(PullDownImpl mPullDownImpl){
        this.exHeader.setVisibility(View.VISIBLE);
        this.exHeader.setOnPullDownListener(mPullDownImpl);
        this.mPullDownImpl = mPullDownImpl;
    }

    public void setPullDownComplete(CharSequence str){
        this.exHeader.setComplete(str);
    }

    /**
     * 监听上拉事件
     */
    public void setOnPullUpListener(PullMoreImpl mPullMoreImpl){
        this.exFooter.setVisibility(View.VISIBLE);
        this.mPullMoreImpl = mPullMoreImpl;
    }


}
