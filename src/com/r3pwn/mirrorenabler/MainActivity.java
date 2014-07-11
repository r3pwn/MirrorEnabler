package com.r3pwn.mirrorenabler;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.content.*;
import android.preference.*;
import java.io.*;
import android.net.*;

public class MainActivity extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
		// Quick, thrown together, don't expect it to look pretty.
		// gms:cast:mirroring_enabled
		// com.google.android.gms
		// com.google.android.apps.chromecast.app
		
		final ToggleButton mirror = (ToggleButton)findViewById(R.id.mirrorButton);
		
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

		// Our Preferences
        final int mirror_status = preferences.getInt("mirror_status", 0);
		final int incompatible_status = preferences.getInt("incompatible_status", 0);
		final int unsupported_shown = preferences.getInt("unsupported_shown", 0);

		// 1 = Enabled        0 = Disabled

		if ( mirror_status == 1)
		{
			mirror.setChecked(true);
		}
		
        final SharedPreferences.Editor prefs_edit = preferences.edit();

		prefs_edit.putInt("incompatible_status", 0);
		prefs_edit.commit();

		if( Build.VERSION.SDK_INT >= 20)
		{
			prefs_edit.putInt("incompatible_status", 1);
			prefs_edit.commit();
			if( unsupported_shown == 0)
			{
				final AlertDialog unsupported_version = new AlertDialog.Builder(MainActivity.this).create();
				unsupported_version.setTitle("Unsupported Android Version");
				unsupported_version.setMessage("The version of android you are running is not fully supported by this app. You will need to reboot after a preference change.");
				prefs_edit.putInt("unsupported_shown", 1);
				prefs_edit.putInt("incompatible_status", 1);
				prefs_edit.commit();
				unsupported_version.setButton("Okay", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog2, int which2) {
							unsupported_version.dismiss();
						}
					});
				unsupported_version.show();
			}
		}

		// Checking preferences and adjusting the buttons accordingly
		
		File subin = new File("/system/bin/su");
		if(subin.exists()) 
		{
			// Nothing
		}
		else
		{
			File suxbin = new File("/system/xbin/su");
			if(suxbin.exists())
			{
				// Nothing
			}
			else
			{
				final AlertDialog sualertDialog = new AlertDialog.Builder(MainActivity.this).create();
				sualertDialog.setTitle("You aren't rooted");
				sualertDialog.setMessage("It looks like you aren't rooted. Most features will not work. There is nothing I can do to help you.");

				sualertDialog.setButton("Alright, thanks anyways.", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							sualertDialog.dismiss();
						}
					});

				sualertDialog.show();  
				mirror.setEnabled(false);
			}
		}

		// SQLite3 binary check.
		File file = new File("/system/bin/sqlite3");
		if(file.exists()) 
		{
			// Nothing
		}
		else
		{
			File file2 = new File("/system/xbin/sqlite3");
			if(file2.exists())
			{
				// Nothing
			}
			else
			{
				AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
				alertDialog.setTitle("No SQLite3 binary found.");
				alertDialog.setMessage("It looks like the SQLite3 binary is not installed. Would you like to install it now?");

				alertDialog.setButton("Yes, take me there!", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							Intent dialogIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=ptSoft.util.sqlite3forroot"));
							dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							startActivity(dialogIntent);                     
						}
					});

				alertDialog.show();  
				mirror.setEnabled(false);
			}
		}
		mirror.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
					if (mirror.isChecked() == true) {
						try {
							java.lang.Process su = Runtime.getRuntime().exec("su");
							DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());
							prefs_edit.putInt("mirror_status", 1);
							prefs_edit.commit();
							outputStream.writeBytes("sqlite3 /data/data/com.google.android.gsf/databases/gservices.db \"INSERT INTO overrides (name, value) VALUES ('gms:cast:mirroring_enabled', 'true');\"\n");
							outputStream.writeBytes("sqlite3 /data/data/com.google.android.gsf/databases/gservices.db \"UPDATE overrides SET value='true' WHERE name='gms:cast:mirroring_enabled';\"\n");
							outputStream.writeBytes("am force-stop com.google.android.gsf\n");
							outputStream.writeBytes("am force-stop com.google.android.gms\n");
							outputStream.writeBytes("am force-stop com.google.android.apps.chromecast.app\n");
							outputStream.writeBytes("exit\n");
							outputStream.flush();
							su.waitFor();

						}
						catch(IOException e)
						{
							// We do this to keep the compiler happy.
						}
						catch (InterruptedException e)
					    {
							// We do this to keep the compiler happy
						}
						if (incompatible_status == 1) 
						{
							Toast.makeText(getApplicationContext(),"Please reboot for changes to take effect.", Toast.LENGTH_LONG).show();
						} else {
							Toast.makeText(getApplicationContext(),"Changes applied.", Toast.LENGTH_LONG).show();
						}
					}
					if (mirror.isChecked() == false) {
						try {
							java.lang.Process su = Runtime.getRuntime().exec("su");
							DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());
							prefs_edit.putInt("mirror_status", 0);
							prefs_edit.commit();
							outputStream.writeBytes("sqlite3 /data/data/com.google.android.gsf/databases/gservices.db \"UPDATE overrides SET value='false' WHERE name='gms:cast:mirroring_enabled';\"\n");
							outputStream.writeBytes("am force-stop com.google.android.gsf\n");
							outputStream.writeBytes("am force-stop com.google.android.gms\n");
							outputStream.writeBytes("am force-stop com.google.android.apps.chromecast.app\n");
							outputStream.writeBytes("exit\n");
							outputStream.flush();
							su.waitFor();
						}
						catch(IOException e)
						{
							// We do this to keep the compiler happy.
						}
						catch (InterruptedException e)
					    {
							// We do this to keep the compiler happy
						}

						if (incompatible_status == 1) 
						{
							Toast.makeText(getApplicationContext(),"Please reboot for changes to take effect.", Toast.LENGTH_LONG).show();
						} else {
							Toast.makeText(getApplicationContext(),"Changes applied.", Toast.LENGTH_LONG).show();
						}
					}
				}
			});
    }
}
