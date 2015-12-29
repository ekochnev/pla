(function (angular){
    "use strict";
    var app=angular.module('searchSbcm', ['common', 'ngRoute', 'mgcrea.ngStrap.select', 'mgcrea.ngStrap.alert', 'mgcrea.ngStrap.popover',
        'directives', 'mgcrea.ngStrap.dropdown', 'ngSanitize', 'commonServices','ui.bootstrap.modal','ngMessages']);

    app.config(["$routeProvider", function ($routeProvider) {
        $routeProvider.when('/', {
                templateUrl: 'searchsbcm.html',
                controller: 'searchSbcmController',
                resolve: {
                         allSBCM : ['$q', '$http', function ($q, $http) {
                                            var deferred = $q.defer();
                                            $http.get('/pla/core/sbcm/getAllSBCM').success(function (response, status, headers, config) {
                                                deferred.resolve(response)
                                            }).error(function (response, status, headers, config) {
                                                deferred.reject();
                                            });
                                            return deferred.promise;
                                        }]


                                    }
            }
        )}]);
    app.controller('searchSbcmController',['$scope','$http','allSBCM', function ( $scope, $http, allSBCM){
        $scope.updateStatusCommand = {};
        $scope.allSBCM = allSBCM;
        $scope.viewsbcm = function (sbcm) {
          window.location.href = "/pla/core/sbcm/getsbcmview?serviceBenefitCoverageMappingId="+ sbcm.serviceBenefitCoverageMappingId + "&mode=view";
        };
        $scope.updatesbcm = function (sbcm) {
        sbcm.status = 'INACTIVE';
        $http({
        url: '/pla/core/sbcm/updateSBCMStatus',
                       method: 'POST',
                       data: JSON.stringify(sbcm)
                   }).success(function(response) {
                   if (response.status === "200" && response.message==='Status Updated Successfully') {
                   console.log(sbcm);
                   console.log($scope.allSBCM);
                        $scope.allSBCM.splice($scope.allSBCM.indexOf(sbcm), 1);
                    }
                   }).error(function(response) {});};

                   $scope.$watchCollection("viewsbcm", function(newValue, oldValue) {});
                   }

    ]);
})(angular);