package com.umeng.android.dialog;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.umeng.android.activity.BaseActivity;
import com.umeng.android.activity.MessageCenterActivity;
import com.umeng.android.activity.ProductDetailActivity;
import com.umeng.android.activity.ProductsActivity;
import com.umeng.android.common.AppApplication;
import com.umeng.android.common.Constants;
import com.umeng.android.util.NetManager;
import com.umeng.android.util.StringUtil;
import com.umeng.android.util.ToastUtils;
import com.umeng.client.R;

public class DialogManager {

	/**
	 * create a exit dialog
	 * 
	 * @param context
	 * @return
	 */
	public static AlertDialog getExitDialog(Context context) {
		if (!(context instanceof BaseActivity)) {
			return null;
		}
		final BaseActivity baseActivity = (BaseActivity) context;
		Builder builder = new AlertDialog.Builder(context).setTitle("")
				.setMessage(R.string.wether_exit);
		builder.setPositiveButton(R.string.exit_yes,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						baseActivity.exit();
					}
				});
		builder.setNegativeButton(R.string.exit_not,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		return builder.create();
	}

	/**
	 * create a exit dialog
	 * 
	 * @param context
	 * @return
	 */
	public static AlertDialog getLogoutDialog(Context context) {
		if (!(context instanceof BaseActivity)) {
			return null;
		}
		final BaseActivity baseActivity = (BaseActivity) context;
		Builder builder = new AlertDialog.Builder(context).setTitle("")
				.setMessage(R.string.log_out);
		builder.setPositiveButton(R.string.exit_yes,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						baseActivity.exit();
					}
				});
		builder.setNegativeButton(R.string.exit_not,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		return builder.create();
	}

	/**
	 * create a loading dialog
	 * 
	 * @param context
	 * @return
	 */
	public static Dialog getLoadingDialog(Context context) {
		return getLoadingDialog(context, 0);
	}

	/**
	 * create a loading dialog using message
	 * 
	 * @param context
	 * @param message
	 * @return
	 */
	public static Dialog getLoadingDialog(Context context, int rid) {
		Dialog loadingDialog = new Dialog(context, R.style.LodingDialog) {

			@Override
			public void onBackPressed() {
				return;
			}
		};
		loadingDialog.setContentView(R.layout.trans_dialog);
		loadingDialog.setCanceledOnTouchOutside(false);
		if (rid > 0) {
			((TextView) loadingDialog.findViewById(R.id.title)).setText(rid);
		}
		return loadingDialog;
	}

	/**
	 * obtain a dialog
	 * 
	 * @param context
	 * @param position
	 * @return
	 */
	public static Dialog getTopDialog(Context context, final int position) {
		if (!(context instanceof ProductsActivity)) {
			return null;
		}
		final ProductsActivity productsActivity = (ProductsActivity) context;
		if (productsActivity.getApps() == null
				|| position > productsActivity.getApps().size()
				|| position == 0) {
			return null;
		}
		final Dialog dialog = new Dialog(context, R.style.LodingDialog);
		dialog.setCanceledOnTouchOutside(true);
		LayoutInflater inflater = productsActivity.getLayoutInflater();
		LinearLayout lin = (LinearLayout) inflater.inflate(
				R.layout.settotop_dialog, null);
		dialog.setContentView(lin, new LayoutParams(
				(int) (AppApplication.width / 1.3), LayoutParams.WRAP_CONTENT));
		TextView settop_title = (TextView) dialog
				.findViewById(R.id.settop_text);
		settop_title.setText(StringUtil.cutString(productsActivity.getApps()
				.get(position - 1).getName(), 130));
		TextView settop_lin_1 = (TextView) dialog
				.findViewById(R.id.settop_lin_1);
		settop_lin_1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SharedPreferences seeAppNumbers = productsActivity
						.getPreferences(Context.MODE_PRIVATE);
				int appNums = seeAppNumbers.getInt(
						ProductsActivity.APP_CLICK_NUMBER, 0);
				appNums++;
				seeAppNumbers.edit()
						.putInt(ProductsActivity.APP_CLICK_NUMBER, appNums)
						.commit();
				dialog.dismiss();
				Intent intent = new Intent(productsActivity,
						ProductDetailActivity.class);
				intent.putExtra(Constants.APP,
						productsActivity.getApps().get(position - 1));
				if (!NetManager.isOnline(productsActivity)) {
					ToastUtils.showMessageShort(productsActivity,
							R.string.net_error);
					return;
				}
				productsActivity.startActivity(intent);
			}
		});
		TextView settop_lin_2 = (TextView) dialog
				.findViewById(R.id.settop_lin_2);
		settop_lin_2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						productsActivity.getAddStarData(position);
					}
				}).start();
			}
		});
		return dialog;
	}

	/**
	 * obtain a dialog
	 * 
	 * @param context
	 * @param position
	 * @return
	 */
	public static Dialog getDeleteMessageDialog(Context context,final String id) {
		if (!(context instanceof MessageCenterActivity)) {
			return null;
		}
		final MessageCenterActivity centerActivity = (MessageCenterActivity) context;

		final Dialog dialog = new Dialog(context, R.style.MyDialog);
		LayoutInflater inflater = centerActivity.getLayoutInflater();
		View lin = inflater.inflate(R.layout.delete_message_dialog, null);
		dialog.setContentView(lin);
		TextView ok = (TextView) dialog.findViewById(R.id.ok);
		TextView cancel = (TextView) dialog.findViewById(R.id.cancel);
		ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				centerActivity.deleteMessage(id);
				dialog.dismiss();
			}
		});
		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});
		return dialog;
	}
}
