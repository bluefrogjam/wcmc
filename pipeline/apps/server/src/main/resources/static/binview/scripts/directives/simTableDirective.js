/**
 * Created by matthewmueller on 10/16/17.
 */

(function() {
    'use strict';

    angular.module('app').directive('simTable', simTable);

    /* @ngInject */
    function simTable() {
        var simTable;

        return {
            restrict: 'E',
            replace: true,
            template: '<table id="simTableDT" class="table table-striped table-bordered table-hover display" width="100%"></table>',
            scope: {
                model: '=ngModel',
                columns: '=',
                filters: '=',
                selectFn: '&'
            },
            controller: function($scope) {

                simTable = $('#simTableDT').DataTable({
                    data: $scope.model,
                    order: [[1, 'desc']],
                    select: {
                        style: 'single',
                        info: false
                    },
                    columns: $scope.columns
                });

                $.fn.dataTable.ext.search.push(
                    function(settings, data, dataIndex) {
                        if (settings.nTable.getAttribute('id') === 'simTableDT') {
                            if ($scope.filters.ri && data[2] === 'none') {
                                return false;
                            }

                            if (!$scope.filters.allLibraries && (!$scope.filters.libraries[data[3].toLowerCase()] || !$scope.filters.libraries[data[3].toLowerCase()].value)) {
                                return false;
                            }
                        }

                        return true;
                    }
                );



                jQuery.extend( jQuery.fn.dataTableExt.oSort, {
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
                } );
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

                    simTable.row(':eq(0)', { page: 'current' }).select();
                }, true);

                $scope.$watch('filters', function(newVal, oldVal) {
                    simTable.draw();
                }, true);

                simTable.on('select', function (e, dt, type, indexes) {
                    $scope.$applyAsync($scope.selectFn({e:e, dt:dt, type:type, indexes:indexes}));
                });
            }
        }
    }
})();
