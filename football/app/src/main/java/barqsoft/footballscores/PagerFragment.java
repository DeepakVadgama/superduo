package barqsoft.footballscores;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PagerFragment extends Fragment {

    public static final int NUM_PAGES = 5;
    public ViewPager mViewPager;
    private MyPageAdapter mPagerAdapter;
    private MainScreenFragment[] viewFragments = new MainScreenFragment[5];

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.pager_fragment, container, false);
        mViewPager = (ViewPager) rootView.findViewById(R.id.pager);
        mPagerAdapter = new MyPageAdapter(getChildFragmentManager());
        for (int i = 0; i < NUM_PAGES; i++) {
            viewFragments[i] = new MainScreenFragment();
            viewFragments[i].setFragmentDate(getDate(i));
        }
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setCurrentItem(MainActivity.current_fragment);

        mViewPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        // When swiping between pages, select the corresponding tab.
                        getActionBar().setSelectedNavigationItem(position);
                    }
                });

        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        // Create a tab listener that is called when the user changes tabs.
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                // When the tab is selected, switch to the corresponding page in the ViewPager.
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
            }

            @Override
            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
            }
        };

        // Add tabs, specifying the tab's text and TabListener
        for (int i = 0; i < NUM_PAGES; i++) {
            boolean selected = i == 2;
            getActionBar().addTab(
                    getActionBar().newTab()
                            .setText(getDayName(i))
                            .setTabListener(tabListener), selected);
        }

        return rootView;
    }

    // StatePage changed to just page, check if data auto refreshes, or listener needed
    private class MyPageAdapter extends FragmentPagerAdapter {
        @Override
        public Fragment getItem(int i) {
            return viewFragments[i];
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        public MyPageAdapter(FragmentManager fm) {
            super(fm);
        }
    }

    public String getDayName(int position) {

        switch (position) {

            case 1:
                return getContext().getString(R.string.yesterday);
            case 2:
                return getContext().getString(R.string.today);
            case 3:
                return getContext().getString(R.string.tomorrow);
            case 0: {
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DATE, -2);
                return calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
            }
            case 4: {
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DATE, 2);
                return calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
            }
        }

        return getContext().getString(R.string.Unknown);
    }


    private ActionBar getActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    private String getDate(int i) {
        Date fragmentdate = new Date(System.currentTimeMillis() + ((i - 2) * 86400000));
        SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd", getResources().getConfiguration().locale);
        return mformat.format(fragmentdate);
    }

}
