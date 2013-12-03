/* Define an album metadata update service.  It's a bit of overkill for what it does,
 * but I wanted to learn how to do this.  */
albumApp.factory('AlbumMetaUpdateService', ['$http', function ($http) {
    return {
        update: function (album_metadata) {
            $http.put("albums/" + album_metadata.name, album_metadata)
                .success(function (data, status, headers, config) {
                })
                .error(function (data, status, headers, config) {
                    console.log("put failed: status=" + status);
                });

        },
        some_other_function: function () {
        }}
}]);