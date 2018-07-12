/**
 * Created by matthewmueller on 10/16/17.
 */

(function() {
    'use strict';

    simTable.$inject = ['dtFilter'];

    angular.module('app').directive('simTable', simTable);

    /* @ngInject */
    function simTable(dtFilter) {
        var simTable;

        return {
            restrict: 'E',
            replace: true,
            template: '<table id="simTableDT" class="table table-striped table-bordered table-hover display" width="100%"></table>',
            scope: {
                model: '=ngModel',
                settings: '='
            },
            controller: function($scope, $rootScope) {

                simTable = $('#simTableDT').DataTable({
                    data: $scope.model,
                    order: [[1, 'desc']],
                    select: {
                        style: 'single',
                        info: false
                    },
                    columns: [
                        {
                            title: 'Name',
                            data: 'hit.compound.0.names.0.name'
                        },
                        {
                            title: 'Similarity',
                            data: function(row, type, set, meta) {
                                return Math.floor(row.score * 1000);
                            }
                        },
                        {
                            title: 'RI Diff',
                            type: 'num-none',
                            data: function(row, type, set, meta) {
                                return row.retentionIndex ? Math.floor(Math.abs(bin.getActiveBin().retentionIndex - row.retentionIndex)) : null;
                            },
                            defaultContent: 'none'
                        },
                        {
                            title: 'Library',
                            data: 'hit.library.library',
                            defaultContent: 'none'
                        }
                    ],
                });

                dtFilter.addFilter('simTableDT', function(settings, data, dataIndex) {
                    if ($scope.settings.filters.ri && data[2] === 'none') {
                        return false;
                    }

                    if (!$scope.settings.filters.allLibraries && (!$scope.settings.filters.libraries[data[3].toLowerCase()] || !$scope.settings.filters.libraries[data[3].toLowerCase()].value)) {
                        return false;
                    }

                    return true;
                });

                // Broadcast event when a row is clicked
                $('#simTableDT').on('click', 'tbody tr', function(e) {
                    var data = simTable.row(this).data();
                    $rootScope.$broadcast('sim-clicked', data);
                });

                jQuery.extend(jQuery.fn.dataTableExt.oSort, {
                    "num-none-pre": function ( a ) {
                        var x = String(a).replace( /<[\s\S]*?>/g, "" );
                        return parseFloat( x );
                    },

                    "num-none-asc": function ( a, b ) {
                        return ((a < b) ? -1 : ((a > b) ? 1 : 0));
                    },

                    "num-none-desc": function ( a, b ) {
                        return ((a < b) ? 1 : ((a > b) ? -1 : 0));
                    }
                });
            },
            link: function($scope) {
                simTable.on('user-select', function( e, dt, type, cell, originalEvent ) {
                    if ($(cell.node()).parent().hasClass('selected')) {
                        e.preventDefault();
                    }
                });

                $scope.$watch('model', function(newVal, oldVal) {
                    simTable.clear()
                        .rows.add(newVal)
                        .draw();

                    if ($scope.settings.autoClick) { simTable.row(':eq(0)', { page: 'current' }).select(); }
                }, true);

                $scope.$watch('settings.filters', function(newVal, oldVal) {
                    simTable.draw();
                }, true);

            }
        }
    }
})();
