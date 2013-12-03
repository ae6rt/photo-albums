An AngularJS photo album manager.  I use this application to teach
myself new AngularJS techniques.

Sample photos are here:  https://github.com/ae6rt/sample-photos.
Clone it and copy albums/ into this project's root directory so that you
get something that looks like this

    sample-photos> find albums
    albums
    albums/1
    albums/1/6D282BA9-A64C-4B02-9476-EAA2E1B0AC72.JPG
    albums/1/8039F075-F7AE-4ABC-A63E-B52E5B9A8D89.JPG
    albums/1/D4CB76F7-8ED8-4EE1-8679-4490242CB5BB.JPG
    albums/2
    albums/2/041F6F45-8734-47B6-BE4A-A25A30EE1423.JPG
    albums/3
    albums/3/82319BF6-0287-4929-9616-926665032CFD.JPG
    albums/3/83600A6E-07A0-4058-ADA2-03694063D16C.JPG
    albums/3/847D32FF-6D44-4781-AC58-1B4985DE0338.JPG
    albums/3/8E988C87-B118-4E6B-854D-953EC8CF5C84.JPG

The backend is Java/JAX-RS.  Open a Terminal, and start it using

   ./gradlew build run [-Daccess.logging]

Then point your browser to http://localhost:8080.

Features:

    * Click on a thumbnail for a larger modal view of a photo (Angular UI modals)
    * Album name and photo caption inline editing (Angular directives)
    * An Angular service to update album descriptions
