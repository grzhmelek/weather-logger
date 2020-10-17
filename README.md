# weather-logger

Application was developed with Kotlin, using Android Jetpack with some components:
- Android KTX for Kotlin usage itself,
- AppCompat to preserve key app functionality,
- Navigation for navigation between fragments,
- ConstraintLayout and RecyclerView for layout creating and displaying,
- DataBinding, LiveData, ViewModel for data surfaced to the UI,
- Room for on-device data storage of weather history in database.
Also there were used such libraries as:
- Kotlin Coroutines for asynchronous task,
- Material for Material Design concept implementation,
- Retrofit2 for retrieving data from API,
- Okhttp3 for logging request/response from API,
- Moshi for Json response body parsing.

OPTIONAL TASKS

● Implement ‘More details’ screen (with ability to view more detailed information
about weather data returned from API)
More details screen is implemented. User can get more weather details on weather item clicked.

● Fetching and processing weather data for more locations
On ADD floating button clicked location permissions are checked. If user allow to use geolocation, appears alert dialog about GSP location with option go to the phone settings and turn on location. When location information is available user can retrieve current weather data using CurrentWeatherData API by current latitude and longitude. If location permission is denied user get weather data location from API statically by Riga city code.

● Refresh the weather data periodically
Data refreshing using WorkManager every 2 hours.

● Ability to access application weather data from 3rd party apps via shared content
provider or other solutions
User can share weather log data using the Android intent resolver, converting layout to the bitmap, saving it as image in external storage public directory and using FileProvider. On home screen (weather log) there is share button in action bar for that purpose.

● Custom animations, transitions between screens
I made some transitions between screens but without custom animations.

● Create Widget for Home Screen
Simple Widget for home screen created.

● All CRUD operations
All CRUD operations are used in the app -
- Create. Application inserts in database table every weather data item retrieved from API.
- Read.	Application selects all data for showing weather log on screen as weather data list. Also SELECT by primary key query is made for testing.
- Delete. User can clear whole weather log using option menu item. Also DELETE one item ability is made for testing.
- Update query is made for testing only.

● Use Kotlin instead of Java for MainActivity
Application is developed with Kotlin, so all custom classes are written using Kotlin.
