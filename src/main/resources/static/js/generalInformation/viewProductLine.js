var App = angular.module('viewProductLine', ['common','ngRoute','ui.bootstrap','ngSanitize','mgcrea.ngStrap.select','mgcrea.ngStrap','mgcrea.ngStrap.alert']);

App.controller('SelectViewController',['$rootScope','$scope','$http','$templateCache','$timeout','$alert','$window','$location',function($rootScope,$scope,$http,$templateCache,$timeout,$alert,$window,$location) {
   // console.log("******************************SELECT VIEW CONTROLLER CALLED*********************************************")
    $scope.selectUpdate=true;

    $scope.$watch('selectedOption.productLine', function (newValue, oldValue) {
        if (newValue) {

            if(newValue=="groupHealth"){
                $rootScope.PanelName ="groupHealth";

                $scope.selectUpdate=false;

            }else if(newValue=="groupLife"){
                $rootScope.PanelName ="groupLife";
                $scope.selectUpdate=false;
            }else if(newValue=="individualLife"){
                $rootScope.PanelName ="individualLife";
                $scope.selectUpdate=false;
            }

        }

    });


}]);
App.controller('ViewGroupHealthLevelInformationController',['$rootScope','$scope','$http','$templateCache','$timeout','$alert','$window','$location',function($rootScope,$scope,$http,$templateCache,$timeout,$alert,$window,$location) {
    $scope.groupHealth=false;
    $rootScope.$watch('PanelName',function(n,o){
        if(n=='groupHealth'){
            $scope.groupHealth=true;
        }
    });

    $http.get('/pla/core/productlineinformation/getproductlineinformation').success(function(data){
        $scope.groupHealthData=data;
        var productLineName="GROUP_HEALTH";
        $scope.items =_.findWhere($scope.groupHealthData,{productLine:productLineName});
    });
    $scope.reloadPage = function(){
        $window.location.href="/pla/core/productlineinformation/openview";

    }



}]);
App.controller('ViewGroupLifeLevelInformationController',['$rootScope','$scope','$http','$templateCache','$timeout','$alert','$window','$location',function($rootScope,$scope,$http,$templateCache,$timeout,$alert,$window,$location) {

    $scope.groupLife=false;
    $rootScope.$watch('PanelName',function(n,o){
        if(n=='groupLife'){
            $scope.groupLife=true;
        }
    });
    $http.get('/pla/core/productlineinformation/getproductlineinformation').success(function(data){
        $scope.groupLifeData=data;
        var productLineName="GROUP_LIFE";
        $scope.items =_.findWhere($scope.groupLifeData,{productLine:productLineName});
    });
    $scope.reloadPage = function(){
        $window.location.href="/pla/core/productlineinformation/openview";

    }


}]);
App.controller('ViewIndividualLifeLevelInformationController',['$rootScope','$scope','$http','$templateCache','$timeout','$alert','$window','$location',function($rootScope,$scope,$http,$templateCache,$timeout,$alert,$window,$location) {

    $scope.individualLife=false;
    $rootScope.$watch('PanelName',function(n,o){
        if(n=='individualLife'){
            $scope.individualLife=true;
        }
    });

    $http.get('/pla/core/productlineinformation/getproductlineinformation').success(function(data){
        $scope.individualLifeData=data;
        var productLineName="INDIVIDUAL_LIFE";
        $scope.items =_.findWhere($scope.individualLifeData,{productLine:productLineName});
    });
    $scope.reloadPage = function(){
        $window.location.href="/pla/core/productlineinformation/openview";

    }

}]);