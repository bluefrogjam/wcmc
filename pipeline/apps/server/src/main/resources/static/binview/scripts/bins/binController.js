/**
 * Created by matthewmueller on 9/18/17.
 */

(function() {
    'use strict';

    BinController.$inject = ['$scope', '$sce', '$http', 'bsLoadingOverlayService', 'bin', 'monaSimilaritySearch', 'stSpectra'];

    angular.module('app')
        .controller('BinController', BinController);

    /* @ngInject */
    function BinController($scope, $sce, $http, bsLoadingOverlayService, bin, monaSimilaritySearch, stSpectra) {

        $scope.binSettings = {
            libraries: [],
            filters: {
                massWindow: 5,
                riWindow: 5,
                named: false,
                unnamed: false,
                retentionRange: false,
                retentionMin: 0,
                retentionMax: 100000
            }
        };

        $scope.simSettings = {
            minSimilarity: 700,
            maxSimilarity: 1000,
            autoClick: true,
            filters: {
                allLibraries: true,
                toggleAllLibraries: function() {
                    for (var library in $scope.simSettings.filters.libraries) {
                        $scope.simSettings.filters.libraries[library].value = $scope.simSettings.filters.allLibraries;
                    }
                },
                libraries: {
                    'fiehnlib': { name: 'FiehnLib', value: true },
                    'gnps': { name: 'GNPS', value: true },
                    'hmdb': { name: 'HMDB', value: true },
                    'itree': { name: 'iTree', value: true },
                    'massbank': { name: 'MassBank', value: true },
                    'metabobase': { name: 'MetaboBase', value: true },
                    'respect': { name: 'ReSpect', value: true }
                },
                ri: false
            }
        };

        $scope.stSpectra = [];

        $scope.stOptions = {
            normalize: true
        };

        activate();

        function activate() {
            $http.get("/rest/library")
                .then(function(response) {
                    $scope.binSettings.libraries = response.data;
                    $scope.binSettings.acquisition = response.data[0].chromatographicMethod.name;
                });

            bsLoadingOverlayService.start();
        };



        $scope.startLoadingService = function() {
            bsLoadingOverlayService.start();
        };

        $scope.startLoadingService();

        $(document).ready(function() {
            $('#bin-table-wrapper').height($(document).height()-30-56);
            $('#uib-tabset-wrapper').height($(document).height()-30-56);
            $('#binvestigate-wrapper').height($(document).height()-30-42-15-56);
            $('.tab-content').height($(document).height()-30-42-56);
        });

        $scope.$on('bin-clicked', function(event, data) {
            $scope.$apply(function() {
                bin.setActiveBin(data);

                $scope.stSpectra = [];
                $scope.stSpectra.push({ spectrumId: 'BIN', spectrum: data.spectrum });

                $scope.similaritySearch();
            });
        });

        $scope.$on('sim-clicked', function(event, data) {
            $scope.$apply(function() {
                $scope.stSpectra.splice(1);
                $scope.stSpectra.push({ spectrumId: 'LIBRARY', spectrum: data.hit.spectrum });
            });
        });

        $scope.similaritySearch = function() {
            var data = bin.getActiveBin();
            var spectrum = '';

            data.spectrum.ions.forEach(function(ion) {
                spectrum += ion.mass + ':' + ion.intensity + ' ';
            });

            monaSimilaritySearch.search(spectrum, $scope.simSettings)
                .then(function(d) {
                    $scope.similaritySearchResults = d.data;

                    $scope.stSpectra.splice(1);

                    if ($scope.simSettings.autoClick && d.data.length > 0) {
                        $scope.stSpectra.push({ spectrumId: 'LIBRARY', spectrum: d.data[0].hit.spectrum });
                    }
                });
        };

        $scope.chemicalSearch = function(data) {
            ctsSearch.search(data)
                .then(function(d) {
                    $scope.compound = d.data;
                });
        };

        $scope.saveImage = function(bin, lib){
            saveSvgAsPng(document.getElementsByClassName('st-base')[0], 'diff-' + bin + '-' + lib + '.png', { scale: 5 });
        };

    }
})();
