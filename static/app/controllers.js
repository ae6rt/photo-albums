var albumApp = angular.module('albumApp', ['ui.bootstrap']);

albumApp.controller('AlbumController', function ($scope, $http, $modal, $log) {
    $scope.album_selector = 1;
    $scope.image_names_by_album = new Array();

    $scope.album_changed = function (album_number) {
        $http.get("albums/" + album_number)
            .success(function (data, status, headers, config) {
                $scope.image_names_by_album[album_number] = data;
            })
            .error(function (data, status, headers, config) {
            });
    };

    $http.get("albums")
        .success(function (data, status, headers, config) {
            $scope.albums = data;
        })
        .error(function (data, status, headers, config) {
        });

    $scope.album_changed($scope.album_selector);

    $scope.open = function (album_number, image_name) {

        $http.get("metadata/" + album_number + "/" + image_name)
            .success(function (data, status, headers, config) {
                $scope.photo_metadata = data;
                $log.info(JSON.stringify($scope.photo_metadata));
                $log.info(JSON.stringify($scope.photo_metadata.lat));
                $log.info(JSON.stringify($scope.photo_metadata.lng));
                $log.info(JSON.stringify($scope.photo_metadata.originalTime));
            })
            .error(function (data, status, headers, config) {
                $log.info("Error getting metadata for " + image_name);
            });

        var modalInstance = $modal.open({
            templateUrl: 'partials/photodetail.html',
            controller: ModalInstanceCtrl,
            resolve: {
                image_info: function () {
                    var r = {
                        album: album_number,
                        image_name: image_name,
                        caption: "",
                        exif: {
                            lat: $scope.photo_metadata.lat,
                            long: $scope.photo_metadata.lng,
                            original_date: $scope.photo_metadata.originalTime
                        }
                    };
                    return r;
                }
            }
        });

        modalInstance.result.then(function () {
        }, function () {
        });
    };

    var ModalInstanceCtrl = function ($scope, $modalInstance, image_info) {
        $scope.image_info = image_info;

        $scope.ok = function () {
            $modalInstance.close();
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    };
});