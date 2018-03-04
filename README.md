# MapNavigationDemo
A demonstration of Google Maps and Directions API in Android

# API Usage
## [Google Maps Android API](https://developers.google.com/maps/documentation/android-api/start)
The API is used to display a map in the application. This API is integrated in Google Play Services, so the setup process is quite simple and well-documented. 
## [Google Directions API](https://developers.google.com/maps/documentation/directions/start)
Interestingly, the directions feature is not included in the GMAA but comes as a standalone API (bundled within [Web Services API](https://developers.google.com/maps/web-services/)). The result is obtained through HTTP request and can be in either JSON or XML format.

Keep in mind that both APIs require an API key to work (which can be created in [Google API Console](https://console.developers.google.com/)) and have to be enabled in the same console link. Just follow the official instructions and everything should work as expected.

# Run demo
Follow the documentation's instructions and obtain an API key, then replace your key with the current one in google_maps_api.xml.