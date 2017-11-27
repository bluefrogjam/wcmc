'use strict';

angular.module('app', ['ngAnimate', 'ngRoute', 'ui.bootstrap', 'ngHandsontable'])

    .config(function($routeProvider) {
        $routeProvider
            .when('/', {
                templateUrl: 'views/schedule.html',
                controller: 'SchedulerController'
            })
            .when('/addTarget', {
                templateUrl: 'views/addTarget.html',
                controller: 'TargetController'
            })
            .when('/addLibrary', {
                templateUrl: 'views/addLibrary.html',
                controller: 'TargetController'
            })
            .when('/download', {
                templateUrl: 'views/download.html',
                controller: 'DownloadController'
            });
    })

    .controller('NavigationController', ['$scope', function($scope) {
        $scope.navCollapsed = true;
    }])

    /**
     * Angular filter to get type of variable
     */
    .filter('getType', function() {
        return function(obj) {
            return typeof obj;
        };
    });
