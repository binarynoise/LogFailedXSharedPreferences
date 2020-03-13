/*
 * Copyright (c) 2020 @binarynoise.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
