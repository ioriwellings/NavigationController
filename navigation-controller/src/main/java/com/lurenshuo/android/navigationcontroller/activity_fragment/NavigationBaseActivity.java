package com.lurenshuo.android.navigationcontroller.activity_fragment;

import android.animation.ValueAnimator;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.lurenshuo.android.navigationcontroller.listener.NavigationTouchListener;
import com.lurenshuo.android.navigationcontroller.utils.DisplayUtil;
import com.lurenshuo.android.navigationcontroller.utils.ScreenUtil;
import com.lurenshuo.android.navigationcontroller.widget.NavigationToolbar;

import java.util.ArrayList;

/**
 * Created by lidajun on 17-6-22.
 */

abstract class NavigationBaseActivity extends AppCompatActivity {
    float mDownY;
    float mDownX;
    NavigationToolbar mNavigationToolbar;
    float edgeSize;
    //动画中
    public boolean inAnimator = false;
    int mScreenWidth;
    // 保存MyTouchListener接口的列表
    ArrayList<NavigationTouchListener> mListeners = new ArrayList<>();
    public ScrollMode mScrollMode = ScrollMode.EDGE;
    GestureDetector mGestureDetector;
    //边的大小，屏幕的20分之1
    int EDGE_SIZE = 20;
    //超过多少，跳转，屏幕的4分之1
    int NAVI_BOUNDED = 4;
    private int scrollMinDistance;
    //导航模式开启中
    public boolean inNavigation = false;
    //已确定事件分发
    boolean determined = false;
    //fragment帮助类
    NavigationFragmentHelper mFragmentHelper;
    //回退的前缀
    public String backPrefix = "<";

    /**
     * 模式
     * 全屏or边
     * 全屏：目前没有解决与其它子控件的onLongClickListener冲突
     * 因为LongClick使用的时间的是move的eventTime和downTime的时间，没有走up，如果不给它down事件，其它的view的事件都会失效
     * 可以把执行longClick时先判断inNavigation,如果不是导航中，再去执行
     */
    public enum ScrollMode {
        FULL_SCREEN, EDGE
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGestureDetector = new GestureDetector(new MyDetector());
        mFragmentHelper = new NavigationFragmentHelper(this);
        scrollMinDistance = ViewConfiguration.get(this).getScaledTouchSlop();
        initScreenSize();
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        mNavigationToolbar = initNavigationToolbar();
        if (null != mNavigationToolbar) {
            mNavigationToolbar.mBackTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        initScreenSize();
    }

    private void initScreenSize() {
        mScreenWidth = ScreenUtil.getScreenWidth(this);
        edgeSize = Math.max(mScreenWidth / EDGE_SIZE, scrollMinDistance * 2);
    }

    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
    }

    protected abstract NavigationToolbar initNavigationToolbar();

    /**
     * 分发触摸事件给所有注册了MyTouchListener的接口
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        NavigationTouchListener listener = getLastTouchListener();
        if (null != listener && mListeners.size() > 1) {
            if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                if (inAnimator) {
                    return false;
                }
            }
            if (mScrollMode == ScrollMode.EDGE) {
                //如果是边模式，只跟据位置判断
                if (ev.getX() < edgeSize && ev.getAction() == MotionEvent.ACTION_DOWN) {
                    return onTouchEvent(ev);
                }
            } else if (mScrollMode == ScrollMode.FULL_SCREEN) {
                //如果滑到边，按边的模式
                if (ev.getX() < edgeSize && ev.getAction() == MotionEvent.ACTION_DOWN) {
                    return onTouchEvent(ev);
                }
                if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                    mDownY = ev.getRawY();
                    mDownX = ev.getRawX();
                    //重执导航中和已确定方向为false
                    determined = false;
                    inNavigation = false;
                }
                if (ev.getAction() == MotionEvent.ACTION_UP) {
                    if (inNavigation) {
                        inNavigation = false;
                        return onTouchEvent(ev);
                    }
                    //重执导航中和已确定方向为false
                    determined = false;
                    inNavigation = false;
                    return super.dispatchTouchEvent(ev);
                }

                //判断滑动方向
                if (!scrollDistanceMinDistance(mDownX, mDownY, ev.getRawX(), ev.getRawY()) && !determined) {
                    //已确定方向
                    if (scrollDistanceMinDistance(mDownX, mDownY, ev.getRawX(), ev.getRawY())) {
                        determined = true;
                    }
                    //判断方向，返回true表示确定是左右，返回false，不一定是确定完了，所以要用时间或距离进行确定，如果在时间距离之后，仍然没有确定是左右，那么就是上下了
                    if (mGestureDetector.onTouchEvent(ev)) {
                        inNavigation = true;
                        determined = true;
                        ev.setAction(MotionEvent.ACTION_DOWN);
                        return onTouchEvent(ev);
                    } else {
                        return super.dispatchTouchEvent(ev);
                    }
                } else {
                    if (inNavigation) {
                        return onTouchEvent(ev);
                    }
                    return super.dispatchTouchEvent(ev);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 按下返回键的监听，如果是messageItem页面就可以退出
     */
    @Override
    public void onBackPressed() {
        if (mListeners.size() == 1) {
            backPressed();
        } else {
            popBackStack();
        }
    }

    abstract void popBackStack();

    NavigationTouchListener getLastTouchListener() {
        if (mListeners.size() > 1) {
            return mListeners.get(mListeners.size() - 1);
        } else {
            return null;
        }
    }

    /**
     * 注册
     * 把Activity的touch事件传递给fragment
     */
    void registerNavigationTouchListener(NavigationTouchListener listener) {
        mListeners.add(listener);
        viewChange(mListeners.size() - 1);
    }

    /**
     * 取消
     * 把Activity的touch事件传递给fragment
     */
    void unRegisterNavigationTouchListener(NavigationTouchListener listener) {
        mListeners.remove(listener);
    }

    /**
     * @return 滑动的距离超过最小识别距离
     */
    boolean scrollDistanceMinDistance(float downX, float downY, float x, float y) {
        return Math.abs(downX - x) > scrollMinDistance || Math.abs(downY - y) > scrollMinDistance;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View currentView = getCurrentView();
            if (null != currentView) {
                if (currentView.getVisibility() != View.VISIBLE) {
                    currentView.setVisibility(View.VISIBLE);
                }
            }
        }
        NavigationTouchListener listener = getLastTouchListener();
        if (null != listener) {
            return listener.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }

    abstract View getNavigationView();

    abstract View getCurrentView();

    abstract String getNavigationText();

    abstract String getNavigationBackText();

    void backStack(final View currentView, final View popBackView) {
        final int PX = DisplayUtil.dip2px(this, 100);
        if (null == currentView || null == popBackView) {
            return;
        }
        ValueAnimator animator = ValueAnimator.ofFloat(0, PX);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                if (value >= PX) {
                    //让栈顶的fragment出栈
                    getFragmentManager().popBackStack();
                    viewChange(mListeners.size() - 2);
                }
                popBackView.setVisibility(View.VISIBLE);
                currentView.setX(value);
            }
        });
        animator.setDuration(100);
        animator.start();

        ValueAnimator alphaAnimator = ValueAnimator.ofFloat(1.0f, 0.0f);
        alphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                currentView.setAlpha((Float) animation.getAnimatedValue());
            }
        });
        alphaAnimator.setDuration(99);
        alphaAnimator.start();
    }

    /**
     * 在根fragment时按返回键
     */
    protected abstract void backPressed();

    /**
     * 功换view
     * Start at page 0
     *
     * @param page 从0页开始
     */
    public void viewChange(float page) {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFragmentHelper = null;
        mGestureDetector = null;
        mListeners = null;
        mNavigationToolbar = null;
    }

    private class MyDetector extends GestureDetector.SimpleOnGestureListener {
        /**
         * @return true: 右左 ；false: 上下
         */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return Math.abs(distanceY) < Math.abs(distanceX);
        }
    }
}