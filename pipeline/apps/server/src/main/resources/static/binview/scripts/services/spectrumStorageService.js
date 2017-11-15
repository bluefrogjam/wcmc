'use strict';

app.service('SpectrumStorage', function() {
    var spectra = [];

    function getSpectra() {
        return spectra;
    }

    function addSpectrum(spectrum) {
        spectra.push(spectrum);
    }

    function removeLastSpectrum() {
        spectra.pop();
    }

    function clearSpectra {
        spectra = [];
    }

    return {
        getSpectra: getSpectra,
        addSpectrum: addSpectrum,
        removeLastSpectrum: removeLastSpectrum,
        clearSpectra: clearSpectra
    };
});