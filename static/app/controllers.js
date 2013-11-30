var albumApp = angular.module('albumApp', ['ui.bootstrap']);

/* Define an album metadata update service.  It's a bit of overkill for what it does,
 * but I wanted to learn how to do this.  */
albumApp.factory('AlbumMetaUpdateService', ['$http', function ($http) {
    return {
        update: function (album_metadata) {
            console.log("in service with metadata.name, metadata.description: " + album_metadata.name + ", " + album_metadata.description);
            $http.put("albums/" + album_metadata.name, album_metadata)
                .success(function (data, status, headers, config) {
                    console.log("put worked: status=" + status);
                })
                .error(function (data, status, headers, config) {
                    console.log("put failed: status=" + status);
                });

        },
        some_other_function: function () {
            console.log("some other function");
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

        $http.get("photo/metadata/" + album_number + "/" + image_name)
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

    var PhotoDetailModalController = function ($scope, $modalInstance, image_info) {
        $scope.image_info = image_info;
        $scope.caption = $scope.image_info.caption;

        $scope.ok = function () {
            $modalInstance.close();
        };

        $scope.edit = function () {
            $scope.show_edit = true;
            console.log("edit clicked: " + $scope.image_info.album + ", " + $scope.image_info.image_name);
        };

        $scope.update_caption = function (new_caption) {
            console.log("updated caption: " + new_caption);
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    };

    /* Glue callback for use by album metadata update modal.  Seems like there should be a better way to
     * get this done.  But no matter how or where this update gets done, the whole thumbnail gallery for this
     * album is updated when the description is updated - which is inefficient.  Can we decouple updating
     * the thumbnails from the description? */
    $scope.change_album_description = function (name, description) {
        for (i = 0; i < $scope.albums.length; ++i) {
            if ($scope.albums[i].name == name) {
                $scope.albums[i] = {name: name, description: description};
            }
        }
    };

    $scope.album_metadata_edit = function (album_metadata) {
        var albumDescriptionModal = $modal.open({
            templateUrl: 'partials/albumdetail.html',
            controller: AlbumDetailModalController,
            resolve: {
                album_meta: function () {
                    return {
                        name: album_metadata.name,
                        description: album_metadata.description,
                        update_callback: $scope.change_album_description
                    };
                }
            }
        });

        albumDescriptionModal.result.then(function () {
        }, function () {
        });
    };

    var AlbumDetailModalController = function ($scope, $modalInstance, AlbumMetaUpdateService, album_meta) {
        $scope.album_meta = album_meta;
        $scope.description = album_meta.description;

        $scope.ok = function (description) {
            $modalInstance.close();
            /* this callback is clunky.  can we not update albums here? */
            AlbumMetaUpdateService.update({name: $scope.album_meta.name, description: description});
            $scope.album_meta.update_callback($scope.album_meta.name, description);
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    };
});