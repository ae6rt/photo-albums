albumApp.directive('inlineEdit', function () {
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