# Pharmacy Stock Management application

### Color Theme

- [Material.io Theme](https://material.io/resources/color/#!/?view.left=0&view.right=0&primary.color=212121&secondary.color=62A3BB)


### Issues

- Toolbar shows Activity name as title, even when the toolbar title is left empty or set to empty


### Todo

- Switch out LinearLayout in Home Activity for Group constraints
- Store the lastSync date in Shared Preferences to facilitate reuse across all activities
- See if [Bufferapp](https://github.com/bufferapp/android-components/blob/main/app/src/main/java/org/buffer/android/components/RoundedButton.kt)'s 
  RoundedButton concept will work for the transaction buttons styling
- Add beautiful looking view when recent activity list is empty
- Figure out to to export the `directional_arrow` in _Recent Activity_ list with the 
  proper dimensions (**24x24**)
- Apply the appropriate theme to the footer text (might use attributes) and recent activity items
- Use custom dividers and spacers for the recyclerview items 
  (https://stackoverflow.com/questions/24618829/how-to-add-dividers-and-spaces-between-items-in-recyclerview)
- See if you can use ViewModel's SavedState to save state across process death or share data between
  different activities
- Handle cases where a valid server URL is not specified and non-XML or JSON responses are obtained
while communicating with the invalid server. This currently isn't handled appropriately
- Clear all the user data on logout: metadata, Preferences, recent activity etc
- SharedPreferences (both implementations are not writing to disk). Fix them
- Go through all the possible return values from `usermodel().login()` and handle them accordingly. 
  There are cases where `ALREADY_AUTHENTICATED` is returned from the call. SUch return values should be
  handled appropriately
- Switch to lottie animation for the `SyncActivity` page
- Handle session expired scenarios (See _ActivityGlobalAbstract.showSessionExpired_) for example
- On logout, delete preferences relating to metadata sync so that a new sync can happen
  when next the user logs in
- Add localization
- Handle Crash Reporting