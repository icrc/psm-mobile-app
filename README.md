# Pharmacy Stock Management application

Simple, user-friendly mobile device application that allows any assisted health care facility 
(or medical stock) to record, store and digitally share all essential data needed for stock management 
(stock on hand, expiry dates, monthly demand and Impress levels), calculate and transmit monthly 
orders as well as (optionally) receive information from the ICRC such as delivery schedules, 
order status information and stock information at supplying ICRC medical distribution centers. 

The application is integrated with the ICRC information system through the IRIS platform.


### How to Create an APK

1. Update `app/src/main/res/raw/openid_config` to match the configuration of your desired 
  OAuth2 provider as described [here](https://github.com/dhis2/dhis2-android-capture-app/wiki/Modifying-the-APK#configure-openid--oauth). 
2. Update the `applicationId` and `serverUrl` in `dependencies.gradle` file accordingly. The former
  will be used as the [_application package name_](https://github.com/dhis2/dhis2-android-capture-app/wiki/Modifying-the-APK#change-package-name), 
  and also in the `user-agent` header of all HTTP requests within the app, while the later is the
  desired DHIS2 server the app will be communicating with.
  
  > N.B: The `serverUrl` must be quoted in single and double quotes as you currently have it, to
  avoid breaking the build.
3. Sync the updated config using the _"Sync now"_ button at the top of the editor window
4. Update the data definition for the activity `net.openid.appauth.RedirectUriReceiverActivity` in 
   `AndroidManifest.xml` (debug & release) to match the `redirectUri` OAuth2 provider configuration defined above 
   in **(1)**. 
   The same should be done for the respective intent defined within the `<queries>` block. i.e.
5. Clean and rebuild the project
6. Build your APK (**Build > Build Bundle(s) / APK (s) menu**)
7. [Sign your APK](https://github.com/dhis2/dhis2-android-capture-app/wiki/Modifying-the-APK#how-to-generate-a-keystore-and-sign-the-apk) for distribution
   


### Notes

- **Android Permissions** - Starting in Android 11 (API level 30), if the user taps Deny for a
specific permission more than once during your app's lifetime of installation on a device,
the user doesn't see the system permissions dialog if your app requests that permission again.
The user's action implies "don't ask again." On previous versions, users would see the system
permissions dialog each time your app requested a permission, unless the user had previously
selected a __"don't ask again"__ checkbox or option. 

  In certain situations, the permission might be denied automatically, without the user taking 
  any action. (Similarly, a permission might be granted automatically as well.) It's important to 
  not assume anything about automatic behavior. Each time your app needs to access functionality 
  that requires a permission, you should check that your app is still granted that permission. 
  ([guide](https://developer.android.com/training/permissions/requesting))


### DHIS2 Android SDK feature requests

- Ability to create multiple events at once


### Todo

- Switch to lottie animation for the `SyncActivity` page (_optional_)
- Handle Crash Reporting (_optional_)
- **Automatically activate microphone after a successful scan:** The first iteration of this was 
  achieved by granting focus to the stock item recyclerview after a successful scan, however 
  due to repeated successive focus being granted to the active field by Android OS, it does not 
  exactly produce the expected result, so this feature is currently deactivated.