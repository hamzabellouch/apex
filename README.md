# BlueIris

<h3 align="center"> Modern, Privacy-First Android Cache Cleaner & System Storage Optimizer.</h3>

<p align="center">
Reclaim storage space and optimize system memory with automated, privacy-first Android cache cleaning.
</p>

<img width="5504" height="3072" alt="BlueIris" src="https://github.com/user-attachments/assets/5f45f01b-f2f7-415c-8202-13a58ff63364" />



## Overview

**BlueIris** is a modern, high-performance, and privacy-focused Android application designed to help users inspect storage usage, clear app cache, and optimize system memory (RAM). 

Unlike traditional cleaning apps filled with ads and background trackers, BlueIris operates **100% on-device** without collecting or transmitting any personal data. It leverages native Android system APIs (`StorageStatsManager`, `UsageStatsManager`) alongside an automated `AccessibilityService` to perform seamless batch cache clearing and app force-stopping without needing root access.

BlueIris is built following **Modern Android Development (MAD)** standards with Kotlin and Jetpack Compose to ensure maximum responsiveness, battery efficiency, and a clean Material Design 3 user interface.



## Screenshots

BlueIris UI & Features:

<div align="center">
  <div>
    <img src="https://github.com/hamzabellouch/blueiris/blob/main/Images/1.jpg" width="30%" />
    <img src="https://github.com/hamzabellouch/blueiris/blob/main/Images/2.jpg" width="30%" />
    <img src="https://github.com/hamzabellouch/blueiris/blob/main/Images/3.jpg" width="30%" />
    <img src="https://github.com/hamzabellouch/blueiris/blob/main/Images/4.jpg" width="30%" />
    <img src="https://github.com/hamzabellouch/blueiris/blob/main/Images/5.jpg" width="30%" />
    <img src="https://github.com/hamzabellouch/blueiris/blob/main/Images/6.jpg" width="30%" />
    <img src="https://github.com/hamzabellouch/blueiris/blob/main/Images/7.jpg" width="30%" />
    <img src="https://github.com/hamzabellouch/blueiris/blob/main/Images/8.jpg" width="30%" />
    <img src="https://github.com/hamzabellouch/blueiris/blob/main/Images/9.jpg" width="30%" />
    <img src="https://github.com/hamzabellouch/blueiris/blob/main/Images/10.jpg" width="30%" />
    <img src="https://github.com/hamzabellouch/blueiris/blob/main/Images/11.jpg" width="30%" />
    <img src="https://github.com/hamzabellouch/blueiris/blob/main/Images/12.jpg" width="30%" />
    <img src="https://github.com/hamzabellouch/blueiris/blob/main/Images/13.jpg" width="30%" />
    <img src="https://github.com/hamzabellouch/blueiris/blob/main/Images/14.jpg" width="30%" />
  </div>
</div>

<br>



## ⭐ Key Features & Capabilities

| Feature | Method / API Used | Performance & Speed | Privacy & Safety Level | Description |
| :--- | :--- | :--- | :--- | :--- |
| **Accurate Cache Scanning** | `StorageStatsManager` & `PACKAGE_USAGE_STATS` | **Ultra-Fast** (< 1 sec) | **100% On-Device** | Reads exact cache sizes for user and pre-installed system applications. |
| **Automated Batch Cleaning** | Custom `AccessibilityService` | **Automated & Fast** | **Strictly Isolated** | Automates navigating to app settings and clicking "Clear Cache" without manual user repetition. |
| **RAM Optimization** | `ActivityManager` & System Memory Trimming | **Instant** | **Safe** | Monitors real-time total, used, and free RAM and releases background app memory. |
| **System App Protection** | Built-in Safety Guard Rules | **Real-Time** | **System Protective** | Automatically prevents force-stopping critical system apps (Settings, SystemUI, Play Services). |
| **Zero Telemetry / No Ads** | Pure Local Logic | **N/A** | **Complete Privacy** | No analytics SDKs, no external network requests, and no user tracking. |



## 🛠 Tech Stack & Architecture

BlueIris follows clean code architecture principles for maintainability, high performance, and minimal memory usage:

* **Language & Concurrency:** `100% Kotlin`, `Coroutines`, & `StateFlow`
* **UI Framework:** `Jetpack Compose` with `Material Design 3` & `Dynamic Color` (Material You)
* **Architecture:** `Single Activity Architecture` (`MainActivity`)
* **Core APIs & Services:**
  - `StorageStatsManager` & `UsageStatsManager` for precise app storage inspection
  - `CacheCleanerAccessibilityService` for automated system UI interaction
  - `AppOpsManager` for safe permission verification
* **Optimization & Caching:** `LruCache` for app icon rendering & transient memory wiping on app lifecycle pause



## 🔥 Installation

1. Go to the Releases page:
   https://github.com/hamzabellouch/blueiris/releases

2. Download the latest `.apk` file.

3. Install the application on your Android device.

4. Make sure that: `Install from unknown sources` is enabled in your Android settings.

## 🔨 Building from Source - Not available yet


>To build Omicron locally, make sure you have the latest version of Android Studio installed.
>1. Clone the repository: `git clone https://github.com/hamzabellouch/blueiris.git`
>2. Open the project in Android Studio.
>3. Sync Gradle dependencies.
>4. Build and run the application on your device or emulator.



> [!WARNING]
> There is always a possibility of error, so we assume no responsibility for any inaccuracies.


### <a name="Copyright©2026"></a> Copyright © 2026

Thank you for checking out Omicron. If you have any feedback or suggestions, feel free to contact us:
hamzabellouchcontact@gmail.com

Stay connected and follow us on:  
[Facebook](https://facebook.com/hamzabellouch1) | [Instagram](https://instagram.com/hamzabellouch0) | [Twitter](https://twitter.com/hamzabellouch0) | [Telegram](https://t.me/hammzabellouch) | [LinkedIn](https://www.linkedin.com/in/hamzabellouch)
