/**
 * Created by sajjan on 5/4/17.
 */
'use strict';

app.directive('binTable', ['bsLoadingOverlayService', '$http', function(bsLoadingOverlayService, $http) {
    var table;
    return {
        restrict: 'E',
        template: '<table id="binTable" class="table table-striped table-bordered table-hover" cellspacing="0" width="100%"></table>',
        replace: true,
        scope: {
            dtDataUrl: '=',
            filters: '='
        },

        controller: function($scope, $rootScope) {
            var columns = [];

            $(document).ready(function() {
                // Define columns
                columns = [
                    {data: 'id', title: 'ID'},
                    {data: 'name', title: 'Name', className: 'editable'},
                    {data: 'precursorMass', title: 'Precursor Mass'},
                    {data: 'retentionIndex', title: 'Retention Index'},
                    {data: 'isRetentionIndexStandard', title: 'Retention Index Standard',
                        render: function(data, type, row) {
                            if (type === 'display') {
                                return '<input type="checkbox" class="editor-active">';
                            }

                            return data;
                        }
                    },
                    {data: 'ionMode.mode', title: 'Ion Mode'}
                ];

                // Define editor properties
                var editor = new $.fn.dataTable.Editor({
                    ajax: function(method, url, data, success, error) {
                        var rowId = Object.keys(data.data)[0];
                        var newData = data.data[rowId];
                        var rowData = table.row('#' + rowId).data();
                        var target = Object.assign({}, rowData, newData);
                        console.log(target);
                        if (target.isRetentionIndexStandard.length && target.isRetentionIndexStandard.length == 1) {
                            target.isRetentionIndexStandard = true;
                        } else {
                            target.isRetentionIndexStandard = false;
                        }

                        $.ajax({
                            type: 'PUT',
                            headers: {
                                    'Accept': 'application/json',
                                    'Content-Type': 'application/json'
                            },
                            url: $scope.dtDataUrl,
                            data: JSON.stringify(target),
                            success: function(json) {
                                success(json);
                                table.ajax.reload();
                            },
                            error: function(xhr, error, thrown) {

                            }
                        });
                    },
                    table: '#binTable',
                    idSrc: 'id',
                    fields: columns.map(function(x) {
                        if (x.data == 'isRetentionIndexStandard') {
                            return {
                                name: x.data,
                                label: x.title,
                                type: 'checkbox',
                                separate: '|',
                                options: [
                                    { label: '', value: true }
                                ]
                            }
                        }
                        return {name: x.data, label: x.title}
                    })
                });

                // Create bin table
                table = $('#binTable').DataTable({
                    rowId: 'id',
                    ajax: {
                        url: $scope.dtDataUrl,
                        dataSrc: function(json) {
                            bsLoadingOverlayService.stop();
                            return json;
                        }
                    },
                    select: {
                        style: 'os',
                        info: false
                    },
                    columns: columns,
                    rowCallback: function(row, data) {
                        console.log(data);
                        $('input.editor-active', row).prop('checked', data.isRetentionIndexStandard == true);
                    }
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
                    $rootScope.$broadcast('bin-clicked', data);
                });

                $('#binTable').on( 'change', 'input.editor-active', function () {

                    console.log($(this).closest('tr'));
                    var checked = $(this).prop('checked');
                    console.log(checked);

                    editor
                        .edit( $(this).closest('tr'), false )
                        .field('isRetentionIndexStandard').set( checked );
                    editor.submit();
                } );

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

            this.getColumns = function() {
                return columns;
            }
        },

        link: function(scope) {
            scope.$watch('dtDataUrl', function(newVal, oldVal) {
                if (table) {
                    table.ajax.url(newVal).load();
                }
            });
        }
    };
}]);
