package org.ametro.activity;

import java.util.ArrayList;
import java.util.HashSet;

import org.ametro.ApplicationEx;
import org.ametro.GlobalSettings;
import org.ametro.R;
import org.ametro.adapter.CheckedCatalogAdapter;
import org.ametro.catalog.Catalog;
import org.ametro.catalog.CatalogMap;
import org.ametro.catalog.CatalogMapPair;
import org.ametro.catalog.CatalogMapPairEx;
import org.ametro.catalog.ICatalogStateProvider;
import org.ametro.catalog.storage.CatalogStorage;
import org.ametro.catalog.storage.CatalogStorageStateProvider;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;

public class CatalogMapSelectionActivity extends Activity implements ICatalogStateProvider {

	public static final String EXTRA_REMOTE_ID = "EXTRA_REMOTE_ID";
	public static final String EXTRA_FILTER = "EXTRA_FILTER";
	public static final String EXTRA_CHECKABLE_STATES = "EXTRA_CHECKABLE_STATES";
	public static final String EXTRA_SORT_MODE = "EXTRA_SORT_MODE";
	public static final String EXTRA_SELECTION = "EXTRA_SELECTION";

	private CatalogStorage mStorage;
	private CatalogStorageStateProvider mStorageState;

	private CheckedCatalogAdapter mAdapter;
	private ListView mList;

	private final int MAIN_MENU_DONE = 1;
	private final int MAIN_MENU_SELECT_ALL = 2;
	private final int MAIN_MENU_SELECT_NONE = 3;

	private Catalog mLocal;
	private Catalog mRemote;

	private int mLocalId;
	private int mRemoteId;

	private int mDiffMode = CatalogMapPair.DIFF_MODE_REMOTE;
	private int mDiffColors;

	private String mFilter;
	private int mSortMode;
	
	private HashSet<Integer> mStates;
	
	protected boolean mIsActionBarAnimated = false;

	/* package */InputMethodManager mInputMethodManager;

	public boolean onSearchRequested() {
		return false;
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		int doneText = mRemoteId == CatalogStorage.ONLINE ? R.string.menu_download : R.string.menu_import;
		menu.add(0, MAIN_MENU_DONE, Menu.NONE, doneText).setIcon( android.R.drawable.ic_menu_add);
		menu.add(0, MAIN_MENU_SELECT_ALL, 1, R.string.menu_select_all).setIcon(android.R.drawable.ic_menu_agenda);
		menu.add(0, MAIN_MENU_SELECT_NONE, 2, R.string.menu_select_none).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MAIN_MENU_DONE:
			invokeFinishWithResult();
			return true;
		case MAIN_MENU_SELECT_ALL:
			setCheckAll();
			mList.invalidateViews();
			return true;
		case MAIN_MENU_SELECT_NONE:
			setCheckNone();
			mList.invalidateViews();
			return true;
			
		}
		return super.onOptionsItemSelected(item);
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
		int[] states = data.getIntArrayExtra(EXTRA_CHECKABLE_STATES);
		if(states!=null){
			mStates = new HashSet<Integer>();
			for(int state : states){
				mStates.add(state);
			}
		}
		mSortMode = data.getIntExtra(EXTRA_SORT_MODE, CheckedCatalogAdapter.SORT_MODE_COUNTRY);
		
		mDiffMode = CatalogMapPair.DIFF_MODE_REMOTE;
		mDiffColors = mRemoteId == CatalogStorage.ONLINE ? R.array.online_catalog_map_state_colors : R.array.import_catalog_map_state_colors;
		mStorage = ((ApplicationEx) getApplicationContext()) .getCatalogStorage();
		mStorageState = new CatalogStorageStateProvider(mStorage);

		mLocal = mStorage.getCatalog(mLocalId);
		mRemote = mStorage.getCatalog(mRemoteId);
		if (mRemote == null || mLocal == null) {
			invokeFinish(); 
		}
		
		setContentView(R.layout.catalog_list);
		mList = (ListView) findViewById(R.id.list);
		mList.setItemsCanFocus(true);
		mList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		
		ArrayList<CatalogMapPairEx> values = getFilteredData(CatalogMapPair.diff(mLocal, mRemote, mDiffMode), mFilter);;
		mAdapter = new CheckedCatalogAdapter(this, mList, values, mDiffColors, this, mSortMode);
		mList.setAdapter(mAdapter);
	}

	/*package*/ ArrayList<CatalogMapPairEx> getFilteredData(ArrayList<CatalogMapPair> values, String prefixString) {
		final int count = values.size();
		final String code = GlobalSettings.getLanguage(this);
		final ArrayList<CatalogMapPairEx> newValues = new ArrayList<CatalogMapPairEx>(count);
		for (int i = 0; i < count; i++) {
		    final CatalogMapPair originalValue = values.get(i);
		    final String cityName = originalValue.getCity(code).toString().toLowerCase();
		    final String countryName = originalValue.getCountry(code).toString().toLowerCase();
		    
		    int state = getCatalogState(originalValue.getLocal(), originalValue.getRemote());
		    boolean checkable = mStates==null || mStates.contains(state);
		    
		    final CatalogMapPairEx value = new CatalogMapPairEx(originalValue, checkable, false);
		    // First match against the whole, non-splitted value
		    if (prefixString==null || cityName.startsWith(prefixString) || countryName.startsWith(prefixString)) {
		        newValues.add(value);
		    } else {
		    	boolean added = false;
		        final String[] cityWords = cityName.split(" ");
		        final int cityWordCount = cityWords.length;

		        for (int k = 0; k < cityWordCount; k++) {
		            if (cityWords[k].startsWith(prefixString)) {
		                newValues.add(value);
		                added = true;
		                break;
		            }
		        }
		        if(!added){
			        final String[] countryWords = countryName.split(" ");
			        final int countryWordCount = countryWords.length;

			        for (int k = 0; k < countryWordCount; k++) {
			            if (countryWords[k].startsWith(prefixString)) {
			                newValues.add(value);
			                break;
			            }
			        }
		        }
		    }
		}
		return newValues;
	}

	protected void invokeFinish() {
		setResult(RESULT_CANCELED);
		finish();
	}

	protected void invokeFinishWithResult() {
		ArrayList<String> selection = new ArrayList<String>();
		final int len = mList.getCount();
		for (int i = 0; i < len; i++) {
			if (mList.isItemChecked(i)) {
				CatalogMapPair pair = (CatalogMapPair) mList.getItemAtPosition(i);
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

	
	public void setCheckAll() {
		final int len = mList.getCount();
		for(int i=0;i<len;i++){
			mList.setItemChecked(i, true);
		}
	}

	public void setCheckNone() {
		final int len = mList.getCount();
		for(int i=0;i<len;i++){
			mList.setItemChecked(i, false);
		}
	}
	
}
