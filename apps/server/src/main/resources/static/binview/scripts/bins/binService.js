(function() {
    'use strict';

    angular.module('app')
        .factory('bin', bin);

    /* @ngInject */
    function bin() {

        var bins = [],
            active = null;

        var service = {
            getBins: getBins,
            setBins: setBins,
            getActiveBin: getActiveBin,
            setActiveBin: setActiveBin
        };

        return service;

        //////////

        function getBins() {
            return bins;
        }

        function setBins(bins) {
            bins = bins;
        }

        function getActiveBin() {
            return active;
        }

        function setActiveBin(bin) {
            active = bin;
        }
    }

})();