package com.maja.TibKeybTest;

//import org.lobsangmonlam.dictionary.R;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

public class mainActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Typeface face= Typeface.createFromAsset(getAssets(), "fonts/DDC_Uchen.ttf");
        setContentView(R.layout.main);
        
        EditText edit2 = (EditText)findViewById(R.id.editText1);
        edit2.setTypeface(face);
//        edit2.setImeOptions(imeOptions)
        
    }
}