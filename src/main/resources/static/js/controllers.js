'use strict';

/* Controllers */

angular.module('ctdhl.controllers', []).
  controller('MainCtrl', ['$scope', '$window', 'ct', function($scope, $window, ct) {
	$scope.search = function(searchTerm) {
		ct.search(searchTerm, {
			success: function(results) {
				$scope.results = results;
				$('#searchModal').modal('show');
			}
		});
	}
  }]).
  
  controller('DashboardCtrl', ['$scope', 'ct', 'ctups', function($scope, ct, ctups) {
	  $scope.drop = function() {
		  ct.drop({success: function(data) {
			  $scope.docs = [];
			  $scope.routes = [];
			  $scope.pieces = [];
		  }});
		  ctups.drop({success: function(data) {
			  $scope.routesups = [];
		  }});
	  }
	  $scope.dropSettings = function() {
		  ctups.dropSettings({success: function(data) {
			  ctups.getSettings({
				 success: function(data) {
					$scope.settings = data; 
				 } 
			  });
		  }});
	  }
	  
	  $scope.setSettings = function(settings) {
		  ctups.setSettings(settings, {
			  success: function(data) {
				  console.log('set', data);
			  }
		  });
	  }
	  
	  ctups.getSettings({
		 success: function(data) {
			$scope.settings = data; 
		 } 
	  });
	  	  
	  ct.getDocs({
		  success: function(data) {
			  $scope.docs = data;
		  }});
	  
	  ct.getRoutes({
		  success: function(data) {
			  $scope.routes = data;
		  }});
	  	  
	  ct.getPieces({
		  success: function(data) {
			  $scope.pieces = data;
		  }});
	  
	  ctups.getDocs({
		  success: function(data) {
			  $scope.upsDocs = data;
		  }});
	  
	  ctups.getPieces({
		  success: function(data) {
			  $scope.upsPieces = data;
		  }});
	  
	  ctups.getRoutes({
		  success: function(data) {
			  $scope.routesups = data;
		  }});
  }]).
  
  controller('FilesCtrl', ['$scope', '$upload', 'ct', 'ctups', function($scope, $upload, ct, ctups) {
	  var updateDHL = function() {
		  ct.getDocs({success: function(data) {
			  $scope.docs = data;
		  }});
	  };
	  var updateUPS = function() {
		  ctups.getDocs({success: function(data) {
			  $scope.upsDocs = data;
		  }});
	  };
	  updateDHL();
	  updateUPS();
	  
	  $scope.onFileSelectDHL = function($files) {
	    for (var i = 0; i < $files.length; i++) {
	      var file = $files[i];
	      $scope.upload = $upload.upload({
	        url: '/api/upload',
	        file: file,
	      }).success(function(data, status, headers, config) {
	    	updateDHL();
	      });
	    }
	  };

	  $scope.onFileSelectUPS = function($files) {
		    for (var i = 0; i < $files.length; i++) {
		      var file = $files[i];
		      $scope.uploadUPS = $upload.upload({
		        url: '/api/ups/upload',
		        file: file,
		      }).success(function(data, status, headers, config) {
		    	updateUPS();
		      });
		    }
		  };

  }]).
  
  controller('StatsCtrl', ['$scope', 'ct', function($scope, ct) {
	  $scope.chartType = 'bar';
	  
	  $scope.data = {
				series: ['Listade', 'Slingade', 'Saknas'],
				data : [{
					x : "120905",
					y: [400, 300, 11],
					tooltip:"this is tooltip"
				},
				{
					x : "120906",
					y: [300, 100, 10]
				},
				{
					x : "120907",
					y: [300, 100, 10]
				},
				{
					x : "120908",
					y: [300, 100, 10]
				},
				{
					x : "120909",
					y: [300, 100, 10]
				},
				{
					x : "120910",
					y: [300, 100, 10]
				},
				{
					x : "120911",
					y: [300, 100, 10]
				},
				{
					x : "120912",
					y: [350, 200, 23]
				}]     
			}
	  $scope.config = {
			labels: true,
			title : "DHL",
			legend : {
				display:true,
				position:'right'
			}
		}
  }]).

  controller('RoutesCtrl', ['$scope', 'ct', 'audio', function($scope, ct, audio) {
	  var getRoutes = function() {
		  ct.getRoutes({success: function(routes) {
			  console.log('last ' + $scope.lastRoute);
			  $scope.routes = routes;
			  
			  if ($scope.lastRoute == undefined && $scope.routes.length > 0) {
				  $scope.lastRoute = $scope.routes[$scope.routes.length - 1];
			  }
		  }});
	  };
	  
	  getRoutes();
	  
	  $scope.isLast = function(route) {
		  return route == $scope.lastRoute;
	  }
	  
	  $scope.createRoute = function(name) {
		  ct.addRoute(name, {success: function(route) {
			  console.log('created route', route);
			  $scope.lastRoute = route;
			  getRoutes();
		  }});
	  };

	  $scope.removeRoute = function(route) {
		  $scope.lastRoute = null;
		  ct.deleteRoute(route.id, {success: function(route) {
			  getRoutes();
		  }});
	  };
	  
	  $scope.cancelSecond = function() {
		  $scope.first = null;
	  }

	  $scope.scanPiece = function (route, scan) {
		  $scope.lastRoute = route;

		  var first = $scope.first ? $scope.first : scan;
		  var second = $scope.first ? scan : null;
		  
		  if (first == second) {
			  audio.other();  
			  return;
		  }
		  
		  ct.scanRoutePiece(route.id, first, second, {success: function(scanned) {
			  if (!scanned) {
				  $scope.first = null;
				  audio.fail();
			  } else if (scanned.result == 'OK') {
				  $scope.first = null;
				  audio.ok();
				  getRoutes();
			  } else if (scanned.result == 'NEED_SECOND') {
				  $scope.first = scanned.first;
				  audio.other();  
			  }  
		  }});
	  };
	  
	  $scope.removePiece = function(route, piece) {
		  $scope.lastRoute = route;
		  ct.deleteRoutePiece(route.id, piece.id, {success: function(data) {
			  getRoutes();
		  }});
	  };
	  
	  $scope.print = function(route) {
		  $scope.lastRoute = route;
		  ct.printRoute(route.id, {success: function(data) {
			  getRoutes();
		  }});
	  };		  
}]).
    
  controller('UPSCtrl', ['$scope', 'ctups', 'audio', function($scope, ctups, audio) {
	  var getRoutes = function() {
		  ctups.getRoutes({success: function(routes) {
			  console.log('last ' + $scope.lastRoute);
			  $scope.routes = routes;
			  
			  if ($scope.lastRoute == undefined && $scope.routes.length > 0) {
				  $scope.lastRoute = $scope.routes[$scope.routes.length - 1];
			  }
		  }});
	  };
	  
	  getRoutes();
	  
	  $scope.isLast = function(route) {
		  return route == $scope.lastRoute;
	  }
	  
	  $scope.createRoute = function(name) {
		  ctups.addRoute(name, {success: function(route) {
			  console.log('created route', route);
			  $scope.lastRoute = route;
			  getRoutes();
		  }});
	  };

	  $scope.removeRoute = function(route) {
		  $scope.lastRoute = null;
		  ctups.deleteRoute(route.id, {success: function(route) {
			  getRoutes();
		  }});
	  };

	  $scope.addDrop = function (route, scan, line1, line2) {
		  line1 = line1 == '' ? undefined : line1;
		  line2 = line2 == '' ? undefined : line2;
		  $scope.lastRoute = route;
		  ctups.addDrop(route.id, scan, line1, line2, {success: function(scanned) {
			  if (!scanned) {
				  audio.fail();
				  return;
			  }

			  audio.ok();
			  getRoutes();
		  }, error: function(err) {
			  audio.fail();
		  }});
	  };
	  	  
	  $scope.removeDrop = function(route, scan) {
		  $scope.lastRoute = route;
		  ctups.deleteDrop(route.id, scan, {success: function(data) {
			  getRoutes();
		  }});
	  };
	  
	  $scope.print = function(route) {
		  $scope.lastRoute = route;
		  ctups.printRoute(route.id, {success: function(data) {
			  getRoutes();
		  }});
	  };
  }]);