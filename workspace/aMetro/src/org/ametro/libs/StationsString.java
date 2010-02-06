package org.ametro.libs;

public class StationsString
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

	public StationsString(String text){
		mText = text;
		mDelimeters = ",()";
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
		if(text.startsWith("\"") && text.endsWith("\"")) 
			text = text.substring(1,text.length()-1);
		return text;
	}

	private void skipToContent(){
		String symbol = null;
		String symbolNext = (mPos < mLen) ? mText.substring(mPos,mPos+1) : null;
		while(mPos < mLen && mDelimeters.contains( symbol = symbolNext )){
			mLastDelimeter = symbol;
			if(symbol.equals("(")){
				mBracketOpened = true;
				mPos++;
				return;
				
			}else if(symbol.equals(")")){
				mBracketOpened = false;
			}
			mPos++;
			symbolNext = (mPos < mLen) ? mText.substring(mPos,mPos+1) : null;
			if(",".equals(symbol) && !"(".equals(symbolNext))return;
		}
	}
	
	
}


