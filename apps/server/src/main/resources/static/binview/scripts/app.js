(function() {
    'use strict';

    angular.module('app', ['app.similarity', 'ngAnimate', 'ui.bootstrap', 'bsLoadingOverlay', 'ngRoute'])
        .run(function(bsLoadingOverlayService) {
            bsLoadingOverlayService.setGlobalConfig({
                templateUrl: 'binview/views/loadingOverlayTemplate.html'
            });
        });
})();
