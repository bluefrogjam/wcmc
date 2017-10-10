'use strict';

angular.module('app', ['ngAnimate', 'ui.bootstrap', 'ngHandsontable'])

    .controller('NavigationController', ['$scope', function($scope) {
        $scope.navCollapsed = true;
    }])

    .controller('MainController', ['$scope', '$window','$timeout', '$filter', 'HttpService', 'hotRegisterer', function($scope, $window, $timeout, $filter, HttpService, hotRegisterer) {

        /**
         * Syncs the select fields with the HandsOnTable column headers
         */
        var updateColumnSelectors = function() {
            var instance = hotRegisterer.getInstance('scheduler');

            if (angular.isDefined(instance)) {
                $scope.columnSelectors = instance.getColHeader();
                $scope.selectedColumn = instance.getColHeader();

                $scope.columnOptions = ['Sample File Name', 'Class', 'Organ', 'Species', 'Comment', 'Label']

                $scope.selectedColumn.forEach(function(col) {
                    for (var i in $scope.columnOptions) {
                        if ($scope.columnOptions[i] == col) {
                            $scope.columnOptions.splice(i, 1);
                        }
                    }
                });
            }
        };

        /**
         * HandsOnTable settings
         */
        $scope.settings = {
            contextMenu: true,
            afterRender: updateColumnSelectors,
            colHeaders: ['Sample File Name', 'Class', 'Organ', 'Species', 'Comment', 'Label']
        };

        /**
         * Table data
         */
        $scope.data = [[]];

        /**
         * Column values and selectors
         */
        $scope.columnSelectors = [];
        $scope.selectedColumn = [];
        $scope.columnOptions = ['Sample File Name', 'Class', 'Organ', 'Species', 'Comment', 'Label']

        /**
         * Acquisition method options
         */
        $scope.acquisitionMethodOptions = [
            'Lipidomics', 'HILIC'
        ];
        $scope.platformOptions = [
            'LC-MS', 'QC-MS'
        ];

        /**
         * Task object
         */
        $scope.task = {};

        $scope.reset = function() {
          $window.location.reload();
        }


        /**
         * Update HandsOnTable with selected column name
         */
        $scope.updateColumnName = function(idx) {
            var instance = hotRegisterer.getInstance('scheduler');
            var headers = instance.getColHeader();

            headers[idx] = $scope.selectedColumn[idx];

            instance.updateSettings({colHeaders: headers});
        };


        $scope.submit = function() {
            // Reset error
            $scope.error = undefined;

            // Check that filename column is selected
            var instance = hotRegisterer.getInstance('scheduler');
            var headers = instance.getColHeader();

            var fileNameCol = headers.indexOf('Sample File Name');
            var classCol = headers.indexOf('Class');
            var speciesCol = headers.indexOf('Species');
            var organCol = headers.indexOf('Organ');
            var commentCol = headers.indexOf('Comment');
            var labelCol = headers.indexOf('Label');

            // Validate form
            if (fileNameCol == -1) {
                $scope.error = 'No sample file name column selected!';
                return;
            }

            if (angular.isUndefined($scope.task.email)) {
                $scope.error = 'No email address provided!';
                return;
            }
            if (angular.isUndefined($scope.task.platform)) {
                $scope.error = 'No platform selected!';
                return;
            }
            if (angular.isUndefined($scope.task.acquisitionMethod)) {
                $scope.error = 'No acquisition method selected!';
                return;
            }

            $scope.running = true;

            // Task object to submit
            var task = {
                samples: [],
                name: $scope.task.email,
                email: $scope.task.email,
                acquisitionMethod: {chromatographicMethod: {name: $scope.task.acquisitionMethod}},
                platform: {platform: {name: $scope.task.platform}}
            };

            // Add task name
            if (angular.isDefined($scope.task.name) && $scope.task.name != '') {
                task.name += '_'+ $scope.task.name;
            } else {
                task.name += '_'+ $filter('date')(new Date(), 'yyyyMMddHHmmss');
            }

            // Check file existence for each row and update the row header with the result
            var rowLabels = instance.getRowHeader();
            var checkCount = 0;

            $scope.data.forEach(function(x, i) {
                if (x[fileNameCol] !== null) {
                    HttpService.checkFileStatus(
                        x[fileNameCol],
                        function(data) {
                            // Add a valid sample to the task
                            var sample = {fileName: x[fileNameCol]};
                            var matrix = {};

                            if (classCol > -1)
                                matrix.identifier = x[classCol]
                            if (speciesCol > -1)
                                matrix.species = x[speciesCol]
                            if (organCol > -1)
                                matrix.organ = x[organCol]
                            if (commentCol > -1)
                                matrix.comment = x[commentCol]
                            if (labelCol > -1)
                                matrix.label = x[labelCol]
                            if (!angular.equals(matrix, {}))
                                sample.matrix = matrix;

                            task.samples.push(sample);

                            // Update row header
                            rowLabels[i] = '<i class="fa fa-check text-success" aria-hidden="true"></i>';
                            instance.updateSettings({rowHeaders: rowLabels});

                            checkCount++;
                        },
                        function(data) {
                            rowLabels[i] = '<i class="fa fa-times text-danger" aria-hidden="true"></i>';
                            instance.updateSettings({rowHeaders: rowLabels});

                            checkCount++;
                        }
                    );
                } else {
                    rowLabels[i] = '<i class="fa fa-times text-danger" aria-hidden="true"></i>';
                    instance.updateSettings({rowHeaders: rowLabels});

                    checkCount++;
                }
            });

            // Submit the task
            var submitTask = function() {
                if (checkCount < $scope.data.length) {
                    $timeout(submitTask, 1000);
                } else {
                    HttpService.submitTask(
                        task,
                        function(data) {
                            $scope.success = data.data;
                            $scope.running = false;
                        },
                        function(data) {
                            $scope.error = data.data;
                            $scope.running = false;
                        }
                    );
                }
            };

            submitTask();
        };
    }])

    /**
     * Service to perform HTTP requests
     */
    .service('HttpService', ['$http', function($http) {

        this.checkFileStatus = function(filename, successCallback, errorCallback) {
            $http({
                method : 'GET',
                url : 'http://trashcan.fiehnlab.ucdavis.edu:8080/rest/file/exists/'+ filename
            }).then(successCallback, errorCallback);
        };

        this.submitTask = function(task, successCallback, errorCallback) {
            $http({
                method : 'POST',
                url : 'http://trashcan.fiehnlab.ucdavis.edu:50000/rest/schedule/submit',
                data: task,
                headers: {
                    'Content-Type': 'application/json'
                }
            }).then(successCallback, errorCallback);
        };
    }]);
