# GEARUP and its sub-projects
This guide provides details about the project GEARUP and its sub-projects

## Table of Contents

1. [Web Application](#webapplication)
2. [Mobile Application](#mobileapplication)
    - [IOS native application](#ios-native-app)
    - [android application](#androi-application)
3. [API service](#api-service)

---

## 1. Web Application

web application is just a web page experience of how the mobile app works it contains all colleges, careers, list view of NC colleges etc  

front end for [360-app](https://360.gearupapp.org/) can be found in this [repository](https://github.com/uncetlab/GEAR-UP-Web-Code.git)

## 2.  Mobile Application

The GearUp VR mobile applications is available for both Android and IOS, their respective code base is handled separately 


#### *) IOS Native application

The native ios code is developed with SWIFT language which can be found at [repository](https://github.com/uncetlab/GEAR-UP-IOS-Code) for the hosted application [application](https://apps.apple.com/us/app/gear-up-vr/id1390999670)


#### *) Android application

The native android app code is developed with KOTLIN language which can be found at  [repository](https://github.com/uncetlab/GEAR-UP-Android-Code) for [application](https://play.google.com/store/apps/details?id=com.askmedia.gearup)



## 3. API service

API service is the key life of all the mobile/web applications without API the mobile/web application won't move a page. The core is developed with python and Django as a framework and uses django templates for frontend, also the api is developed with DRF (django rest framework) powered by Postgresql as the database. the code base for admin [repository](https://github.com/uncetlab/GEAR-UP-Admin-Code.git) the web application can be found at [web-admin-app](https://www.gearupapp.org/) which is also the home page of gearupapp.org
