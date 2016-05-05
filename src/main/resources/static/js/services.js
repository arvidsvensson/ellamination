'use strict';

angular.module('ctdhl.services', []).
  value('version', '0.1').
  
  service('audio', [function($http) {
	  var audioOk = document.createElement('audio');
	  audioOk.src = '/sound/beep-08b.mp3';
	  
	  var audioFail = document.createElement('audio');
	  audioFail.src = '/sound/button-10.mp3';

	  var audioOther = document.createElement('audio');
	  audioOther.src = '/sound/blop.mp3';

	  this.ok = function() {
	    audioOk.play();
	  };

	  this.fail = function() {
		  audioFail.play();
	  };

	  this.other = function() {
		  audioOther.play();
	  };

	  var stop = function() {
	    audioOk.load();
	    audioFail.load();
	    audioOther.load();
	  };
	  
	  audioOk.addEventListener('ended', stop);
	  audioFail.addEventListener('ended', stop);
	  audioOther.addEventListener('ended', stop);
  }]).
  
  service('ct', ['$http', function($http) {
	  this.drop = function(callback) {
		  $http.get('/api/drop').
		  	success(callback.success).
		  	error(callback.error);
	  };
	  	  	  
	  this.getDocs = function(callback) {
		  $http.get('/api/docs').
		  	success(callback.success).
		  	error(callback.error);
	  };
	  
	  this.getRoutes = function(callback) {
		  $http.get('/api/routes').
		  	success(callback.success).
		  	error(callback.error);
	  };
	  
	  this.getRoute = function(id, callback) {
		  $http.get('/api/route/' + id).
		  	success(callback.success).
		  	error(callback.error);
	  };
	  	  
	  this.addRoute = function(name, callback) {
		  $http.get('/api/route/add/' + name).
		  	success(callback.success).
		  	error(callback.error);
	  };
	  	  
	  this.deleteRoute = function(id, callback) {
		  $http.get('/api/route/delete/' + id).
		  	success(callback.success).
		  	error(callback.error);
	  };
	  	  
	  this.scanRoutePiece = function(id, first, second, callback) {
		  var path = '/api/route/piece/add/scan/' + id + '/' + first;
		  if (second) {
			  path += '/' + second;
		  }
		  
		  $http.get(path).
		  	success(callback.success).
		  	error(callback.error);
	  };

	  this.deleteRoutePiece = function(id, piece, callback) {
		  console.log('DEL', id, piece);
		  $http.get('/api/route/piece/delete/' + id + '/' + piece).
		  	success(callback.success).
		  	error(callback.error);
	  };
	  
	  this.combineRoute = function(id, callback) {
		  $http.get('/api/route/combine/' + id).
		  	success(callback.success).
		  	error(callback.error);
	  };
	  	  
	  this.getPieces = function(callback) {
		  $http.get('/api/pieces').
		  	success(callback.success).
		  	error(callback.error);
	  };
	  	  
	  this.getPiece = function(id, callback) {
		  $http.get('/api/piece/' + id).
		  	success(callback.success).
		  	error(callback.error);
	  };
	  	  
	  this.getPieces = function(callback) {
		  $http.get('/api/pieces').
		  	success(callback.success).
		  	error(callback.error);
	  };
	  
	  this.search = function(term, callback) {
		  $http.get('/api/search/' + term).
		  	success(callback.success).
		  	error(callback.error);
	  };
	  
	  this.printRoute = function(id, callback) {
		  $http.get('/api/route/print/' + id).
		  	success(callback.success).
		  	error(callback.error);
	  };
}]).
  
  service('ctups', ['$http', function($http) {
	  this.dropSettings = function(callback) {
		  $http.get('/api/ups/drop/settings').
		  	success(callback.success).
		  	error(callback.error);
	  };
	  
	  this.getDocs = function(callback) {
		  $http.get('/api/ups/docs').
		  	success(callback.success).
		  	error(callback.error);
	  };
	  
	  this.getSettings = function(callback) {
		  $http.get('/api/ups/settings').
		  	success(callback.success).
		  	error(callback.error);
	  };
	  
	  this.setSettings = function(settings, callback) {
		  $http.get('/api/ups/settings/' + settings.startRow + '/' + settings.waybillColumn + '/' + settings.nameColumn + '/' + settings.addressColumn).
		  	success(callback.success).
		  	error(callback.error);
	  };
	  
	  this.drop = function(callback) {
		  $http.get('/api/ups/drop').
		  	success(callback.success).
		  	error(callback.error);
	  };
	  
	  this.getPieces = function(callback) {
		  $http.get('/api/ups/pieces').
		  	success(callback.success).
		  	error(callback.error);
	  };
	  	  	  
	  this.getRoutes = function(callback) {
		  $http.get('/api/ups/routes').
		  	success(callback.success).
		  	error(callback.error);
	  };
	  
	  this.getRoute = function(id, callback) {
		  $http.get('/api/ups/route/' + id).
		  	success(callback.success).
		  	error(callback.error);
	  };
	  	  
	  this.addRoute = function(name, callback) {
		  $http.get('/api/ups/route/add/' + name).
		  	success(callback.success).
		  	error(callback.error);
	  };
	  	  
	  this.deleteRoute = function(id, callback) {
		  $http.get('/api/ups/route/delete/' + id).
		  	success(callback.success).
		  	error(callback.error);
	  };
	  
	  this.addDrop = function(id, scan, line1, line2, callback) {
		  console.log('add scan', id, scan, line1, line2);
		  $http.get('/api/ups/route/piece/add/scan/' + id + '/' + scan + '/' + line1 + '/' + line2).
		  	success(callback.success).
		  	error(callback.error);
	  };
	  
	  this.deleteDrop = function(id, scan, callback) {
		  console.log('DEL', id, scan);
		  $http.get('/api/ups/route/piece/delete/' + id + '/' + scan).
		  	success(callback.success).
		  	error(callback.error);
	  };

	  	  	  	  
	  this.printRoute = function(id, callback) {
		  $http.get('/api/ups/route/print/' + id).
		  	success(callback.success).
		  	error(callback.error);
	  };
  }]);
