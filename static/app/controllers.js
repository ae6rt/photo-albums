//var albumApp = angular.module('albumApp', ['ui.bootstrap']);
var albumApp = angular.module('albumApp', []);

albumApp.controller('AlbumController', function ($scope, $http) {
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

    $scope.image_popup = function (album_number, image_name) {
        console.log("album/image: " + album_number + "/26" + image_name)
    }
});