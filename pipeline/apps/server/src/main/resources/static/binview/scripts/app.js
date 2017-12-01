(function() {
    'use strict';

    angular.module('app', ['app.similarity', 'ngAnimate', 'ui.bootstrap', 'bsLoadingOverlay'])
        .run(function(bsLoadingOverlayService) {
            bsLoadingOverlayService.setGlobalConfig({
                templateUrl: 'binview/views/loadingOverlayTemplate.html'
            });
        });
})();