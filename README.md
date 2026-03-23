# KioskSample
Kiosk sample app.

---

### Set device owner

```
$ adb shell dpm set-device-owner test.android.kiosk.debug/test.android.kiosk.MainDeviceAdminReceiver
```

#### List owners

```
$ adb shell dpm list-owners
```

#### Unset device owner

```
$ adb shell dpm remove-active-admin test.android.kiosk.debug/test.android.kiosk.MainDeviceAdminReceiver
```

---
