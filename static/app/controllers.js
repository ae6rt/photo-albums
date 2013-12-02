/*
 Use Angular Bootstrap for nice widgets: http://angular-ui.github.io/bootstrap/
 */
var albumApp = angular.module('albumApp', ['ui.bootstrap']);

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

albumApp.controller('AlbumController', function ($scope, $http, $modal, $log) {

    $scope.image_names_by_album = [];

    $http.get("albums")
        .success(function (data, status, headers, config) {
            $scope.albums = data;
            for (var i = 0; i < $scope.albums.length; i++) {
                $scope.album_fetcher($scope.albums[i]);
            }
        })
        .error(function (data, status, headers, config) {
        });

    $scope.album_fetcher = function (album_metadata) {
        $http.get("albums/" + album_metadata.name + "/photos")
            .success(function (data, status, headers, config) {
                $scope.image_names_by_album[album_metadata.name] = data;
            })
            .error(function (data, status, headers, config) {
            });
    };

    $scope.open = function (album_number, image_name) {

        $http.get("photo/" + album_number + "/" + image_name + "/metadata")
            .success(function (data, status, headers, config) {
                $scope.openPhotoDetail(data);
            })
            .error(function (data, status, headers, config) {
                $log.info("Error getting metadata for " + image_name);
            });

        $scope.openPhotoDetail = function (photo_metadata) {
            var photoDetailModal = $modal.open({
                templateUrl: 'partials/photodetail.html',
                controller: PhotoDetailModalController,
                resolve: {
                    image_info: function () {
                        return {
                            album: album_number,
                            image_name: image_name,
                            caption: photo_metadata.caption,
                            exif: {
                                lat: photo_metadata.lat,
                                long: photo_metadata.lng,
                                original_date: photo_metadata.originalTime
                            }
                        };
                    }
                }
            });

            photoDetailModal.result.then(function () {
            }, function () {
            });
        };
    };

    var PhotoDetailModalController = function ($scope, $modalInstance, $http, image_info) {
        $scope.image_info = image_info;
        $scope.caption = $scope.image_info.caption;

        $scope.ok = function () {
            $modalInstance.close();
        };

        $scope.update_caption = function (new_caption, album_number, image_name) {
            $http.put("photo/" + album_number + "/" + image_name + "/metadata", {caption: new_caption})
                .success(function (data, status, headers, config) {
                })
                .error(function (data, status, headers, config) {
                    $log.info("Error putting metadata for " + image_name);
                });
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    };

    $scope.new_album = function () {
        console.log("new album clicked");
    }

});