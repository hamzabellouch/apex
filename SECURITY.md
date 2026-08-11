# Security Policy

## Supported Versions

The following versions of Apex are currently supported with security, privacy, and stability updates.

| Version       | Supported          |
| ------------- | ------------------ |
| + 1.0.0         | :white_check_mark: |
| < 1.0.0       | :x:                |



## Reporting a Vulnerability

If you discover a security vulnerability, privacy issue, unexpected behavior with system permissions, or any issue that could negatively affect users or device stability, please report it responsibly.

### Before Reporting
Please make sure that:
- The issue is reproducible
- You are using the latest supported version of Apex
- The issue is not caused by third-party modifications, custom ROMs, or unsupported Android environments

### How to Report
You can report vulnerabilities through:
- GitHub Issues (for non-sensitive reports and general bug reports)
- Direct private contact channels / email for sensitive vulnerabilities or security concerns

When reporting, please include:
- Device model and Android OS version
- Apex application version
- Granted permissions state (e.g., Usage Access, Accessibility Service status)
- Steps to reproduce the issue
- Screenshots or system logcat outputs if available
- A clear explanation of the potential security or privacy impact

### Response Policy
Security reports are reviewed as quickly as possible.  
If the issue is confirmed:
- The vulnerability will be investigated, verified, and patched
- A fix will be included in the next update release
- Credit may be given to the reporter if requested

If the report is invalid, incomplete, or not reproducible, it may be closed without action.



## Security & Architecture Notes

Apex is designed with a strict **Privacy-First & On-Device Security Model**:

- **Local Execution:** Apex operates 100% locally on your device. It does not transmit app data, package lists, or usage stats to external servers.
- **Root-Free Operation:** Apex does not require root privileges. It relies entirely on official Android system APIs (`StorageStatsManager`, `ActivityManager`, `UsageStatsManager`).
- **Accessibility Service Integrity:** The Accessibility Service is used strictly to automate UI navigation for clearing app cache and force-stopping user-selected apps. It **never** reads, logs, or transmits sensitive screen text, passwords, or personal user data.
- **Transient Memory Handling:** Temporary memory caches held by Apex during scanning are automatically cleared upon application pause, trim, or termination to keep the footprint minimal and secure.
