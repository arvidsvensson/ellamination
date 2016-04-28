'use strict';


// Declare app level module which depends on filters, and services
angular.module('ctdhl', [
  'ngRoute',
  'ngResource',
  'ctdhl.filters',
  'ctdhl.services',
  'ctdhl.directives',
  'ctdhl.controllers',
  'angularFileUpload',
  'ui.bootstrap',
  'angularCharts'
]).
config(['$routeProvider', function($routeProvider) {
  $routeProvider.when('/', {templateUrl: '/partials/partial_home.html'});
  $routeProvider.when('/files', {templateUrl: '/partials/partial_files.html', controller: 'FilesCtrl'});
  $routeProvider.when('/routes', {templateUrl: '/partials/partial_routes.html', controller: 'RoutesCtrl'});
  $routeProvider.when('/routesups', {templateUrl: '/partials/partial_routesups.html', controller: 'UPSCtrl'});
  $routeProvider.when('/dashboard', {templateUrl: '/partials/partial_dashboard.html', controller: 'DashboardCtrl'});
  $routeProvider.when('/tech', {templateUrl: '/partials/partial_tech.html'});
  $routeProvider.when('/messlife', {templateUrl: '/partials/partial_messlife.html'});
  $routeProvider.when('/stats', {templateUrl: '/partials/partial_stats.html', controller: 'StatsCtrl'});
  $routeProvider.otherwise({redirectTo: '/'});
}]);
