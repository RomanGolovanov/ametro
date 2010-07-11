package org.ametro.activity;

import java.util.ArrayList;

import org.ametro.ApplicationEx;
import org.ametro.Constants;
import org.ametro.R;
import org.ametro.adapter.CheckedCatalogAdapter;
import org.ametro.catalog.Catalog;
import org.ametro.catalog.CatalogMap;
import org.ametro.catalog.CatalogMapPair;
import org.ametro.catalog.ICatalogStateProvider;
import org.ametro.catalog.storage.CatalogStorage;
import org.ametro.catalog.storage.CatalogStorageStateProvider;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

public class CatalogMapSelectionActivity extends Activity implements ICatalogStateProvider, OnClickListener, OnFocusChangeListener {

	public static final String EXTRA_REMOTE_ID = "EXTRA_REMOTE_ID";
	public static final String EXTRA_FILTER = "EXTRA_FILTER";
	public static final String EXTRA_SORT_MODE = "EXTRA_SORT_MODE";
	public static final String EXTRA_SELECTION = "EXTRA_SELECTION";

	protected CatalogStorage mStorage;
	protected CatalogStorageStateProvider mStorageState;

	protected CheckedCatalogAdapter mAdapter;
	protected ListView mList;

	protected View mActionBar;
	protected EditText mActionBarEditText;
	protected ImageButton mActionBarCancelButton;

	protected Handler mUIEventDispacher = new Handler();

	private final int MAIN_MENU_DONE = 994;
//	private final int MAIN_MENU_SORT = 995;
//	private final int MAIN_MENU_SEARCH = 996;

	protected Catalog mLocal;
	protected Catalog mRemote;

	protected int mLocalId;
	protected int mRemoteId;

	protected int mDiffMode = CatalogMapPair.DIFF_MODE_REMOTE;
	protected int mDiffColors;

	protected String mFilter;
	protected int mSortMode;
	
	protected boolean mIsActionBarAnimated = false;

	/* package */InputMethodManager mInputMethodManager;

	public boolean onSearchRequested() {
//		if (mActionBar != null && mActionBar.getVisibility() == View.GONE) {
//			setActionBarVisibility(true);
//			return true;
//		}
		return false;
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		int doneText = mRemoteId == CatalogStorage.ONLINE ? R.string.menu_download : R.string.menu_import;
		menu.add(0, MAIN_MENU_DONE, Menu.NONE, doneText).setIcon( android.R.drawable.ic_menu_add);
//		menu.add(0, MAIN_MENU_SEARCH, Menu.NONE, R.string.menu_search).setIcon( android.R.drawable.ic_menu_search);
//		menu.add(0, MAIN_MENU_SORT, Menu.NONE, R.string.menu_sort).setIcon( android.R.drawable.ic_menu_sort_alphabetically);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MAIN_MENU_DONE:
			invokeFinishWithResult();
			return true;
//		case MAIN_MENU_SEARCH:
//			onSearchRequested();
//			return true;
//		case MAIN_MENU_SORT:
//			onSortModeChange();
//			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mActionBar != null
					&& mActionBar.getVisibility() == View.VISIBLE) {
				setActionBarVisibility(false);
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent data = getIntent();
		if (data == null) {
			invokeFinish();
			return;
		}

		mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		mLocalId = CatalogStorage.LOCAL;
		mRemoteId = data.getIntExtra(EXTRA_REMOTE_ID, -1);
		mFilter = data.getStringExtra(EXTRA_FILTER);
		mSortMode = data.getIntExtra(EXTRA_SORT_MODE, CheckedCatalogAdapter.SORT_MODE_COUNTRY);
		
		mDiffMode = CatalogMapPair.DIFF_MODE_REMOTE;
		mDiffColors = mRemoteId == CatalogStorage.ONLINE ? R.array.online_catalog_map_state_colors
				: R.array.import_catalog_map_state_colors;
		mStorage = ((ApplicationEx) getApplicationContext()) .getCatalogStorage();
		mStorageState = new CatalogStorageStateProvider(mStorage);

		mLocal = mStorage.getCatalog(mLocalId);
		mRemote = mStorage.getCatalog(mRemoteId);
		if (mRemote == null || mLocal == null) {
			invokeFinish();
		}

		setListView();
	}

	protected void setListView() {
		mAdapter = new CheckedCatalogAdapter(this, mLocal, mRemote, mDiffMode, mDiffColors, this, mSortMode, mFilter);

		setContentView(R.layout.catalog_list);
		mList = (ListView) findViewById(R.id.list);
		mList.setItemsCanFocus(true);
		mList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		mList.setAdapter(mAdapter);

		mActionBar = (View) findViewById(R.id.actionbar);
		mActionBarEditText = (EditText) findViewById(R.id.actionbar_text);
		mActionBarCancelButton = (ImageButton) findViewById(R.id.actionbar_hide);
		mActionBarCancelButton.setOnClickListener(this);

		mActionBarEditText.addTextChangedListener(mActionTextWatcher);
		mActionBarEditText.setOnFocusChangeListener(this);
	}

	public void onFocusChange(View v, boolean hasFocus) {
		if (v == mActionBarEditText) {
			if (hasFocus) {
				mInputMethodManager.showSoftInput(v, 0);
				Log.i(Constants.LOG_TAG_MAIN, "show IME");
			} else {
				mInputMethodManager.hideSoftInputFromWindow(v.getWindowToken(),
						0);
				Log.i(Constants.LOG_TAG_MAIN, "hide IME");
			}
		}
	}

//	private void onSortModeChange() {
//		AlertDialog.Builder builder = new AlertDialog.Builder(this);
//		builder.setTitle(R.string.menu_sort);
//		builder.setSingleChoiceItems(R.array.sort_modes, mAdapter.getSortMode() == CatalogAdapter.SORT_MODE_CITY ? 0 : 1, 
//			new DialogInterface.OnClickListener() {
//				public void onClick(DialogInterface dialog, int item) {
//					mAdapter.updateSort(item == 0 ? CatalogAdapter.SORT_MODE_CITY : CatalogAdapter.SORT_MODE_COUNTRY);
//					dialog.dismiss();
//				}
//			});
//		AlertDialog alertDialog = builder.create();
//		alertDialog.show();
//	}

//	protected void onSettingsChanged() {
//		String oldLanguage = mAdapter.getLanguage();
//		String newLanguage = GlobalSettings.getLanguage(this);
//		if (!oldLanguage.equalsIgnoreCase(newLanguage)) {
//			mAdapter.updateLanguage();
//		}
//	}

	public void onClick(View v) {
		if (v == mActionBarCancelButton) {
			setActionBarVisibility(false);
		}
	}

	private void setActionBarVisibility(boolean isVisible) {
		if (mIsActionBarAnimated) {
			return;
		}
		final float scale = getResources().getDisplayMetrics().density;
		if (isVisible) {
			if (mActionBar.getVisibility() == View.GONE) {
				mIsActionBarAnimated = true;
				TranslateAnimation anim = new TranslateAnimation(0, 0, -50 * scale, 0);
				anim.setDuration(250);
				anim.setAnimationListener(new AnimationListener() {
					public void onAnimationStart(Animation animation) {
					}

					public void onAnimationRepeat(Animation animation) {
					}

					public void onAnimationEnd(Animation animation) {
						mActionBarEditText.requestFocus();
						mIsActionBarAnimated = false;
					}
				});
				mActionBar.startAnimation(anim);
				mActionBar.setVisibility(View.VISIBLE);
			}
		} else {
			if (mActionBar.getVisibility() == View.VISIBLE) {
				mIsActionBarAnimated = true;
				TranslateAnimation anim = new TranslateAnimation(0, 0, 0, -50
						* scale);
				anim.setDuration(250);
				anim.setAnimationListener(new AnimationListener() {
					public void onAnimationStart(Animation animation) {
					}

					public void onAnimationRepeat(Animation animation) {
					}

					public void onAnimationEnd(Animation animation) {
						mActionBarEditText.setText("");
						mActionBar.setVisibility(View.GONE);
						mList.requestFocus();
						mIsActionBarAnimated = false;
					}
				});
				mActionBar.startAnimation(anim);
			}
		}
	}

	private TextWatcher mActionTextWatcher = new TextWatcher() {
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			mAdapter.getFilter().filter(s);
		}

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		public void afterTextChanged(Editable s) {
		}
	};

	protected void invokeFinish() {
		setResult(RESULT_CANCELED);
		finish();
	}

	protected void invokeFinishWithResult() {

		ArrayList<String> selection = new ArrayList<String>();
		final int len = mList.getCount();
		for (int i = 0; i < len; i++) {
			if (mList.isItemChecked(i)) {
				CatalogMapPair pair = (CatalogMapPair) mList
						.getItemAtPosition(i);
				selection.add(pair.getSystemName());
			}
		}
		Intent data = new Intent();
		data.putExtra(EXTRA_SELECTION, (String[]) selection.toArray(new String[selection.size()]));
		setResult(RESULT_OK, data);
		finish();
	}

	public int getCatalogState(CatalogMap local, CatalogMap remote) {
		return mRemoteId == CatalogStorage.ONLINE ? mStorageState
				.getOnlineCatalogState(local, remote) : mStorageState
				.getImportCatalogState(local, remote);
	}

}
