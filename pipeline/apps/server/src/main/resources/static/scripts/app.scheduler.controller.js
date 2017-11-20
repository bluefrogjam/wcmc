angular.module('app')
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
        $scope.task = {platform: "LC-MS"};

        $scope.reset = function() {
          $window.location.reload();
        };


        /**
         * MiniX Integration
         */
        $scope.pullMiniXStudy = function() {
            $scope.miniXError = undefined;

            if (angular.isUndefined($scope.task.minix) || $scope.task.minix == '') {
                $scope.miniXError = 'No MiniX study ID provided!';
            } else {
                $scope.miniXLoading = true;

                var instance = hotRegisterer.getInstance('scheduler');
                var headers = instance.getColHeader();

                HttpService.getMinixStudyExport(
                    $scope.task.minix,
                    function(data) {
                        $scope.miniXLoading = false;

                        var fileNameCol = headers.indexOf('Sample File Name');
                        var classCol = headers.indexOf('Class');
                        var speciesCol = headers.indexOf('Species');
                        var organCol = headers.indexOf('Organ');
                        var commentCol = headers.indexOf('Comment');
                        var labelCol = headers.indexOf('Label');


                        data.data.forEach(function(x, i) {
                            var values = Array($scope.columnOptions.length).fill('');

                            if (fileNameCol > -1)
                                values[fileNameCol] = x.sample;
                            if (classCol > -1)
                                values[classCol] = x.className;
                            if (speciesCol > -1)
                                values[speciesCol] = x.species;
                            if (organCol > -1)
                                values[organCol] = x.organ;
                            if (commentCol > -1)
                                values[commentCol] = x.comment;
                            if (labelCol > -1)
                                values[labelCol] = x.label;

                            if (i < $scope.data.length) {
                                $scope.data[i] = values;
                            } else {
                                $scope.data.push(values);
                            }

                            instance.updateSettings({data: $scope.data});
                        })
                    },
                    function(data) {
                        $scope.miniXLoading = false;
                        $scope.miniXError = 'MiniX study ID could not be found!'
                    });
            }
        };


        /**
         * Table functions
         */
        $scope.resetTableRowHeaders = function() {
            var instance = hotRegisterer.getInstance('scheduler');
            var rowLabels = instance.getRowHeader();

            $scope.data.forEach(function(x, i) {
                rowLabels[i] = i + 1;
            });

            instance.updateSettings({rowHeaders: rowLabels});
        };

        $scope.resetTable = function() {
            $scope.resetTableRowHeaders();
            $scope.data = [[]];
        };

        $scope.sortTableByValidity = function(includeBadRows) {
            includeBadRows = angular.isDefined(includeBadRows) ? includeBadRows : true;

            var instance = hotRegisterer.getInstance('scheduler');
            var rowLabels = instance.getRowHeader();

            // Find valid and invalid samples
            var goodRows = [], badRows = [];

            $scope.data.forEach(function(x, i) {
                if (angular.isString(rowLabels[i])) {
                    if (rowLabels[i].indexOf("fa-check") > 0) {
                        goodRows.push(x);
                    } else if (rowLabels[i].indexOf('fa-times') > 0) {
                        badRows.push(x);
                    }
                }
            });

            // Update data
            $scope.data = includeBadRows ? goodRows.concat(badRows) : goodRows;

            if ($scope.data.length == 0) {
                $scope.data = [[]];
            }

            // Update row headers
            for (var i = 0; i < goodRows.length + badRows.length; i++) {
                if (i < goodRows.length) {
                    rowLabels[i] = '<i class="fa fa-check text-success" aria-hidden="true"></i>';
                } else if (includeBadRows && i < goodRows.length + badRows.length) {
                    rowLabels[i] = '<i class="fa fa-times text-danger" aria-hidden="true"></i>';
                } else {
                    rowLabels[i] = i + 1;
                }
            }

            instance.updateSettings({rowHeaders: rowLabels});
        };

        $scope.removeInvalidRows = function() {
            $scope.sortTableByValidity(false);
            $scope.checkFileError = undefined
            $scope.checkFileSuccess = $scope.taskToSubmit.samples.length > 0;
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


        $scope.checkFiles = function() {
            // Reset error
            $scope.success = false;
            $scope.checkFileSuccess = undefined;
            $scope.checkFileError = undefined;
            $scope.error = undefined;
            $scope.miniXError = undefined;

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

            $scope.checkingFiles = true;
            $scope.resetTableRowHeaders();

            // Task object to submit
            $scope.taskToSubmit = {
                samples: []
            };

            // Check file existence for each row and update the row header with the result
            var rowLabels = instance.getRowHeader();
            var checkCount = 0;
            var validCount = 0;
            var errorCount = 0;

            $scope.data.forEach(function(x, i) {
                if (x[fileNameCol] !== null && x[fileNameCol] !== "") {
                    // Replace extension if desired
                    if (angular.isDefined($scope.task.extension) && $scope.task.extension != "") {
                        if (x[fileNameCol].indexOf('.') > -1) {
                            x[fileNameCol] = x[fileNameCol].substr(0, x[fileNameCol].lastIndexOf('.')) +'.'+ $scope.task.extension;
                        } else if (x[fileNameCol] != "") {
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

                            $scope.taskToSubmit.samples.push(sample);

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
                            errorCount++;
                        }
                    );
                } else if (x.filter(function(x) { return x != null && x != ""; }).length == 0) {
                    // Ignore empty rows
                    rowLabels[i] = i + 1;
                    instance.updateSettings({rowHeaders: rowLabels});
                    checkCount++;
                } else {
                    rowLabels[i] = '<i class="fa fa-times text-danger" aria-hidden="true"></i>';
                    instance.updateSettings({rowHeaders: rowLabels});
                    checkCount++;
                }
            });

            // Submit the task, waiting until all checks are complete
            var monitorFileCheck = function() {
                if (checkCount < $scope.data.length) {
                    $timeout(monitorFileCheck, 1000);
                } else if (errorCount > 0) {
                    $scope.checkFileError = errorCount +"/"+ (validCount + errorCount) +" sample files could not be found!  Please remove or rename these."
                    $scope.checkingFiles = false;
                } else {
                    $scope.checkFileSuccess = true;
                    $scope.checkingFiles = false;
                }
            };

            monitorFileCheck();
        };

        $scope.submit = function() {
            $scope.error = undefined;

            if ($scope.checkFileError) {
                $scope.error = 'Not all sample files are valid - please re-check files before submitting!';
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

            $scope.taskToSubmit.name = $scope.task.email;
            $scope.taskToSubmit.email = $scope.task.email;
            $scope.taskToSubmit.acquisitionMethod = $scope.task.acquisitionMethod;
            $scope.taskToSubmit.platform = {platform: {name: $scope.task.platform}};

            // Add task name
            if (angular.isDefined($scope.task.name) && $scope.task.name != '') {
                $scope.taskToSubmit.name += '_'+ $scope.task.name;
            } else {
                $scope.taskToSubmit.name += '_'+ $filter('date')(new Date(), 'yyyyMMddHHmmss');
            }

            // Submit the task, waiting until all checks are complete
            HttpService.submitJob(
                $scope.taskToSubmit,
                function(data) {
                    $scope.success = data.data;
                    $scope.submitting = false;
                },
                function(data) {
                    $scope.error = data.data;
                    $scope.submitting = false;
                }
            );
        };
    }]);