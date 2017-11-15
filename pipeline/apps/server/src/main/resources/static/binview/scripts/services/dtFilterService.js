'use strict';

app.service('DTFilter', function() {
    var service = {
        addFilter: function(filter, tableId) {
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