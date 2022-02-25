# Pharmacy Stock Management application

Simple, user-friendly mobile device application that allows any assisted health care facility 
(or medical stock) to record, store and digitally share all essential data needed for stock management 
(stock on hand, expiry dates, monthly demand and Impress levels), calculate and transmit monthly 
orders as well as (optionally) receive information from the ICRC such as delivery schedules, 
order status information and stock information at supplying ICRC medical distribution centers. 

The application is integrated with the ICRC information system through the IRIS platform.

### Todo

- Go through all the possible return values from `usermodel().login()` and handle them accordingly. 
  There are cases where `ALREADY_AUTHENTICATED` is returned from the call. SUch return values should be
  handled appropriately
- Switch to lottie animation for the `SyncActivity` page (_optional_)
- Handle session expired scenarios (See _ActivityGlobalAbstract.showSessionExpired_) for example
- Handle Crash Reporting (_optional_)

### DHIS2 Android SDK feature requests

- Ability to create multiple events at once

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