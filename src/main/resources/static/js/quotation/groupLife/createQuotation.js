angular.module('createQuotation', ['common', 'ngRoute', 'mgcrea.ngStrap.select', 'mgcrea.ngStrap.alert', 'mgcrea.ngStrap.popover', 'directives', 'angularFileUpload',
    'mgcrea.ngStrap.dropdown', 'ngSanitize', 'commonServices'])
    .controller('quotationCtrl', ['$scope', '$http', '$timeout', '$location', '$route', '$upload', 'provinces', 'industries', 'getProvinceAndCityDetail', 'globalConstants', 'agentDetails', 'stepsSaved', 'proposerDetails',
        'quotationNumber', 'getQueryParameter', '$window', 'checkIfInsuredUploaded', 'premiumData',
        function ($scope, $http, $timeout, $location, $route, $upload, provinces, industries, getProvinceAndCityDetail, globalConstants, agentDetails, stepsSaved, proposerDetails, quotationNumber, getQueryParameter,
                  $window, checkIfInsuredUploaded, premiumData) {
            var mode = getQueryParameter("mode");
            $scope.mode = mode;
            $scope.qId = null;
            if (mode == 'view') {
                $scope.isViewMode = true;
                $scope.isEditMode = true;
            } else if (mode == 'edit') {
                $scope.isEditMode = true;
            }

            $scope.industries = industries;

            $scope.premiumData = premiumData;

            $scope.showDownload = true;
            /*This scope holds the list of installments from which user can select one */
            $scope.numberOfInstallmentsDropDown = [];

            /*regex for number pattern for more details see commonModule.js*/
            $scope.numberPattern = globalConstants.numberPattern;

            $scope.fileSaved = null;

            /*This scope value is binded to fueluxWizard directive and hence it changes as and when next button is clicked*/
            $scope.selectedItem = 1;

            /*Holds the indicator for steps in which save button is clicked*/
            $scope.stepsSaved = stepsSaved;
            $scope.stepsSaved["3"] = checkIfInsuredUploaded;
            /*Inter id used for programmatic purpose*/
            $scope.quotationId = getQueryParameter('quotationId') || null;

            $scope.versionNumber = 0;

            /*actual quotation number to be used in the view*/
            $scope.quotationNumber = quotationNumber;

            $scope.provinces = provinces;
            // console.log($scope.premiumData);
            $scope.quotationDetails = {
                /*initialize with default values*/
                plan: {
                    samePlanForAllRelation: false,
                    samePlanForAllCategory: false
                },
                premium: $scope.premiumData
            };

            if ($scope.premiumData)
                $scope.selectedInstallment = $scope.premiumData.premiumInstallment;

            $scope.quotationDetails.basic = agentDetails;
            $scope.quotationDetails.proposer = proposerDetails;

            if (proposerDetails && proposerDetails.proposerCode) {
                $scope.proposerCodeDisabled = true;
            }
            // console.log(getQueryParameter('quotationId'));
            // console.log($scope.quotationId);


            $scope.$watchCollection('[quotationId,showDownload]', function (n) {
                if (n[0]) {
                    $scope.qId = n[0];
                    //  console.log(n[0]);
                    // console.log(n[1]);
                    if (n[1]) {
                        $scope.dropdown = [
                            {
                                "text": "<a><img src=\"/pla/images/xls-icon.png\">Ready Reckoner</a>",
                                "href": "/pla/quotation/grouplife/downloadplandetail/" + $scope.qId
                            },
                            {
                                "text": "<a><img src=\"/pla/images/xls-icon.png\">Template</a>",
                                "href": "/pla/quotation/grouplife/downloadinsuredtemplate/" + $scope.qId
                            }
                        ];
                    } else {
                        $scope.dropdown = [
                            {
                                "text": "<a><img src=\"/pla/images/xls-icon.png\">Ready Reckoner</a>",
                                "href": "/pla/quotation/grouplife/downloadplandetail/" + $scope.qId
                            },
                            {
                                "text": "<a><img src=\"/pla/images/xls-icon.png\">Template</a>",
                                "href": "/pla/quotation/grouplife/downloadinsuredtemplate/" + $scope.qId
                            },
                            {
                                "text": "<a><img src=\"/pla/images/xls-icon.png\">Error File</a>",
                                "href": "/pla/quotation/grouplife/downloaderrorinsuredtemplate/" + $scope.qId
                            }
                        ];
                    }
                }
            });

            $scope.$watch('quotationDetails.proposer.province', function (newVal, oldVal) {
                if (newVal) {
                    $scope.getProvinceDetails(newVal);
                }
            });

            $scope.populateProposerDetailFromClientRepository = function () {
                $http.get("/pla/quotation/grouplife/getproposerdetailfromclient/" + $scope.quotationDetails.proposer.proposerCode + "/" + $scope.quotationId)
                    .success(function (data) {
                        $scope.quotationDetails.proposer = data;
                    });
            }

            $scope.getProvinceDetails = function (provinceCode) {
                var provinceDetails = getProvinceAndCityDetail(provinces, provinceCode);
                if (provinceDetails) {
                    $scope.cities = provinceDetails.cities;
                }
            };
            $scope.accordionStatus = {
                contact: false,
                proposer: true
            };
            $scope.$watch('fileSaved', function (n, o) {
                if (n && n.length) {
                    $scope.fileName = n[0].name
                }
            });
            $scope.openNewTab = function (event) {
                /*keyCode 9 is tab key*/
                if (event && event.keyCode == 9) {
                    $scope.accordionStatus.contact = true;
                    $scope.accordionStatus.proposer = false;
                }
            };

            /*clear all fields in the agent details except agentId*/
            $scope.clearAgentDetails = function () {
                angular.extend($scope.quotationDetails.basic, {agentName: null, branchName: null, teamName: null});
            };

            $scope.isSaveDisabled = function (formName) {
                return formName.$invalid || ($scope.stepsSaved[$scope.selectedItem] && !mode == 'new')
            };

            $scope.searchAgent = function () {
                $http.get("/pla/quotation/grouplife/getagentdetail/" + $scope.quotationDetails.basic.agentId)
                    .success(function (data) {
                        if (data.status === "200") {
                            $scope.agentNotFound = false;
                            $scope.quotationDetails.basic = data.data;
                            var agentName = $scope.quotationDetails.basic.agentName.replace("null", '').replace('null', '').trim();
                            $scope.quotationDetails.basic.agentName = agentName;
                        } else {
                            $scope.agentNotFound = true;
                        }
                    })
                    .error(function (data, status) {

                    });
            };

            function isInteger(x) {
                return Math.round(x) === x;
            }

            $scope.quotationDetails.premium.profitAndSolvencyLoading = $scope.quotationDetails.premium.profitAndSolvencyLoading || 0;
            $scope.quotationDetails.premium.discounts = $scope.quotationDetails.premium.discounts || 0;

            $scope.inappropriatePolicyTerm = false;
            $scope.$watch('quotationDetails.premium.policyTermValue', function (newVal, oldVal) {
                /*TODO check for the minimum amd maximum value for the policy term value*/
                console.log(' 1 ' + newVal);
                if (newVal && newVal != 365 && newVal >= 30 && newVal <= 9999) {
                    /*used to toggle controls between dropdown and text*/
                    $scope.isPolicyTermNot365 = true;
                    /*used to show the error message when inappropriate value is entered*/
                    $scope.inappropriatePolicyTerm = false;

                } else {
                    console.log(' 2 ' + newVal);
                    if (newVal < 30 || newVal > 9999) {
                        $scope.inappropriatePolicyTerm = true;
                    } else {
                        $scope.inappropriatePolicyTerm = false;
                        $scope.isPolicyTermNot365 = false;
                    }
                }
            });

            var setQuotationNumberAndVersionNumber = function (quotationId) {
                $http.get("/pla/quotation/grouplife/getquotationnumber/" + quotationId)
                    .success(function (data, status) {
                        $scope.quotationNumber = data.id;
                    });
                $http.get("/pla/quotation/grouplife/getversionnumber/" + quotationId)
                    .success(function (data, status) {
                        $scope.versionNumber = data.id;
                    });
            };


            $scope.$watch('quotationId', function (newval, oldval) {
                if (newval && newval != oldval) {
                    $window.location.href = '/pla/quotation/grouplife/creategrouplifequotation?quotationId=' + $scope.quotationId + '&mode=edit';
                }
            });

            $scope.saveBasicDetails = function () {
                if ($scope.quotationId) {
                    $http.post("/pla/quotation/grouplife/updatewithagentdetail", angular.extend($scope.quotationDetails.basic, {
                        proposerName: $scope.quotationDetails.proposer.proposerName
                        , quotationId: $scope.quotationId
                    }))
                        .success(function (agentDetails) {
                            if (agentDetails.status == "200") {
                                $scope.quotationId = agentDetails.id;
                                setQuotationNumberAndVersionNumber(agentDetails.id);
                                saveStep();
                            }
                        });
                } else {
                    $http.post("/pla/quotation/grouplife/createquotation", angular.extend($scope.quotationDetails.basic, {proposerName: $scope.quotationDetails.proposer.proposerName}))
                        .success(function (agentDetails) {
                            if (agentDetails.status == "200") {
                                $window.location.href = '/pla/quotation/grouplife/creategrouplifequotation?quotationId=' + agentDetails.id + '&mode=new';
                                saveStep();
                            }
                        });
                }
            };

            var saveStep = function () {
                $scope.stepsSaved[$scope.selectedItem] = true;
            };

            $scope.saveProposerDetails = function () {
                $http.post("/pla/quotation/grouplife/updatewithproposerdetail", angular.extend({},
                    {proposerDto: $scope.quotationDetails.proposer},
                    {"quotationId": $scope.quotationId}))
                    .success(function (data) {
                        if (data.status == "200") {
                            $scope.quotationId = data.id;
                            $scope.proposerCodeDisabled = true;
                            saveStep();
                        }
                    });
            };

            $scope.updatePremiumDetail = function (quotationId) {
                $http.get("/pla/quotation/grouplife/getpremiumdetail/" + quotationId)
                    .success(function (data) {
                        console.log('received data' + JSON.stringify(data));
                        $scope.quotationDetails.premium = data;
                        $scope.premiumData = data;
                        $scope.quotationDetails.premium.policyTermValue = data.policyTermValue;
                        $scope.quotationDetails.premium.profitAndSolvencyLoading = $scope.quotationDetails.premium.profitAndSolvencyLoading || 0;
                        $scope.quotationDetails.premium.discounts = $scope.quotationDetails.premium.discounts || 0;

                    });
            }

            $scope.savePlanDetails = function () {
                $upload.upload({
                    url: '/pla/quotation/grouplife/uploadinsureddetail?quotationId=' + $scope.quotationId,
                    headers: {'Authorization': 'xxx'},
                    fields: $scope.quotationDetails.plan,
                    file: $scope.fileSaved
                }).success(function (data, status, headers, config) {
                    if (data.status == "200") {
                        $scope.quotationId = data.id;
                        $timeout($scope.updatePremiumDetail($scope.quotationId), 500);
                        saveStep();
                        $scope.showDownload = true;
                    } else {
                        $scope.showDownload = false;
                        // console.log($scope.showDownload);
                    }

                });
            };
            $scope.premiumInstallment = false;


            $scope.updateNumberOfInstallments = function (installmntNo) {
                if (installmntNo > 0) {
                    $scope.premiumInstallment = true;
                }
            };

            $scope.installments = _.sortBy($scope.premiumData.installments, 'installmentNo');


            $scope.recalculatePremium = function () {
                $scope.premiumInstallment = false;
                $scope.quotationDetails.premium.premiumInstallment = $scope.selectedInstallment || null;
                $http.post('/pla/quotation/grouplife/recalculatePremium', angular.extend({},
                    {premiumDetailDto: $scope.quotationDetails.premium},
                    {"quotationId": $scope.quotationId})).success(function (data) {
                    // console.log(data.data);
                    $scope.quotationDetails.premium = data.data;
                    $scope.premiumData.totalPremium = data.data.totalPremium;
                    if (data.data.annualPremium) {
                        $scope.premiumData.annualPremium = data.data.annualPremium;
                        $scope.premiumData.semiannualPremium = data.data.semiannualPremium;
                        $scope.premiumData.quarterlyPremium = data.data.quarterlyPremium;
                        $scope.premiumData.monthlyPremium = data.data.monthlyPremium;
                    }
                    if (data.data.installments) {
                        $scope.selectedInstallment = null;
                        $scope.installments = _.sortBy(data.data.installments, 'installmentNo');
                    }
                });

            }


            $scope.setSelectedInstallment = function (selectedInstallment) {
                $scope.selectedInstallment = selectedInstallment;
                console.log('setSelectedInstallment ***');
            }

            $scope.savePremiumDetails = function () {
                console.log($scope.selectedInstallment);
                var request = angular.extend({premiumDetailDto: $scope.quotationDetails.premium},
                    {"quotationId": $scope.quotationId});
                request.premiumDetailDto["premiumInstallment"] = $scope.selectedInstallment;
                console.log(JSON.stringify(request));
                $http.post('/pla/quotation/grouplife/savepremiumdetail', request).success(function (data) {
                    $http.post("/pla/quotation/grouplife/generate", angular.extend({},
                        {"quotationId": $scope.quotationId}))
                        .success(function (data) {
                            if (data.status == "200") {
                                saveStep();
                                $('#searchFormQuotation').val($scope.quotationId);
                                $('#searchForm').submit();
                            }
                        });
                });
            }

            $scope.back = function () {
                $window.location.href = 'listgrouplifequotation';
            }
        }])
    .config(['$dropdownProvider', function ($dropdownProvider) {
        angular.extend($dropdownProvider.defaults, {
            html: true
        });
    }])
    .config(["$routeProvider", function ($routeProvider) {
        var stepsSaved = {};
        var queryParam = null;
        $routeProvider.when('/', {
            templateUrl: 'createQuotationTpl.html',
            controller: 'quotationCtrl',
            resolve: {

                provinces: ['$q', '$http', function ($q, $http) {
                    var deferred = $q.defer();
                    $http.get('/pla/core/master/getgeodetail').success(function (response, status, headers, config) {
                        deferred.resolve(response)
                    }).error(function (response, status, headers, config) {
                        deferred.reject();
                    });
                    return deferred.promise;
                }],
                industries: ['$q', '$http', function ($q, $http) {
                    var deferred = $q.defer();
                    $http.get('/pla/core/master/getindustry').success(function (response, status, headers, config) {
                        deferred.resolve(response)
                    }).error(function (response, status, headers, config) {
                        deferred.reject();
                    });
                    return deferred.promise;
                }],
                agentDetails: ['$q', '$http', 'getQueryParameter', function ($q, $http, getQueryParameter) {
                    queryParam = getQueryParameter('quotationId');
                    if (queryParam && !_.isEmpty(queryParam)) {
                        var deferred = $q.defer();
                        $http.get('/pla/quotation/grouplife/getagentdetailfromquotation/' + queryParam).success(function (response, status, headers, config) {
                            deferred.resolve(response)
                        }).error(function (response, status, headers, config) {
                            deferred.reject();
                        });
                        stepsSaved["1"] = true;
                        return deferred.promise;
                    } else {
                        return {};
                    }
                }],
                proposerDetails: ['$q', '$http', function ($q, $http) {
                    if (queryParam && !_.isEmpty(queryParam)) {
                        var deferred = $q.defer();
                        $http.get('/pla/quotation/grouplife/getproposerdetail/' + queryParam).success(function (response, status, headers, config) {
                            deferred.resolve(response)
                        }).error(function (response, status, headers, config) {
                            deferred.reject();
                        });
                        stepsSaved["2"] = true;
                        return deferred.promise;
                    } else {
                        return {};
                    }
                }],
                checkIfInsuredUploaded: ['$q', '$http', function ($q, $http) {
                    if (queryParam && !_.isEmpty(queryParam)) {
                        var deferred = $q.defer();
                        $http.get('/pla/quotation/grouplife/isinsureddetailavailable/' + queryParam).success(function (response, status, headers, config) {
                            deferred.resolve(response)
                        }).error(function (response, status, headers, config) {
                            deferred.reject();
                        });
                        return deferred.promise;
                    } else {
                        return false;
                    }
                }],
                quotationNumber: ['$q', '$http', function ($q, $http) {
                    if (queryParam && !_.isEmpty(queryParam)) {
                        var deferred = $q.defer();
                        $http.get("/pla/quotation/grouplife/getquotationnumber/" + queryParam)
                            .success(function (response) {
                                deferred.resolve(response.id)
                            })
                            .error(function () {
                                deferred.reject();
                            });
                        return deferred.promise;
                    } else {
                        return null;
                    }
                }],
                premiumData: ['$q', '$http', function ($q, $http) {
                    if (queryParam && !_.isEmpty(queryParam)) {
                        var deferred = $q.defer();
                        $http.get('/pla/quotation/grouplife/getpremiumdetail/' + queryParam).success(function (response, status, headers, config) {
                            deferred.resolve(response)
                        }).error(function (response, status, headers, config) {
                            deferred.reject();
                        });
                        return deferred.promise;
                    } else {
                        return false;
                    }
                }],
                stepsSaved: function () {
                    return stepsSaved;
                }
            }
        })
    }]);



