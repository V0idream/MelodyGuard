package com.codex.oplusmelody173hook;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public final class HookEntry implements IXposedHookLoadPackage {
    private static final String TAG = "[OplusMelody173Hook] ";
    private static final String TARGET_PACKAGE = "com.oplus.melody";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        if (!TARGET_PACKAGE.equals(loadPackageParam.packageName)) {
            return;
        }

        hookNoiseReductionDecision(loadPackageParam.classLoader);
        hookWhitelistRefresh(loadPackageParam.classLoader);
    }

    private static void hookNoiseReductionDecision(ClassLoader classLoader) {
        try {
            XposedHelpers.findAndHookMethod(
                    "com.oplus.melody.common.data.WhitelistConfigDTO$NoiseReductionMode",
                    classLoader,
                    "getDecideByEarDevice",
                    XC_MethodReplacement.returnConstant(false));
            XposedBridge.log(TAG + "Hook 1 installed: getDecideByEarDevice() = false");
        } catch (Throwable throwable) {
            XposedBridge.log(TAG + "Hook 1 failed");
            XposedBridge.log(throwable);
        }
    }

    private static void hookWhitelistRefresh(ClassLoader classLoader) {
        try {
            XposedHelpers.findAndHookMethod(
                    "h7.e",
                    classLoader,
                    "l",
                    String.class,
                    XC_MethodReplacement.DO_NOTHING);
            XposedBridge.log(TAG + "Hook 2 installed: WhitelistRepositoryServerImpl refresh skipped");
        } catch (Throwable throwable) {
            XposedBridge.log(TAG + "Hook 2 failed");
            XposedBridge.log(throwable);
        }
    }
}
