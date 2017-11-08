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

            // Add filters to the data table
//            $.fn.dataTable.ext.search.push(
//                function(settings, data, dataIndex) {
//                    if (settings.nTable.getAttribute('id') === 'binTable') {
//                        return matchFilter('binid_filter', data[0]) &&
//                            regexFilter('bin_filter', data[1]) &&
//                            stringFilter('group_filter', data[2]) &&
//                            rangeFilter('retentionindex_filter', data[3], 2000) &&
//                            matchFilter('binUniqueMass_filter', data[4]) &&
//                            matchFilter('quantMass_filter', data[5]);
//                    } else {
//                        return true;
//                    }
//                }
//            );

//            $.fn.dataTable.ext.search.push(
//                function(settings, searchData, index, rowData, counter) {
//                    if (settings.nTable.getAttribute('id') === 'binTable') {
//                        if ($scope.filters.libraryMatches) {
//                            if (typeof rowData.libraryMatches !== 'undefined') {
//                                if (rowData.libraryMatches === 0) {
//                                    return false;
//                                }
//                            }
//                        }
//
//                        if ($scope.filters.sameSample) {
//                            if (rowData.sampleName === $scope.filters.sampleName) {
//                                return true;
//                            }
//
//                            return false;
//                        }
//
//                    }
//
//                    return true;
//                }
//            );

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
                        var target = table.row(this).data();
                        target.name = data.data[Object.keys(data.data)[0]].name;
                        console.log(target);

                        $.ajax( {
                            type: 'PUT',
                            url: 'rest/library/' + $scope.endpoint,
                            data: target,
                            dataType: 'application/json',
                            success: function(json) {
                                //success(json);
                            },
                            error: function(xhr, error, thrown) {
                                //error(xhr, error, thrown);
                            }
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

//                $.contextMenu({
//                    selector: '#binTable tr:not(thead tr, tfoot tr)',
//                    build: function($trigger, e) {
//                        var rows = table.rows({selected: true}).data().toArray();
//
//                        var menu = {
//                            existing: {},
//                            new: {
//                                name: 'Create new group',
//                                disabled: !$trigger.hasClass('selected')
//                            },
//                            comment: {
//                                name: 'Edit comments',
//                                disabled: !$trigger.hasClass('selected')
//                            }
//                        };
//
//                        if (rows.length) {
//                            menu.existing = {
//                                name: 'Group selected under...',
//                                items: {},
//                                disabled: !$trigger.hasClass('selected')
//                            };
//
//                            rows.forEach(function(row) {
//                                if (row.group && !(row.group in menu.existing.items)) {
//                                    menu.existing.items[row.group] = { name: row.group };
//                                }
//                            });
//
//                            if (Object.getOwnPropertyNames(menu.existing.items).length === 0) {
//                                menu.existing.disabled = true;
//                            }
//                        }
//
//                        return {
//                            callback: function(key, options) {
//
//                                if (key === 'comment') {
//                                    $ctrl.items = rows;
//                                    $scope.openCommentModal();
//                                } else if (key === 'new') {
//                                    $ctrl.items = rows;
//                                    $scope.open();
//                                } else {
//                                    var request = {
//                                        groupName: key,
//                                        bins: rows.map(function(row){return row.id;})
//                                    }
//
//                                    $http.post('/rest/group/add', request)
//                                        .then(function(response) {
//                                            table.ajax.reload(null, false);
//                                        });
//                                }
//                            },
//                            items: menu
//                        }
//                    }
//                });
//
//                $scope.open = function (size, parentSelector) {
//                    var parentElem = parentSelector ?
//                    angular.element($document[0].querySelector('.modal-demo ' + parentSelector)) : undefined;
//                    var modalInstance = $uibModal.open({
//                        animation: $ctrl.animationsEnabled,
//                        ariaLabelledBy: 'modal-title',
//                        ariaDescribedBy: 'modal-body',
//                        templateUrl: 'binview/views/groupModalTemplate.html',
//                        controller: 'ModalInstanceCtrl',
//                        controllerAs: '$ctrl',
//                        size: size,
//                        appendTo: parentElem,
//                        resolve: {
//                            items: function () {
//                                return $ctrl.items;
//                            }
//                        }
//                    });
//
//                    modalInstance.result.then(function (groupName) {
//                        $http.post('/rest/group/add', { groupName: groupName, bins: $ctrl.items.map(function(row){return row.id;}) })
//                            .then(function(response) {
//                                table.ajax.reload(null, false);
//                            });
//                    }, function () {
//                        console.log('Modal dismissed at: ' + new Date());
//                    });
//                };
//
//                $scope.openCommentModal = function (size) {
//                    var modalInstance = $uibModal.open({
//                        animation: $ctrl.animationsEnabled,
//                        ariaLabelledBy: 'modal-title',
//                        ariaDescribedBy: 'modal-body',
//                        templateUrl: 'binview/views/commentModalTemplate.html',
//                        controller: 'ModalInstanceCtrl',
//                        controllerAs: '$ctrl',
//                        size: size,
//                        resolve: {
//                            items: function () {
//                                return $ctrl.items;
//                            }
//                        }
//                    });
//
//                    modalInstance.result.then(function (comment) {
//
//                    }, function () {
//                        console.log('Modal dismissed at: ' + new Date());
//                    });
//                };

                // Activate an inline edit on click of a table cell
                $('#binTable').on('click', 'tbody td.editable', function (e) {
                    editor.inline(this, { submit: 'all' });
                });

                // Broadcast event when a row is clicked
                $('#binTable').on('click', 'tbody tr', function (e) {
                    var data = table.row(this).data();
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


                // Add column filter footer
                var footer = $('#binTable').append('<tfoot><tr></tr></tfoot>');

                $.each(columns, function (i, x) {
                    var inputId = x.data +'_filter';
                    $('#binTable').find('tfoot tr').append('<th><input type="text" class="form-control input-sm" id="'+ inputId +'" placeholder="Search ' + x.title + '" /></th>');

                    $('#'+ inputId).on('keypress', function(e) {
                        if (e.keyCode == 13 || e.keyCode == 9) {
                            table.draw();
                        }
                    });
                });
            });
        },

        link: function(scope) {
            scope.$watch('endpoint', function(newVal, oldVal) {
                if (table) {
                    table.ajax.url('/rest/library/' + newVal).load();
                }
            });

            scope.$watch('filters', function(newVal, oldVal) {

                if (newVal.unnamed) {
                    $('#bin_filter').val('^[0-9]+$');
                } else if (oldVal.unnamed) {
                    $('#bin_filter').val('');
                }

                if (newVal.ungrouped) {
                    $('#group_filter').val('none');
                } else if (oldVal.ungrouped) {
                    $('#group_filter').val('');
                }

                if (newVal.retentionRange) {
                    $('#retentionindex_filter').val(newVal.retentionMin + '-' + newVal.retentionMax);
                } else if (oldVal.retentionRange) {
                    $('#retentionindex_filter').val('');
                }

                if (newVal.sampleName !== oldVal.sampleName) {
                    return;
                }

                if (table) {
                    table.draw();
                }

            }, true);
        }
    };
}]);
