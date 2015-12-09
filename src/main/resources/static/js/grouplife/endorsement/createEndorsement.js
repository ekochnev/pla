angular.module('createEndorsement', ['common', 'ngRoute', 'mgcrea.ngStrap.select', 'mgcrea.ngStrap.alert', 'mgcrea.ngStrap.popover', 'directives',
    'angularFileUpload', 'mgcrea.ngStrap.dropdown', 'ngSanitize', 'commonServices'])

    .controller('EndorsementCtrl', ['$scope', '$http', '$timeout', '$upload', 'provinces', 'getProvinceAndCityDetail', 'globalConstants',
        'agentDetails', 'stepsSaved', 'policyDetails', 'endorsementNumber', 'getQueryParameter', '$window', 'premiumData', 'documentList',
        function ($scope, $http, $timeout, $upload, provinces, getProvinceAndCityDetail, globalConstants, agentDetails, stepsSaved, policyDetails, endorsementNumber,
                  getQueryParameter, $window, premiumData, documentList) {

             $scope.endorsementId = getQueryParameter('endorsementId') || null;
           // $scope.endorsementId = window.location.search.split('=')[1];
          //  alert($scope.endorsementId);
            /*This scope value is binded to fueluxWizard directive and hence it changes as and when next button is clicked*/
            $scope.selectedItem = 1;

            /*Holds the indicator for steps in which save button is clicked*/
            $scope.stepsSaved = stepsSaved;
            console.log(stepsSaved);

            // $scope.versionNumber = getQueryParameter('version') || null;

            /*actual quotation number to be used in the view*/
            console.log('Response JSON*******'+JSON.stringify(endorsementNumber));
            $scope.endorsementNumber = endorsementNumber.endorsementNumber;
            // console.log(endorsementNumber);
            $scope.isEnablePolicyHolderMode = false;
            $scope.isEnableContactMode = false;
            $scope.isEnablePlanMode = false;
            $scope.isEnable = false;
            $scope.calculatePlan = false;
            $scope.submittedButton = false;
            $scope.disableProposerSaveButton = true;
            var enableTab = getQueryParameter("endorsementType");
            $scope.endorsementType = enableTab;
            console.log(enableTab);
            if (enableTab == 'CHANGE_POLICY_HOLDER_NAME' || enableTab == 'Correction of Name - Policy Holder') {
                $scope.submittedButton = false;
                $scope.isEnablePolicyHolderMode = true;
                $scope.isEnable = true;
                $scope.disableProposerSaveButton = true;
            } else if (enableTab == 'CHANGE_POLICY_HOLDER_CONTACT_DETAIL' || enableTab == 'Change of Contact Details') {
                $scope.submittedButton = false;
                $scope.isEnableContactMode = true;
                $scope.isEnable = true;
                $scope.disableProposerSaveButton = true;
            } else {
                $scope.submittedButton = true;
                $scope.isEnablePlanMode = true;
                $scope.isEnable = false;
                $scope.stepsSaved["2"] = endorsementNumber.hasUploaded;
            }
            var mode = getQueryParameter("mode");
            if (mode == 'view') {
                $scope.stepsSaved["2"] = true;
                $scope.isViewMode = true;
                $scope.isEnableContactMode = false;
                $scope.isEnablePlanMode = false;
                $scope.isEnable = false;
                $scope.isEnablePolicyHolderMode = false;
            }
            var status = getQueryParameter("status");
            if (status == 'return') {
                $scope.isReturnStatus = true;
                $scope.stepsSaved["2"] = true;
                $scope.stepsSaved["3"] = true;


                $http.get("/pla/grouplife/endorsement/getapprovercomments/" + $scope.endorsementId).success(function (data, status) {
                    console.log(data);
                    $scope.approvalCommentList = data;
                });

            }
            var method = getQueryParameter("method");
            if (method == 'approval') {
                $scope.isViewMode = true;
                $scope.stepsSaved["2"] = true;
                $scope.stepsSaved["3"] = true;

                $http.get("/pla/grouplife/endorsement/getapprovercomments/" + $scope.endorsementId).success(function (data, status) {
                    // console.log(data);
                    $scope.approvalCommentList = data;
                });

            }

            /*This scope holds the list of installments from which user can select one */
            $scope.numberOfInstallmentsDropDown = [];

            /*regex for number pattern for more details see commonModule.js*/
            $scope.numberPattern = globalConstants.numberPattern;

            $scope.fileSaved = null;
            $scope.disableUploadButton = false;


            /*Inter id used for programmatic purpose*/
            $scope.provinces = provinces;
            $scope.showDownload = true;

            $scope.documentList = documentList;
            //$scope.stepsSaved["3"] = false
            /* if(status != 'return' && method != 'approval' && $scope.isEnable!= true && mode != 'view' && mode != 'edit' ){
             $scope.stepsSaved["3"] = false;

             }*/

            $scope.uploadDocumentFiles = function () {
                // console.log($scope.documentList.length);
                for (var i = 0; i < $scope.documentList.length; i++) {
                    var document = $scope.documentList[i];
                    var files = document.documentAttached;
                    // console.log(files);
                    if (files) {
                        $upload.upload({
                            url: '/pla/grouplife/endorsement/uploadmandatorydocument',
                            file: files,
                            fields: {
                                documentId: document.documentId,
                                endorsementId: $scope.endorsementId,
                                mandatory: true
                            },
                            method: 'POST'
                        }).progress(function (evt) {
                            console.log('percent: ' + parseInt(100.0 * evt.loaded / evt.total));
                        }).success(function (data, status, headers, config) {
                           // console.log('file ' + config.file.name);
                            //console.log(data);


                        });
                    }

                }

            };
            $scope.uploadAdditionalDocument = function () {
                for (var i = 0; i < $scope.additionalDocumentList.length; i++) {
                    var document = $scope.additionalDocumentList[i];
                    var files = document.documentAttached;
                    if (files) {
                        $upload.upload({
                            url: '/pla/grouplife/endorsement/uploadmandatorydocument',
                            file: files,
                            fields: {
                                documentId: document.documentId,
                                endorsementId: $scope.endorsementId,
                                mandatory: false
                            },
                            method: 'POST'
                        }).progress(function (evt) {

                        }).success(function (data, status, headers, config) {
                            //console.log('file ' + config.file.name + 'uploaded. Response: ' +
                            // JSON.stringify(data));
                        });
                    }

                }
            };


            $scope.additionalDocumentList = [{}];
            $http.get("/pla/grouplife/endorsement/getadditionaldocuments/" + $scope.endorsementId).success(function (data, status) {
                console.log(data);
                $scope.additionalDocumentList = data;
                $scope.checkDocumentAttached = $scope.additionalDocumentList != null;

            });

            $scope.addAdditionalDocument = function () {
                $scope.additionalDocumentList.unshift({});
                $scope.checkDocumentAttached = $scope.isUploadEnabledForAdditionalDocument();

            };

            $scope.removeAdditionalDocument = function (index) {
                $scope.additionalDocumentList.splice(index, 1);
                $scope.checkDocumentAttached = $scope.isUploadEnabledForAdditionalDocument();
            };
            $scope.callAdditionalDoc = function (file) {
                if (file[0]) {
                    $scope.checkDocumentAttached = $scope.isUploadEnabledForAdditionalDocument();
                }
            }

            $scope.isUploadEnabledForAdditionalDocument = function () {
                var enableAdditionalUploadButton = ($scope.additionalDocumentList != null);
                for (var i = 0; i < $scope.additionalDocumentList.length; i++) {
                    var document = $scope.additionalDocumentList[i];
                    var files = document.documentAttached;
                    //  alert(i+"--"+files)
                    //  alert(i+"--"+document.content);
                    if (!(files || document.content)) {
                        enableAdditionalUploadButton = false;
                        break;
                    }
                }
                return enableAdditionalUploadButton;
            }

            $scope.submitAdditionalDocument = function () {
                $http.post('/pla/grouplife/endorsement/submit', angular.extend({},
                    {"endorsementId": $scope.endorsementId})).success(function (data) {
                    if (data.status == "200") {
                        saveStep();
                        $('#searchFormEndorsement').val($scope.endorsementId);
                        $('#searchForm').submit();
                    }

                });

            }
            $scope.saveProposerDetails = function () {
                $http.post("/pla/grouplife/endorsement/updatewithproposerdetail", angular.extend({},
                    {proposerDto: $scope.policyDetails.proposer},
                    {"endorsementId": $scope.endorsementId}))
                    .success(function (data) {
                        if (data.status == "200") {
                            $scope.submittedButton = true;
                            saveStep();
                        }
                    });
            };

            $scope.savePlanDetails = function () {
                $upload.upload({
                    url: '/pla/grouplife/endorsement/uploadinsureddetail?endorsementType=' + $scope.endorsementType + "&endorsementId=" + $scope.endorsementId,
                    headers: {'Authorization': 'xxx'},
                    //fields: $scope.policyDetails.plan,
                    file: $scope.fileSaved
                }).success(function (data, status, headers, config) {
                    if (data.status == "200") {
                        $scope.submittedButton = true;
                        $scope.calculatePlan = true;
                        $scope.stepsSaved["2"] = true;
                        $scope.stepsSaved["3"] = true;
                       // $scope.stepsSaved["4"] = false;
                        // saveStep();
                         $http.get("/pla/grouplife/endorsement/getpremiumdetail/" + $scope.endorsementId)
                         .success(function (data,status) {
                                 if(status==200){
                                     $scope.policyDetails.premium=data;
                                 }


                             })

                        $scope.showDownload = true;
                    }else{
                        $scope.showDownload = false;
                    }
                });
            };
            $scope.approveEndorsement = function () {
                var request = angular.extend({comment: $scope.comment},
                    {"endorsementId": $scope.endorsementId});

                $http.post('/pla/grouplife/endorsement/approve', request).success(function (data) {
                    if (data.status == 200) {

                        $window.location.href = "/pla/grouplife/endorsement/openapprovalendorsement";

                    }

                });
            }
            $scope.comment = '';
            $scope.returnEndorsement = function () {
                var request = angular.extend({comment: $scope.comment}, {"endorsementId": $scope.endorsementId});

                $http.post('/pla/grouplife/endorsement/return', request).success(function (data) {
                    if (data.status == 200) {


                        $window.location.href = "/pla/grouplife/endorsement/openapprovalendorsement";

                    }

                });
            }
            $scope.rejectEndorsement = function () {
                var request = angular.extend({comment: $scope.comment}, {"endorsementId": $scope.endorsementId});

                $http.post('/pla/grouplife/endorsement/reject', request).success(function (data) {
                    if (data.status == 200) {


                        $window.location.href = "/pla/grouplife/endorsement/openapprovalendorsement";

                    }

                });
            }

            $scope.submitComments = function (comment) {
                $http.post('/pla/grouplife/endorsement/submit', angular.extend({},
                    {"endorsementId": $scope.endorsementId, comment: comment})).success(function (data) {
                    if (data.status == "200") {
                       saveStep();
                        $('#searchFormEndorsement').val($scope.endorsementId);
                        $('#searchForm').submit();
                    }

                });

            }

            $scope.waiverByApproved = [];
            $scope.getMandatoryDocumentDetials = function ($event, document) {
                var checkbox = $event.target;
                if (checkbox.checked) {
                    $scope.waiverByApproved.push({
                        documentId: document.documentId,
                        documentName: document.documentName,
                        mandatory: true,
                        isApproved: true
                    });
                }
                else {
                    //alert(index);
                    for (i in $scope.waiverByApproved) {
                        if ($scope.waiverByApproved[i].documentName == document.documentName) {
                            $scope.waiverByApproved.splice(i, 1);
                            //$scope.waiverByApproved[i].mandatory=false;
                            //$scope.waiverByApproved[i].isApproved=false;
                        }
                    }
                }
            }

            $scope.updateMandatoryDocumentForApproval = function () {
                //alert($scope.proposal.proposalId);

                var requestForApproval =
                {
                    "waivedDocuments": $scope.waiverByApproved,
                    "endorsementId": $scope.endorsementId
                }
                console.log('requestForApprovalTest'+JSON.stringify(requestForApproval));

                $http.post('waivedocument', requestForApproval).success(function (response, status, headers, config) {
                    //$scope.proposal.proposalId=response.id;
                }).error(function (response, status, headers, config) {
                });

            }

            $scope.isBrowseDisable=function(document)
            {
                if(document.fileName == null && document.submitted)
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }


            $scope.$watch('policyDetails.proposer.proposerName', function (newVal, oldVal) {
                //   console.log("****************************OLD VALUE------------>"+oldVal);
                //    console.log("****************************NEW VALUE------------>"+newVal);
                if (newVal) {
                    //  console.log(newVal);
                    $scope.disableProposerSaveButton = true;
                } else {
                    $scope.disableProposerSaveButton = false;
                }
            }, true);
            $scope.isSaveDisabled = function (formName) {
                return formName.$invalid;
            };
            $scope.getCurrentVal = function (val) {

                $scope.policyDetails.proposer.town = '';

            }


            $scope.$watchGroup(['policyDetails.proposer.contactPersonName', 'policyDetails.proposer.contactPersonMobileNumber', 'policyDetails.proposer.contactPersonWorkPhoneNumber'], function (newValues, oldValues) {
                if (newValues[0]) {
                    $scope.disableProposerSaveButton = true;
                } else if (newValues[1]) {
                    $scope.disableProposerSaveButton = true;
                } else if (newValues[2]) {
                    $scope.disableProposerSaveButton = true;
                } else {
                    $scope.disableProposerSaveButton = false;
                }
                if (!newValues[0] || !newValues[1] || !newValues[2]) {
                    $scope.disableProposerSaveButton = false;
                }

            });


            if ($scope.documentList) {
                if ($scope.documentList.documentAttached) {
                    if ($scope.documentList.documentAttached.length == $scope.documentList.documentName.length) {
                        $scope.disableUploadButton = true;
                    } else {
                        $scope.disableUploadButton = false;
                    }
                }
            }
            $http.get("/pla/grouplife/endorsement/getpolicydetail/" + $scope.endorsementId).success(function (data, status) {
                // console.log(data);
                $scope.policyDetails.basicDetails = data;
                $scope.policyDetails.basicDetails.inceptionDate = data.inceptionDate;
                $scope.policyDetails.basicDetails.expiryDate = data.expiredDate;


            });

            $scope.policyDetails = {
                /*initialize with default values*/
                plan: {
                    samePlanForAllRelation: false,
                    samePlanForAllCategory: false
                },
                premium: {
                    addOnBenefit: 20,
                    profitAndSolvencyLoading: 0,
                    discounts: 0
                }
            };


            $scope.policyDetails.basic = agentDetails;

            $scope.policyDetails.premium = premiumData || {};

            $scope.changeAgent = false;
            console.log($scope.policyDetails.basic['active']);
            if (!$scope.policyDetails.basic['active']) {
                //  $('#agentModal').modal('show');
                //  $scope.changeAgent = true;
                //  $scope.stepsSaved["2"] = !$scope.changeAgent;
                $scope.stepsSaved["2"] = true;
            }

            $scope.policyDetails.proposer = policyDetails;
            /*used for bs-dropdown*/
            $scope.dropdown = [
                {
                    "text": "<a><img src=\"/pla/images/xls-icon.png\">Template</a>",
                    "href": "/pla/grouplife/endorsement/downloadtemplatebyendorsementtype/" + $scope.endorsementType + "/" + $scope.endorsementId
                }
            ];
            $scope.$watchCollection('[endorsementId,showDownload]', function (n) {
                if (n[0]) {
                  //  $scope.qId = n[0];
                    //  console.log(n[0]);
                    // console.log(n[1]);
                    if (n[1]) {
                        $scope.dropdown = [
                            {
                                "text": "<a><img src=\"/pla/images/xls-icon.png\">Template</a>",
                                "href": "/pla/grouplife/endorsement/downloadtemplatebyendorsementtype/" + $scope.endorsementType + "/" + $scope.endorsementId
                            }
                        ];
                    } else {
                        $scope.dropdown = [
                            {
                                "text": "<a><img src=\"/pla/images/xls-icon.png\">Template</a>",
                                "href": "/pla/grouplife/endorsement/downloadtemplatebyendorsementtype/" + $scope.endorsementType + "/" + $scope.endorsementId
                            },
                            {
                                "text": "<a><img src=\"/pla/images/xls-icon.png\">Error File</a>",
                                "href": "/pla/grouplife/endorsement/downloaderrortemplate/" + $scope.endorsementId
                            }
                        ];
                    }
                }
            });

            $scope.$watch('policyDetails.proposer.province', function (newVal, oldVal) {
                if (newVal) {
                    $scope.getProvinceDetails(newVal);
                }
            });

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
            $scope.accordionStatusDocuments = {
                documents: true,
                additionalDocuments: false
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
                angular.extend($scope.policyDetails.basic, {agentName: null, branchName: null, teamName: null});
            };


            function isInteger(x) {
                return Math.round(x) === x;
            }

            function generateListOfInstallments(numberOfInstallments) {
                $scope.numberOfInstallmentsDropDown = [];
                for (var installment = 1; installment <= numberOfInstallments; installment++) {
                    $scope.numberOfInstallmentsDropDown.push(installment);
                }
            }

            $scope.$watch('policyDetails.premium.policyTermValue', function (newVal, oldVal) {
                /*TODO check for the minimum amd maximum value for the policy term value*/
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
            $scope.backToApproverPortlet = function () {
                //$window.location.href = 'viewApprovalEndorsement';
                $window.location.href = 'openapprovalendorsement';

            }


            $scope.selectedInstallment = premiumData.premiumInstallment;
            $scope.installments = $scope.policyDetails.premium.installments;
            $scope.disableSaveButton = false;

            $scope.setSelectedInstallment = function (selectedInstallment) {
                $scope.selectedInstallment = selectedInstallment;
                console.log('setSelectedInstallment ***');
            };


            var saveStep = function () {
                $scope.stepsSaved[$scope.selectedItem] = true;
            };


            $scope.back = function () {
              //  $window.location.href = 'openpolicysearchpage';
                $window.location.href = 'opensearchendorsement';
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
            templateUrl: 'createEndorsementTpl.html',
            controller: 'EndorsementCtrl',
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
                agentDetails: ['$q', '$http', 'getQueryParameter', function ($q, $http, getQueryParameter) {
                    queryParam = getQueryParameter('endorsementId');
                    if (queryParam && !_.isEmpty(queryParam)) {
                        var deferred = $q.defer();
                        $http.get('/pla/grouplife/endorsement/getagentdetailfrompolicy/' + queryParam).success(function (response, status, headers, config) {
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
                policyDetails: ['$q', '$http', 'getQueryParameter', function ($q, $http, getQueryParameter) {
                    if (queryParam && !_.isEmpty(queryParam)) {
                        var deferred = $q.defer();
                        $http.get('/pla/grouplife/endorsement/getproposerdetail/' + queryParam).success(function (response, status, headers, config) {
                            deferred.resolve(response)
                        }).error(function (response, status, headers, config) {
                            deferred.reject();
                        });
                        stepsSaved["1"] = true;
                        stepsSaved["3"] = true;
                         stepsSaved["4"] = true;
                        return deferred.promise;
                    } else {
                        return {};
                    }
                }],
                endorsementNumber: ['$q', '$http', function ($q, $http) {
                    if (queryParam && !_.isEmpty(queryParam)) {
                        var deferred = $q.defer();
                        $http.get("/pla/grouplife/endorsement/getendorsementnumber/" + queryParam)
                            .success(function (response) {
                                deferred.resolve(response)
                            })
                            .error(function () {
                                deferred.reject();
                            });
                        return deferred.promise;
                    } else {
                        return 1;
                    }
                }],
                premiumData: ['$q', '$http', 'getQueryParameter', function ($q, $http, getQueryParameter) {
                    if (queryParam && !_.isEmpty(queryParam)) {
                        var deferred = $q.defer();
                        $http.get('/pla/grouplife/endorsement/getpremiumdetail/' + queryParam).success(function (response, status, headers, config) {


                            deferred.resolve(response)
                        }).error(function (response, status, headers, config) {
                            deferred.reject();
                        });
                        stepsSaved["5"] = true;
                        return deferred.promise;
                    } else {
                        return false;
                    }
                }],
                documentList: ['$q', '$http', function ($q, $http) {
                    if (queryParam && !_.isEmpty(queryParam)) {
                        var deferred = $q.defer();
                        $http.get('/pla/grouplife/endorsement/getmandatorydocuments/' + queryParam).success(function (response, status, headers, config) {

                            deferred.resolve(response)
                        }).error(function (response, status, headers, config) {
                            deferred.reject();
                        });
                        stepsSaved["6"] = true;

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
