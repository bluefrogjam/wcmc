/**
 * Created by sajjan on 5/4/17.
 */
'use strict';

app.directive('binTable', ['bsLoadingOverlayService', '$uibModal', '$http', function(bsLoadingOverlayService, $uibModal, $http) {
    var table;
    return {
        restrict: 'E',
        template: '<table id="binTable" class="table table-striped table-bordered table-hover" cellspacing="0" width="100%"></table>',
        replace: true,
        scope: {
            endpoint: '=',
            filters: '='
        },

        controller: function($scope, $rootScope, $uibModal) {

            var $ctrl = this;
            $ctrl.items = [];

            $ctrl.animationsEnabled = true;

            /**
             * Filter to find exact matches for one or more query strings,
             * using OR between multiple queries
             * @param queryId id of query text field
             * @param data column data from DataTables filter
             * @param minLength
             * @returns {boolean}
             */
            var matchFilter = function(queryId, data, minLength) {
                var query = $('#'+ queryId).val();
                minLength = minLength || 0;

                if (query.trim() === '' || query.trim().length < minLength) {
                    return true;
                } else {
                    var result = false;

                    $.each(query.split(','), function (i, x) {
                        if (x.trim() === data)
                            result = true;
                    });

                    return result;
                }
            };

            /**
             * Filter to find partial string matches, checking only if the query is
             * contained within the data field
             * @param queryId id of query text field
             * @param data column data from DataTables filter
             * @param minLength
             * @returns {boolean}
             */
            var stringFilter = function(queryId, data, minLength) {
                var value = $('#'+ queryId).val();
                minLength = minLength || 0;

                return value === '' || value.trim().length < minLength || data.indexOf(value) > -1;
            };

            /**
             * Filter to find matches within a given range.  If only a single value is
             * provided, a tolerance is used around the value as a search range
             * @param queryId id of query text field
             * @param data column data from DataTables filter
             * @param defaultTolerance
             * @param minLength
             * @returns {boolean}
             */
            var rangeFilter = function(queryId, data, defaultTolerance, minLength) {
                var query = $('#'+ queryId).val();
                minLength = minLength || 0;

                if (query.trim() === '' || query.trim().length < minLength) {
                    return true;
                } else {
                    var result = false;
                    data = parseFloat(data) || 0;

                    $.each(query.split(','), function(i, x) {
                        if (x.indexOf('-') > -1) {
                            x = x.split('-');
                            var min = parseInt(x[0], 10);
                            var max = parseInt(x[1], 10);

                            if ((isNaN(min) && data <= max) || (min <= data && isNaN(max)) || (min <= data && data <= max))
                                result = true;
                        } else {
                            var value = parseInt(x, 10);

                            if (Math.abs(value - data) <= defaultTolerance)
                                result = true;
                        }
                    });

                    return result;
                }
            };

            /**
             * Filter to find regular expression matches
             * @param queryId id of query text field
             * @param data column data from DataTables filter
             * @param minLength
             * @returns {boolean}
             */
            var regexFilter = function(queryId, data, minLength) {
                var value = $('#'+ queryId).val();
                minLength = minLength || 0;

                var regex = new RegExp(value);

                return value === '' || value.trim().length < minLength || data.match(regex, 'i');
            };

            $(document).ready(function() {
                // Define columns
                var columns = [
                    {data: 'id', title: 'ID'},
                    {data: 'name', title: 'Name', className: 'editable'},
                    {data: 'precursorMass', title: 'Precursor Mass'},
                    {data: 'retentionIndex', title: 'Retention Index'},
                    {data: 'ionMode.mode', title: 'Ion Mode'}
                ];

                // Define editor properties
                var editor = new $.fn.dataTable.Editor({
                    ajax: function(method, url, data, success, error) {
                        var newData = data.data[Object.keys(data.data)[0]];
                        var rowData = $scope.activeBin;
                        var rowId = Object.keys(data.data)[0];
                        var rows = table.rows();

                        var url = 'rest/library/' + $scope.endpoint;

                        var target = {
                            id: newData.id,
                            confirmed: rowData.confirmed,
                            inchiKey: rowData.inchiKey,
                            ionMode: newData.ionMode,
                            isRetentionIndexStandard: rowData.isRetentionIndexStandard,
                            msmsSpectrum: rowData.msmsSpectrum,
                            name: newData.name,
                            precursorMass: newData.precursorMass,
                            requiredForCorrection: rowData.requiredForCorrection,
                            retentionIndex: newData.retentionIndex,
                            retentionTimeInSeconds: rowData.retentionTimeInSeconds,
                            spectrum: rowData.spectrum
                        };

                        $.ajax( {
                            type: 'PUT',
                            headers: {
                                    'Accept': 'application/json',
                                    'Content-Type': 'application/json'
                            },
                            url: url,
                            data: JSON.stringify(target),
                            success: function(json) { console.log('success', json); table.ajax.reload(); },
                            error: function(xhr, error, thrown) { }
                        } );
                    },
                    table: '#binTable',
                    idSrc: 'id',
                    fields: columns.map(function(x) {
                        return {name: x.data, label: x.title}
                    })
                });

                editor.on('preSubmit', function(e, data, action) {
                    console.log(e, data, action);
                });

                // Create bin table
                table = $('#binTable').DataTable({
                    ajax: {
                        url: '/rest/library/' + $scope.endpoint,
                        dataSrc: function(json) {
                            bsLoadingOverlayService.stop();
                            return json;
                        }
                    },
                    select: {
                        style: 'os',
                        info: false
                    },
                    columns: columns
                });

                table.on('user-select', function( e, dt, type, cell, originalEvent ) {
                    if ($(cell.node()).parent().hasClass('selected')) {
                        e.preventDefault();
                    }
                });

                // Activate an inline edit on click of a table cell
                $('#binTable').on('click', 'tbody td.editable', function (e) {
                    editor.inline(this, { submit: 'all' });
                });

                // Broadcast event when a row is clicked
                $('#binTable').on('click', 'tbody tr', function (e) {
                    var data = table.row(this).data();
                    $scope.activeBin = data;
                    $rootScope.$broadcast('bin-clicked', data);
                });

                editor
                    .on('open', function (e, mode, action) {
                        if (mode === 'main') {
                            table.keys.disable();
                        }
                    })
                    .on('close', function () {
                        table.keys.enable();
                    });

            });
        },

        link: function(scope) {
            scope.$watch('endpoint', function(newVal, oldVal) {
                if (table) {
                    table.ajax.url('/rest/library/' + newVal).load();
                }
            });
        }
    };
}]);
