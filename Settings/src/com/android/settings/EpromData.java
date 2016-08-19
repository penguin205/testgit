package com.android.settings;

import android.R.integer;
import android.util.Log;

public class EpromData {
	private static final String TAG = "SystemInformationActivity";
	public byte[] mProduct_Series_Number = new byte[15];
	public byte[] mNC_Series_Number = new byte[6];
	public byte[] mManufactur_Date = new byte[5];
	public byte[][] mTest_Result_n = new byte[10][4];
	public byte[] mHardVersion = new byte[2];
	public byte[] mBT_Address = new byte[7];
	public byte[] mAging_Result = new byte[2];

	public boolean mbWaitAnswer[] = new boolean[17];
	public static final byte Product_Series_Number_Area=0x00;
	public static final byte NC_Series_Number_Area=0x01;
	public static final byte Manufactur_Date_Area=0x02;
	public static final byte HardVersion_Area=0x0D;
	
	public boolean mbInit_Product_Series_Number;
	public boolean mbInit_NC_Series_Number;
	public boolean mbInit_Manufactur_Date;
	public boolean mbInit_HardVersion;
	
	public EpromData() {
		
	}
	
	public boolean isMachineVersionInited(){
		if(mbInit_Product_Series_Number&&
				mbInit_NC_Series_Number&&
				mbInit_Manufactur_Date
				){
			return true;
		}
		else {
			return false;
		}
	}
	
    public void readEprom(int type){
    	if (type < mbWaitAnswer.length) {
			mbWaitAnswer[type] = true;
			if (type==Product_Series_Number_Area) {
				mbInit_Product_Series_Number=false;
			}
			else if (type==NC_Series_Number_Area) {
				mbInit_NC_Series_Number=false;
			}
			else if (type==Manufactur_Date_Area) {
				mbInit_Manufactur_Date=false;
			}else if (type==HardVersion_Area) {
				mbInit_HardVersion=false;
			}
		}
    	 
    }
    public void gotAnswer(int type){
    	if (mbWaitAnswer[type]) {
    		mbWaitAnswer[type] = false;
    		
    		if (type==Product_Series_Number_Area) {
				mbInit_Product_Series_Number=true;
			}
			else if (type==NC_Series_Number_Area) {
				mbInit_NC_Series_Number=true;
			}
			else if (type==Manufactur_Date_Area) {
				mbInit_Manufactur_Date=true;
			}else if (type==HardVersion_Area) {
				mbInit_HardVersion=true;
			}
    	}
    	
    }
    
	public String getHardVersion(){
		 
		String string= String.format("%02d", mHardVersion[1]);
		return string;
	}
	
	public String getAgingResult(){
		 
		String string= String.format("%02d", mAging_Result[1]);
		return string;
	}
	
	public String getTestResult(){
		String tRslt="";
		int itemN=10;
		for (int i = 0; i < itemN; i++) {
			String tRslti="";
			int a;
			String string;
			for (int j = 1; j < 4; j++) {
				
				a=mTest_Result_n[i][j]&0xFF;
				 string= String.format("%02x", a);
				tRslti=tRslti+string; 
			}
			
			tRslt=tRslt+tRslti;
			if (i!=itemN-1) {
				tRslt=tRslt+"-";
			}
		}
		
		 
		return tRslt;
	}
	
	public String getMachineVersion() {
		//
		String verSeriesNumString = getProductSeriesNumber();

		String ncSeriesNumString = getNcSeriesNum1();

		String manufactur_DateString = getManufacturDate2();

		return ncSeriesNumString + "-" + manufactur_DateString + "-"
				+ verSeriesNumString;
	}

	/**
	 * if the product serial number or the nc number is 0,it is invalid
	 * 
	 * @return
	 */
	public boolean isMachineVersionValid() {
		boolean bProductSNValid = isProductSeriesNumberValid();
		boolean bNcValid = isNcSeriesNumValid();
		Log.i(TAG, "bProductSNValid:"+bProductSNValid+" bNcValid:"+bNcValid);
		if (bProductSNValid && bNcValid) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isProductSeriesNumberValid() {
		boolean bValid = false;
		for (int i = 1; i < mProduct_Series_Number.length; i++) {
			Log.i(TAG, "mProduct_Series_Number[i]:"+mProduct_Series_Number[i]);
			if (mProduct_Series_Number[i] != 0x00) {
				bValid = true;
				break;
			}
			else {
				Log.i(TAG, "invalid,i:"+i);
			}
		}
		Log.i(TAG, "isProductSeriesNumberValid:"+bValid);
		return bValid;
	}

	private String getProductSeriesNumber() {
		String verSeriesNumString = "";
		for (int i = 1; i < mProduct_Series_Number.length; i++) {
			char a = (char) (mProduct_Series_Number[i] & 0xFF);
			verSeriesNumString += a;
		}

		return verSeriesNumString;
	}

	public boolean isNcSeriesNumValid() {
		boolean bValid = false;
		for (int i = 1; i < mNC_Series_Number.length; i++) {
			Log.i(TAG, "mNC_Series_Number[i]:"+mNC_Series_Number[i]);
			if (mNC_Series_Number[i] != 0x00) {
				bValid = true;
				break;
			}
			else {
				Log.i(TAG, "invalid,i:"+i);
			}
		}
		
       Log.i(TAG, "isNcSeriesNumValid:"+bValid);
		return bValid;
	}

	private String getNcSeriesNum() {
		Long ncSeriesNum = (long) 0;

		for (int i = 1; i < mNC_Series_Number.length; i++) {
			ncSeriesNum = ncSeriesNum
					+ ((long) (mNC_Series_Number[i] & 0xFF) << (long) ((i - 1) * 8));
		}

		String ncSeriesNumString = ncSeriesNum.toString();

		return ncSeriesNumString;
	}

	private String getNcSeriesNum1() {
		String string;
        int a;
        String ncSeriesNumString ="";
		for (int i = 1; i < mNC_Series_Number.length-1; i++) {
			a=mNC_Series_Number[i]&0xFF;
			 string= String.format("%02x", a);
			 
			 ncSeriesNumString=ncSeriesNumString+string;
		}

		 

		return ncSeriesNumString;
	}
	
	private String getManufacturDate() {
		Long manufactur_Date = (long) 0;

		for (int i = 1; i < mManufactur_Date.length; i++) {
			manufactur_Date = manufactur_Date
					+ ((long) (mManufactur_Date[i] & 0xFF) << (long) ((i - 1) * 8));
		}

		String manufactur_DateString = manufactur_Date.toString();

		return manufactur_DateString;
	}
	
	private String getManufacturDate1() {
		 
        int a;
        String manufactur_DateString="";
        String string;
		for (int i = 1; i < mManufactur_Date.length; i++) {
			 a=mManufactur_Date[i]&0xFF;
			 string= String.format("%02d", a);
			 manufactur_DateString=manufactur_DateString+string;
		}
		
		return manufactur_DateString;
	}
	
	private String getManufacturDate2() {
		 
        int a;
        String manufactur_DateString="";
        String string;
        // 0x20 0x15 0x01 0x05-->"20150105"
		for (int i = 2; i < mManufactur_Date.length; i++) {
			 a=mManufactur_Date[i]&0xFF;
			 string= String.format("%02x", a);
			 manufactur_DateString=manufactur_DateString+string;
		}
		
		return manufactur_DateString;
	}

	public byte[] transProductSerialNumber(String productSN) {

		if (productSN.length() != 6) {
			return null;
		}
		byte[] data = productSN.getBytes();
		byte[] value = new byte[data.length + 1];
		value[0] = 0x00;
		// src,dest
		System.arraycopy(data, 0, value, 1, data.length);
		return value;
	}

	public byte[] trans12NCSerialNumber(String ncSN) {
		if (ncSN.length() != 8) {
			return null;
		}

		byte[] data = new byte[ncSN.length() / 2];
		for (int i = 0; i < data.length; i++) {
			data[i] = (byte) Integer.parseInt(ncSN.substring(i * 2, i * 2 + 2),
					16);
		}

		byte[] value = new byte[data.length + 1];
		value[0] = 0x01;
		for (int i = 0; i < data.length; i++) {
			value[i + 1] = data[i];
		}

		return value;
	}

	public byte[] transManuFactDateSerialNumber(String mfdate) {
		if (mfdate.length() != 8) {

			return null;
		}
		byte[] data = new byte[mfdate.length() / 2];
		for (int i = 0; i < data.length; i++) {
			data[i] = (byte) Integer.parseInt(
					mfdate.substring(i * 2, i * 2 + 2), 16);
		}

		byte[] value = new byte[data.length + 1];
		value[0] = 0x02;
		for (int i = 0; i < data.length; i++) {
			value[i + 1] = data[i];
		}

		return value;
	}
}
