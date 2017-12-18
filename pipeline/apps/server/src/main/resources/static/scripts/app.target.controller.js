angular.module('app')
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
            $scope.errors = [];

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
                } else {
                     rowLabels[i] = i + 1;
                     instance.updateSettings({rowHeaders: rowLabels});
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
                    var target = angular.copy($scope.target);
                    target.targetName = $scope.data[i].targetName;
                    target.precursor = $scope.data[i].precursor;
                    target.retentionTime = $scope.data[i].retentionTime;

                    // Handle string values for checkboxes when pasted
                    if (angular.isDefined($scope.data[i].riMarker)) {
                        if ((angular.isString($scope.data[i].riMarker) && $scope.data[i].riMarker == 'TRUE') || $scope.data[i].riMarker == true) {
                          target.riMarker = true;
                        } else {
                            target.riMarker = false;
                        }
                    } else {
                        target.riMarker = false;
                    }

                    if (target.ri_unit == 'minutes') {
                        target.retentionTime *= 60;
                    }

                    HttpService.submitTarget(
                        target,
                        function(data) {
                            rowLabels[i] = '<i class="fa fa-check text-success" aria-hidden="true"></i>';
                            instance.updateSettings({rowHeaders: rowLabels});

                            $scope.successCount++;
                        },
                        function(data) {
                            rowLabels[i] = '<i class="fa fa-times text-danger" aria-hidden="true"></i>';
                            instance.updateSettings({rowHeaders: rowLabels});

                            $scope.errorCount++;

                            if (data.status == 409) {
                                $scope.errors.push("Target \""+ data.config.data.targetName +"\" already exists in specified library!");
                            } else {
                                $scope.errors.push("Internal server error for target: "+ data.config.data.targetName);
                            }
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
    }]);