
angular.module('createAgent',['common','ngRoute','mgcrea.ngStrap.select','mgcrea.ngStrap.alert','commonServices'])
    .controller('agentCtrl',['$scope','$http','channelType','authorisedToSell','teamDetails','provinces','$timeout','$alert','$route','$window','transformJson','getQueryParameter','agentDetails','globalConstants','nextAgentSequence','getProvinceAndCityDetail',

        function($scope,$http,channelType,authorisedToSell,teamDetails,provinces,$timeout,$alert,$route,$window,transformJson,getQueryParameter,agentDetails,globalConstants,nextAgentSequence,getProvinceAndCityDetail){
            $scope.numberPattern =globalConstants.numberPattern;

            /*Wizard initial step*/
            $scope.selectedWizard = 1;
            $scope.search = {};
            $scope.searchResult = {
                isEmpty:false,
                isSearched:false
            };
            $scope.agentDetails={
                teamDetail:{"teamId":"","teamName":"","teamCode":"","currentTeamLeader":"","firstName":"","lastName":"","fromDate":""
                    ,"endDate":"","branchCode":"","regionCode":""}
            };
            $scope.isFormSubmitted =  false;
            /*Initially hide the alert window
             * which will be shown only when the search result is empty
             * */
            $scope.hideAlert=true;
            $scope.isEditMode =  false;
            /*The values that will be shown in the ui for each step
             * Search step will hold value 1 and 2,3,4 for agent team and contact
             * */
            $scope.stepValues = {
                agent:2,
                team:3,
                contact:4
            };
            $scope.searchedValue=false;
            $scope.$watch('searchedValue',function(newVal,oldVal){

                if(newVal){
                    $scope.editContactDetails();
                    $scope.searchedValue=false;
                }
            });
            $scope.empDetails={};
            /*  CHECK WHETHER EMPLOYEE EXISTS IN HRMS */
            $scope.editContactDetails=function() {
                //   console.log($scope.search);
                if ($scope.searchedValue) {
                    $http.get("/pla/core/agent/getemployeedeatil", {params: $scope.search})
                        .success(function (data, status) {

                            $scope.empDetails = data;
                            console.log($scope.empDetails);
                            if ($scope.empDetails.employeeId) {
                                return false;
                            } else {
                                return true;
                            }
                        });
                } else if (_.size(agentDetails) != 0) {
                    if (agentDetails.agentProfile.employeeId) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }



            /*agentDetails will be empty if its a create page else it is an update
             * and agentDetails will be pre-populated
             * */
            if(_.size(agentDetails)!=0){
                $scope.agentDetails=angular.copy(agentDetails);
                //console.log(agentDetails);
                if(agentDetails.agentProfile) {
                    if (agentDetails.agentProfile.overrideCommissionApplicable) {
                        $scope.agentDetails.overrideCommissionApplicable = agentDetails.agentProfile.overrideCommissionApplicable;
                    }
                }
                /*This is used to disabled and hide some of the fields in the UI*/
                $scope.isEditMode =  true;
                $scope.trainingCompleteOn = agentDetails.agentProfile.trainingCompleteOn;
                /*during edit mode search field will be hidden and hence the step value changes*/
                $scope.stepValues = {
                    agent:1,
                    team:2,
                    contact:3
                };
                /*  Disable Channel Type IF it is broker in update*/
                $scope.disableChannelType = function(){
                    if (agentDetails) {
                        if (agentDetails.channelType.channelName=='Broker') {
                            return true;
                        } else {
                            return false;
                        }

                    }
                }



                // console.log($scope.agentDetails);
                /*remove search step in the wizard in edit mode
                 * @link directive.js
                 * */
                $scope.stepsToRemove={index:1,howMany:1};
            }
            $scope.$watch('agentDetails.teamDetail.teamId',function(newVal,oldVal){
                if(newVal){
                    $scope.prePopulateTeamLeader();
                }
            });
            $scope.$watch('agentDetails.channelType',function(n,o){
                if(n){
                    if(n.channelName=='Broker'){
                        $scope.teamDetailsForm.$valid=true;
                        $scope.teamDetailsForm.$submitted=true;

                    }
                }else{
                    $scope.agentDetails.channelType ={channelName:"Personal Selling",channelCode:"PERSONAL_SELLING"};;
                }

            });
            $scope.primaryCities = [];
            $scope.physicalCities = [];
            $scope.$watch('agentDetails.physicalAddress.physicalGeoDetail.provinceCode',function(newVal,oldVal){
                if(newVal){
                    var provinceDetails = $scope.getProvinceDetails(newVal);
                    $scope.physicalCities =  provinceDetails.cities;
                    $scope.agentDetails.physicalAddress.physicalGeoDetail.provinceName = provinceDetails.provinceName;

                }
            });

            $scope.$watch('selectedWizard',function(newVal,oldVal){
                if(newVal >1){
                    if($scope.agentDetails) {
                        if ($scope.agentDetails.channelType) {
                            if ($scope.agentDetails.channelType.channelName == 'Broker') {
                                $scope.teamDetailsForm.$valid = true;
                                $scope.teamDetailsForm.$submitted = true;


                            }
                        }
                    }
                }
            });

            $scope.$watch('agentDetails.contactDetail.geoDetail.provinceCode',function(newVal,oldVal){
                if(newVal){
                    var provinceDetails = $scope.getProvinceDetails(newVal);
                    $scope.primaryCities = provinceDetails.cities;
                    $scope.agentDetails.contactDetail.geoDetail.provinceName = provinceDetails.provinceName;
                }
            });
            $scope.getProvinceDetails=function(provinceCode){
                return getProvinceAndCityDetail(provinces,provinceCode);
            };

            if(!$scope.isEditMode){
                $scope.agentDetails={ agentProfile :{designationDto:{description:"Agent",code:"AGENT"}}};
            }
            $scope.teamCodeStatus = {
                isRequired : true,
                isDisabled : false
            };

            $scope.isSearchDisabled =  function(){
                var searchPattern = new RegExp("^[0-9]{6}\/[0-9]{2}\/[0-9]{1}$");
                if($scope.search && $scope.search.nrcNumber && !searchPattern.test($scope.search.nrcNumber)){
                    return true;
                }
                if($scope.search && ((!$scope.search.nrcNumber && $scope.search.employeeId) || ($scope.search.nrcNumber && !$scope.search.employeeId))){
                    return false;
                }
                return true;
            };
            $scope.channelTypes = {channelName:"Personal Selling",channelCode:"PERSONAL_SELLING"};
            $scope.authorisedToSell = authorisedToSell;
            $scope.teamDetails= teamDetails;
            $scope.provinces=provinces;
            $scope.today = new Date();
            $scope.datePickerSettings = {
                isOpened:false,
                dateOptions:{
                    formatYear: 'yyyy',
                    startingDay: 1
                }
            };
            $scope.open = function($event) {
                $event.preventDefault();
                $event.stopPropagation();
                $scope.datePickerSettings.isOpened = true;
            };

            /*wizard will be set to active state on the step we provide */
            $scope.jumpToNthStep = function(step){
                $scope.selectedWizard=step;
                $scope.hideAlert=true;
            };
            $scope.searchAgent =  function(){
                $scope.searchedValue=true;
                $scope.searchResult.isSearched=true;
                $http.get("/pla/core/agent/getemployeedeatil",{params:$scope.search})
                    .success(function(response,status){
                        var data = response.data;
                      //make alreadyExists as true when the Agent is already existed in PLA DB with the nrc number.
                        if(response.status==500 && response.message!=null){
                            $scope.alreadyExists=true;
                            $scope.serverErrorMsg=response.message;
                            return;
                        }
                        if(data && (_.size(data) ==0 || data.firstName==null)){
                            $scope.hideAlert=false;
                            $scope.searchResult.isEmpty=true;
                            $scope.agentDetails.agentProfile.nrcNumberInString=$scope.search.nrcNumber;
                        }else{
                            $scope.jumpToNthStep(2);
                            $scope.agentDetails = transformJson.fromHrmsToPla(data);
                            //console.log($scope.agentDetails);
                            $scope.searchResult.isEmpty=false;
                            $scope.trainingCompleteOn = $scope.agentDetails.trainingCompleteOn;
                        }
                        if(nextAgentSequence){
                            $scope.agentDetails.agentId = nextAgentSequence;
                        }
                    })
                    .error(function(data,status){
                        $scope.hideAlert=false;
                        $scope.searchResult.isEmpty=true;
                        $scope.agentDetails.agentProfile.nrcNumberInString=$scope.search.nrcNumber;
                        if(nextAgentSequence){
                            $scope.agentDetails.agentId = nextAgentSequence;
                        }
                    });

            };
            $scope.isSearchEmptyOrIsEdit = function(){
                return !$scope.searchResult.isEmpty || $scope.isEditMode;
            };
            $scope.prePopulateTeamLeader = function(){
                var teamDetails = _.findWhere($scope.teamDetails, {teamId:$scope.agentDetails.teamDetail.teamId});
                $scope.teamLeaderName = teamDetails.firstName+' '+teamDetails.lastName;
                $scope.branchName =teamDetails.branchName;
                $scope.regionName = teamDetails.regionName;


            };
            $scope.cancel = function(){
                $window.location.href = "listagent"
            };
            $scope.update =  function(){
                $scope.isFormSubmitted = true;
                console.log($scope.agentDetails);
                if($scope.agentDetailsForm.$valid && $scope.teamDetailsForm.$valid && $scope.contactDetailsForm.$valid){
                    $http.post('/pla/core/agent/update',transformJson.createCompatibleJson(angular.copy($scope.agentDetails),$scope.physicalCities,$scope.primaryCities,$scope.trainingCompleteOn,true))
                        .success(function(response, status, headers, config){
                            if(response.status=="200"){
                                $scope.contactDetailsForm.$submitted=true;
                            }
                        })
                        .error(function(response, status, headers, config){
                        });
                }
            };
            $scope.submit = function(){
                $scope.isFormSubmitted = true;
                if ($scope.agentDetails.channelType.channelName == 'Broker') {
                    $scope.teamDetailsForm.$valid = true;
                    $scope.teamDetailsForm.$submitted = true;
                }
                if($scope.agentDetailsForm.$valid && $scope.teamDetailsForm.$valid  && $scope.contactDetailsForm.$valid){
                    $http.post('/pla/core/agent/create',transformJson.createCompatibleJson(angular.copy($scope.agentDetails),$scope.physicalCities,$scope.primaryCities,$scope.trainingCompleteOn,false))
                        .success(function(response, status, headers, config){
                            if(response.status=="200"){
                                $scope.contactDetailsForm.$submitted=true;
                            }
                        })
                        .error(function(response, status, headers, config){
                        });
                }
            };



        }])
    .factory('transformJson',['formatJSDateToDDMMYYYY',function(formatJSDateToDDMMYYYY){
        var transformService = {};
        transformService.createCompatibleJson = function (agentDetails,physicalCities,primaryCities,trainingCompleteOn,isUpdate) {
            agentDetails.physicalAddress.physicalGeoDetail.cityName = _.findWhere(physicalCities,{geoId:agentDetails.physicalAddress.physicalGeoDetail.cityCode}).geoName;
            agentDetails.contactDetail.geoDetail.cityName =_.findWhere(primaryCities,{geoId:agentDetails.contactDetail.geoDetail.cityCode}).geoName;
            if(!isUpdate){
                agentDetails.agentProfile.trainingCompleteOn = formatJSDateToDDMMYYYY(trainingCompleteOn);
                delete agentDetails.agentStatus;
            }
            if(agentDetails.teamDetail){
                if(agentDetails.teamDetail.regionName) {
                    delete agentDetails.teamDetail.regionName;
                    delete agentDetails.teamDetail.branchName;
                }
            }

            if (!agentDetails.licenseNumber || _.size(agentDetails.licenseNumber.licenseNumber) == 0) {
                delete agentDetails.licenseNumber;
            }
            return agentDetails;


        };
        transformService.toPlanIdPlanNameObject = function(authorisedToSell){
            var plans = [];
            angular.forEach(authorisedToSell,function(plan,key){
                this.push({planId:plan.planId,planName:plan.planDetail.planName});
            },plans);
            return plans;
        };
        transformService.fromHrmsToPla = function(agentDetails){
            return {
                "agentProfile": {
                    "title": agentDetails.title,
                    "firstName": agentDetails.firstName,
                    "lastName": agentDetails.lastName,
                    "nrcNumberInString": agentDetails.nrcNumber,
                    "employeeId": agentDetails.employeeId,
                    "designationDto": {
                        "code": agentDetails.designation,
                        "description": agentDetails.designationDescription
                    }
                },
                "licenseNumber": {"licenseNumber": agentDetails.licenseNumber},
                "teamDetail": agentDetails.teamDetail,
                "contactDetail": {
                    "mobileNumber": agentDetails.primaryContactDetail.mobileNumber,
                    "homePhoneNumber": agentDetails.primaryContactDetail.homePhoneNumber,
                    "workPhoneNumber": agentDetails.primaryContactDetail.workPhoneNumber,
                    "emailAddress": agentDetails.primaryContactDetail.email,
                    "addressLine1": agentDetails.primaryContactDetail.addressLine1,
                    "addressLine2": agentDetails.primaryContactDetail.addressLine2,
                    "geoDetail": {
                        "provinceName":"",
                        "postalCode": agentDetails.primaryContactDetail.postalCode,
                        "cityName": "",
                        "provinceCode": agentDetails.primaryContactDetail.province,
                        "cityCode":agentDetails.primaryContactDetail.city
                    }
                },
                "physicalAddress": {
                    "physicalAddressLine1": agentDetails.physicalContactDetail.addressLine1,
                    "physicalAddressLine2": agentDetails.physicalContactDetail.addressLine2,
                    "physicalGeoDetail": {
                        "provinceName":"",
                        "postalCode": agentDetails.physicalContactDetail.postalCode,
                        "cityName": "",
                        "provinceCode": agentDetails.physicalContactDetail.province,
                        "cityCode":agentDetails.physicalContactDetail.city
                    }
                },
                "authorizePlansToSell": agentDetails.authorizePlansToSell,
                "overrideCommissionApplicable": agentDetails.overrideCommissionApplicable,
                "channelType": agentDetails.channelType,
                "agentId": agentDetails.agentId
            }
        };
        return transformService;
    }])
    .config(["$routeProvider",function($routeProvider){
        $routeProvider.when('/', {
            templateUrl: 'createAgentTpl.html',
            controller: 'agentCtrl',
            resolve: {
                nextAgentSequence:['$q','$http','$window',function($q,$http,$window){
                    if($window.location.href.indexOf("/openeditpage")>-1){
                        return null;
                    }else{
                        var deferred = $q.defer();
                        $http.get('/pla/core/agent/getagentid').success(function (response, status, headers, config) {
                            deferred.resolve(response)
                        }).error(function (response, status, headers, config) {
                            deferred.reject();
                        });
                        return deferred.promise;
                    }
                }],
                agentDetails:['$q','$http','getQueryParameter','$window',function($q,$http,getQueryParameter,$window){
                    if($window.location.href.indexOf("/openeditpage")>-1){
                        var queryParam = {'agentId':getQueryParameter('agentId')};
                        var deferred = $q.defer();
                        $http.get('/pla/core/agent/agentdetail',{params:queryParam}).success(function (response, status, headers, config) {
                            deferred.resolve(response)
                        }).error(function (response, status, headers, config) {
                            deferred.reject();
                        });
                        return deferred.promise;
                    }else{
                        return [];
                    }
                }],
                channelType:['$q', '$http', function ($q, $http) {
                    var deferred = $q.defer();
                    $http.get('/pla/core/master/getchannelType').success(function (response, status, headers, config) {
                        deferred.resolve(response)
                    }).error(function (response, status, headers, config) {
                        deferred.reject();
                    });
                    return deferred.promise;
                }],
                authorisedToSell:['$q', '$http','transformJson', function ($q, $http,transformJson) {
                    var deferred = $q.defer();
                    $http.get('/pla/core/plan/getallplan').success(function (response, status, headers, config) {
                        deferred.resolve(transformJson.toPlanIdPlanNameObject(response))
                    }).error(function (response, status, headers, config) {
                        deferred.reject();
                    });
                    return deferred.promise;
                }],
                teamDetails:['$q', '$http', function ($q, $http) {
                    var deferred = $q.defer();
                    $http.get('/pla/core/agent/getteams').success(function (response, status, headers, config) {
                        deferred.resolve(response)
                    }).error(function (response, status, headers, config) {
                        deferred.reject();
                    });
                    return deferred.promise;
                }],
                provinces:['$q', '$http', function ($q, $http) {
                    var deferred = $q.defer();
                    $http.get('/pla/core/master/getgeodetail').success(function (response, status, headers, config) {
                        deferred.resolve(response)
                    }).error(function (response, status, headers, config) {
                        deferred.reject();
                    });
                    return deferred.promise;
                }]
            }
        })
    }])




