package de.binarynoise.logfailedxsharedpreferences;

import android.util.Log;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import java.io.File;

import static de.robv.android.xposed.XposedBridge.hookAllMethods;

@SuppressWarnings("NonPrivateFieldAccessedInSynchronizedContext")
public class Hook implements IXposedHookLoadPackage {
	
	final static Object lock = new Object();
	static boolean hasTrowable = false;

	@Override
	public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
		Class<?> xmlUtilsClass = XposedHelpers.findClass("com.android.internal.util.XmlUtils", lpparam.classLoader);
		
		hookAllMethods(xmlUtilsClass, "readMapXml", new XC_MethodHook() {
			public void afterHookedMethod(MethodHookParam param) {
				if (param.hasThrowable()) synchronized (lock) {
					hasTrowable = param.hasThrowable();
				}
			}
		});
		
		hookAllMethods(XSharedPreferences.class, "loadFromDiskLocked", new XC_MethodHook() {
			public void afterHookedMethod(MethodHookParam param) {
				synchronized (lock) {
					if (hasTrowable) {
						XSharedPreferences thisRef = (XSharedPreferences) param.thisObject;
						File file = thisRef.getFile();
						Log.w("XSharedPreferences", "failed accessing" + file.getAbsolutePath());
					}
				}
			}
		});
	}
}
