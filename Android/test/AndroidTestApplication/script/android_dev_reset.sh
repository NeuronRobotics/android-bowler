#!/bin/bash
# android_device_reset script
sudo /home/opt/android-sdk-linux_x86/platform-tools/adb kill-server
sudo service udev stop
sudo /home/opt/android-sdk-linux_x86/platform-tools/adb start-server
sudo /home/opt/android-sdk-linux_x86/platform-tools/adb devices
