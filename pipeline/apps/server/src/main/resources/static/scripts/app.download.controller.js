angular.module('app')
    .controller('DownloadController', ['$scope', 'HttpService', function($scope, HttpService) {

        $scope.DOWNLOAD_URL = HttpService.getFileDownloadPath();
        $scope.task = {};

        $scope.download = function() {
            $scope.success = undefined;
            $scope.error = undefined;

            if (angular.isUndefined($scope.task.filename) || $scope.task.filename == '') {
                $scope.error = 'No filename specified!';
            } else {
                $scope.checkingFile = true;

                HttpService.checkFileStatus(
                    $scope.task.filename,
                    function(data) {
                        $scope.checkingFile = false;
                        $scope.success = true;

                        window.location.href = $scope.DOWNLOAD_URL + $scope.task.filename;
                    },
                    function(data) {
                        $scope.checkingFile = false;
                        $scope.error = 'File could not be found!';
                    }
                )
            }
        };
    }]);