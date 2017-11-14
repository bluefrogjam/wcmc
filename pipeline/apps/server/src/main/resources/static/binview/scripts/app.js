'use strict';

var app = angular.module('app', ['ngAnimate', 'ui.bootstrap', 'bsLoadingOverlay'])
    .run(function(bsLoadingOverlayService) {
        bsLoadingOverlayService.setGlobalConfig({
            templateUrl: 'binview/views/loadingOverlayTemplate.html'
        });
    });
