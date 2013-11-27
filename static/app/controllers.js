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
        $log.info("album_number, image_name: " + album_number + ", " + image_name);
        var modalInstance = $modal.open({
            templateUrl: 'myModalContent.html',
            controller: ModalInstanceCtrl,
            resolve: {
                image_info: function () {
                    var r = {album: album_number, image_name: image_name};
                    return r;
                }
            }
        });

        modalInstance.result.then(function () {
            $log.info("modalInstance promise then");
        }, function () {
            $log.info('Modal dismissed at: ' + new Date());
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