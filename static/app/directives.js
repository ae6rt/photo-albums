albumApp.directive('inlineCaptionEdit', function () {
    return {
        restrict: 'E',
        templateUrl: 'partials/componentTpl.html',
        scope: {
            model: '=',
            album: "=",
            imageName: "="
        },
        controller: ['$scope', '$http', function ($scope, $http) {
            $scope.update_caption = function () {
                $http.put("photo/" + $scope.album + "/" + $scope.imageName + "/metadata", {caption: $scope.model})
                    .success(function (data, status, headers, config) {
                    })
                    .error(function (data, status, headers, config) {
                        console.log("photo caption update failed with status: " + status);
                    });
            }
        }]
    };
});

albumApp.directive('inlineAlbumTitleEdit', function () {
    return {
        restrict: 'E',
        templateUrl: 'partials/albumTitleEdit.html',
        scope: {
            model: '=',
            album: "="
        },
        controller: ['$scope', 'AlbumMetaUpdateService', function ($scope, AlbumMetaUpdateService) {
            $scope.update_album_title = function () {
                AlbumMetaUpdateService.update({name: $scope.album, description: $scope.model});
            }
        }]
    };
});
