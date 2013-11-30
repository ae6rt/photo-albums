var albumApp = angular.module('albumApp', ['ui.bootstrap']);

/* Define an album metadata update service. */
albumApp.factory('AlbumMetaUpdateService', ['$http', function ($http, album_metadata) {
    return function (album_metadata) {
        console.log("in service with metadata.name, metadata.description: " + album_metadata.name + ", " + album_metadata.description);
        $http.put("albums/" + album_metadata.name, album_metadata)
            .success(function (data, status, headers, config) {
                console.log("put worked");
            })
            .error(function (data, status, headers, config) {
                console.log("put failed");
            });
    }
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
                            caption: "",
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
        $scope.show_edit = false;
        $scope.caption = $scope.image_info.caption;

        $scope.ok = function () {
            $modalInstance.close();
        };

        $scope.edit = function () {
            $scope.show_edit = true;
            console.log("edit clicked: " + $scope.image_info.album + ", " + $scope.image_info.image_name);
        };

        $scope.update_caption = function () {
            $scope.show_edit = false;
            console.log("update caption" + $scope.image_info.caption);
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    };

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

                        /* deprecated:  let the album meta service update albums[] to avoid this callback */
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
            $scope.album_meta.update_callback($scope.album_meta.name, description);
            AlbumMetaUpdateService({name: $scope.album_meta.name, description: description});
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    };
});