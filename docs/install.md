---
layout: default
title: Install
permalink: /install/
---

<p align="center">
  <a href="/ametro">Home</a> •
  <a href="/ametro/privacy/">Privacy</a> •
  <a href="/ametro/install/">Install</a> •
  <a href="/ametro/maps/">Maps</a> •
  <a href="/ametro/maps-editing/">Map Editing</a> •
  <a href="/ametro/contributing/">Contribute</a> •
  <a href="/ametro/faq/">FAQ</a> •
</p>


# 📲 Install aMetro

You can get aMetro in one of three ways:  

---

## ✅ 1) Google Play (recommended)

- The app is published under the package ID: `io.github.romangolovanov.apps.ametro`  
- [Google Play link](https://play.google.com/store/apps/details?id=io.github.romangolovanov.apps.ametro) *(currently under review — will be live soon)*  

---

## 📦 2) GitHub Releases

- Download prebuilt APKs from the [Releases page](https://github.com/RomanGolovanov/ametro/releases).  
- These builds are signed and ready to install on any Android 7.0+ (API 24) device.  
- File size: ~15 MB (all maps are bundled inside, no extra downloads required).

---

## 🔧 3) Build from Source

If you want the latest development version or prefer to compile yourself:

1. **Clone the repo**  
```
   git clone https://github.com/RomanGolovanov/ametro.git  
   cd ametro  
```

2. **Open in Android Studio**  
   - Install the latest [Android Studio](https://developer.android.com/studio).  
   - Open the cloned project.

3. **Let Gradle sync**  
   - Android Studio will download required dependencies automatically.  

4. **Build the APK**  
   - From the menu: **Build > Build Bundle(s) / APK(s) > Build APK(s)**.  
   - The output APK will be in `app/build/outputs/apk/`.  

5. **Install on your device**  
   - Transfer the APK to your phone.  
   - Enable *Install from unknown sources* (if not from Play Store).  
   - Tap the file to install.

---

## 💡 Notes

- All maps are **bundled inside the app**, so you don’t need an internet connection to fetch maps.  
- The app requires only minimal permissions (e.g., storage access if you want to load or patch maps manually).  
- No ads, no tracking, no background services.  
