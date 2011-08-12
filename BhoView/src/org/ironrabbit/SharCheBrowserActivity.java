/**
 * @author Tom Meyer
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Basic Tibetan web browser, which converts Tibetan text on the fly between 
 * Unicode and GB/T20524-2006 encodings.
 *    
 * Jan 16 2010, Initial creation, proof of concept
 * Jan 24 2010, Added URL choosing and pop-up menu
 * Jan 29 2010, Filtered out non-HTML types
 *              
 * TODO: proper documentation and unit tests
 * TODO: create Tibetan error web page
 * TODO: create Tibetan "home page"
 * TODO: user-defined home page
 * TODO: bookmarks (can we access bookmarks from built-in browser?
 * TODO: make it look more like the built-in web browser (url entry scrolls
 *       down from top, etc)
 *
 */

package org.ironrabbit;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Stack;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.PluginData;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.Toast;


public class SharCheBrowserActivity extends Activity {
	final int MENU_URLENTRY = 0;
	final int MENU_BACK = 1;
	final int MENU_REFRESH = 2;
	final int MENU_QUIT = 3;
	final int MENU_FORWARD = 4;
	final int MENU_ABOUT = 5;
	
	WebView webview;
	TableLayout urlBar;
	EditText gotoUrl;
	Button urlButton;
	
	private String fontCSS;
	
	private Stack<String> history;
	
	protected static final String errorPage = "about:error";

	String fontName = "DDC_Uchen.ttf";
	String fontFamily = "DDC_Uchen";
   // String fontPath = "file://"+ getFilesDir().getAbsolutePath()+ "/" + fontName;
    String fontPath = "file:///android_asset/" + fontName;

    String userAgent;
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        history = new Stack<String>();
        
        copyFile(this, fontName);
        
        fontCSS = buildFontCSS();
        
    	
        setContentView(R.layout.main_browser);
        
        webview = (WebView) findViewById(R.id.webview);
        webview.setWebViewClient(new SharCheClient());
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setBuiltInZoomControls(true);
        
        webview.getSettings().setStandardFontFamily(fontFamily);
        webview.getSettings().setFantasyFontFamily(fontFamily);
        webview.getSettings().setSansSerifFontFamily(fontFamily);
        webview.getSettings().setCursiveFontFamily(fontFamily);
        
        userAgent = webview.getSettings().getUserAgentString();
        
        
        urlBar = (TableLayout)findViewById(R.id.UrlEntry);
        gotoUrl = (EditText)findViewById(R.id.goUrl);
        urlButton = (Button)findViewById(R.id.goUrlButton);
        urlButton.setOnClickListener(myUrlButtonOnClickListener);        
         
        Intent intent = getIntent();
        
        if (intent != null)
        {
        	
        	String action = intent.getAction();
        	 Bundle extras = intent.getExtras();
  	       
    		if (Intent.ACTION_SEARCH.equals(action)) {
    			// Navigate to the URL
    			String url = intent.getStringExtra(SearchManager.QUERY);
    			displayUrl(url);
    		} else if (Intent.ACTION_VIEW.equals(action)) {
    			// Navigate to the URL
    			String url = intent.getDataString();
    			displayUrl(url);
    		}
    		else if (extras != null && extras.containsKey("text"))
	        {
	        	
	        	String text = extras.getString("text");
	        	try
	        	{
	        		loadData("http://","text/html", "utf-8",text);
	        	}
	        	catch (Exception e){
	        		Log.e(BhoBookActivity.TAG, "error loading intent extras content", e);
	        	}
	        }
	        else if (extras != null && extras.containsKey("url"))
	        {
	        	String url = extras.getString("url");
	        	
	        	
	        	displayUrl(url);
	        }
        }
        
        /*
        try {
			loadData("http://somewebsite.com/","text/html","utf-8",getAssets().open("taras.html"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        */
        
	}

    private String buildFontCSS ()
    {
    	
           
    	//fontCSS = "<style>\n@font-face {font-family: '" + fontFamily + "';\nsrc: url('" + fontPath + "');\n}\nbody {font-family: '" + fontFamily + "';}\n</style>";
    	
    	String[] fontFamilies = {"DDC_Uchen","MonlamBodyig","Microsoft Himalaya","TCRC Youtso Unicode","Georgia", "Verdana", "Arial", "Serif","Helvetica Neue", "Helvetica", "Palatino", "Times", "Times New Roman"};
    	
    	StringBuilder css = new StringBuilder();
    	
    	css.append("<style>").append('\n');
    	
    	for (int i = 0; i < fontFamilies.length; i++)
    	{
    		css.append("@font-face {").append('\n');
    		css.append("font-family: '" + fontFamilies[i] + "';").append('\n');
    		css.append("src: url('" + fontPath + "');").append('\n');
    		css.append("}");
    		
    	}
    	
    	css.append("body {font-family: '" + fontFamilies[0] + "';}");
    	
    	css.append("</style>").append('\n');
    	
    	return css.toString();
    }
    
    private boolean copyFile(Context context,String fileName) {
        boolean status = false;
        try { 
            FileOutputStream out = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            InputStream in = context.getAssets().open(fileName);
            // Transfer bytes from the input file to the output file
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            // Close the streams
            out.close();
            in.close();
            status = true;
        } catch (Exception e) {
            System.out.println("Exception in copyFile:: "+e.getMessage());
            status = false;
        }
        System.out.println("copyFile Status:: "+status);
        return status;
    }

	/**
	 * Fetches an asset as if it were an HTTP request.
	 * 
	 * @param path
	 *            the path of the asset to get
	 * @return the PluginData structure containing the asset
	 */
	private PluginData getFromAsset(String path) {
		InputStream in;
		try {
			// Fetch an InputStream of the asset
			in = this.getAssets().open("internal_web/" + path);
		} catch (IOException e) {
			return stringToPluginData("An error has occurred: " + e.toString(),
					200);
		}

		return new PluginData(in, 0L, new HashMap<String, String[]>(), 200);
	}
	
	/**
	 * Returns a PluginData object filled with HTML from a string
	 * 
	 * @param s
	 *            the string containing HTML
	 * @param statuscode
	 *            the HTTP status code for the object
	 * @return an appropriate PluginData object
	 */
	private PluginData stringToPluginData(String s, int statuscode) {

		// Default error if can't convert provided string
		byte[] err = { 68, 111, 104 }; // Doh
		try {
			err = s.getBytes("utf-8");
		} catch (UnsupportedEncodingException e) {
			// Oh dear. Not much we can do if UTF-8 isn't supported
			// except go with "Doh"
			e.printStackTrace();
		}

		ByteArrayInputStream b = new ByteArrayInputStream(err);
		PluginData p = new PluginData(b, err.length,
				new HashMap<String, String[]>(), statuscode);
		return p;
	}

    protected void displayUrl(String url) {
        try {
        	
        	history.push(url);
        	
        	Toast.makeText(this, "Loading page: " + url, Toast.LENGTH_LONG).show();

        	/*
        	URL siteURL = new URL(url);
        	URLConnection uc = siteURL.openConnection();
        	
        	Object content = uc.getContent();
        	String contentEncoding = uc.getContentEncoding();
        	String contentType = uc.getContentType();

        	if (contentType == null) {
        		contentType = URLConnection.guessContentTypeFromName(url);
        	} else {
        		if (contentEncoding == null) {
	        		int semi = contentType.indexOf(";");
	        		if (semi > -1) {
	        			String newContentType = contentType.substring(0, semi);

	        			String[] splits = contentType.split("charset=");
        				if (splits.length > 1) {
        					contentEncoding = splits[1];
        				}
            			contentType = newContentType;
        			}
        		}
        	}*/
        	
        	DefaultHttpClient client = new DefaultHttpClient();
        	
            HttpGet request = new HttpGet();
            request.setURI(new URI(url));
            
            request.setHeader("User-Agent", userAgent);
            
            HttpResponse response = client.execute(request);
            
            Object content = response.getEntity().getContent();
            String contentType = response.getEntity().getContentType().getValue();
            String contentEncoding = null;
            
            
            if (response.getEntity().getContentEncoding() != null)
            	contentEncoding = response.getEntity().getContentEncoding().getValue();
            
            if (contentEncoding == null) {
        		int semi = contentType.indexOf(";");
        		if (semi > -1) {
        			String newContentType = contentType.substring(0, semi);

        			String[] splits = contentType.split("charset=");
    				if (splits.length > 1) {
    					contentEncoding = splits[1];
    				}
        			contentType = newContentType;
    			}
    		}
            
        	loadData (url, contentType, contentEncoding, (InputStream)content);
        	
        } catch (Exception e) {
        	String data = "ERROR: " + e.toString() + "\n";
        	StackTraceElement[] ste = e.getStackTrace();
        	for (int i= 0; i< ste.length; i++) {
        		data += ste[i].toString() + "\n"                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ;
        	}
        	webview.loadDataWithBaseURL("", data, "text/html", "utf-8", errorPage);
        	System.out.println(e.toString());
        }
        	
        }
        
    
    
        
        public void loadData (String url, String contentType, String contentEncoding, Object content) throws IOException
        {
        	String data;
        	
        	
        	
        	
        	if (content instanceof InputStream) {
        		if (contentType == null) {
        			contentType = URLConnection.guessContentTypeFromStream((InputStream)content);
        		}

        		String line;
        		StringBuilder outputBuilder = new StringBuilder();
        		
        		
        		BufferedReader reader =
        	          new BufferedReader(new InputStreamReader((InputStream)content));
        	    while (null != (line = reader.readLine())) {
        	    	
        	    	
        	    	
        	        if (line.indexOf("</head>")!=-1)
        	    	{
        	        
        	        	outputBuilder.append(fontCSS).append('\n');
        	    	}        	        
        	        
        	        outputBuilder.append(line).append('\n');
        	        
        	    }
        	    
        	    
        	    
        		data = outputBuilder.toString();
        	} else if (content instanceof String)
        	{
        		data = (String)content;
        	}
        	else {
        		data = "Invalid content type is: " + content.toString();
        	}
        	
        	// TODO: Are there other types of content and encoding that we'd like to 
        	// handle?  RTF, doc, text, etc?
    		if (contentType.equals("text/html") && contentEncoding.equals("utf-8")) {
    			data = TibConvert.convertUnicodeToPrecomposedTibetan(data);
    		}
    		
        	String baseURL = url.substring(0, url.lastIndexOf("/")+1);

        	webview.loadDataWithBaseURL(baseURL,  
        								data, contentType, contentEncoding, 
        								errorPage);
       
        	
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
          //  webview.goBack();
        	goBack();
        	
        	
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    private void goBack ()
    {
    	history.pop();
    	
    	if (!history.empty())
    		displayUrl(history.pop());
    	else
    		finish();
    	
    }
    
    private class SharCheClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            
        	//view.loadUrl(url);
            
        	displayUrl(url);
        	
            return true;
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    
   //  menu.add(0, MENU_URLENTRY, 0, R.string.str_URLbar);
     menu.add(0, MENU_BACK,  0, R.string.str_Back).setIcon(R.drawable.ic_menu_back);
     
     menu.add(0, MENU_REFRESH,  0, R.string.str_Refresh).setIcon(R.drawable.ic_menu_refresh);
    // menu.add(0, MENU_FORWARD,  0, R.string.str_Forward).setIcon(R.drawable.ic_menu_forward);
     //menu.add(0, MENU_ABOUT,  0, R.string.str_About);
     
     return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		
		switch(item.getItemId())
		{
		case MENU_URLENTRY:
			toggleGotoVisibility();
		    break;
		case MENU_ABOUT:
			openAboutDialog();
			break;
		case MENU_BACK:
			goBack();
			
			
			break;
		case MENU_REFRESH:
			webview.reload();
			break;
		case MENU_FORWARD:
			if(webview.canGoForward())
				webview.goForward();
		break;
		}
		return true;
    }
    
    void toggleGotoVisibility()
    {
    	if (urlBar.getVisibility() == View.GONE)
    	{
    		urlBar.setVisibility(View.VISIBLE);
    	}
    	else
    	{
    		urlBar.setVisibility(View.GONE);
    	}
    }
    
    
    private void openAboutDialog()
    {
    	AlertDialog.Builder adb = new AlertDialog.Builder(this);
    	adb.setTitle(R.string.str_About);
    	adb.setMessage(R.string.str_about_message);
    	adb.setPositiveButton(R.string.str_ok,
	       new DialogInterface.OnClickListener()
	       {
		        public void onClick(DialogInterface dialoginterface, int i)
		        {}
	       });	
    	adb.show();
    }

    
    private Button.OnClickListener myUrlButtonOnClickListener
     = new Button.OnClickListener()
    {
    	public void onClick(View v) {
	    	webview.loadUrl(gotoUrl.getText().toString());
	    }
    };
}
