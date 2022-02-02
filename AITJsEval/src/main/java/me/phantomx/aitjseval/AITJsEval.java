package me.phantomx.aitjseval;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import me.phantomx.aitjseval.listener.OnJavaScriptResponseListener;

public class AITJsEval {
    private static AITJsEval instance;

    private static AtomicReference<WebView> mWebView;
    private static Handler mHandler;

    private static List<Script> mQueueList;
    private static AtomicReference<Script> mQueue;

    private static final String JS_RUN = "JsEval";
    private static final String JS_EXCEPTION = "JsException";
    private static final String JS_KEY = ":TAG:";

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    AITJsEval(Context context) {
        mHandler = new Handler(Looper.getMainLooper());
        mWebView = new AtomicReference<>(new WebView(context));

        mWebView.get().setWillNotDraw(true);
        mWebView.get().getSettings().setJavaScriptEnabled(true);
        mWebView.get().addJavascriptInterface(this, JS_RUN);

        mQueueList = new ArrayList<>();
        mQueue = new AtomicReference<>(null);
    }

    public static void initialize(Context context) {
        instance = new AITJsEval(context);
    }

    /**
     * call {@link #initialize(Context)} first
     * @return null if not initialized
     * @throws IllegalStateException if not initialized
     * @see #initialize(Context)
     */
    public static AITJsEval get() throws IllegalStateException {
        if (instance == null) throw new IllegalStateException("AITJsEval didn't initialized, call initialize first!");
        return instance;
    }

    /**
     * Eval javascript
     * @param tag is key don't put same existing key if in queue
     * @param script is eval javascript
     * @param listener response if code executed
     */
    @AnyThread
    public void enqueue(@NonNull String tag, @NonNull String script, @NonNull OnJavaScriptResponseListener listener) {
        script = safeStringInJsCode(script);
        script = String.format("%s.%s(\"%s\"+eval('try{%s}catch(e){\"%s\"+e}'));", JS_RUN, getClass().getSimpleName(), tag + JS_KEY, script, JS_EXCEPTION);
        Script reqScript = new Script(tag, script, listener);
        mQueueList.add(reqScript);
        if (mQueue.get() == null) runJavaScript(reqScript);
    }

    private void runJavaScript(@NonNull Script script) {
        script.setStatus(ScriptStatus.RUNNING);
        mQueue.set(script);
        if (Looper.myLooper() == Looper.getMainLooper())
            runJs(script);
        else
            mHandler.post(() -> runJs(script));
    }

    @SuppressWarnings("all")
    private void runJs(@NonNull Script script) {
        try {
            String s = "</script>";
            String j = (s.replace("/", "") + script.getScript() + s);
            mWebView.get().loadUrl("data:text/html;charset=utf-8;base64," +
                    Base64.encodeToString(
                            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) ?
                                    j.getBytes(StandardCharsets.UTF_8)
                                    :
                                    j.getBytes("UTF-8"),
                            Base64.NO_WRAP));
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Nothing to do!
     * @param r is result
     */
    @SuppressWarnings("all")
    @JavascriptInterface
    public void AITJsEval(@NonNull String r) {
        if (mQueue.get() == null) return;
        try {
            if (!r.startsWith(mQueue.get().getTag())) return;
            r = r.split(Pattern.quote(JS_KEY))[1];
        } catch (Exception e) {
            e.printStackTrace();
            if (!r.startsWith(JS_EXCEPTION)) r = JS_EXCEPTION + e.getMessage();
        }
        mQueue.get().setError(r.startsWith(JS_EXCEPTION));
        mQueue.get().setResult(mQueue.get().isError() ? r.substring(JS_EXCEPTION.length()) : r);
        mQueue.get().setStatus(ScriptStatus.COMPLETED);
        final Script script = mQueue.get();
        mQueueList.remove(script);
        mQueue.set(null);
        if (script.getCallback() != null && !script.isCancelled())
            mHandler.post(() -> script.getCallback().onResponse(script));
        if (mQueueList.size() > 0)
            runJavaScript(mQueueList.get(0));
        else
            mHandler.post(() -> mWebView.get().loadUrl("about:blank"));
    }

    /**
     * cancel Script by specific tag
     * @param tag is key
     */
    public void cancel(@NonNull String tag) {
        if (mQueue.get() != null && mQueue.get().getTag().equals(tag)) {
            mQueueList.remove(mQueue.get());
            mQueue.get().setCancelled(true);
            return;
        }
        int index = mQueueList.indexOf(new Script(tag, "", null));
        if (index != -1) mQueueList.remove(index);
    }

    /**
     * Release all resources and remove all queue!.
     * You need call {@link #initialize(Context)} again to call {@link #enqueue(String, String, OnJavaScriptResponseListener)}
     * @see #initialize(Context)
     * @see #enqueue(String, String, OnJavaScriptResponseListener)
     */
    public void release() {
        if (mWebView == null) return;
        mWebView.get().removeJavascriptInterface(JS_RUN);
        mWebView.get().loadUrl("about:blank");
        mWebView.get().stopLoading();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) mWebView.get().freeMemory();
        mWebView.get().clearHistory();
        mWebView.get().removeAllViews();
        mWebView.get().destroyDrawingCache();
        mWebView.get().destroy();
        mWebView.set(null);
        mQueueList.clear();
        mQueue.set(null);
        mHandler.removeCallbacks(null);
    }

    @NonNull
    private String safeStringInJsCode(@NonNull String val) {
        val = escapeSlash(val);
        val = escapeSingleQuotes(val);
        val = escapeClosingScript(val);
        val = escapeNewLines(val);
        val = escapeCarriageReturn(val);
        return val;
    }

    @NonNull
    private String escapeCarriageReturn(@NonNull String str) {
        return str.replace("\r", "\\r");
    }

    @NonNull
    private String escapeClosingScript(@NonNull String str) {
        return str.replace("</", "<\\/");
    }

    @NonNull
    private String escapeNewLines(@NonNull String str) {
        return str.replace("\n", "\\n");
    }

    @NonNull
    private String escapeSingleQuotes(@NonNull String str) {
        return str.replace("'", "\\'");
    }

    @NonNull
    private String escapeSlash(@NonNull String str) {
        return str.replace("\\", "\\\\");
    }

}
