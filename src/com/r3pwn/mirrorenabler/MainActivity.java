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
		// gms:cast:remote_display_enabled
		// com.google.android.gms
		// com.google.android.apps.chromecast.app
		
		final ToggleButton mirror = (ToggleButton)findViewById(R.id.mirrorButton);
		final ToggleButton fix = (ToggleButton)findViewById(R.id.fixtb);
		final ToggleButton qsmirror = (ToggleButton)findViewById(R.id.qsmirrortb);
		
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

		// Our Preferences
        final int mirror_status = preferences.getInt("mirror_status", 0);
		final int fix_status = preferences.getInt("fix_status", 0);
		final int qsmirror_status = preferences.getInt("qsmirror_status", 0);
		final int incompatible_status = preferences.getInt("incompatible_status", 0);
		final int unsupported_shown = preferences.getInt("unsupported_shown", 0);
		
		final File backup = new File("/system/etc/audio_policy.conf.backup");

		// 1 = Enabled        0 = Disabled

		if ( mirror_status == 1)
		{
			mirror.setChecked(true);
		}
		if(backup.exists()) {
			fix.setChecked(true);
		}
		if ( fix_status == 1)
		{
			fix.setChecked(true);
		}
		if ( qsmirror_status == 1)
		{
			qsmirror.setChecked(true);
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

		if( Build.VERSION.SDK_INT <= 18)
		{
			prefs_edit.putInt("incompatible_status", 1);
			prefs_edit.commit();
			if( unsupported_shown == 0)
			{
				final AlertDialog unsupported_version = new AlertDialog.Builder(MainActivity.this).create();
				unsupported_version.setTitle("Unsupported Android Version");
				unsupported_version.setMessage("You must be running android KitKat to use this the mirroring feature.");
				prefs_edit.putInt("unsupported_shown", 1);
				prefs_edit.putInt("incompatible_status", 1);
				prefs_edit.commit();
				unsupported_version.setButton("Okay", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog2, int which2) {
							unsupported_version.dismiss();
						}
					});
				unsupported_version.show();
				mirror.setEnabled(false);
				fix.setEnabled(false);
				qsmirror.setEnabled(false);
			}
		}
		
		
		File subin  = new File("/system/bin/su");
		File suxbin = new File("/system/xbin/su");
		if(!(subin.exists() || suxbin.exists())) 
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
			fix.setEnabled(false);
			qsmirror.setEnabled(false);
		}
		
		
		mirror.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
					if (mirror.isChecked() == true) {
						try {
							java.lang.Process su = Runtime.getRuntime().exec("su");
							DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());
							prefs_edit.putInt("mirror_status", 1);
							prefs_edit.commit();
							outputStream.writeBytes("/data/data/com.r3pwn.mirrorenabler/lib/libhackyworkaround.so /data/data/com.google.android.gsf/databases/gservices.db \"INSERT INTO overrides (name, value) VALUES ('gms:cast:mirroring_enabled', 'true');\"\n");
							outputStream.writeBytes("/data/data/com.r3pwn.mirrorenabler/lib/libhackyworkaround.so /data/data/com.google.android.gsf/databases/gservices.db \"UPDATE overrides SET value='true' WHERE name='gms:cast:mirroring_enabled';\"\n");
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
							outputStream.writeBytes("/data/data/com.r3pwn.mirrorenabler/lib/libhackyworkaround.so /data/data/com.google.android.gsf/databases/gservices.db \"UPDATE overrides SET value='false' WHERE name='gms:cast:mirroring_enabled';\"\n");
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
			
		fix.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
					if (fix.isChecked() == true) {
						try {
							java.lang.Process su = Runtime.getRuntime().exec("su");
							DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());
							prefs_edit.putInt("fix_status", 1);
							prefs_edit.commit();
							outputStream.writeBytes("mount -o rw,remount /system\n");
							outputStream.writeBytes("mv /system/etc/audio_policy.conf /system/etc/audio_policy.conf.backup\n");
							outputStream.writeBytes("cp /data/data/com.r3pwn.mirrorenabler/lib/libaudiopolicyconf.so /system/etc/audio_policy.conf\n");
							outputStream.writeBytes("chmod 0644 /system/etc/audio_policy.conf\n");
							outputStream.writeBytes("mount -o ro,remount /system\n");
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
						Toast.makeText(getApplicationContext(),"Please reboot for changes to take effect.", Toast.LENGTH_LONG).show();
					}
					if (fix.isChecked() == false) {
						try {
							java.lang.Process su = Runtime.getRuntime().exec("su");
							DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());
							if(backup.exists()) 
							{
								prefs_edit.putInt("fix_status", 0);
								prefs_edit.commit();
								outputStream.writeBytes("mount -o rw,remount /system\n");
								outputStream.writeBytes("rm /system/etc/audio_policy.conf\n");
								outputStream.writeBytes("mv /system/etc/audio_policy.conf.backup /system/etc/audio_policy.conf\n");
								outputStream.writeBytes("chmod 0644 /system/etc/audio_policy.conf\n");
								outputStream.writeBytes("mount -o ro,remount /system\n");
								outputStream.writeBytes("exit\n");
								outputStream.flush();
								su.waitFor();
								Toast.makeText(getApplicationContext(),"Please reboot for changes to take effect.", Toast.LENGTH_LONG).show();
							} else {
								Toast.makeText(getApplicationContext(),"Backup not found.", Toast.LENGTH_LONG).show();
								fix.setChecked(true);
							}
						}
						catch(IOException e)
						{
							// We do this to keep the compiler happy.
						}
						catch (InterruptedException e)
					    {
							// We do this to keep the compiler happy
						}
					}
				}
			});
			
		qsmirror.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
					if (qsmirror.isChecked() == true) {
						try {
							java.lang.Process su = Runtime.getRuntime().exec("su");
							DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());
							prefs_edit.putInt("qsmirror_status", 1);
							prefs_edit.commit();
							outputStream.writeBytes("/data/data/com.r3pwn.mirrorenabler/lib/libhackyworkaround.so /data/data/com.google.android.gsf/databases/gservices.db \"INSERT INTO overrides (name, value) VALUES ('gms:cast:remote_display_enabled', 'true');\"\n");
							outputStream.writeBytes("/data/data/com.r3pwn.mirrorenabler/lib/libhackyworkaround.so /data/data/com.google.android.gsf/databases/gservices.db \"UPDATE overrides SET value='true' WHERE name='gms:cast:remote_display_enabled';\"\n");
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
					if (qsmirror.isChecked() == false) {
						try {
							java.lang.Process su = Runtime.getRuntime().exec("su");
							DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());
							prefs_edit.putInt("qsmirror_status", 0);
							prefs_edit.commit();
							outputStream.writeBytes("/data/data/com.r3pwn.mirrorenabler/lib/libhackyworkaround.so /data/data/com.google.android.gsf/databases/gservices.db \"UPDATE overrides SET value='false' WHERE name='gms:cast:remote_display_enabled';\"\n");
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
