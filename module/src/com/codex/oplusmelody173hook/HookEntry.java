package com.codex.oplusmelody173hook;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Runtime-only hooks for Oplus Wireless Earphones 17.0.3.
 *
 * Target classes are intentionally accessed by reflection: the module is compiled
 * without the application's private classes and remains installable as a small
 * standalone LSPosed package.
 */
public final class HookEntry implements IXposedHookLoadPackage {
    private static final String TAG = "[MelodyGuard] ";
    private static final String TARGET_PACKAGE = "com.oplus.melody";
    private static final String FREE4_NAME = "Free4 高解析通透";
    private static final int[] DEFAULT_EQ_FREQUENCIES = {
            62, 250, 1000, 4000, 8000, 16000
    };

    private static final Map<String, Boolean> FREE4_DISPATCHED = new ConcurrentHashMap<>();
    private static final Map<String, Long> LAST_DEEP_DISPATCH = new ConcurrentHashMap<>();

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        if (!TARGET_PACKAGE.equals(loadPackageParam.packageName)) {
            return;
        }

        ClassLoader classLoader = loadPackageParam.classLoader;
        hookNoiseReductionDecision(classLoader);
        hookWhitelistRefresh(classLoader);
        hookDeepNoiseRecovery(classLoader);
        hookWearCountRecovery(classLoader);
        hookFree4Equalizer(classLoader);
    }

    private static void hookNoiseReductionDecision(ClassLoader classLoader) {
        try {
            XposedHelpers.findAndHookMethod(
                    "com.oplus.melody.common.data.WhitelistConfigDTO$NoiseReductionMode",
                    classLoader,
                    "getDecideByEarDevice",
                    XC_MethodReplacement.returnConstant(false));
            log("Hook 1 installed: getDecideByEarDevice() = false");
        } catch (Throwable throwable) {
            logFailure("Hook 1 failed", throwable);
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
            log("Hook 2 installed: WhitelistRepositoryServerImpl refresh skipped");
        } catch (Throwable throwable) {
            logFailure("Hook 2 failed", throwable);
        }
    }

    /**
     * The 15.8.1 reference inserts this logic into NoiseReductionItem's data
     * observer.  In 17.0.3 the same observer is
     * NoiseReductionItem#onEarphoneDataChanged(E8.s).  When the selected
     * whitelist mode is v2 (type 7 or 10), resend the type-4 protocol command.
     */
    private static void hookDeepNoiseRecovery(final ClassLoader classLoader) {
        try {
            final Class<?> noiseReductionVoClass = Class.forName("E8.s", false, classLoader);
            XposedHelpers.findAndHookMethod(
                    "com.oplus.melody.ui.component.detail.noisereduction.NoiseReductionItem",
                    classLoader,
                    "onEarphoneDataChanged",
                    noiseReductionVoClass,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            try {
                                dispatchDeepFromNoiseReductionItem(
                                        classLoader, param.thisObject, param.args[0]);
                            } catch (Throwable throwable) {
                                logFailure("Deep ANC observer failed", throwable);
                            }
                        }
                    });
            log("Hook 3 installed: v2 noise reduction resends type-4 deep mode");
        } catch (Throwable throwable) {
            logFailure("Hook 3 failed", throwable);
        }
    }

    /**
     * The reference build also changes the wear transition policy: only an
     * increase in the number of in-ear buds causes a new deep-mode command.
     * EarphoneControlProvider receives both the previous and new EarphoneDTO
     * states, so this hook implements that policy without touching UI code.
     */
    private static void hookWearCountRecovery(final ClassLoader classLoader) {
        try {
            final Class<?> earphoneDtoClass = Class.forName(
                    "com.oplus.melody.model.repository.earphone.EarphoneDTO",
                    false,
                    classLoader);
            XposedHelpers.findAndHookMethod(
                    "com.oplus.melody.provider.EarphoneControlProvider",
                    classLoader,
                    "onActiveEarphoneChanged",
                    earphoneDtoClass,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            try {
                                Object nextEarphone = param.args[0];
                                Object previousEarphone = getFieldValue(
                                        param.thisObject, "activeEarphone");
                                int previousCount = inEarCount(
                                        call(previousEarphone, "getEarStatus"));
                                int nextCount = inEarCount(
                                        call(nextEarphone, "getEarStatus"));
                                if (nextCount <= previousCount) {
                                    return;
                                }
                                dispatchDeepForEarphone(classLoader, nextEarphone);
                            } catch (Throwable throwable) {
                                logFailure("Wear-count recovery failed", throwable);
                            }
                        }
                    });
            log("Hook 4 installed: deep ANC dispatched only when in-ear count increases");
        } catch (Throwable throwable) {
            logFailure("Hook 4 failed", throwable);
        }
    }

    /**
     * Reproduces the Free4 block found in the supplied 15.8.1 reference APK.
     * Q6.d#b(address) is the 17.0.3 EQ-data receive/normalisation point.
     */
    private static void hookFree4Equalizer(final ClassLoader classLoader) {
        try {
            XposedHelpers.findAndHookMethod(
                    "Q6.d",
                    classLoader,
                    "b",
                    String.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            try {
                                dispatchFree4Equalizer(classLoader, param.thisObject, param.args[0],
                                        param.getResult());
                            } catch (Throwable throwable) {
                                logFailure("Free4 EQ hook failed", throwable);
                            }
                        }
                    });
            log("Hook 5 installed: Free4 high-resolution EQ auto preset");
        } catch (Throwable throwable) {
            logFailure("Hook 5 failed", throwable);
        }
    }

    private static void dispatchDeepFromNoiseReductionItem(
            ClassLoader classLoader, Object item, Object noiseReductionVo) throws Exception {
        if (item == null || noiseReductionVo == null) {
            return;
        }
        Object state = call(noiseReductionVo, "getConnectionState");
        if (!(state instanceof Number) || ((Number) state).intValue() != 2) {
            return;
        }

        Object currentIndexObject = call(noiseReductionVo, "getCurrentNoiseReductionModeIndex");
        if (!(currentIndexObject instanceof Number)) {
            return;
        }
        int currentIndex = ((Number) currentIndexObject).intValue();
        List<?> modes = asList(call(noiseReductionVo, "getNoiseReductionModeList"));
        int currentType = Integer.MIN_VALUE;
        for (Object mode : modes) {
            if (mode == null) {
                continue;
            }
            Integer protocolIndex = asInt(call(mode, "getProtocolIndex"));
            if (protocolIndex != null && protocolIndex == currentIndex) {
                Integer modeType = asInt(call(mode, "getModeType"));
                if (modeType != null) {
                    currentType = modeType;
                }
                break;
            }
        }
        if (currentType != 7 && currentType != 10) {
            return;
        }

        int deepProtocolIndex = findProtocolIndex(modes, 4);
        if (deepProtocolIndex < 0) {
            return;
        }
        Object viewModel = getFieldValue(item, "mViewModel");
        Object addressObject = getFieldValue(viewModel, "b");
        if (!(addressObject instanceof String) || ((String) addressObject).length() == 0) {
            return;
        }
        sendDeepCommand(classLoader, deepProtocolIndex, (String) addressObject);
    }

    private static void dispatchDeepForEarphone(ClassLoader classLoader, Object earphone)
            throws Exception {
        if (earphone == null) {
            return;
        }
        Object addressObject = call(earphone, "getMacAddress");
        if (!(addressObject instanceof String) || ((String) addressObject).length() == 0) {
            return;
        }
        String address = (String) addressObject;

        Object productObject = call(earphone, "getProductId");
        Object colorObject = call(earphone, "getColorId");
        if (!(productObject instanceof String) || colorObject == null) {
            return;
        }
        String productId = (String) productObject;
        String colorId = String.valueOf(colorObject);
        Object whitelistRepository = invokeStatic(classLoader, "h7.a", "f");
        Object config = call(whitelistRepository, "c", productId, colorId);
        Object function = call(config, "getFunction");
        List<?> modes = asList(call(function, "getNoiseReductionMode"));
        int deepProtocolIndex = findProtocolIndex(modes, 4);
        if (deepProtocolIndex < 0) {
            return;
        }

        // Preserve an explicitly selected light/transparent/off mode.  Unknown
        // initial states and the two adaptive v2 types use the requested default.
        Integer currentIndex = asInt(call(earphone, "getNoiseReductionModeIndex"));
        if (currentIndex != null) {
            Integer currentType = findModeTypeByProtocol(modes, currentIndex);
            if (currentType != null && currentType != 4
                    && currentType != 7 && currentType != 10) {
                return;
            }
        }
        sendDeepCommand(classLoader, deepProtocolIndex, address);
    }

    private static void sendDeepCommand(ClassLoader classLoader, int protocolIndex, String address)
            throws Exception {
        if (!markDeepDispatch(address)) {
            return;
        }
        Object repository = invokeStatic(
                classLoader,
                "com.oplus.melody.model.repository.earphone.b",
                "E");
        call(repository, "p0", protocolIndex, address);
        log("Deep ANC command sent: protocolIndex=" + protocolIndex
                + ", address=" + maskAddress(address));
    }

    private static void dispatchFree4Equalizer(
            ClassLoader classLoader, Object repository, Object addressObject, Object result)
            throws Exception {
        if (!(addressObject instanceof String)) {
            return;
        }
        String address = (String) addressObject;
        if (address.length() == 0 || FREE4_DISPATCHED.containsKey(address)) {
            return;
        }

        Object managerHolder = getStaticFieldValue(classLoader,
                "com.oplus.melody.btsdk.api.manager.DeviceInfoManager$a", "a");
        Object deviceInfo = call(managerHolder, "h", address);
        Object nameObject = call(deviceInfo, "getDeviceName");
        if (!(nameObject instanceof String)
                || !((String) nameObject).toLowerCase(Locale.ROOT).contains("free4")) {
            return;
        }

        FREE4_DISPATCHED.put(address, Boolean.TRUE);
        try {
            List<?> eqList = asList(result);
            Object existing = findPreset(eqList);
            if (existing != null) {
                call(existing, "setIsSelected", 1);
                call(repository, "i", address, existing, 2);
                log("Free4 EQ selected: address=" + maskAddress(address));
                return;
            }

            int[] frequencies = findFrequencies(eqList);
            int[] dbValues = buildDbValues(frequencies);
            Class<?> presetClass = Class.forName("Q6.b", false, classLoader);
            Constructor<?> constructor = presetClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            Object preset = constructor.newInstance();
            call(preset, "setEqId", 0);
            call(preset, "setName", FREE4_NAME);
            call(preset, "setNameLength", 21);
            call(preset, "setMaxValue", 6);
            call(preset, "setMinValue", -6);
            call(preset, "setIsSelected", 1);
            call(preset, "setFrequency", frequencies);
            call(preset, "setDbValue", dbValues);
            call(repository, "i", address, preset, 1);
            log("Free4 EQ created and selected: address=" + maskAddress(address));
        } catch (Throwable throwable) {
            FREE4_DISPATCHED.remove(address);
            throw throwable;
        }
    }

    private static Object findPreset(List<?> eqList) throws Exception {
        for (Object entry : eqList) {
            Object name = call(entry, "getName");
            if (FREE4_NAME.equals(name)) {
                return entry;
            }
        }
        return null;
    }

    private static int[] findFrequencies(List<?> eqList) throws Exception {
        for (Object entry : eqList) {
            Object value = call(entry, "getFrequency");
            if (value instanceof int[] && ((int[]) value).length > 0) {
                int[] frequencies = (int[]) value;
                int[] copy = new int[frequencies.length];
                System.arraycopy(frequencies, 0, copy, 0, frequencies.length);
                return copy;
            }
        }
        int[] fallback = new int[DEFAULT_EQ_FREQUENCIES.length];
        System.arraycopy(DEFAULT_EQ_FREQUENCIES, 0, fallback, 0, fallback.length);
        return fallback;
    }

    private static int[] buildDbValues(int[] frequencies) {
        int[] values = new int[frequencies.length];
        for (int i = 0; i < frequencies.length; i++) {
            int frequency = frequencies[i];
            if (frequency <= 125) {
                values[i] = -3;
            } else if (frequency <= 500) {
                values[i] = -2;
            } else if (frequency <= 1000) {
                values[i] = -1;
            } else if (frequency <= 2000) {
                values[i] = 0;
            } else if (frequency <= 4000) {
                values[i] = 1;
            } else if (frequency <= 8000) {
                values[i] = 2;
            } else {
                values[i] = 1;
            }
        }
        return values;
    }

    private static int findProtocolIndex(List<?> modes, int modeType) throws Exception {
        for (Object mode : modes) {
            Integer type = asInt(call(mode, "getModeType"));
            if (type != null && type == modeType) {
                Integer protocol = asInt(call(mode, "getProtocolIndex"));
                if (protocol != null) {
                    return protocol;
                }
            }
        }
        return -1;
    }

    private static Integer findModeTypeByProtocol(List<?> modes, int protocolIndex)
            throws Exception {
        for (Object mode : modes) {
            Integer protocol = asInt(call(mode, "getProtocolIndex"));
            if (protocol != null && protocol == protocolIndex) {
                return asInt(call(mode, "getModeType"));
            }
        }
        return null;
    }

    private static int inEarCount(Object earStatus) throws Exception {
        if (earStatus == null) {
            return 0;
        }
        Integer left = asInt(call(earStatus, "getLeftStatus"));
        Integer right = asInt(call(earStatus, "getRightStatus"));
        int count = 0;
        if (left != null && (left & 2) == 2) {
            count++;
        }
        if (right != null && (right & 2) == 2) {
            count++;
        }
        return count;
    }

    private static boolean markDeepDispatch(String address) {
        long now = System.currentTimeMillis();
        Long previous = LAST_DEEP_DISPATCH.get(address);
        if (previous != null && now - previous < 1200L) {
            return false;
        }
        LAST_DEEP_DISPATCH.put(address, now);
        return true;
    }

    private static List<?> asList(Object value) {
        if (value instanceof List<?>) {
            return (List<?>) value;
        }
        return Collections.emptyList();
    }

    private static Integer asInt(Object value) {
        return value instanceof Number ? ((Number) value).intValue() : null;
    }

    private static Object invokeStatic(
            ClassLoader classLoader, String className, String methodName, Object... args)
            throws Exception {
        return call(Class.forName(className, false, classLoader), methodName, args);
    }

    private static Object getStaticFieldValue(
            ClassLoader classLoader, String className, String fieldName) throws Exception {
        return getFieldValue(Class.forName(className, false, classLoader), fieldName);
    }

    private static Object getFieldValue(Object receiver, String fieldName) throws Exception {
        if (receiver == null) {
            return null;
        }
        Class<?> type = receiver instanceof Class<?> ? (Class<?>) receiver : receiver.getClass();
        Field field = findField(type, fieldName);
        if (field == null) {
            return null;
        }
        field.setAccessible(true);
        return field.get(receiver instanceof Class<?> ? null : receiver);
    }

    private static Field findField(Class<?> type, String fieldName) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                // Search the superclass; obfuscated view-model fields are inherited.
            }
        }
        return null;
    }

    private static Object call(Object receiver, String methodName, Object... args) throws Exception {
        if (receiver == null) {
            return null;
        }
        Class<?> type = receiver instanceof Class<?> ? (Class<?>) receiver : receiver.getClass();
        Method method = findMethod(type, methodName, args);
        if (method == null) {
            throw new NoSuchMethodException(type.getName() + "#" + methodName);
        }
        method.setAccessible(true);
        return method.invoke(receiver instanceof Class<?> ? null : receiver, args);
    }

    private static Method findMethod(Class<?> type, String methodName, Object[] args) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (!methodName.equals(method.getName())
                        || method.getParameterTypes().length != args.length) {
                    continue;
                }
                if (parametersMatch(method.getParameterTypes(), args)) {
                    return method;
                }
            }
        }
        return null;
    }

    private static boolean parametersMatch(Class<?>[] parameterTypes, Object[] args) {
        for (int i = 0; i < parameterTypes.length; i++) {
            if (args[i] == null) {
                continue;
            }
            Class<?> parameterType = wrap(parameterTypes[i]);
            if (!parameterType.isAssignableFrom(args[i].getClass())) {
                return false;
            }
        }
        return true;
    }

    private static Class<?> wrap(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
        if (type == Integer.TYPE) {
            return Integer.class;
        }
        if (type == Boolean.TYPE) {
            return Boolean.class;
        }
        if (type == Long.TYPE) {
            return Long.class;
        }
        if (type == Short.TYPE) {
            return Short.class;
        }
        if (type == Byte.TYPE) {
            return Byte.class;
        }
        if (type == Character.TYPE) {
            return Character.class;
        }
        if (type == Float.TYPE) {
            return Float.class;
        }
        if (type == Double.TYPE) {
            return Double.class;
        }
        return type;
    }

    private static String maskAddress(String address) {
        if (address == null || address.length() < 5) {
            return "<hidden>";
        }
        return address.substring(0, 3) + "***" + address.substring(address.length() - 2);
    }

    private static void log(String message) {
        XposedBridge.log(TAG + message);
    }

    private static void logFailure(String message, Throwable throwable) {
        XposedBridge.log(TAG + message);
        XposedBridge.log(throwable);
    }
}
