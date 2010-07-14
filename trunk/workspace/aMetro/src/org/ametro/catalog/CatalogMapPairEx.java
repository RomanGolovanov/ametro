package org.ametro.catalog;

public class CatalogMapPairEx extends CatalogMapPair {

	private boolean mIsVisible;
	private boolean mIsCheckable;
	private boolean mIsChecked;

	public CatalogMapPairEx(CatalogMap mLocal, CatalogMap mRemote, int preffered, boolean checkable, boolean checked, boolean visible) {
		super(mLocal, mRemote, preffered);
		this.mIsCheckable = checkable;
		this.mIsChecked = checked;
		this.mIsVisible = visible;
	}

	public CatalogMapPairEx(CatalogMapPair src, boolean checkable, boolean checked, boolean visible) {
		super(src.mLocal, src.mRemote, src.mPreffered);
		this.mIsCheckable = checkable;
		this.mIsChecked = checked;
		this.mIsVisible = visible;
	}

	public boolean isVisible() {
		return mIsVisible;
	}
	
	public boolean isCheckable() {
		return mIsCheckable;
	}

	public boolean isChecked() {
		return mIsChecked;
	}

	public void setChecked(boolean checked) {
		mIsChecked = checked;
	}

}
