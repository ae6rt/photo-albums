### An AngularJS photo album manager

I use this simple photo manager application to teach myself new
AngularJS techniques.  The app comes with some sample albums.

The backend is Java/JAX-RS hosted within Jetty-based
[FeatherCon](https://github.com/xoom/feathercon).  Open a Terminal,
and start the backend with

    ./gradlew build run [-Daccess.logging]
    ...
    INFO: Root resource classes found:
         class org.petrovic.photos.resources.PhotoResource
         class org.petrovic.photos.resources.StaticResource
         class org.petrovic.photos.resources.AlbumsResource
       Dec 3, 2013 4:19:12 AM com.sun.jersey.api.core.ScanningResourceConfig init
    INFO: No provider classes found.
       Dec 3, 2013 4:19:12 AM com.sun.jersey.server.impl.application.WebApplicationImpl _initiate
    INFO: Initiating Jersey application, version 'Jersey: 1.17 01/17/2013 03:31 PM'
    > Building > :run    [ed:  < the app is now running >]

Then point your browser to http://localhost:8080.

##### Features:

    * Click on a thumbnail for a larger modal view of a photo (Angular UI modals)
    * Album name and photo caption inline editing (Angular directives), with backend persistence
    * An Angular service to update album descriptions
    * Backend thumbnail generation using [ImgScalr](https://github.com/thebuzzmedia/imgscalr)
    * Basic Exif data extraction to acquire date/time photo was taken, along with GPS if it's available.

##### What it still needs:

    * Unit tests using whatever the Angular community uses
    * Add new album
    * Add photo to existing album
    * Keyboard handling for <enter> detection during album title and photo caption editing

Some of the Angular code is clunky and naive. I'm still learning.
