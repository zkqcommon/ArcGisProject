package org.sdujq.frame;

import java.util.ArrayList;

import org.sdu.gis.R;

import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TopMenu {
	// 包含菜单的ArrayList
	private ArrayList<TextView> menuList = null;
	private FrameActivity activity;
	private TextView textView = null;
	private int menuCount=0;
	public TopMenu(FrameActivity activity) {
		this.activity = activity;
		menuList = new ArrayList<TextView>();
		if(activity.info.getViews()!=null){
			slideMenuOnChange(activity.info.getViews()[0][0]);
		}
	}

	/**
	 * 顶部滑动菜单布局
	 * 
	 * @param menuTextViews
	 * @param layoutWidth
	 */
	public View getSlideMenuLinerLayout(AbsShow[] menuTextViews, int layoutWidth) {
		// 包含TextView的LinearLayout
		LinearLayout menuLinerLayout = new LinearLayout(activity);
		menuLinerLayout.setOrientation(LinearLayout.HORIZONTAL);

		// 参数设置
		LinearLayout.LayoutParams menuLinerLayoutParames = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT, 1);
		menuLinerLayoutParames.gravity = Gravity.CENTER_HORIZONTAL;

		// 添加TextView控件
		for (int i = 0; i < menuTextViews.length; i++) {
			TextView tvMenu = new TextView(activity);
			// 设置标识值
			tvMenu.setTag(menuTextViews[i]);
			tvMenu.setLayoutParams(new LayoutParams(layoutWidth / 4, 35));
			tvMenu.setPadding(30, 14, 30, 10);
			tvMenu.setText(menuTextViews[i].name);
			tvMenu.setTextColor(Color.WHITE);
			tvMenu.setGravity(Gravity.CENTER_HORIZONTAL);
			tvMenu.setOnClickListener(SlideMenuOnClickListener);

			// 菜单项计数
			menuCount++;

			// 设置第一个菜单项背景
			if (menuCount == 1) {
				tvMenu.setBackgroundResource(R.drawable.menu_bg);
			}

			menuLinerLayout.addView(tvMenu, menuLinerLayoutParames);
			menuList.add(tvMenu);
		}

		return menuLinerLayout;
	}

	// 单个菜单事件
	OnClickListener SlideMenuOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			AbsShow menuTag = (AbsShow) v.getTag();

			if (v.isClickable()) {
				textView = (TextView) v;
				Log.i("SlideMenu", "width：" + textView.getWidth() + "height："
						+ textView.getHeight());

				textView.setBackgroundResource(R.drawable.menu_bg);

				for (int i = 0; i < menuList.size(); i++) {
					if (!menuTag.name.equals(menuList.get(i).getText())) {
						menuList.get(i).setBackgroundDrawable(null);
					}
				}

				// 点击菜单时改变内容
				slideMenuOnChange(menuTag);
			}
		}
	};

	// TODO changed
	private void slideMenuOnChange(AbsShow show) {
			show.show();
	}
}