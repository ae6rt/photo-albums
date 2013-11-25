var albumApp = angular.module('albumApp', []);

albumApp.controller('AlbumController', function ($scope, $http) {
    $scope.message = "hello";
    $scope.album_selector = 1;

    $http.get("albums")
        .success(function (data, status, headers, config) {
            $scope.albums = data;
        })
        .error(function (data, status, headers, config) {
            console.log("initial GET error");
            console.log(data);
            console.log(headers);
            console.log(status);
            console.log(config);
        });

    $http.get("albums/" + $scope.album_selector)
        .success(function (data, status, headers, config) {
            $scope.album_list = data;
        })
        .error(function (data, status, headers, config) {
            console.log("initial GET error");
            console.log(data);
            console.log(headers);
            console.log(status);
            console.log(config);
        });


});