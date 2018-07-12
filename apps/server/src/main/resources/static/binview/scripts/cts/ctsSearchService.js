(function() {
    'use strict';

    angular.module('app.cts')
        .factory('ctsSearch', ctsSearch);

    ctsSearch.$inject = ['$http'];

    /* @ngInject */
    function ctsSearch($http) {

        var name = '',
            inchi = '',
            compound = '',
            mol = '';

        var service = {
            search: search
        };

        return service;

        //////////

        function search(data) {


            return $http.get('/rest/chemical/nameToInchi/' + data.bin)
                .then(function(response) {
                    if (response.data && response.data.result.length > 0 && response.data.result[0].length > 0) {
                        $http.get('/rest/chemical/inchiToCompound/' + response.data.result[0])
                            .then(function(innerResponse) {
                                response.compound = innerResponse.data
                            });

                        $http.get('/rest/chemical/inchiToMol/' + response.data.result[0])
                            .then(function(innerResponse) {
                                response.mol = innerResponse.data;
                            });
                    }

                    return response;
                });
        }

        function clearFields() {
            name = '';
            inchi = '';
            compound = '';
            mol = '';
        }
    }

})();