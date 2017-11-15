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
            });
    })

    .controller('NavigationController', ['$scope', function($scope) {
        $scope.navCollapsed = true;
    }])

    .controller('SchedulerController', ['$scope', '$window','$timeout', '$filter', 'HttpService', 'hotRegisterer', function($scope, $window, $timeout, $filter, HttpService, hotRegisterer) {

        /**
         * Syncs the select fields with the HandsOnTable column headers
         */
        var updateColumnSelectors = function() {
            var instance = hotRegisterer.getInstance('scheduler');

            if (angular.isDefined(instance)) {
                $scope.columnSelectors = instance.getColHeader();
                $scope.selectedColumn = instance.getColHeader();

                $scope.columnOptions = ['Sample File Name', 'Class', 'Organ', 'Species', 'Comment', 'Label'];

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
        $scope.columnOptions = ['Sample File Name', 'Class', 'Organ', 'Species', 'Comment', 'Label'];

        /**
         * Acquisition method options
         */
        $scope.pullAcquisitionMethodsAndPlatforms = function() {
            HttpService.getAcquisitionMethods(function(data) {
                $scope.acquisitionMethodOptions = data;
            });

            HttpService.getPlatforms(function(data) {
                $scope.platformOptions = data;
            });
        };
        $scope.pullAcquisitionMethodsAndPlatforms();

        /**
         * Task object
         */
        $scope.task = {};

        $scope.reset = function() {
          $window.location.reload();
        };


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
            $scope.success = false;
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
                acquisitionMethod: $scope.task.acquisitionMethod,
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
            var validCount = 0;

            $scope.data.forEach(function(x, i) {

                if (x[fileNameCol] !== null) {
                    // Replace extension if desired
                    if (angular.isDefined($scope.task.extension) && $scope.task.extension != "") {
                        if (x[fileNameCol].indexOf('.') > -1) {
                            x[fileNameCol] = x[fileNameCol].substr(0, x[fileNameCol].lastIndexOf('.'));
                        } else {
                            x[fileNameCol] += '.'+ $scope.task.extension;
                        }
                    }

                    HttpService.checkFileStatus(
                        x[fileNameCol],
                        function(data) {
                            // Add a valid sample to the task
                            var sample = {fileName: x[fileNameCol]};
                            var matrix = {};

                            if (classCol > -1)
                                matrix.identifier = x[classCol];
                            if (speciesCol > -1)
                                matrix.species = x[speciesCol];
                            if (organCol > -1)
                                matrix.organ = x[organCol];
                            if (commentCol > -1)
                                matrix.comment = x[commentCol];
                            if (labelCol > -1)
                                matrix.label = x[labelCol];
                            if (!angular.equals(matrix, {}))
                                sample.matrix = matrix;

                            task.samples.push(sample);

                            // Update row header
                            rowLabels[i] = '<i class="fa fa-check text-success" aria-hidden="true"></i>';
                            instance.updateSettings({rowHeaders: rowLabels});

                            checkCount++;
                            validCount++;
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

            // Submit the task, waiting until all checks are complete
            var submitTask = function() {
                if (checkCount < $scope.data.length) {
                    $timeout(submitTask, 1000);
                } else if (validCount > 0) {
                    HttpService.submitJob(
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
                } else {
                    $scope.error = 'No valid sample files were provided!';
                    $scope.running = false;
                }
            };

            submitTask();
        };
    }])


    .controller('TargetController', ['$scope', '$window','$timeout', '$filter', 'HttpService', 'hotRegisterer', function($scope, $window, $timeout, $filter, HttpService, hotRegisterer) {

        /**
         * Acquisition method options
         */
        $scope.pullAcquisitionMethodsAndPlatforms = function() {
            HttpService.getAcquisitionMethods(function(data) {
                $scope.acquisitionMethodOptions = data;
            });

            HttpService.getPlatforms(function(data) {
                $scope.platformOptions = data;
            });
        };
        $scope.pullAcquisitionMethodsAndPlatforms();

        /**
         * HandsOnTable settings
         */
        $scope.settings = {
            contextMenu: true
            // colHeaders: ['Target Name', 'Precursor m/z', 'Retention Index', 'RI Standard']
        };

        /**
         * Table data for library mode
         */
        $scope.data = [];

        /**
         * Target object for single target mode
         */
        $scope.target = {ri_unit: 'minutes'};

        $scope.reset = function() {
            $window.location.reload();
            $scope.submitting = $scope.success = $scope.error = false;
        };


        var validateAcquisitionMode = function() {
            if (angular.isUndefined($scope.target.selectedMethod)) {
                $scope.error = 'No acquisition method selected!';
                return false;
            }

            // Handle a custom library name
            if (typeof $scope.target.selectedMethod == 'string') {
                var titles = $scope.acquisitionMethodOptions.map(function(x) { return x.title; });

                // Check whether user actually selected an existing method that didn't get selected properly
                if (titles.indexOf($scope.target.selectedMethod) > -1) {
                    var method = $scope.acquisitionMethodOptions[titles.indexOf($scope.target.library)];
                    $scope.target.library = method.chromatographicMethod.name;
                    $scope.target.mode = method.chromatographicMethod.ionMode.mode;
                    return true;
                }

                // Check that a user selected an ion mode
                else if (angular.isDefined($scope.target.mode)) {
                    $scope.target.library = $scope.target.selectedMethod;
                    return true;
                }

                // If not, check that (positive) or (negative) is found in the library name
                else if ($scope.target.selectedMethod.toLowerCase().indexOf('(positive)') > 0 ||
                         $scope.target.selectedMethod.toLowerCase().indexOf('(negative)') > 0) {

                    $scope.target.library = $scope.target.selectedMethod.replace(/\(positive\)/ig, '').replace(/\(negative\)/ig, '').trim();
                    $scope.target.mode = ($scope.target.selectedMethod.toLowerCase().indexOf('(positive)') > 0) ? 'positive' : 'negative';
                }

                // Otherwise, error
                else {
                    $scope.error = 'No ionization mode selected!';
                    return false;
                }
            }

            // Handle an acquisition method object
            else {
                $scope.target.library = $scope.target.selectedMethod.chromatographicMethod.name;
                $scope.target.mode = $scope.target.selectedMethod.chromatographicMethod.ionMode.mode;
                return true;
            }

            return true;
        }


        $scope.submitSingleTarget = function() {
            $scope.success = false;
            $scope.error = undefined;

            if (angular.isUndefined($scope.target.targetName)) {
                $scope.error = 'No target name provided!';
                return;
            }
            if (angular.isUndefined($scope.target.precursor)) {
                $scope.error = 'No precursor m/z provided!';
                return;
            }
            if (angular.isUndefined($scope.target.retentionTime)) {
                $scope.error = 'No retention time provided!';
                return;
            }
            if (!validateAcquisitionMode()) {
                return;
            }

            var target = angular.copy($scope.target)

            if (target.ri_unit == 'minutes') {
                target.retentionTime *= 60;
            }

            $scope.submitting = true;

            HttpService.submitTarget(
                target,
                function (data) {
                    $scope.submitting = false;
                    $scope.success = true;
                },
                function (data) {
                    $scope.submitting = false;
                    $scope.error = data;
                }
            );
        };


        $scope.submitLibrary = function() {
            $scope.success = false;
            $scope.error = undefined;
            $scope.totalCount = 0;

            if (!validateAcquisitionMode()) {
                return;
            }

            function isRowEmpty(row) {
                var values = Object.values(row).filter(function(x) { return x != null && x != ""; });
                return angular.equals($scope.data[i], {}) || values.length == 0;
            }

            // Validate data table
            var instance = hotRegisterer.getInstance('library');
            var rowLabels = instance.getRowHeader();

            for (var i = 0; i < $scope.data.length; i++) {
                // Ignore empty rows
                if (!isRowEmpty($scope.data[i])) {
                    // Error if a required field is missing
                    if (angular.isUndefined($scope.data[i].targetName) ||
                        angular.isUndefined($scope.data[i].precursor) ||
                        angular.isUndefined($scope.data[i].retentionTime)) {

                        rowLabels[i] = '<i class="fa fa-times text-danger" aria-hidden="true"></i>';
                        instance.updateSettings({rowHeaders: rowLabels});

                        $scope.error = 'Please ensure that all targets are completed!'
                    } else {
                        $scope.totalCount++;
                        rowLabels[i] = i + 1;
                        instance.updateSettings({rowHeaders: rowLabels});
                    }
                }
            }

            if (angular.isDefined($scope.error)) {
                return;
            }

            // Submit
            $scope.successCount = 0;
            $scope.errorCount = 0;
            $scope.submitting = true;

            for (var i = 0; i < $scope.data.length; i++) {
                // Ignore empty rows
                if (!isRowEmpty($scope.data[i])) {
                    $scope.target.targetName = $scope.data[i].targetName;
                    $scope.target.precursor = $scope.data[i].precursor;
                    $scope.target.retentionTime = $scope.data[i].retentionTime;
                    $scope.target.riMarker = $scope.data[i].riMarker;

                    if ($scope.target.ri_unit == 'minutes') {
                        $scope.target.retentionTime *= 60;
                    }

                    HttpService.submitTarget(
                        $scope.target,
                        function(data) {
                            rowLabels[i] = '<i class="fa fa-check text-success" aria-hidden="true"></i>';
                            instance.updateSettings({rowHeaders: rowLabels});

                            $scope.successCount++;
                        },
                        function(data) {
                            rowLabels[i] = '<i class="fa fa-times text-danger" aria-hidden="true"></i>';
                            instance.updateSettings({rowHeaders: rowLabels});

                            $scope.errorCount++;
                        }
                    );
                }
            }

            var submitLibrary = function() {
                if ($scope.successCount + $scope.errorCount < $scope.totalCount) {
                    $timeout(submitLibrary, 1000);
                } else {
                    $scope.submitting = false;

                    if ($scope.totalCount == $scope.successCount) {
                        $scope.success = true;
                    } else {
                        $scope.error = 'Only '+ $scope.successCount +' / '+ $scope.totalCount +' targets were successfully submitted.'
                    }
                }
            };

            submitLibrary();
        };
    }])

    /**
     * Service to perform HTTP requests
     */
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
    }])

    /**
     * Angular filter to get type of variable
     */
    .filter('getType', function() {
        return function(obj) {
            return typeof obj;
        };
    });
