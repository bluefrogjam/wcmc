(function() {
    'use strict';

    angular.module('app.similarity')
        .factory('monaSimilaritySearch', monaSimilaritySearch);

    monaSimilaritySearch.$inject = ['$http'];

    /* @ngInject */
    function monaSimilaritySearch($http) {

        var service = {
            search: search
        };

        return service;

        //////////

        function search(spectrum, settings) {

            var postData = {
                'spectrum': spectrum,
                'minSimilarity': settings.minSimilarity || 700,
                'maxSimilarity': settings.maxSimilarity || 1000
            };

            return $http.post('/rest/similarity/search', postData)
                .then(function(response) {
                    response.data.forEach(function(result, index) {
                        if (result.hit.metaData) {
                            result.hit.metaData.forEach(function(data){
                                if (data.name === 'retention index') {
                                    result.retentionIndex = data.value;

                                    return;
                                }
                            });
                        }
                    });

                    return response;
                });
        }
    }

})();