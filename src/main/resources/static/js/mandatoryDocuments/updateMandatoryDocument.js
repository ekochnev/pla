var App = angular.module('updateMandatoryDocument', ['common','commonServices','ngRoute','ui.bootstrap','ngSanitize','mgcrea.ngStrap.select','mgcrea.ngStrap','mgcrea.ngStrap.alert']);

App.controller('UpdateMandatoryDocumentsController',['$scope','$http','$rootScope','$window','$location','$alert',function($scope,$http,$rootScope,$window,$location,$alert){

          $scope.showOptionalCoverage=false;
          $scope.boolVal=false;
          $scope.url = window.location.search.split('=')[1];
          $http.get('/pla/core/master/getdocument').success(function(data){
            $scope.mandatoryDocList=data;
          });

        $http.get('/pla/core/mandatorydocument/getmandatorydocumentdetail/'+$scope.url).success(function(data){

                  $scope.updateMandatoryDocument=data[0];

                  if($scope.updateMandatoryDocument.coverageId){

                       $scope.showOptionalCoverage=true;
                  }

          });
    $scope.$watch('updateMandatoryDocument.document',function(newValue, oldValue){
        if(newValue) {
            if (newValue.length > 0) {

                $scope.boolVal = true;
            } else {
                $scope.boolVal = false;
            }
        } else {
            $scope.boolVal = false;
        }
    });

         $scope.updateMandatoryDoc = function(){
            //console.log($scope.updateMandatoryDocument);
             $scope.updateMandatoryDocument.documents=$scope.updateMandatoryDocument.document;
            $http.post('/pla/core/mandatorydocument/update',$scope.updateMandatoryDocument).success(function(data){
                  if(data.status==200){
                     //$scope.alert = {title:'Success Message! ', content:data.message, type: 'success'};
                      $window.location.href="/pla/core/mandatorydocument/view";
                   //  $scope.reset();
                  }else{
                   //   $scope.alert = {title:'Error Message! ', content:data.message, type: 'danger'};
                  }
            });

         }
         $scope.reset = function(){
            $scope.updateMandatoryDocument.definedFor='';
           // $scope.showPlan=false;
            $scope.showOptionalCoverage=false;
            $scope.updateMandatoryDocument.process='';
            $scope.updateMandatoryDocument.multiSelectList='';
         }

}]);
