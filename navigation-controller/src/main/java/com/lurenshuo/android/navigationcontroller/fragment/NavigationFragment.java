package com.lurenshuo.android.navigationcontroller.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;

import com.lurenshuo.android.navigationcontroller.activity.NavigationActivity;
import com.lurenshuo.android.navigationcontroller.listener.NavigationTouchListener;
import com.lurenshuo.android.navigationcontroller.listener.PopBackListener;

/**
 * 导航：
 * 作为滑动导航的父fragment使用
 * 1,添加fragment时，需要 fragmentTransaction.addToBackStack(null);
 * 2,不能使用 fragmentTransaction.setTransition(XXX) ,否则没有导航view的动画
 * 3,添加切换动画时使用两个参数的，这两个参数的是只做开始的动画，popBackTack的动画由NavigationActivity做
 *  例如：transaction.setCustomAnimations(R.animator.fragment_slide_left_enter,R.animator.fragment_slide_left_exit);
 * Created by lidajun on 17-4-21.
 */

public abstract class NavigationFragment extends Fragment implements NavigationTouchListener {
    public NavigationActivity mActivity;
    public String mAnimatorTitle;

    /**
     * 注册touch事件
     * 添加view
     */
    @Override

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (NavigationActivity) activity;
        mActivity.addNavigationFragment(this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        NavigationFragmentUtil.created(mActivity);
        if (null != mActivity.mNavigationToolbar) {
            mAnimatorTitle = getToolbarTitle();
            mActivity.mNavigationToolbar.mTitleTv.setText(mAnimatorTitle);
        }
    }

    public abstract String getToolbarTitle();

    @Override
    public void onResume() {
        super.onResume();
        NavigationFragmentUtil.initToolbarNavigationText(mActivity,mAnimatorTitle);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            NavigationFragmentUtil.initToolbarNavigationText(mActivity,mAnimatorTitle);
        }
    }

    /**
     * 取消注册touch事件和移除view
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        mActivity.removeNavigationView(this);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        return NavigationFragmentUtil.touchEvent(event, mActivity, new PopBackListener() {
            @Override
            public void popBack() {
                getFragmentManager().popBackStack();
            }
        });
    }
}