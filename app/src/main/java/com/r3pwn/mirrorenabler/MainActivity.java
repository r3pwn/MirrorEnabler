package com.r3pwn.mirrorenabler;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import android.content.*;
import android.database.sqlite.*;
import eu.chainfire.libsuperuser.*;
import android.database.*;

public class MainActivity extends Activity {
    private static final int ENABLED = 1;
    private static final int DISABLED = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
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
        final int mirror_status = preferences.getInt("mirror_status", DISABLED);
        final int fix_status = preferences.getInt("fix_status", DISABLED);
        final int qsmirror_status = preferences.getInt("qsmirror_status", DISABLED);
        final File backup = new File("/system/etc/audio_policy.conf.backup");
        final File audiosubmix = new File("/system/lib/hw/audio.r_submix.default.so");
        if (mirror_status == ENABLED) {
            mirror.setChecked(true);
        }
        if(backup.exists()) {
            fix.setChecked(true);
        }
        if (fix_status == ENABLED) {
            fix.setChecked(true);
        }
        if (qsmirror_status == ENABLED) {
            qsmirror.setChecked(true);
        }
        final SharedPreferences.Editor prefs_edit = preferences.edit();
        prefs_edit.putInt("incompatible_status", DISABLED);
        prefs_edit.apply();
        if(Shell.SU.available() == false) {
            final AlertDialog sualertDialog = new AlertDialog.Builder(MainActivity.this).create();
            sualertDialog.setTitle("You aren't rooted");
            sualertDialog.setMessage("It looks like you aren't rooted. Most features will not work. There is nothing I can do to help you.");
            sualertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Alright, thanks anyways.", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    sualertDialog.dismiss();
                }
            });
            sualertDialog.show();  
            mirror.setEnabled(false);
            fix.setEnabled(false);
            qsmirror.setEnabled(false);
        } else
		{
			Shell.SU.run("rm -f /data/data/com.google.android.gsf/databases/gservices.db-journal\n");
		}
        if (!audiosubmix.exists()) {
            final AlertDialog asalertDialog = new AlertDialog.Builder(MainActivity.this).create();
            asalertDialog.setTitle("Your ROM is incompatible");
            asalertDialog.setMessage("It seems that your ROM is lacking the audio_submix file. You must contact your device maintainer and ask them NICELY to implement it. Please refer to the red part of the original post on XDA-Developers.");
            asalertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Okay, will do.",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            asalertDialog.dismiss();
                        }
                    });
            asalertDialog.show();  
            fix.setEnabled(false);
        }
        mirror.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mirror.isChecked()) {
					try
					{
					Shell.SU.run("cp /data/data/com.google.android.gsf/databases/gservices.db /data/data/com.r3pwn.mirrorenabler/databases/gservices.db\n");
					SQLiteDatabase db=openOrCreateDatabase("gservices.db", Context.MODE_WORLD_READABLE, null);
					db.execSQL("INSERT INTO overrides (name, value) VALUES ('gms:cast:mirroring_enabled', 'true');");
					db.execSQL("UPDATE overrides SET value='true' WHERE name='gms:cast:mirroring_enabled';");
					Shell.SU.run("am force-stop com.google.android.gsf\n");
					Shell.SU.run("am force-stop com.google.android.gms\n");
					Shell.SU.run("am force-stop com.google.android.apps.chromecast.app\n");
					Shell.SU.run("cp /data/data/com.r3pwn.mirrorenabler/databases/gservices.db /data/data/com.google.android.gsf/databases/gservices.db\n");
					prefs_edit.putInt("mirror_status", ENABLED);
                    prefs_edit.apply();
					if (Build.VERSION.SDK_INT >= 21)
					{
						Toast.makeText(getApplicationContext(),"Please reboot for changes to take effect.", Toast.LENGTH_LONG).show();
					} else
					{
						Toast.makeText(getApplicationContext(),"Changes applied.", Toast.LENGTH_LONG).show();
					}
					} catch (SQLException sqle) {
						Shell.SU.run("cp /data/data/com.google.android.gsf/databases/gservices.db /data/data/com.r3pwn.mirrorenabler/databases/gservices.db\n");
						SQLiteDatabase db=openOrCreateDatabase("gservices.db", Context.MODE_WORLD_READABLE, null);
						db.execSQL("UPDATE overrides SET value='true' WHERE name='gms:cast:mirroring_enabled';");
						Shell.SU.run("am force-stop com.google.android.gsf\n");
						Shell.SU.run("am force-stop com.google.android.gms\n");
						Shell.SU.run("am force-stop com.google.android.apps.chromecast.app\n");
						Shell.SU.run("cp /data/data/com.r3pwn.mirrorenabler/databases/gservices.db /data/data/com.google.android.gsf/databases/gservices.db\n");
						prefs_edit.putInt("mirror_status", ENABLED);
                        prefs_edit.apply();
						if (Build.VERSION.SDK_INT >= 21)
						{
							Toast.makeText(getApplicationContext(),"Please reboot for changes to take effect.", Toast.LENGTH_LONG).show();
						} else
						{
							Toast.makeText(getApplicationContext(),"Changes applied.", Toast.LENGTH_LONG).show();
						}
					} catch (Exception e) {
						// Bleh.
					}
                } else {
					Shell.SU.run("cp /data/data/com.google.android.gsf/databases/gservices.db /data/data/com.r3pwn.mirrorenabler/databases/gservices.db\n");
					SQLiteDatabase db=openOrCreateDatabase("gservices.db", Context.CONTEXT_IGNORE_SECURITY, null);
					db.execSQL("UPDATE overrides SET value='false' WHERE name='gms:cast:mirroring_enabled';");
					Shell.SU.run("am force-stop com.google.android.gsf\n");
					Shell.SU.run("am force-stop com.google.android.gms\n");
					Shell.SU.run("am force-stop com.google.android.apps.chromecast.app\n");
					Shell.SU.run("cp /data/data/com.r3pwn.mirrorenabler/databases/gservices.db /data/data/com.google.android.gsf/databases/gservices.db\n");
					prefs_edit.putInt("mirror_status", DISABLED);
					prefs_edit.apply();
					if (Build.VERSION.SDK_INT >= 21)
					{
						Toast.makeText(getApplicationContext(),"Please reboot for changes to take effect.", Toast.LENGTH_LONG).show();
					} else
					{
						Toast.makeText(getApplicationContext(),"Changes applied.", Toast.LENGTH_LONG).show();
					}
                }
            }
        });
        fix.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (fix.isChecked()) {
                    try {
                        java.lang.Process su = Runtime.getRuntime().exec("su");
                        DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());
                        prefs_edit.putInt("fix_status", ENABLED);
                        prefs_edit.apply();
                        outputStream.writeBytes("mount -o rw,remount /system\n");
                        outputStream.writeBytes("mv /system/etc/audio_policy.conf /system/etc/audio_policy.conf.backup\n");
                        outputStream.writeBytes("cp /data/data/com.r3pwn.mirrorenabler/lib/libaudiopolicyconf.so /system/etc/audio_policy.conf\n");
                        outputStream.writeBytes("chmod 0644 /system/etc/audio_policy.conf\n");
                        outputStream.writeBytes("mount -o ro,remount /system\n");
                        outputStream.writeBytes("exit\n");
                        outputStream.flush();
                        su.waitFor();
                    } catch(IOException e) {
                        // We do this to keep the compiler happy.
                    } catch (InterruptedException e) {
                        // We do this to keep the compiler happy.
                    }
                    Toast.makeText(getApplicationContext(),"Please reboot for changes to take effect.", Toast.LENGTH_LONG).show();
                } else {
                    try {
                        java.lang.Process su = Runtime.getRuntime().exec("su");
                        DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());
                        if (backup.exists()) {
                            prefs_edit.putInt("fix_status", DISABLED);
                            prefs_edit.apply();
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
                    } catch(IOException e) {
                        // We do this to keep the compiler happy.
                    } catch (InterruptedException e) {
                        // We do this to keep the compiler happy.
                    }
                }
            }
        });
        qsmirror.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (qsmirror.isChecked()) {
					try
					{
					Shell.SU.run("cp /data/data/com.google.android.gsf/databases/gservices.db /data/data/com.r3pwn.mirrorenabler/databases/gservices.db\n");
					SQLiteDatabase db=openOrCreateDatabase("gservices.db", Context.CONTEXT_IGNORE_SECURITY, null);
					db.execSQL("INSERT INTO overrides (name, value) VALUES ('gms:cast:remote_display_enabled', 'true');");
					db.execSQL("UPDATE overrides SET value='true' WHERE name='gms:cast:remote_display_enabled';");
					Shell.SU.run("am force-stop com.google.android.gsf\n");
					Shell.SU.run("am force-stop com.google.android.gms\n");
					Shell.SU.run("am force-stop com.google.android.apps.chromecast.app\n");
					Shell.SU.run("cp /data/data/com.r3pwn.mirrorenabler/databases/gservices.db /data/data/com.google.android.gsf/databases/gservices.db\n");
					prefs_edit.putInt("qsmirror_status", ENABLED);
                    prefs_edit.apply();
					if (Build.VERSION.SDK_INT >= 21)
					{
						Toast.makeText(getApplicationContext(),"Please reboot for changes to take effect.", Toast.LENGTH_LONG).show();
					} else
					{
						Toast.makeText(getApplicationContext(),"Changes applied.", Toast.LENGTH_LONG).show();
					}
					} catch(SQLException sqle)
					{
						SQLiteDatabase db=openOrCreateDatabase("gservices.db", Context.CONTEXT_IGNORE_SECURITY, null);
						db.execSQL("UPDATE overrides SET value='true' WHERE name='gms:cast:remote_display_enabled';");
						Shell.SU.run("am force-stop com.google.android.gsf\n");
						Shell.SU.run("am force-stop com.google.android.gms\n");
						Shell.SU.run("am force-stop com.google.android.apps.chromecast.app\n");
						Shell.SU.run("cp /data/data/com.r3pwn.mirrorenabler/databases/gservices.db /data/data/com.google.android.gsf/databases/gservices.db\n");
						prefs_edit.putInt("qsmirror_status", ENABLED);
                        prefs_edit.apply();
						if (Build.VERSION.SDK_INT >= 21)
						{
							Toast.makeText(getApplicationContext(),"Please reboot for changes to take effect.", Toast.LENGTH_LONG).show();
						} else
						{
							Toast.makeText(getApplicationContext(),"Changes applied.", Toast.LENGTH_LONG).show();
						}
					} catch(Exception e)
					{
						// This part shouldn't happen.
					}
                } else {
					Shell.SU.run("cp /data/data/com.google.android.gsf/databases/gservices.db /data/data/com.r3pwn.mirrorenabler/databases/gservices.db\n");
					SQLiteDatabase db=openOrCreateDatabase("gservices.db", Context.CONTEXT_IGNORE_SECURITY, null);
					db.execSQL("UPDATE overrides SET value='false' WHERE name='gms:cast:remote_display_enabled';");
					Shell.SU.run("am force-stop com.google.android.gsf\n");
					Shell.SU.run("am force-stop com.google.android.gms\n");
					Shell.SU.run("am force-stop com.google.android.apps.chromecast.app\n");
					Shell.SU.run("cp /data/data/com.r3pwn.mirrorenabler/databases/gservices.db /data/data/com.google.android.gsf/databases/gservices.db\n");
					prefs_edit.putInt("qsmirror_status", DISABLED);
					prefs_edit.apply();
					if (Build.VERSION.SDK_INT >= 21)
					{
						Toast.makeText(getApplicationContext(),"Please reboot for changes to take effect.", Toast.LENGTH_LONG).show();
					} else
					{
						Toast.makeText(getApplicationContext(),"Changes applied.", Toast.LENGTH_LONG).show();
					}
                }
            }
        });
    }
}
