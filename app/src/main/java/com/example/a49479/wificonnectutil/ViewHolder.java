package com.example.a49479.wificonnectutil;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


public class ViewHolder
{
	private final SparseArray<View> mViews;
	private int mPosition;
	private View mConvertView;

	private ViewHolder(Context context, ViewGroup parent, int layoutId,
					   int position)
	{
		this.mPosition = position;
		this.mViews = new SparseArray<View>();
		mConvertView = LayoutInflater.from(context).inflate(layoutId, parent,	false);
		// setTag
		mConvertView.setTag(this);
	}

	/**
	 * 拿到一个ViewHolder对象
	 * 
	 * @param context
	 * @param convertView
	 * @param parent
	 * @param layoutId
	 * @param position
	 * @return
	 */
	public static ViewHolder get(Context context, View convertView, ViewGroup parent, int layoutId, int position)
	{
		if (convertView == null)
		{
			return new ViewHolder(context, parent, layoutId, position);
		}

		return (ViewHolder) convertView.getTag();
	}

	public View getConvertView()
	{
		return mConvertView;
	}

	/**
	 * 通过控件的Id获取对于的控件，如果没有则加入views
	 * 
	 * @param viewId
	 * @return
	 */
	public <T extends View> T getView(int viewId)
	{
		View view = mViews.get(viewId);
		if (view == null)
		{
			view = mConvertView.findViewById(viewId);
			mViews.put(viewId, view);
		}
		return (T) view;
	}

	/**
	 * 为TextView设置字符串
	 * 
	 * @param viewId
	 * @param text
	 * @return
	 */
	public ViewHolder setText(int viewId, String text)
	{
		TextView view = getView(viewId);
		view.setText(text);
		return this;
	}
	/**
	 * 为TextView设置字符串
	 *
	 * @param viewId
	 * @param textcolor
	 * @return
	 */
	public ViewHolder setTextColor(int viewId, int textcolor)
	{
		TextView view = getView(viewId);
		view.setTextColor(textcolor);
		return this;
	}
	/**
	 * 为ImageView设置图片
	 * 
	 * @param viewId
	 * @param drawableId
	 * @return
	 */
	public ViewHolder setImageResource(int viewId, int drawableId)
	{
		ImageView view = getView(viewId);
		view.setImageResource(drawableId);

		return this;
	}

	/**
	 * 为ImageView设置图片
	 * 
	 * @param viewId
	 * @param
	 * @return
	 */
	public ViewHolder setImageBitmap(int viewId, Bitmap bm)
	{
		ImageView view = getView(viewId);
		view.setImageBitmap(bm);
		return this;
	}

	public int getPosition()
	{
		return mPosition;
	}

	@SuppressWarnings("unchecked")
	private <T extends View> T retrieveView(int viewId) {
		View view = mViews.get(viewId);
		if (view == null) {
			view = mConvertView.findViewById(viewId);
			mViews.put(viewId, view);
		}
		return (T) view;
	}

	public ViewHolder setOnClickListener(int viewId, View.OnClickListener listener) {
		View view = retrieveView(viewId);
		if (view != null && listener != null)
			view.setOnClickListener(listener);
		return this;
	}

	public ViewHolder setEnable(int viewId) {
		View retrieveView = retrieveView(viewId);
		retrieveView.setEnabled(true);
		return this;
	}

	public ViewHolder setDisable(int viewId) {
		View retrieveView = retrieveView(viewId);
		retrieveView.setEnabled(false);
		return this;
	}

	public ViewHolder setHideGone(int viewId) {
		View view = retrieveView(viewId);
		view.setVisibility(View.GONE);
		return this;
	}

	public ViewHolder setHideInvisible(int viewId) {
		View view = retrieveView(viewId);
		view.setVisibility(View.INVISIBLE);
		return this;
	}

	public ViewHolder setVisible(int viewId) {
		View view = retrieveView(viewId);
		view.setVisibility(View.VISIBLE);
		return this;
	}

}
