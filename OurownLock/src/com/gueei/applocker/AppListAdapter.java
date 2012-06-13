package com.gueei.applocker;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gueei.applocker.R;

public class AppListAdapter extends BaseAdapter {

	private static View toggleImage = null;
	private static boolean b = false;
	ListInstalledApps lt;
	private LayoutInflater mInflater;

	private List<App> mApps;
	/** a map which maps the package name of an app to its icon drawable */
	private Map<String, Drawable> mIcons;
	private Drawable mStdImg;


	public AppListAdapter(Context context, boolean b1, ImageView toggleImage1) {
		// cache the LayoutInflater to avoid asking for a new one each time
		mInflater = LayoutInflater.from(context);

		mStdImg = context.getResources().getDrawable(R.drawable.ic_launcher);
		toggleImage=toggleImage1;
		b= b1;

	}

	public int getCount() {
		return mApps.size();
	}

	public Object getItem(int position) {
		return mApps.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		AppViewHolder holder;

		if(convertView == null) {
			convertView = mInflater.inflate(R.layout.row, null);

			// creates a ViewHolder and stores a reference to the children view we want to bind data to
			holder = new AppViewHolder();
			holder.mTitle = (TextView) convertView.findViewById(R.id.apptitle);
			holder.mIcon = (ImageView) convertView.findViewById(R.id.appicon);
			holder.toggle = (ImageView) convertView.findViewById(R.id.toggleImage);			
			convertView.setTag(holder);
		}
		else { 
			// reuse/overwrite the view passed assuming(!) that it is castable!
			holder = (AppViewHolder) convertView.getTag();
		}

		App app = mApps.get(position);
		holder.setTitle(app.getTitle());
		if (mIcons == null || mIcons.get(app.getPackageName()) == null) {
			holder.setIcon(mStdImg);
		} else {
			holder.setIcon(mIcons.get(app.getPackageName()));
		}

		if (b==true){
			holder.toggle.setBackgroundResource(R.drawable.enter);// .setVisibility(View.GONE);
		}
		else{
			holder.toggle.setBackgroundResource(R.drawable.wrong);// .setVisibility(View.VISIBLE);
		}



		return convertView; 
	}
	public void setListItems(List<App> list) { 
		mApps = list; 
	}


	public void setIcons(Map<String, Drawable> icons) {
		this.mIcons = icons;
	}


	public Map<String, Drawable> getIcons() {
		return mIcons;
	}


	public class AppViewHolder {
		public ImageView toggle;
		private TextView mTitle;
		private ImageView mIcon;		
		public void setTitle(String title) {
			mTitle.setText(title);
		}
		public void setIcon(Drawable img) {
			if (img != null) {
				mIcon.setImageDrawable(img);
			}
		}
	}
}
