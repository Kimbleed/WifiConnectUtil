package com.example.a49479.wificonnectutil;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.text.ParseException;
import java.util.List;

public abstract class CommonAdapter<T> extends BaseAdapter
{
	protected LayoutInflater mInflater;
	protected Context mContext;
	protected List<T> mDatas;
	protected final int mItemLayoutId;

	public CommonAdapter(Context context, List<T> mDatas, int itemLayoutId)
	{
		this.mContext = context;
		this.mInflater = LayoutInflater.from(mContext);
		this.mDatas = mDatas;
		this.mItemLayoutId = itemLayoutId;
	}

	@Override
	public int getCount()
	{
		return mDatas.size();
	}

	@Override
	public T getItem(int position)
	{
		return mDatas.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		final ViewHolder viewHolder = getViewHolder(position, convertView,	parent);
		try {
			convert(viewHolder, getItem(position), convertView, position);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return viewHolder.getConvertView();

	}

	public abstract void convert(ViewHolder helper, T item, View convertView, int position) throws ParseException;

	private ViewHolder getViewHolder(int position, View convertView, ViewGroup parent)
	{
		return ViewHolder.get(mContext, convertView, parent, mItemLayoutId,	position);
	}

	public void update(List<T> mDatas)
	{
		this.mDatas = mDatas;
		this.notifyDataSetChanged();
	}
}
