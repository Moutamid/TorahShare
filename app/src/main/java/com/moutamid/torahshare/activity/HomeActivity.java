package com.moutamid.torahshare.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.moutamid.torahshare.R;
import com.moutamid.torahshare.activity.approval.ApprovalActivity;
import com.moutamid.torahshare.authentication.RegistrationActivity;
import com.moutamid.torahshare.databinding.ActivityHomeBinding;
import com.moutamid.torahshare.fragments.MessagesFragment;
import com.moutamid.torahshare.fragments.ProfileFragment;
import com.moutamid.torahshare.fragments.search.SearchFragment;
import com.moutamid.torahshare.utils.Constants;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding b;
    private ViewPagerFragmentAdapter viewPagerFragmentAdapter;
    private NonSwipableViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        if (Constants.auth().getCurrentUser() == null) {
            startActivity(new Intent(this, RegistrationActivity.class));
            return;
        }

        viewPager = findViewById(R.id.main_view_pager);

        viewPagerFragmentAdapter = new ViewPagerFragmentAdapter(getSupportFragmentManager());

//        controller.fetchAllPolygonBoundaries();
//        controller.fetchAllLatLngsOfCities();

        // Setting up the view Pager
        setupViewPager(viewPager);

        b.addBottomBarBtn.setOnClickListener(view -> {
//            UserModel userModel = (UserModel) Stash.getObject(Constants.CURRENT_USER_MODEL, UserModel.class);
//            if (userModel.isApproved) {
//                 USER IS APPROVED
//            } else {
            startActivity(new Intent(HomeActivity.this, ApprovalActivity.class));
//            }
        });
        b.searchBottomBarBtn.setOnClickListener(view -> {
            viewPager.setCurrentItem(0);
            b.bottomBarImageview.setImageResource(R.drawable.ic_bottom_search);
        });
        b.messagesBottomBarBtn.setOnClickListener(view -> {
            viewPager.setCurrentItem(1);
            b.bottomBarImageview.setImageResource(R.drawable.ic_bottom_messages);
        });
        b.profileBottomBarBtn.setOnClickListener(view -> {
            viewPager.setCurrentItem(2);
            b.bottomBarImageview.setImageResource(R.drawable.ic_bottom_profile);
        });
    }

    private static final String TAG = "HomeActivity";

    private void setupViewPager(ViewPager viewPager) {
        // Adding Fragments to Adapter
//        adapter.addFragment(new SaveFragment());
        viewPagerFragmentAdapter.addFragment(new SearchFragment());
        viewPagerFragmentAdapter.addFragment(new MessagesFragment());
        viewPagerFragmentAdapter.addFragment(new ProfileFragment());

        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(viewPagerFragmentAdapter);

        Log.d(TAG, "setupViewPager: adapter attached");

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float v, int i1) {
            }

            @Override
            public void onPageSelected(int position) {
//                if (position == 0) {
//                    changeNavTo(b.homeDotBtnNav, b.homeBtnNavMain, R.drawable.ic_selected_home_24);
//                } else
                if (position == 0) {
                    b.bottomBarImageview.setImageResource(R.drawable.ic_bottom_search);
                } else if (position == 1) {
                    b.bottomBarImageview.setImageResource(R.drawable.ic_bottom_messages);

                } else if (position == 2) {
                    b.bottomBarImageview.setImageResource(R.drawable.ic_bottom_profile);
                }
            }

            int lastPosition = 0;

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);

    }

    public static class NonSwipableViewPager extends ViewPager {


        public NonSwipableViewPager(@NonNull Context context) {
            super(context);
        }

        public NonSwipableViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            return false;
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            return false;
        }

        public void setMyScroller() {

            try {
                Class<?> viewpager = ViewPager.class;
                Field scroller = viewpager.getDeclaredField("mScroller");
                scroller.setAccessible(true);
                scroller.set(this, new NonSwipableViewPager.MyScroller(getContext()));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        public class MyScroller extends Scroller {

            public MyScroller(Context context) {
                super(context, new DecelerateInterpolator());
            }

            @Override
            public void startScroll(int startX, int startY, int dx, int dy, int duration) {
                super.startScroll(startX, startY, dx, dy, 350);
            }
        }
    }

    public static class ViewPagerFragmentAdapter extends FragmentStatePagerAdapter {

        private final List<Fragment> mFragmentList = new ArrayList<>();

        public void addFragment(Fragment fragment) {
            mFragmentList.add(fragment);
        }

        public ViewPagerFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }
    }


}