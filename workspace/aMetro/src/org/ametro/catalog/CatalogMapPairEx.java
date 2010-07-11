package org.ametro.catalog;

public class CatalogMapPairEx extends CatalogMapPair {

	private boolean mIsCheckable;
	private boolean mIsChecked;

	public CatalogMapPairEx(CatalogMap mLocal, CatalogMap mRemote, int preffered, boolean checkable, boolean checked) {
		super(mLocal, mRemote, preffered);
		this.mIsCheckable = checkable;
		this.mIsChecked = checked;
	}

	public CatalogMapPairEx(CatalogMapPair src, boolean checkable, boolean checked) {
		super(src.mLocal, src.mRemote, src.mPreffered);
		this.mIsCheckable = checkable;
		this.mIsChecked = checked;
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
