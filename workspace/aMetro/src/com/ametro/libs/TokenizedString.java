package com.ametro.libs;

public class TokenizedString
{
	private String mText;
	private String mDelimeters;
	private int mPos;
	private int mLen;
	
	private boolean mBracketOpened;
	private boolean mQuoteOpened;
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
		mQuoteOpened = false;
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
		if(mPos==mLen){
			return "";
		}
		int pos = mPos;
		String symbol = null;
		boolean quotes = false;
		while(pos < mLen && ( !mDelimeters.contains( symbol = mText.substring(pos,pos+1)) || quotes  )  ){
			if("\"".equals(symbol)){
				quotes = !quotes;
			}
			pos++;
		}
		int end = symbol==null ? pos-1 : pos;
		mNextDelimeter = symbol;
		String text = mText.substring(mPos,  end);
		mPos = end;
		if(text.startsWith("\"")) 
			text = text.substring(1);
		if(text.endsWith("\"")) 
			text = text.substring(0,text.length()-1);
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


