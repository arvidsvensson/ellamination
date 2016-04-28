'use strict';

/* Filters */

angular.module('ctdhl.filters', []).
  filter('reverse', [function() {
	  return function(items) {
	    return items.slice().reverse();
	  }
  }]).
  filter('dashZero', [function() {
	  return function(value) {
	    return value == 0 ? '-' : value;
	  }
  }]);
