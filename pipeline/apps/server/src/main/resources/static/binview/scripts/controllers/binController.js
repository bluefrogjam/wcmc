/**
 * Created by matthewmueller on 9/18/17.
 */

(function() {
    'use strict';
    binController.$inject = ['$scope', '$sce', '$http', 'bsLoadingOverlayService'];
    angular.module('app')
        .controller('BinController', binController);

    /* @ngInject */
    function binController($scope, $sce, $http, bsLoadingOverlayService) {

        $scope.binSettings = {
            libraries: [],
            filters: {
                massWindow: 5,
                riWindow: 5
            }
        };

        $scope.simSettings = {
            minSimilarity: 700,
            maxSimilarity: 1000,
            dtColumns: [
                {
                    title: 'Name',
                    data: 'hit.compound.0.names.0.name'
                },
                {
                    title: 'Similarity',
                    data: function(row, type, set, meta) {
                        return Math.floor(row.score * 1000);
                    }
                },
                {
                    title: 'RI Diff',
                    type: 'num-none',
                    data: function(row, type, set, meta) {
                        return row.retentionIndex ? Math.floor(Math.abs($scope.bin.retentionindex - row.retentionIndex)) : null;
                    },
                    defaultContent: 'none'
                },
                {
                    title: 'Library',
                    data: 'hit.library.library',
                    defaultContent: 'none'
                }
            ],
            filters: {
                libraries: {
                    all: true,
                    fiehnlib: true,
                    gnps: true,
                    hmdb: true,
                    itree: true,
                    massbank: true,
                    metabobase: true,
                    respect: true
                },
                ri: false
            }
        };

        $http.get("/rest/library")
            .then(function(response) {
                console.log(response);
                $scope.binSettings.libraries = response.data;
                $scope.binSettings.acquisition = response.data[0].chromatographicMethod.name;
            });

        $scope.stSpectra = {
            data: []
        }

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
            console.log('bin-clicked', data);
                $scope.bin = data;
                $scope.stSpectra.data = [];
                $scope.stSpectra.data.push({ spectrumId: 'BIN', spectrum: data.spectrum });
                $scope.similaritySearch(data);
            })
        });

        $scope.similaritySearch = function(data) {

            var spectrum = '';

            data.spectrum.ions.forEach(function(ion) {
                spectrum += ion.mass + ':' + ion.intensity + ' ';
            });

            var postData = {
                'spectrum': spectrum,
                'minSimilarity': $scope.simSettings.minSimilarity || 700,
                'maxSimilarity': $scope.simSettings.maxSimilarity || 1000,
                'precursorMZ': $scope.simSettings.precursorMZ,
                'precursorToleranceDa': $scope.simSettings.precursorToleranceDa
            };

            $http
                .post('/rest/similarity/search', postData)
                .then(function(response) {
                    data.libraryMatches = response.data.length;
                    $scope.similaritySearchResults = response.data;
                    $scope.similaritySearchResults.forEach(function(result, index) {
                        if (result.hit.metaData) {
                            result.hit.metaData.forEach(function(data){
                                if (data.name === 'retention index') {
                                    $scope.similaritySearchResults[index].retentionIndex = data.value;
                                    return;
                                }
                            });
                        }
                    });
                });
        };

        $scope.ctsSearch = function(data) {
            $http
                .get('/rest/chemical/nameToInchi/'+data.bin)
                .then(function(response) {
                    if (response.data && response.data.result.length > 0 && response.data.result[0].length > 0) {
                        $http
                            .get('/rest/chemical/inchiToCompound/' + response.data.result[0])
                            .then(function(innerResponse) {
                                $scope.compound = innerResponse.data;

                                $scope.compound.formula = innerResponse.data.inchicode.split('/')[1];
                            });

                        $http
                            .get('/rest/chemical/inchiToMol/' + response.data.result[0])
                            .then(function(innerResponse) {
                                $scope.mol = innerResponse.data.molecule;
                            });
                    }
                },
                function(response) {

                });
        };

        $scope.setLibraryBin = function(clicked) {
            $scope.libraryBin = clicked;

            if ($scope.stSpectra.data.length < 1) {
                if (clicked.hit.spectrum !== '') {
                    $scope.stSpectra.data.push({ spectrumId: 'LIBRARY', spectrum: clicked.hit.spectrum });
                }
            } else {
                $scope.stSpectra.data.splice(1, 1);
                if (clicked.hit.spectrum !== '') {
                    $scope.stSpectra.data.push({ spectrumId: 'LIBRARY', spectrum: clicked.hit.spectrum });
                }
            }
        };

        $scope.onSelectSim = function (e, dt, type, indexes) {
            $scope.setLibraryBin(dt.data());
        };

        $scope.saveImage = function(bin, lib){
            saveSvgAsPng(document.getElementsByClassName('st-base')[0], 'diff-' + bin + '-' + lib + '.png', { scale: 5 });
        };

    }
})();
