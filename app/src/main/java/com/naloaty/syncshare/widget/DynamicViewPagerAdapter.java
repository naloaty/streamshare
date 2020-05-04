package com.naloaty.syncshare.widget;

import android.view.View;
import android.view.ViewGroup;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.List;


public class DynamicViewPagerAdapter extends PagerAdapter
{
    private List<View> views = new ArrayList<View>();

    @Override
    public int getItemPosition(Object object)
    {
        int index = views.indexOf(object);
        if (index == -1)
            return POSITION_NONE;
        else
            return index;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position)
    {
        View v = views.get(position);
        container.addView(v);
        return v;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object)
    {
        container.removeView(views.get(position));
    }

    @Override
    public int getCount()
    {
        return views.size();
    }


    @Override
    public boolean isViewFromObject(View view, Object object)
    {
        return view == object;
    }


    public int addView(View v)
    {
        return addView(v, views.size());
    }

    public int addView(View v, int position)
    {
        views.add(position, v);
        return position;
    }

    public int removeView(ViewPager pager, View v)
    {
        return removeView(pager, views.indexOf(v));
    }

    public int removeView(ViewPager pager, int position)
    {
        /*
         * Note that we set the adapter to null before removing the
         * view from "views" - that's because while ViewPager deletes all its views,
         * it will call destroyItem which will in turn cause a null pointer ref.
         */
        pager.setAdapter(null);
        views.remove(position);
        pager.setAdapter(this);

        return position;
    }

    public View getView(int position)
    {
        return views.get(position);
    }

}
