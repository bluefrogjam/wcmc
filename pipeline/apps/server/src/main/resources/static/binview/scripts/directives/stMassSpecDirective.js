/**
 * Created by matthewmueller on 10/6/17.
 *
 * Uses SpeckTackle to display one or more mass spectra
 *
 */

(function() {
    'use strict';

    angular.module('app')
        .directive('stMassSpec', stMassSpec);

        /* @ngInject */
        function stMassSpec() {
            var directive = {
                restrict: 'A',
                replace: false,
                require: 'ngModel',
                template: '<div id="stgraph" class="stgraph" />',

                scope: {
                    model: '=ngModel',
                    normalize: '='
                },

                link: function(scope) {
                    var xMin = 0,
                        xMax = 100,
                        yMin = 0,
                        yMax = 1;

                    function processSpectrum(input, invert = false, normalize = true) {
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

                                output.peaks.push({mz: ion.mass, intensity: ion.intensity});
                            });
                        } else {
                            var tokens = input.spectrum.match(/\S+/g);

                            tokens.forEach(function(token) {
                                var tuple = token.split(':');

                                if (parseInt(tuple[0]) < xMin) { xMin = parseInt(tuple[0]); }
                                if (parseInt(tuple[0]) > xMax) { xMax = parseInt(tuple[0]); }

                                if (parseInt(tuple[1]) > maxIntensity) {
                                    maxIntensity = parseInt(tuple[1]);
                                }
                            });

                            tokens.forEach(function(token) {
                                var tuple = token.split(':');

                                var pair = {
                                    mz: tuple[0],
                                    intensity: normalize ?
                                        (invert ? -100 * tuple[1] / maxIntensity : 100 * tuple[1] / maxIntensity) :
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

                    scope.$watch('model', function(newVal, oldVal) {
                        var spectra = [];

                        switch (newVal.data.length) {
                            case 0:
                                break;
                            case 1:
                                spectra.push(processSpectrum(newVal.data[0], false, scope.normalize));
                                if (scope.normalize) {
                                    yMin = 0;
                                    yMax = 100;
                                }
                                break;
                            case 2:
                                spectra.push(processSpectrum(newVal.data[0], false, scope.normalize));
                                spectra.push(processSpectrum(newVal.data[1], true, scope.normalize));
                                if (scope.normalize) {
                                    yMin = -100;
                                    yMax = 100;
                                }
                                break;
                            default:
                                break;
                        }

                        $("#stgraph").empty();

                        var chart = st.chart
                            .ms()
                            .legend(true)
                            .xlabel("m/z")
                            .ylabel("Abundance")
                            .labels(true);

                        chart.render("#stgraph");

                        var handle = st.data
                            .set()
                            .xlimits([xMin, xMax])
                            .ylimits([yMin, yMax])
                            .title("spectrumId")
                            .x("peaks.mz")
                            .y("peaks.intensity");

                        // bind the data handler to the chart
                        chart.load(handle);
                        // load the spectrum and annotations for Uridine
                        handle.add(spectra);
                    }, true);

                }
            };

            return directive;
        }
})();
