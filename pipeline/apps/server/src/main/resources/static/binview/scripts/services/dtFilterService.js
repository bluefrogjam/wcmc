'use strict';

app.service('dtFilter', function() {
    var service = {
        addFilter: function(tableId, filter) {
            $.fn.dataTable.ext.search.push(
                function(settings, data, dataIndex) {
                    if (settings.nTable.getAttribute('id') === tableId) {
                        return filter(settings, data, dataIndex);
                    }

                    return true;
                }
            );
        }
    };

    return service;
});