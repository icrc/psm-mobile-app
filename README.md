# Pharmacy Stock Management application

### Color Theme

- [Material.io Theme](https://material.io/resources/color/#!/?view.left=0&view.right=0&primary.color=212121&secondary.color=62A3BB)

### Todo

- Voice input
- Use custom dividers and spacers for the recyclerview items
- Use ViewModel's SavedState to save state across process death or share data between
  different activities
- Handle cases where a valid server URL is not specified and non-XML or JSON responses are obtained
while communicating with the invalid server. This currently isn't handled appropriately
- Go through all the possible return values from `usermodel().login()` and handle them accordingly. 
  There are cases where `ALREADY_AUTHENTICATED` is returned from the call. SUch return values should be
  handled appropriately
- Switch to lottie animation for the `SyncActivity` page (_optional_)
- Handle session expired scenarios (See _ActivityGlobalAbstract.showSessionExpired_) for example
- On logout, delete preferences relating to metadata sync so that a new sync can happen
  when next the user logs in
- Handle Crash Reporting (_optional_)
- Use fragments and adaptable UI where possible
- Use `CoordinatorLayout` to replace `ConstraintLayout` where necessary

### DHIS2 Android SDK feature requests

- Ability to create multiple events at once