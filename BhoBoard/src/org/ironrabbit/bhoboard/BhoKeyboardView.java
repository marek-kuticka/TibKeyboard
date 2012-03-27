/*
 * Copyright (C) 2008-2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.ironrabbit.bhoboard;

import java.util.Iterator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.Keyboard.Key;
import android.util.AttributeSet;
import android.util.Log;

public class BhoKeyboardView extends KeyboardView {

    static final int KEYCODE_OPTIONS = -100;
    
    public BhoKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
    }

    public BhoKeyboardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        
    }
    
    public void setupKeys (Typeface typeface)
    {
    	
    	
    	Iterator<Key> itKeys = getKeyboard().getKeys().iterator();
    	Key key;
    	
    	while (itKeys.hasNext())
    	{
    		key = itKeys.next();
    	
    		if (key.codes[0] > 0)
    		{
	    		String keyCode = ((char)key.codes[0])+"";
	    		
	    		if (key.codes[0] >= 3953 && key.codes[0] <= 4027) {
					keyCode = "\u25CC" + keyCode;
				}
	    		
	    		key.icon = new DynaDrawable (getContext(), key, typeface, keyCode, Color.WHITE);
	            key.iconPreview = new DynaDrawable (getContext(), key, typeface,  keyCode, Color.BLACK);
    		}
    	}
    	
    }

    @Override
    protected boolean onLongPress(Key key) {
    	
        if (key.codes[0] == Keyboard.KEYCODE_CANCEL) {
            getOnKeyboardActionListener().onKey(KEYCODE_OPTIONS, null);
            return true;
        } else {
            return super.onLongPress(key);
        }
    }

    /*
	@Override
	public void onDraw(Canvas canvas) {
		
		
		super.onDraw(canvas);
	}
	*/
}
