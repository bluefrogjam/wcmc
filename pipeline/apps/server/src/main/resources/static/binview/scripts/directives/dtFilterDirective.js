'use strict';

app.directive('dtFilter', function() {
    return {
        restrict: 'E',
        scope: {
            tableId: '=',
            filter: '='
        },
        controller: function($scope) {
            $.fn.dataTable.ext.search.push(
                function(settings, data, dataIndex) {
                    if (settings.nTable.getAttribute('id') === $scope.tableId) {
                        return filter(settings, data, dataIndex);
                    }

                    return true;
                }
            );
        },
        link: function($scope) {

        }
    }
});