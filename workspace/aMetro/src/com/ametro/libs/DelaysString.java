package com.ametro.libs;

public class DelaysString {

	private String mText;
	//private String[] mParts;
	private int mPos;
	private int mLen;

	public DelaysString(String text){
		//text = text.replaceAll("\\(","");
		//text = text.replaceAll("\\)","");
		//mParts = text.split(",");
		mText = text;
		mLen = text!=null ? mText.length() : 0;
		mPos = 0;
	}

	public boolean beginBracket(){
		if(mText == null) return false; 
		return mPos < mLen && mText.charAt(mPos) == '(';
	}

	private String nextBlock(){
		if(mText == null) return null; 
		int nextComma = mText.indexOf(",", beginBracket() ? mText.indexOf(")", mPos) : mPos);
		String block = nextComma!=-1 ? mText.substring(mPos, nextComma) : mText.substring(mPos);
		mPos =  nextComma!=-1 ? nextComma+1 : mLen;
		return block;
	}

	public Double next(){
		return Helpers.parseNullableDouble( nextBlock() );
	}

	public Double[] nextBracket(){
		if(mText == null) return null; 
		String block = nextBlock();
		return Helpers.parseDoubleArray( block.substring(1, block.length()-1) );
	}

}
