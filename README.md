# MapNavigationDemo
A demonstration of Google Maps and Directions API in Android

# API Usage
## [Google Maps Android API](https://developers.google.com/maps/documentation/android-api/start)
The API is used to display a map in the application. This API is integrated in Google Play Services, so the setup process is quite simple and well-documented. 
## [Google Places API for Android](https://developers.google.com/places/android-api/)
This API provide convenient search features (or place picker) to acquire place details, including name, latitude and longitude (LatLng), place ID (unique to each place), even address or phone number (if any).  
Places API is also integrated in Google Play Services suite.
## [Google Directions API](https://developers.google.com/maps/documentation/directions/start)
Interestingly, the directions feature is not included in the GMAA but comes as a standalone API (bundled within [Web Services API](https://developers.google.com/maps/web-services/)). The result is obtained through HTTP request, in either JSON or XML format.  

Keep in mind that all aforementioned APIs require an API key to work (which can be created in [Google API Console](https://console.developers.google.com/)) and have to be enabled in the same console link. Just follow the official instructions and everything should work as expected.

# Run demo
1. Obtain the API key according to the [instruction](https://developers.google.com/maps/documentation/android-api/signup)
2. Enable corresponding APIs in console. For this project, those are Google Maps Android API, Google Maps Directions API
and Google Places API for Android.
3. Open global gradle.properties (in Gradle Scripts), then add this line with your API key obtained in step 1.
```
mapsApiKey="YOUR_API_KEY_HERE"
```
