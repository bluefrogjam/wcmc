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

            this.refresh = function() {
                if (table) {
                    table.draw();
                }
            }
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
