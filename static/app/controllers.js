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

    $scope.album_fetcher = function (album_number) {
        $http.get("albums/" + album_number)
            .success(function (data, status, headers, config) {
                $scope.image_names_by_album[album_number] = data;
            })
            .error(function (data, status, headers, config) {
            });
    };

    $scope.open = function (album_number, image_name) {

        $http.get("metadata/" + album_number + "/" + image_name)
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

        $scope.ok = function () {
            $modalInstance.close();
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    };
});