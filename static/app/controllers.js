var albumApp = angular.module('albumApp', ['ui.bootstrap']);

albumApp.controller('AlbumController', function ($scope, $http, $modal, $log) {

    $scope.image_names_by_album = new Array();

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

    $scope.foo = function (name, description) {
        console.log("new meta.name: " + name);
        console.log("new meta.description: " + description);
        for (i = 0; i < $scope.albums.length; ++i) {
            console.log("old meta: " + $scope.albums[i].description);
            if ($scope.albums[i].name == name) {
                console.log("   found it: " + $scope.albums[i].description);
                $scope.albums[i] = album_metadata;
            }
        }
    };

    $scope.album_metadata_edit = function (album_metadata) {
        console.log("editing meta.name=" + album_metadata.name);
        console.log("editing meta.description=" + album_metadata.description);
        var albumDescriptionModal = $modal.open({
            templateUrl: 'partials/albumdetail.html',
            controller: AlbumDetailModalController,
            resolve: {
                album_meta: function () {
                    return {
                        name: album_metadata.name,
                        f: $scope.foo
                    };
                }
            }
        });

        albumDescriptionModal.result.then(function () {
        }, function () {
        });
    };

    var AlbumDetailModalController = function ($scope, $modalInstance, album_meta) {
        $scope.album_meta = album_meta;

        $scope.ok = function () {
            $modalInstance.close();
            console.log("modal closed.  meta.name=" + $scope.album_meta.name);
            console.log("modal closed.  meta.description=" + $scope.description);
            $scope.album_meta.f({name: $scope.album_meta.name, description: $scope.description});
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    };


});