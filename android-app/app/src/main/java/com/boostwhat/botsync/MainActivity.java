package com.boostwhat.botsync;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private static final String SERVER_API_URL = "https://sosmed.boostwhat.web.id/api.php?action=sync_mobile_cookie";
    private static final String PANEL_URL = "https://sosmed.boostwhat.web.id/";
    private static final String IG_LOGIN_URL = "https://www.instagram.com/accounts/login/";
    private static final String ACCESS_KEY = "boostwhat2026";
    private static final String MOBILE_UA = "Mozilla/5.0 (Linux; Android 14; SM-S928B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36";

    private WebView webView;
    private ProgressBar progressBar;
    private TextView txtStatus;
    private View statusIndicatorDot;
    private Button btnNewBot;
    private Button btnDashboard;
    private ImageButton btnRefresh;

    private final Set<String> syncedUserIds = new HashSet<>();
    private boolean isSyncing = false;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupWebView();
        setupListeners();

        // Load Instagram login initially
        webView.loadUrl(IG_LOGIN_URL);
    }

    private void initViews() {
        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        txtStatus = findViewById(R.id.txtStatus);
        statusIndicatorDot = findViewById(R.id.statusIndicatorDot);
        btnNewBot = findViewById(R.id.btnNewBot);
        btnDashboard = findViewById(R.id.btnDashboard);
        btnRefresh = findViewById(R.id.btnRefresh);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setUserAgentString(MOBILE_UA);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress < 100) {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                checkAndInterceptCookies(url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false; // Load inside WebView
            }
        });
    }

    private void setupListeners() {
        btnNewBot.setOnClickListener(v -> clearSessionAndReloadLogin());
        btnDashboard.setOnClickListener(v -> {
            updateStatus("Membuka Dashboard /3in1 Boostwhat…", Color.parseColor("#38BDF8"));
            webView.loadUrl(PANEL_URL);
        });
        btnRefresh.setOnClickListener(v -> webView.reload());
    }

    /**
     * Intercepts cookies whenever a page finishes loading
     */
    private void checkAndInterceptCookies(String currentUrl) {
        if (isSyncing) return;

        CookieManager cookieManager = CookieManager.getInstance();
        String cookies = cookieManager.getCookie("https://www.instagram.com");

        if (cookies != null && cookies.contains("sessionid=") && cookies.contains("ds_user_id=")) {
            String userId = extractUserIdFromCookie(cookies);
            if (!userId.isEmpty() && !syncedUserIds.contains(userId)) {
                // Sesi akun baru terdeteksi!
                syncCookieToServer(cookies, userId);
            }
        }
    }

    private String extractUserIdFromCookie(String cookies) {
        Pattern pattern = Pattern.compile("ds_user_id=([^;\\s]+)");
        Matcher matcher = pattern.matcher(cookies);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    /**
     * Sends the extracted cookies directly to the Boostwhat backend
     */
    private void syncCookieToServer(String cookies, String userId) {
        isSyncing = true;
        updateStatus("🎉 Login IG terdeteksi! Mengirim session ke server…", Color.parseColor("#F59E0B"));

        executor.execute(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(SERVER_API_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String postParams = "cookies=" + URLEncoder.encode(cookies, "UTF-8")
                        + "&access_key=" + URLEncoder.encode(ACCESS_KEY, "UTF-8");

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(postParams.getBytes("UTF-8"));
                    os.flush();
                }

                int code = conn.getResponseCode();
                BufferedReader reader;
                if (code >= 200 && code < 300) {
                    reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                } else {
                    reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                }

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject json = new JSONObject(response.toString());
                boolean isSuccess = "success".equalsIgnoreCase(json.optString("status"));
                String username = json.optString("username", "Akun Bot");
                String message = json.optString("message", "");

                mainHandler.post(() -> {
                    isSyncing = false;
                    if (isSuccess) {
                        syncedUserIds.add(userId);
                        updateStatus("✅ @" + username + " aktif di pool bot /3in1!", Color.parseColor("#22C55E"));
                        showSuccessDialog(username);
                    } else {
                        updateStatus("❌ Gagal sinkronisasi: " + message, Color.parseColor("#EF4444"));
                        Toast.makeText(MainActivity.this, "Gagal simpan: " + message, Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {
                mainHandler.post(() -> {
                    isSyncing = false;
                    updateStatus("⚠️ Gagal koneksi ke server: " + e.getMessage(), Color.parseColor("#EF4444"));
                    Toast.makeText(MainActivity.this, "Koneksi Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        });
    }

    private void showSuccessDialog(String username) {
        new AlertDialog.Builder(this)
                .setTitle("🎉 Bot Berhasil Terhubung!")
                .setMessage("Akun @" + username + " berhasil diverifikasi dan tersimpan ke database bot Boostwhat (/3in1).\n\nApa langkah selanjutnya yang ingin Anda lakukan?")
                .setPositiveButton("Buka Panel /3in1", (dialog, which) -> {
                    updateStatus("Membuka Dashboard /3in1…", Color.parseColor("#38BDF8"));
                    webView.loadUrl(PANEL_URL);
                })
                .setNeutralButton("➕ Tambah Akun Bot Lain", (dialog, which) -> {
                    clearSessionAndReloadLogin();
                })
                .setCancelable(false)
                .show();
    }

    /**
     * Clears cookies and cache so the user can easily login with a second bot account
     */
    private void clearSessionAndReloadLogin() {
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookies(null);
        cookieManager.flush();
        WebStorage.getInstance().deleteAllData();
        webView.clearCache(true);
        webView.clearHistory();

        updateStatus("Silakan login akun bot berikutnya…", Color.parseColor("#94A3B8"));
        webView.loadUrl(IG_LOGIN_URL);
        Toast.makeText(this, "Sesi dibersihkan. Siap untuk login akun baru!", Toast.LENGTH_SHORT).show();
    }

    private void updateStatus(String status, int dotColor) {
        txtStatus.setText(status);
        statusIndicatorDot.setBackgroundColor(dotColor);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
