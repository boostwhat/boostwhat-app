# 📱 Panduan Lengkap Aplikasi Android "Boostwhat Bot Sync"

Aplikasi Android (APK WebView) ini dirancang khusus untuk mengatasi keterbatasan browser HP (yang tidak memiliki tombol Inspect Element / F12). 

Dengan aplikasi ini, Anda cukup **login akun bot di layar HP Anda**, dan aplikasi akan secara otomatis menyadap cookies (`sessionid`, `ds_user_id`, `csrftoken`) lalu menyimpannya langsung ke database bot `/3in1` di server Boostwhat.

---

## 🚀 Mengapa Solusi Ini 100% Bebas Checkpoint / HTTP 403?
- **IP Residential / Seluler**: Login dilakukan langsung dari HP Anda menggunakan jaringan kuota/Wi-Fi asli (Telkomsel, Indosat, XL, Tri, Smartfren, IndiHome), bukan dari IP hosting cPanel.
- **Instagram Menganggapnya Pengguna Asli**: Instagram membaca User-Agent Chrome Android resmi pada smartphone fisik, sehingga akun bot tidak akan dicurigai atau dibekukan.

---

## 🌟 Fitur Utama Aplikasi
1. **Auto-Capture Cookies Native**: Memakai `android.webkit.CookieManager` yang memiliki izin sistem untuk membaca cookie `HttpOnly` (`sessionid`).
2. **Auto-Sync ke Server**: Begitu halaman login IG berhasil masuk, aplikasi otomatis mengirim cookie ke endpoint API:
   `https://sosmed.boostwhat.web.id/api.php?action=sync_mobile_cookie`
3. **Multi-Bot Importer (+ Akun Baru)**: Ada tombol khusus untuk menghapus sesi dan memuat ulang form login, sehingga Anda bisa mengimpor akun bot ke-1, ke-2, ke-3, dan seterusnya secara cepat.
4. **Buka Panel Otomasi (/3in1)**: Terdapat tombol cepat untuk langsung membuka dashboard kontrol botting dan pemesanan auto follower/komentar.

---

## 🛠️ Cara Mengubah Menjadi File `.apk` (Siap Pasang di HP)

Pilih salah satu dari 3 cara termudah di bawah ini:

### Cara 1: Otomatis & Gratis via GitHub Actions (Rekomendasi Terbaik)
Project ini sudah dilengkapi file workflow otomatis di [`.github/workflows/build-apk.yml`](.github/workflows/build-apk.yml).
1. Buat repository baru di [GitHub](https://github.com) (bisa private).
2. Upload seluruh isi folder `android-app` ini ke repository GitHub tersebut.
3. Buka tab **Actions** di GitHub repository Anda.
4. Workflow **Build Android APK** akan berjalan otomatis selama ~1–2 menit.
5. Setelah selesai (centang hijau), klik workflow tersebut dan download artifact file **`Boostwhat-Bot-Sync-Debug-APK`**.
6. Ekstrak file zip dan install file `.apk`-nya di HP Android Anda!

---

### Cara 2: Langsung Kompilasi di HP Menggunakan Aplikasi "AIDE"
Jika Anda tidak ingin menyalakan PC/laptop:
1. Di HP Android Anda, pasang aplikasi **AIDE - IDE for Android Java C++** (tersedia di Play Store atau browser).
2. Salin folder `android-app` ini ke penyimpanan internal HP Anda.
3. Buka folder ini di dalam aplikasi AIDE.
4. Tekan tombol **Play / Run**. AIDE akan mengompilasi dan langsung memunculkan tombol **Install APK** di layar HP Anda.

---

### Cara 3: Menggunakan Android Studio di PC
1. Buka software **Android Studio** di PC/laptop.
2. Pilih **File** ➡️ **Open...** ➡️ arahkan ke folder `android-app`.
3. Tunggu proses sinkronisasi Gradle selesai.
4. Klik menu **Build** ➡️ **Build Bundle(s) / APK(s)** ➡️ **Build APK(s)**.
5. Setelah selesai, klik tombol **locate** untuk mengambil file `app-debug.apk`.
6. Kirim file `.apk` tersebut ke HP Anda via WhatsApp atau Google Drive lalu pasang.

---

## 📲 Cara Penggunaan di HP
1. Buka aplikasi **Boostwhat Bot Sync** di HP.
2. Masukkan Username dan Password akun Instagram bot Anda di layar.
3. Masukkan kode 2FA jika akun memiliki 2FA.
4. Begitu Anda masuk ke halaman feed/beranda Instagram:
   - Aplikasi akan otomatis memunculkan notifikasi: *"🎉 Bot Berhasil Terhubung!"*
   - Akun tersebut kini **otomatis aktif** di database `boostwha_auto` (tabel `bot_accounts` dan `instagram`).
5. Jika ingin memasukkan akun bot lain, klik tombol **+ Akun Baru**.
6. Jika ingin langsung mencoba komentar atau follower, klik tombol **Panel /3in1**.
