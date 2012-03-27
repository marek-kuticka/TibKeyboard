package com.maja.test;

import com.maja.test.TibConvert;

public class Test {
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		

		String data;
		String data2;
		
		data = "\u0F62\u0F90\u0F0B\u0F62\u0F90\u0F0B\u0F0B";
		data2 = TibConvert.convertUnicodeToPrecomposedTibetan(data);
		data2.substring(0);
		
		String log = "";
		
		for (int i = 0; i < data2.length(); i++) {
			int str = data2.charAt(i);
			log += " \\u" + Integer.toHexString(str | 0x10000).substring(1).toUpperCase() ;
		}
		
		System.out.println( log );
	}

}
