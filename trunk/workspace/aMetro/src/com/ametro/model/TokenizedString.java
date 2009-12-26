package com.ametro.model;

public class TokenizedString
{
	private String mText;
	private String mDelimeters;
	private int mPos;
	private int mLen;
	
	private boolean mBracketOpened;
	private String mLastDelimeter;
	private String mNextDelimeter;
	
	public boolean isBracketOpened() {
		return mBracketOpened;
	}

	public String getLastDelimeter() {
		return mLastDelimeter;
	}

	public String getNextDelimeter() {
		return mNextDelimeter;
	}

	public TokenizedString(String text, String delimeters){
		text = text.replaceAll(",,",",");
		text = text.replaceAll("\\(,","(");
		text = text.replaceAll(",\\)",")");
		mText = text;
		mDelimeters = delimeters;
		mPos = 0;
		mLen = text.length();
		mBracketOpened = false;
		mLastDelimeter = null; 
		skipToContent();
	}
	
	public boolean hasNext(){
		int saved = mPos;
		skipToContent();
		boolean result = mPos != mLen;
		mPos = saved;
		return result;
	}
	
	public String next(){
		skipToContent();
		int pos = mPos;
		String symbol = null;
		while(pos < mLen && !mDelimeters.contains( symbol = mText.substring(pos,pos+1) )){
			pos++;
		}
		int end = symbol==null ? pos-1 : pos;
		mNextDelimeter = symbol;
		String text = mText.substring(mPos,  end);
		mPos = end;
		return text;
	}
	
	private void skipToContent(){
		String symbol = null;
		while(mPos < mLen && mDelimeters.contains( symbol = mText.substring(mPos,mPos+1) )){
			mLastDelimeter = symbol;
			if(symbol.equals("(")){
				mBracketOpened = true;
			}else if(symbol.equals(")")){
				mBracketOpened = false;
			}
			mPos++;
		}
	}
	
}


