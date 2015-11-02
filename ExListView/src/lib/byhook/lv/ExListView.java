package lib.byhook.lv;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListAdapter;
import android.widget.ListView;

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
     * 数据监听器
     */
    private DataNotify mDataNotify;

    /**
     * 加载更多
     */
    private boolean pullMore;

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
        exHeader.initHeader(getDisplayWidth(),getDisplayHeight());


        exFooter = new ExFooter(getContext());
        addFooterView(exFooter);
        exFooter.setVisibility(View.GONE);
        exFooter.initFooter(getDisplayWidth(),getDisplayHeight());
    }

    /**
     * 获取默认屏幕宽度
     * @return
     */
    private DisplayMetrics getDisplayMetrics(){
        WindowManager windowManager = (WindowManager) getContext().getSystemService(
                Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics;
    }

    private int getDisplayWidth(){
        return  getDisplayMetrics().widthPixels;
    }

    private int getDisplayHeight(){
        return getDisplayMetrics().heightPixels;
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
        this.mItemCount = adapter.getCount()+1+1;  //Header + Footer
        this.mDataNotify = new DataNotify();
        adapter.registerDataSetObserver(mDataNotify);
    }

    /**
     * 销毁窗口
     */
    @Override
    protected void onDetachedFromWindow() {
        if(mDataNotify!=null){
            getAdapter().unregisterDataSetObserver(mDataNotify);
            mDataNotify = null;
        }
        super.onDetachedFromWindow();
    }

    /**
     * 是否在最顶部
     * @return
     */
    private boolean isOnHeader(){
        return getFirstVisiblePosition()==0;
    }

    /**
     * 是否在最底部
     * @return
     */
    private boolean isOnFooter(){
        return getLastVisiblePosition()==mItemCount-1;
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
        if(exFooter.allow() && (clampedY && scrollY>0) || pullMore){
            if(DEBUG) Log.d(TAG,"Ex...onOverScrolled");
            exFooter.pullMore();
        }
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

                //头部不许回调
                exHeader.setOnTouching(true);

                //初始化Footer是否显示
                initFooter();

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
                //头部不许回调
                exHeader.setOnTouching(true);

                //初始化Footer是否显示
                initFooter();

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

                if (isOnHeader()) {
                    //位于最顶部
                    if (exHeader.allow() && (deltaY > 0 || exHeader.getHeight() > 0)) {
                        deltaY *= ELASTICITY_COEFFICIENT;
                        //smoothScrollBy(0, (int) deltaY);
                        exHeader.setExHeight((int) deltaY);
                        setSelection(0);
                    }
                } else if (isOnFooter()) {
                    if(exFooter.allow()){
                        pullMore = true;
                    }
                } else {
                    pullMore = false;
                }
                break;
            case MotionEvent.ACTION_UP:
                exHeader.setOnTouching(false);
                mActivePointerId = INVALID_POINTER;
                if(exHeader.allow() && exHeader.getHeight()>0){
                    exHeader.resetHeader();
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    private void initFooter(){
        if(exFooter.allow() && mItemCount-2>getLastVisiblePosition()-getFirstVisiblePosition()){
            exFooter.showFooter();
        }
    }

    /**
     * 监听下拉事件
     */
    public void setOnPullDownListener(PullDownImpl mPullDownImpl){
        this.exHeader.setVisibility(View.VISIBLE);
        this.exHeader.setOnPullDownListener(mPullDownImpl);
    }

    public void setPullDownComplete(CharSequence str){
        this.mItemCount = getAdapter().getCount();
        this.exHeader.setComplete(str);
    }

    /**
     * 监听上拉事件
     */
    public void setOnPullMoreListener(PullMoreImpl mPullMoreImpl){
        this.exFooter.setVisibility(View.VISIBLE);
        this.exFooter.setOnPullMoreListener(mPullMoreImpl);
    }

    /**
     * 加载更多完成
     */
    public void setPullMoreComplete(){
        this.mItemCount = getAdapter().getCount();
        this.exFooter.setComplete();
    }

    /**
     * 数据更新监听
     */
    private class DataNotify extends DataSetObserver{
        @Override
        public void onChanged() {
            super.onChanged();

        }
    }

}
