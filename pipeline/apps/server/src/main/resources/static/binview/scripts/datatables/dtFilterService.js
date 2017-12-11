(function(){
    'use strict';

    angular.module('app')
        .factory('dtFilter', dtFilter);

    function dtFilter() {

        var service = {
            addFilter: addFilter
        };

        return service;

        //////////

        function addFilter(tableId, filter) {
            $.fn.dataTable.ext.search.push(
                function(settings, data, dataIndex) {
                    if (settings.nTable.getAttribute('id') === tableId) {
                        return filter(settings, data, dataIndex);
                    }

                    return true;
                }
            );
        }
    }

})();