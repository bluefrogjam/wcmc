(function() {
    'use strict';

    dtFooter.$inject = ['dtFilter'];

    angular.module('app')
        .directive('dtFooter', dtFooter);

    function dtFooter(dtFilter) {
        return {
            restrict: 'A',
            require: 'binTable',
            link: function(scope, elem, attr, controller) {
                console.log(scope, attr);

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

                    var regex = new RegExp(value, 'i');

                    return value === '' || value.trim().length < minLength || data.match(regex);
                };

                var columns = [
                    {data: 'id', title: 'ID'},
                    {data: 'name', title: 'Name', className: 'editable'},
                    {data: 'precursorMass', title: 'Precursor Mass'},
                    {data: 'retentionIndex', title: 'Retention Index'},
                    {data: 'isRetentionIndexStandard', title: 'Retention Index Standard'},
                    {data: 'confirmed', title: 'Confirmed'},
                    {data: 'ionMode.mode', title: 'Ion Mode'}
                ];

                dtFilter.addFilter('binTable', function(settings, data, dataIndex) {
                    return stringFilter(columns[0].data + '_filter', data[0]) &&
                        regexFilter(columns[1].data + '_filter', data[1]) &&
                        rangeFilter(columns[2].data + '_filter', data[2], scope.binSettings.filters.massWindow) &&
                        rangeFilter(columns[3].data + '_filter', data[3], scope.binSettings.filters.riWindow) &&
                        stringFilter(columns[4].data + '_filter', data[4]) &&
                        stringFilter(columns[5].data + '_filter', data[5]) &&
                        stringFilter(columns[6].data.replace('.','_') + '_filter', data[6]);
                });

                var footer = $('#' + attr.id).append('<tfoot><tr></tr></tfoot>');

                $.each(columns, function (i, x) {
                    var inputId = x.data +'_filter';
                    $('#' + attr.id).find('tfoot tr').append('<th><input type="text" class="form-control input-sm" id="' + inputId.replace('.','_') + '" placeholder="Search ' + x.title + '" /></th>');

                    $('#' + inputId).on('keypress', function(e) {
                        if (e.keyCode == 13 || e.keyCode == 9) {
                            controller.refresh();
                        }
                    });
                });

                scope.$watch('binSettings.filters', function(newVal, oldVal) {
                    if (newVal.named) {
                        var regex = '^(?!unknown_[0-9]+_[0-9]+)';

                        $('#name_filter').val(regex);
                    } else if (oldVal.named) {
                        $('#name_filter').val('');
                    }

                    if (newVal.unnamed) {
                        var regex = '^unknown_[0-9]+_[0-9]+$';

                        $('#name_filter').val(regex);
                    } else if (oldVal.unnamed) {
                        $('#name_filter').val('');
                    }

                    if (newVal.retentionRange) {
                        $('#retentionIndex_filter').val(newVal.retentionMin + '-' + newVal.retentionMax);
                    } else if (oldVal.retentionRange) {
                        $('#retentionIndex_filter').val('');
                    }

                    controller.refresh();
                }, true);
            }
        };
    }
})();