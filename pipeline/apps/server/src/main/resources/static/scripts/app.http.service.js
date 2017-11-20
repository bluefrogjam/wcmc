/**
 * Service to perform HTTP requests
 */
angular.module('app')
    .service('HttpService', ['$http', function($http) {

        this.checkFileStatus = function(filename, successCallback, errorCallback) {
            $http({
                method: 'GET',
                url: '/rest/file/exists/'+ filename
            }).then(successCallback, errorCallback);
        };

        this.submitJob = function(task, successCallback, errorCallback) {
            $http({
                method: 'POST',
                url: '/rest/schedule/submit',
                data: task,
                headers: {
                    'Content-Type': 'application/json'
                }
            }).then(successCallback, errorCallback);
        };

        this.submitTarget = function(target, successCallback, errorCallback) {
            $http({
                method: 'POST',
                url: '/rest/library',
                data: target,
                headers: {
                    'Content-Type': 'application/json'
                }
            }).then(successCallback, errorCallback);
        };

        this.getPlatforms = function(successCallback, errorCallback) {
            successCallback(['LC-MS', 'GC-MS']);
        };

        this.getAcquisitionMethods = function(successCallback, errorCallback) {
            $http({
                method: 'GET',
                url: '/rest/library',
                headers: {
                    'Content-Type': 'application/json'
                }
            }).then(function(data) {
                successCallback(data.data.map(function(x) {
                    // Combine name and ion mode for selections
                    x.title = x.chromatographicMethod.name;

                    if (x.chromatographicMethod.ionMode != null) {
                        x.title += ' ('+ x.chromatographicMethod.ionMode.mode +')';
                    }

                    return x;
                }));
            }, errorCallback);
        };

        this.getMinixStudyExport = function(id, successCallback, errorCallback) {
            $http({
                method: 'get',
                url: '/rest/integration/minix/'+ id
            }).then(successCallback, errorCallback);
        };
    }]);