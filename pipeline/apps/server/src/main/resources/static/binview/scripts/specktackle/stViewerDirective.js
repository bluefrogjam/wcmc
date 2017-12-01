/**
 * Created by matthewmueller on 10/6/17.
 *
 * SpeckTackle spectrum viewer
 *
 */

(function() {
    'use strict';

    angular.module('app')
        .directive('stViewer', stViewer);

        /* @ngInject */
        function stViewer() {
            var directive = {
                restrict: 'A',
                replace: false,
                template: '<div id="stgraph-{{::$id}}" class="stgraph" />',

                scope: {
                    spectra: '=',
                    options: '=?'
                },

                link: function(scope) {
                    var xMin = 80,
                        xMax = 100,
                        yMin = 0,
                        yMax = 1,
                        normalize = true;

                    function processSpectrum(input, invert) {
                        var maxIntensity = 0;

                        var output = {
                            spectrumId: input.spectrumId,
                            peaks: []
                        };

                        if (input.spectrum.ions) {
                            input.spectrum.ions.forEach(function(ion) {
                                if (ion.mass < xMin) {
                                    xMin = ion.mass;
                                }

                                if (ion.mass > xMax) {
                                    xMax = ion.mass;
                                }

                                if (ion.intensity > maxIntensity) {
                                    maxIntensity = ion.intensity;
                                }

                                output.peaks.push({ mz: ion.mass, intensity: ion.intensity });
                            });
                        } else {
                            var tokens = input.spectrum.match(/\S+/g);

                            tokens.forEach(function(token) {
                                var tuple = token.split(':');

                                if (parseInt(tuple[0]) < xMin) {
                                    xMin = parseInt(tuple[0]);
                                }

                                if (parseInt(tuple[0]) > xMax) {
                                    xMax = parseInt(tuple[0]);
                                }

                                if (parseInt(tuple[1]) > maxIntensity) {
                                    maxIntensity = parseInt(tuple[1]);
                                }
                            });

                            tokens.forEach(function(token) {
                                var tuple = token.split(':');

                                var pair = {
                                    mz: tuple[0],
                                    intensity: normalize ?
                                        (invert ?
                                            -100 * tuple[1] / maxIntensity :
                                            100 * tuple[1] / maxIntensity) :
                                        tuple[1]
                                }

                                output.peaks.push(pair);
                            });

                            if (normalize === false) {
                                if (!invert && maxIntensity > yMax) {
                                    yMax = maxIntensity;
                                } else if (invert && -1 * maxIntensity < yMin) {
                                    yMin = -1 * maxIntensity;
                                }
                            }
                        }

                        return output;
                    }

                    scope.$watch('spectra', function(newVal, oldVal) {
                        var spectra = [];

                        if (scope.options && typeof scope.options.normalize !== 'undefined') {
                            normalize = scope.options.normalize;
                        }

                        xMin = 80;
                        xMax = 100;

                        switch (newVal.length) {
                            case 0:
                                break;
                            case 1:
                                spectra.push(processSpectrum(newVal[0], false));
                                if (normalize) {
                                    yMin = 0;
                                    yMax = 100;
                                }
                                break;
                            case 2:
                                spectra.push(processSpectrum(newVal[0], false));
                                spectra.push(processSpectrum(newVal[1], true));
                                if (normalize) {
                                    yMin = -100;
                                    yMax = 100;
                                }
                                break;
                            default:
                                newVal.forEach(function(val) {
                                    spectra.push(processSpectrum(val, false));
                                });
                                if (normalize) {
                                    yMin = 0;
                                    yMax = 100;
                                }
                                break;
                        }

                        $("#stgraph-" + scope.$id).empty();

                        var chart = st.chart
                            .ms()
                            .xlabel("m/z")
                            .ylabel("Abundance")
                            .legend(true)
                            .labels(true);

                        chart.render("#stgraph-" + scope.$id);

                        var handle = st.data
                            .set()
                            .xlimits([xMin, xMax])
                            .ylimits([yMin, yMax])
                            .title("spectrumId")
                            .x("peaks.mz")
                            .y("peaks.intensity");

                        chart.load(handle);
                        handle.add(spectra);
                    }, true);

                }
            };

            return directive;
        }
})();
