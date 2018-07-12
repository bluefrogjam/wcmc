(function() {
    'use strict';

    angular.module('app.similarity')
        .factory('similaritySearch', similaritySearch);

    /* @ngInject */
    function similaritySearch() {

        var service = {
            search: search
        };

        return service;

        //////////

        function search(spectrum, settings) {

        }

    }

})();