package org.ironrabbit;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class BhoBookActivity extends Activity {
	
	public final static String TAG = "Bho";

	EditText ev;
	
	
      Typeface face;
//	    Typeface face=Typeface.createFromAsset(getAssets(), "monlambodyig.ttf");
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     
        face = Typeface.createFromAsset(getAssets(), "DDC_Uchen.ttf");
        
        if (getIntent() != null && getIntent().getExtras() != null)
        {
        	
	        String text = parseExtras (getIntent().getExtras());

			setContentView(R.layout.main_simple);
	        
	        if (text != null)
	        {
	        	TextView tv=(TextView)findViewById(R.id.output);
	        	
	        	if (text.toLowerCase().startsWith("http"))
	        	{
	        		
	        		Intent intent = new Intent(this, SharCheBrowserActivity.class);
	        		
	        		intent.putExtra("url", text);
	                startActivity(intent);
	                
	                finish();
	        		
	        	}
	        	else
	        	{
	        	

	                String tibText = TibConvert.convertUnicodeToPrecomposedTibetan(text);
	        		showText(tibText, tv);
	        	}
	        	
	        	return;
	        }
        }
       
    	setContentView(R.layout.main_edit);
    	ev = (EditText)findViewById(R.id.editor);

    	
    	setupEditor();
        
    }
    
    
    public String parseExtras (Bundle extras)   
    {
    	if (extras.containsKey(Intent.EXTRA_STREAM))
        {
            try
            {
                // Get resource path from intent callee
                Uri uri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);

                ContentResolver cr = getContentResolver();
                InputStream is = cr.openInputStream(uri);
                // Get binary bytes for encode
                byte[] data = getBytesFromFile(is);

                return new String(data);
            } catch (Exception e)
            {
                Log.e(this.getClass().getName(), e.toString());
            }

        } 
    	else if (extras.containsKey(Intent.EXTRA_TEXT))
        {
        	
            return extras.getString(Intent.EXTRA_TEXT);
        }
     
       	return null;
    }
    
    public static byte[] getBytesFromFile(InputStream is)
	{
		try
		{
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();

			int nRead;
			byte[] data = new byte[16384];

			while ((nRead = is.read(data, 0, data.length)) != -1)
			{
				buffer.write(data, 0, nRead);
			}

			buffer.flush();

			return buffer.toByteArray();
		} catch (IOException e)
		{
			Log.e("com.eggie5.post_to_eggie5", e.toString());
			return null;
		}
	}
    
    public void showText (String text, TextView tv)
    {
	    
	    tv.setTypeface(face);
	    tv.setTypeface(face, TextUtils.CAP_MODE_CHARACTERS);
	    
	    tv.setText(text);
        
	    
        
    }
    
    String lastChange = "";
    
    public void setupEditor ()
    {
    	ev.setTypeface(face);
    	
    	ev.addTextChangedListener(new TextWatcher() { 
            public void  afterTextChanged (Editable s){
            	
            	String newText = s.toString();
            	
            	if (!lastChange.equals(newText))
            	{
            		if (Math.abs(newText.length()-lastChange.length())>2)
            		{
    	                newText = TibConvert.convertUnicodeToPrecomposedTibetan(newText);

            		}
            		
            		lastChange = newText;
            		showText(lastChange, ev);
            		
            		ev.setSelection(lastChange.length());
            		
            	}
            	
            } 
            public void  beforeTextChanged  (CharSequence s, int start, int 
count, int after){ 
            } 
            public void  onTextChanged  (CharSequence s, int start, int before, 
int count) { 
            	
            	
            }
    	}
           );
            
    	    }

	@Override
	protected void onResume() {
		super.onResume();
		
	}
    
    
}
/*
// Precomposed Tibetan version of a supplication to Tara
String tibetanText = "\u0F04\u0F05\u0F0D \u0F0D\u0F60\u0F55\u0F42\u0F66\u0F0B\u0F58\u0F0B" +
"\uF397\u0F63\u0F0B\u0F58\u0F0B\u0F63\u0F0B\u0F56\uF43A\u0F51\u0F0B\u0F54\u0F0B\n" +
"\uF4BA\u0F42\u0F0B\u0F60\u0F5A\u0F63\u0F0B\u0F56\u0F0B" +
"\uF3ED\u0F0B\uF5BE\u0F0B\uF53F\u0F0B\u0F42\uF3B7\u0F42\u0F0B\u0F54" +
"\uF591\u0F0B\uF5AA\u0F58\u0F0B\u0F54\u0F0B\uF473\u0F0D\u0F0D";
*/