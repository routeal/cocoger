package com.routeal.cocoger.ui.login;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.RadioGroup;

import com.routeal.cocoger.R;

/**
 * Created by nabe on 8/21/17.
 */

public class LoginActivity extends FragmentActivity
        implements ViewPager.OnPageChangeListener, RadioGroup.OnCheckedChangeListener {

    private static final int NUMBER_OF_PAGES = 2;
    ViewPager pager;
    private RadioGroup radioGroup;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        pager = (ViewPager) findViewById(R.id.viewPager);
        pager.setAdapter(new LoginPagerAdapter(getSupportFragmentManager()));
        pager.addOnPageChangeListener(this);

        radioGroup = (RadioGroup) findViewById(R.id.radiogroup);
        radioGroup.setOnCheckedChangeListener(this);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        switch (position) {
            case 0:
                radioGroup.check(R.id.radioButton1);
                break;
            case 1:
                radioGroup.check(R.id.radioButton2);
                break;
            default:
                radioGroup.check(R.id.radioButton1);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        switch (checkedId) {
            case R.id.radioButton1:
                pager.setCurrentItem(0);
                break;
            case R.id.radioButton2:
                pager.setCurrentItem(1);
                break;
        }
    }

    private class LoginPagerAdapter extends FragmentPagerAdapter {

        LoginFragment login = new LoginFragment();
        PasswordFragment password = new PasswordFragment();

        LoginPagerAdapter(FragmentManager fm) {
            super(fm);
            password.setLoginFragment(login);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment;
            switch (position) {
                case 1:
                    fragment = password;
                    break;
                case 0:
                default:
                    fragment = login;
                    break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return NUMBER_OF_PAGES;
        }
    }

}
