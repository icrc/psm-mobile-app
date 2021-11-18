# Pharmacy Stock Management application

### Color Theme

- [Material.io Theme](https://material.io/resources/color/#!/?view.left=0&view.right=0&primary.color=212121&secondary.color=62A3BB)


### Issues

- Toolbar shows Activity name as title, even when the toolbar title is left empty or set to empty


### Todo

- Add beautiful looking view when recent activity list is empty
- Use custom dividers and spacers for the recyclerview items
- See if you can use ViewModel's SavedState to save state across process death or share data between
  different activities
- Handle cases where a valid server URL is not specified and non-XML or JSON responses are obtained
while communicating with the invalid server. This currently isn't handled appropriately
- Clear all the user data on logout: metadata, preferences, recent activity etc
- Go through all the possible return values from `usermodel().login()` and handle them accordingly. 
  There are cases where `ALREADY_AUTHENTICATED` is returned from the call. SUch return values should be
  handled appropriately
- Switch to lottie animation for the `SyncActivity` page
- Handle session expired scenarios (See _ActivityGlobalAbstract.showSessionExpired_) for example
- On logout, delete preferences relating to metadata sync so that a new sync can happen
  when next the user logs in
- Add localization
- Handle Crash Reporting