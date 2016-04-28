'use strict';

/* Directives */


angular.module('ctdhl.directives', [])
  .directive('appVersion', ['version', function(version) {
    return function(scope, elm, attrs) {
      elm.text(version);
    };
  }])
  
  .directive('blop', [function() {
    return function(scope, elm, attrs) {
      elm.text('');
    };
  }])
  
	.directive('toFocus', function ($timeout) {
	    return function (scope, elem, attrs) {
	        scope.$watch(attrs.toFocus, function (newval) {
                $timeout(function () {
                    elem[0].focus();
                }, 1000, false);
            });
	    };
});

