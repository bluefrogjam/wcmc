/**
 * Service to parse MiniX XML study information
 */
angular.module('app')
    .service('MinixService', ['$http', 'HttpService', function($http, HttpService) {
        this.getMinixStudy = function(id) {
            HttpService.getMinixStudyExport(
                id,
                function(data) {
                    console.log(data)
                },
                function(data) {
                    console.log(data)
                });
        };
    }]);