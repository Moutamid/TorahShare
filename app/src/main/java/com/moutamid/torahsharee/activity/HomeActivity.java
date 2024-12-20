package com.moutamid.torahsharee.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.Scroller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moutamid.torahsharee.R;
import com.moutamid.torahsharee.activity.addbutton.ApprovalActivity2;
import com.moutamid.torahsharee.authentication.RegistrationActivity;
import com.moutamid.torahsharee.databinding.ActivityHomeBinding;
import com.moutamid.torahsharee.fragments.MessagesFragment;
import com.moutamid.torahsharee.fragments.ProfileFragment;
import com.moutamid.torahsharee.fragments.search.SearchFragment;
import com.moutamid.torahsharee.model.ContactRequestModel;
import com.moutamid.torahsharee.model.PostModel;
import com.moutamid.torahsharee.model.UserModel;
import com.moutamid.torahsharee.utils.Constants;
import com.moutamid.torahsharee.utils.Stash;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding b;
    private ViewPagerFragmentAdapter viewPagerFragmentAdapter;
    private NonSwipableViewPager viewPager;

    @Override
    protected void onResume() {
        super.onResume();
        Constants.checkLanguage(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        Constants.checkLanguage(this);
        b = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        if (Constants.auth().getCurrentUser() == null) {
            startActivity(new Intent(this, RegistrationActivity.class));
            return;
        }

        PostModel dummyPost = new PostModel();

        // Setting dummy data
//        dummyPost.name = "Sunset";
//        dummyPost.date = "2024-11-04";
//        dummyPost.caption = "Enjoying the beautiful sunset!";
//        dummyPost.video_link = "https://firebasestorage.googleapis.com/v0/b/recover-projects-2.appspot.com/o/videos%2Fprimary%3ADCIM%2FCamera%2FVID_20241023_165145.mp41729684335065?alt=media&token=f341e973-aafc-4d3c-adad-b0b2850bb82a";
//        dummyPost.profile_link = "https://firebasestorage.googleapis.com/v0/b/recover-projects-2.appspot.com/o/profile_icon.jpg?alt=media&token=0b817f6f-47ea-4fd7-a584-626bc7212827";
//        dummyPost.my_uid = "bosnWAWIWibXOvZEsePgqqTAY5G2";
//        dummyPost.share_count = 45;
//        dummyPost.comment_count = 0;
//        dummyPost.tagsList = Arrays.asList("sunset", "nature", "photography");
//        dummyPost.thumbnailUrl = "https://en.wikipedia.org/wiki/File:Anatomy_of_a_Sunset-2.jpg";
//        dummyPost.push_key = Constants.databaseReference().child(Constants.PUBLIC_POSTS).push().getKey();
//
//        Constants.databaseReference().child(Constants.PUBLIC_POSTS).push().setValue(dummyPost);

        viewPager = findViewById(R.id.main_view_pager);

        viewPagerFragmentAdapter = new ViewPagerFragmentAdapter(getSupportFragmentManager());

//        controller.fetchAllPolygonBoundaries();
//        controller.fetchAllLatLngsOfCities();

        // Setting up the view Pager
        setupViewPager(viewPager);

        b.addBottomBarBtn.setOnClickListener(view -> {
            UserModel userModel2 = (UserModel) Stash.getObject(Constants.CURRENT_USER_MODEL, UserModel.class);
            if (userModel2.gender.equals(Constants.GENDER_FEMALE)) {
                startActivity(new Intent(HomeActivity.this, WomanApprovalActivity.class));
                return;
            }

            if (userModel2.is_approved) {
//                USER IS APPROVED
                startActivity(new Intent(HomeActivity.this, UploadPostActivity.class));
            } else {
                startActivity(new Intent(HomeActivity.this, ApprovalActivity2.class));
            }
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

        Constants.databaseReference().child(Constants.USERS)
                .child(Constants.auth().getUid())
                .child(Constants.CONTACT_REQUESTS)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            int count = 1;

                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                count++;
                                ContactRequestModel contactRequestModel = dataSnapshot.getValue(ContactRequestModel.class);

                                Stash.put(Constants.CURRENT_CONTACT_REQUEST, contactRequestModel);

                                if (count == 3)
                                    break;
                            }

                            startActivity(new Intent(HomeActivity.this, ContactRequestsActivity.class));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        KeyboardVisibilityEvent.setEventListener(
                HomeActivity.this,
                new KeyboardVisibilityEventListener() {
                    @Override
                    public void onVisibilityChanged(boolean isOpen) {
                        if (isOpen) {
                            b.bottomBarImageview.setVisibility(View.GONE);
                        } else {
                            b.bottomBarImageview.setVisibility(View.VISIBLE);
                        }
                    }
                });

    }

    public ImageView bottomBar() {
        return b.bottomBarImageview;
    }

    public void showFirst(){
        viewPager.setCurrentItem(0);
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