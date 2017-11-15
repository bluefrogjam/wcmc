'use strict';

angular.module('app').directive('dtFooter', [ 'dtFilter', function(dtFilter) {
    return {
        restrict: 'A',
        require: 'binTable',
        link: function(scope, elem, attr, controller) {
            console.log('dtFooterDirective: link', scope, elem, attr, controller);

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

            dtFilter.addFilter('binTable', function(settings, data, dataIndex) {
                return matchFilter('id_filter', data[0]) &&
                    stringFilter('name_filter', data[1]) &&
                    rangeFilter('precursorMass_filter', data[2], scope.binSettings.filters.massWindow) &&
                    rangeFilter('retentionIndex_filter', data[3], scope.binSettings.filters.riWindow) &&
                    matchFilter('ionMode_filter', data[4]);
            });

            var footer = $('#' + attr.id).append('<tfoot><tr></tr></tfoot>');

            var columns = [
                {data: 'id', title: 'ID'},
                {data: 'name', title: 'Name'},
                {data: 'precursorMass', title: 'Precursor Mass'},
                {data: 'retentionIndex', title: 'Retention Index'},
                {data: 'ionMode', title: 'Ion Mode'}
            ];

            $.each(columns, function (i, x) {
                var inputId = x.data +'_filter';
                $('#' + attr.id).find('tfoot tr').append('<th><input type="text" class="form-control input-sm" id="' + inputId + '" placeholder="Search ' + x.title + '" /></th>');

                $('#' + inputId).on('keypress', function(e) {
                    if (e.keyCode == 13 || e.keyCode == 9) {
                        controller.refresh();
                    }
                });
            });

            scope.$watch('binSettings.filters', function() {
                controller.refresh();
            }, true);
        }
    };
}]);
