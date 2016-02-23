(function (angular) {
    "use strict";
    var  app=angular.module('createpreauthunderwriterleveltwo', ['common', 'ngRoute','ngMessages', 'mgcrea.ngStrap.select', 'mgcrea.ngStrap.alert', 'mgcrea.ngStrap.popover',
        'directives', 'mgcrea.ngStrap.dropdown', 'ngSanitize', 'commonServices','ui.bootstrap.modal','angularFileUpload', 'angucomplete-alt'])
    app.config(['datepickerPopupConfig', function (datepickerPopupConfig) {
            datepickerPopupConfig.datepickerPopup = 'MM/dd/yyyy';
            datepickerPopupConfig.currentText = 'Today';
            datepickerPopupConfig.clearText = 'Clear';
            datepickerPopupConfig.closeText = 'Done';
            datepickerPopupConfig.closeOnDateSelection = true;
        }])
        .config(["$routeProvider", function ($routeProvider) {
            $routeProvider.when('/', {
                    templateUrl: 'preauthorizationunderwriter.html',
                    controller: 'createpreauthunderwriterleveltwoctrl',
                    resolve:{

                        createUpdateDto: ['$q', '$http', 'getQueryParameter', function ($q, $http, getQueryParameter) {
                            var deferred = $q.defer();
                            var preAuthorizationId = getQueryParameter('preAuthorizationId');

                            $http.get('/pla/grouphealth/claim/cashless/preauthorizationrequest/getpreauthorizationclaimantdetailcommandfrompreauthorizationrequestid?preAuthorizationId='+preAuthorizationId).success(function (response, status, headers, config) {
                                deferred.resolve(response);

                            }).error(function (response, status, headers, config) {
                                deferred.reject();
                            });
//                        //console.log(deferred);

                            return deferred.promise;
                        }],
                        documentList: ['$q', '$http','getQueryParameter', function ($q, $http, getQueryParameter) {
                            var deferred = $q.defer();
                            var clientId = getQueryParameter('clientId');
                            var preAuthorizationId = getQueryParameter('preAuthorizationId');
                            if (clientId && !_.isEmpty(clientId)) {
                                var deferred = $q.defer();
                                $http.get("/pla/grouphealth/claim/cashless/preauthorizationrequest/getmandatorydocuments/" + clientId + "/"+preAuthorizationId).success(function (response, status, headers, config) {
                                    deferred.resolve(response)
                                }).error(function (response, status, headers, config) {
                                    deferred.reject();
                                });
                                return deferred.promise;
                            } else {
                                return false;
                            }
                        }],
                        preAuthorizationId : ['$q', '$http','getQueryParameter', function ($q, $http, getQueryParameter) {
                            var deferred = $q.defer();
                            var deferred = $q.defer();
                            var preAuthorizationId = getQueryParameter('preAuthorizationId');
                            deferred.resolve(preAuthorizationId)
                            return deferred.promise;
                        }],

                        clientId : ['$q', '$http','getQueryParameter', function ($q, $http, getQueryParameter) {
                            var deferred = $q.defer();
                            var deferred = $q.defer();
                            var clientId = getQueryParameter('clientId');
                            deferred.resolve(clientId)
                            return deferred.promise;
                        }]
                    }

                }
            )}])

        .controller('createpreauthunderwriterleveltwoctrl', ['$scope', '$http','createUpdateDto','getQueryParameter','$window','documentList','$upload','preAuthorizationId','clientId',
            function ($scope, $http, createUpdateDto, getQueryParameter, $window, documentList, $upload,preAuthorizationId, clientId) {
                $scope.createUpdateDto = createUpdateDto;
                $scope.drugServicesDtoList = $scope.createUpdateDto.drugServicesDtos;
                $scope.treatmentDiagnosis = {};
                $scope.diagnosisTreatmentDtoToUpdate = {};
                $scope.index = null;
                $scope.treatmentDiagnosisIndex = null;
                $scope.isEditDrugTriggered = false;
                $scope.isEditDiagnosisTriggered = false;
                $scope.createUpdateCommand = {};
                $scope.diagnosisTreatmentDto = {};
                $scope.createUpdateCommand.claimantHCPDetailDto = $scope.createUpdateDto.claimantHCPDetailDto;
                $scope.createUpdateCommand.claimantHCPDetailDto = {};
                $scope.createUpdateCommand.claimantPolicyDetailDto = {};
                $scope.createUpdateCommand.diagnosisTreatmentDtos = [];
                $scope.createUpdateCommand.illnessDetailDto = {};
                $scope.createUpdateCommand.drugServicesDtos = [];
                $scope.documentList = documentList;
                console.log("-"+documentList);
                $scope.additionalDocumentList = [{}];
                $scope.disableSubmit = false;
                $scope.documentmaster = [];
                $scope.selectedItem = 1;
                $scope.comment = {};
                $scope.fileSaved = null;
                $scope.isViewMode = false;
                $scope.stepsSaved = [];
                $scope.isSeniorUnderwriter = false;

                $scope.$watch('createUpdateDto.status', function(newVal, oldVal){
                    if ($scope.createUpdateDto.status !== 'Underwriting') {
                        $scope.isViewMode = true;
                    }
                });

                $scope.$watch('documentList', function (newCollection, oldCollection) {
                    $scope.disableSubmit = $scope.shouldSubmitBeDisabled(newCollection);
                });

                $scope.shouldSubmitBeDisabled = function (documentList) {
                    for (var i = 0; i < documentList.length; i++) {
                        var document = documentList[i];
                        if (document.fileName == null || document.content == null) {
                            return true;
                        }
                    }
                    return false;
                };

                $scope.levelOfUnderwriter = null;
                $scope.getUnderWriterLevel = function(){
                    $http.get("/pla/grouphealth/claim/cashless/preauthorizationrequest/getunderwriterlevelforpreauthorization/"+preAuthorizationId)
                        .success(function(response) {
                            if (response) {
                                $scope.levelOfUnderwriter = response.data;
                            }
                        }).error(function (response) {
                    });
                };
                $scope.getAllDocuments = function(){
                    $http.get('/pla/core/master/getdocument').success(function(data){
                        $scope.documentmaster = data;
                        for(var documentIndex in $scope.documentmaster){
                            var document = $scope.documentmaster[documentIndex];
                            for(var uploadedDocIndex in $scope.documentList){
                                var uploadedDoc = $scope.documentList[uploadedDocIndex];
                                if(document.documentCode.trim() === uploadedDoc.documentId.trim()){
                                    console.log(document.documentCode.trim()+" - "+uploadedDoc.documentId.trim());
                                    delete $scope.documentmaster[documentIndex];
                                }
                            }
                        }
                        $scope.documentmaster = $scope.documentmaster.filter(function(n){ return n != undefined });
                        console.log($scope.documentmaster);
                    });
                };

                $http.get("/pla/grouphealth/claim/cashless/preauthorizationrequest/getadditionaldocuments/" + preAuthorizationId).success(function (data, status, headers, config) {
                    $scope.additionalDocumentList = data;
                    $scope.checkDocumentAttached = $scope.additionalDocumentList != null;
                }).error(function (response, status, headers, config) {
                    deferred.reject();
                });
                $scope.tratementdignosisnextbuttonfalse= function(){
                    $scope.provisionaldignosisdiv = true;
                    $scope.stepsSaved["4"] = true;
                };
                $scope.viewTreatmentDiagnosis = function (treatmentDiagnosis, treatmentDiagnosisIndex) {
                    $scope.tratementdignosisnextbuttonfalse();
                    $scope.isEditDiagnosisTriggered = true;
                    $scope.treatmentDiagnosisIndex = treatmentDiagnosisIndex;
                    $scope.diagnosisTreatmentDto = treatmentDiagnosis;
                    $scope.drugServicesDto = {};
                    $scope.diagnosisTreatmentDtosUpdate = {};
                    $scope.createUpdateDto.drugServicesDtosSave = [];
                    $scope.provisionaldignosisdiv=true;
                };

                $scope.updateTreatmentAndDiagnosis = function (diagnosisTreatmentDto) {
                    if ($scope.isEditDiagnosisTriggered) {
                        $scope.createUpdateDto.diagnosisTreatmentDtos[$scope.treatmentDiagnosisIndex] = diagnosisTreatmentDto;
                        $scope.savePreAuthorizationRequest();
                        $scope.isEditDiagnosisTriggered = false;
                        $scope.provisionaldignosisdiv=false;
                    } else {
                        $scope.createUpdateDto.diagnosisTreatmentDtos.push(diagnosisTreatmentDto);
                        $scope.savePreAuthorizationRequest();

                    }
                    $scope.provisionaldignosisdiv = false;
                    $scope.stepsSaved["4"] = false;
                };

                $scope.create= function(){
                    $scope.provisionaldignosisdiv = true;
                    $scope.stepsSaved["4"] = true;
                };

                $scope.activenextbuttonforprovisional = function(){
                    $scope.diagnosisTreatmentDto = {};
                    $scope.diagnosisTreatmentDto.dateOfConsultation = $scope.createUpdateDto.diagnosisTreatmentDtos[0].dateOfConsultation;
                    $scope.diagnosisTreatmentDto.doctorName = $scope.createUpdateDto.diagnosisTreatmentDtos[0].doctorName;
                    $scope.provisionaldignosisdiv = true;
                    $scope.stepsSaved["4"] = true;
                };

                $scope.activenextbuttonforprovisionalclickon= function(){
                    $scope.provisionaldignosisdiv = true;
                    $scope.stepsSaved["4"] = true;
                };

//for add sevice drug availed section start
                //create function detect Add Service/Drug button
                $scope.create= function(){
                    $scope.showservicedrugdiv = true;
                    $scope.diagnosisTreatmentDtoToUpdate='';
                    $scope.stepsSaved["1"] = true;
                };
                //updateDrugServicesDto function detect update button
                $scope.updateDrugServicesDto = function (drugServicesDto, index) {
                    $scope.create();
                    $scope.index = index;
                    $scope.isEditDrugTriggered = true;
                    $scope.diagnosisTreatmentDtoToUpdate = drugServicesDto;
                    $scope.showservicedrugdiv=true;
                };

                $scope.saveDiagnosisTreatmentDto = function (diagnosisTreatmentDtoToUpdate) {
                    if ($scope.isEditDrugTriggered) {
                        $scope.createUpdateDto.drugServicesDtos[$scope.index] = diagnosisTreatmentDtoToUpdate;
                        $scope.savePreAuthorizationRequest();
                        $scope.isEditDrugTriggered = false;

                    } else {
                        $scope.createUpdateDto.drugServicesDtos.push(diagnosisTreatmentDtoToUpdate);
                        $scope.savePreAuthorizationRequest();
                    }
                    $scope.showservicedrugdiv = false;
                    $scope.stepsSaved["1"] = false;
                };
//cancel button
                $scope.activenextbuttonfordrugservice= function(){
                    $scope.showservicedrugdiv = false;
                    $scope.stepsSaved["1"] = false;
                };

                $scope.nextBtnActiveOnCancelButton= function(){
                    $scope.provisionaldignosisdiv = false;
                    $scope.stepsSaved["4"] = false;
                };
                $scope.deleteTreatmentDiagnosis = function (index) {
                    $scope.createUpdateDto.drugServicesDtos.splice(index, 1);
                    $scope.savePreAuthorizationRequest();

                };

                /*Holds the indicator for steps in which save button is clicked*/

//end service drug availed section

                var saveStep = function () {
                    $scope.stepsSaved[$scope.selectedItem] = true;
                };

                $scope.isSaveDisabled = function (formName) {
                    return formName.$invalid;
                };

                $scope.savePreAuthorizationRequest = function () {
                    $http({
                        url: '/pla/grouphealth/claim/cashless/preauthorizationrequest/underwriter/update',
                        method: 'POST',
                        data: $scope.createUpdateDto
                    }).success(function (response) {
                        $http.get('/pla/grouphealth/claim/cashless/preauthorizationrequest/getpreauthorizationclaimantdetailcommandfrompreauthorizationrequestid?preAuthorizationId='+preAuthorizationId)
                            .success(function (response) {
                                $scope.createUpdateDto = response;
                            }).error(function (response, status, headers, config) {
                        });
                    }).error();
                };

                $scope.submitPreAuthorizationRequest = function () {
                    $scope.createUpdateDto.submitEventFired = true;
                    $http({
                        url: '/pla/grouphealth/claim/cashless/preauthorizationrequest/submitpreauthorization',
                        method: 'POST',
                        data: $scope.createUpdateDto
                    }).success(function () {
                        window.location.reload();
                    }).error();

                };

                $scope.back = function () {
                    //window.location.reload();
                    $http.get("/pla/grouphealth/claim/cashless/preauthorizationrequest/getunderwriterlevelforpreauthorization/"+preAuthorizationId)
                        .success(function(response) {
                            if (response) {
                                //console.log(response.data);
                                if (response.data === 'LEVEL1') {
                                    window.location.href = '/pla/grouphealth/claim/cashless/preauthorizationrequest/underwriter/getdefaultlistofunderwriterlevels/LEVEL1';
                                }
                                if (response.data === 'LEVEL2') {
                                    window.location.href = '/pla/grouphealth/claim/cashless/preauthorizationrequest/underwriter/getdefaultlistofunderwriterlevels/LEVEL2';
                                }
                                if(response.data === '' || angular.isUndefined(response.data)){
                                    window.location.href = '/pla/grouphealth/claim/cashless/preauthorizationrequest/getpreauthorizationfordefaultlist';
                                }
                            }
                        }).error(function (response) {
                    });
                };

                $scope.checkIfUnderwriterLevel2 = function(){
                    $http.get("/pla/grouphealth/claim/cashless/preauthorizationrequest/getunderwriterlevelforpreauthorization/"+preAuthorizationId)
                        .success(function(response) {
                            if (response) {
                                if (response.data === 'LEVEL2') {
                                    $scope.isSeniorUnderwriter = true;
                                }
                            }
                        }).error(function (response) {
                    });
                };

                $scope.isBrowseDisable = function (document) {
                    if (document.fileName == null && document.submitted) {
                        return true;
                    }
                    else {
                        return false;
                    }
                };

                $scope.uploadDocumentFiles = function () {
                    for (var i = 0; i < $scope.documentList.length; i++) {
                        var document = $scope.documentList[i];
                        var files = document.documentAttached;

                        if (files) {
                            $upload.upload({
                                url: '/pla/grouphealth/claim/cashless/preauthorizationrequest/uploadmandatorydocument',
                                file: files,
                                fields: {
                                    documentId: document.documentId,
                                    preAuthorizationRequestId: $scope.createUpdateDto.preAuthorizationRequestId,
                                    mandatory: true
                                },
                                method: 'POST'
                            }).progress(function (evt) {
                                //console.log('percent: ' + parseInt(100.0 * evt.loaded / evt.total));
                            }).success(function (data, status, headers, config) {
                                $http.get("/pla/grouphealth/claim/cashless/preauthorizationrequest/getmandatorydocuments/" + clientId + "/" + preAuthorizationId).success(function (response, status, headers, config) {
                                    $scope.documentList = response;
                                });
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
                                url: '/pla/grouphealth/claim/cashless/preauthorizationrequest/uploadmandatorydocument',
                                file: files,
                                fields: {
                                    documentId: document.documentId,
                                    preAuthorizationRequestId: $scope.createUpdateDto.preAuthorizationRequestId,
                                    mandatory: false
                                },
                                method: 'POST'
                            }).progress(function (evt) {

                            }).success(function (data, status, headers, config) {
                                $http.get("/pla/grouphealth/claim/cashless/preauthorizationrequest/getadditionaldocuments/" + preAuthorizationId).success(function (response, status, headers, config) {
                                    $scope.additionalDocumentList = response;
                                });
                            });
                        }

                    }
                };

                $scope.isUploadEnabledForAdditionalDocument = function () {
                    var enableAdditionalUploadButton = ($scope.additionalDocumentList != null);
                    for (var i = 0; i < $scope.additionalDocumentList.length; i++) {
                        var document = $scope.additionalDocumentList[i];
                        var files = document.documentAttached;
                        //alert(i+"--"+files)
                        //alert(i+"--"+document.content);
                        if (!(files || document.content)) {
                            enableAdditionalUploadButton = false;
                            break;
                        }
                    }
                    return enableAdditionalUploadButton;
                };

                $scope.addAdditionalDocument = function () {
                    $scope.additionalDocumentList.unshift({});
                };

                $scope.callAdditionalDoc = function (file) {
                    if (file[0]) {
                        $scope.checkDocumentAttached = $scope.isUploadEnabledForAdditionalDocument();
                    }
                };

                $scope.removeAdditionalDocumentCommand = {};
                $scope.removeAdditionalDocument = function (index, gridFsDocId) {
                    $scope.removeAdditionalDocumentCommand.gridFsDocId = gridFsDocId;
                    $scope.removeAdditionalDocumentCommand.preAuthorizationId = $scope.createUpdateDto.preAuthorizationId;
                    if (gridFsDocId) {
                        $http({
                            url: '/pla/grouphealth/claim/cashless/preauthorizationrequest/removeadditionalDocument',
                            method: 'POST',
                            data: $scope.removeAdditionalDocumentCommand
                        }).success(function () {
                            $scope.additionalDocumentList.splice(index, 1);
                            $scope.checkDocumentAttached = $scope.isUploadEnabledForAdditionalDocument();
                        }).error();
                    } else {
                        $scope.additionalDocumentList.splice(index, 1);
                        $scope.checkDocumentAttached = $scope.isUploadEnabledForAdditionalDocument();

                    }
                };

                $scope.launchClaimDate = function ($event) {
                    $event.preventDefault();
                    $event.stopPropagation();
                    $scope.datepicker = {'opened': true};

                };

                $scope.changeClaimDate = function(iem){
                    $scope.createUpdateDto.claimIntimationDate = formatDate(iem);

                    $scope.createUpdateDto.claimIntimationDate=$scope.createUpdateDto.claimIntimationDate;
                    //console.log("qwqe############"+$scope.createUpdateDto.claimIntimationDate );
                };

                $scope.launchPreAuthDate = function ($event){
                    $event.preventDefault();
                    $event.stopPropagation();
                    $scope.datepickerpreauth = {'opened': true};
                };

                $scope.changePreAuthDate=function(preAuthDate) {
                    $scope.createUpdateDto.preAuthorizationDate = formatDate(preAuthDate);
                    //console.log("qwqe############"+$scope.createUpdateDto.preAuthorizationDate );
                };

                $scope.launchProbableDate = function ($event){
                    $event.preventDefault();
                    $event.stopPropagation();
                    $scope.datepickerprobable = {'opened': true};
                };

                $scope.changeProbableDate=function(ProbableDate) {
                    $scope.diagnosisTreatmentDto.pregnancyDateOfDelivery = formatDate(ProbableDate);
                    //console.log("qwqe############"+$scope.diagnosisTreatmentDto.pregnancyDateOfDelivery );
                };

                $scope.launchFirstConsultanceDate = function ($event){
                    $event.preventDefault();
                    $event.stopPropagation();
                    $scope.datepickerconsultance = {'opened': true};
                };

                $scope.changelaunchFirstConsultanceDate=function(consultationDate) {
                    $scope.diagnosisTreatmentDto.dateOfConsultation = formatDate(consultationDate);
                };

                $scope.launchProbAdmissionDate = function ($event){
                    $event.preventDefault();
                    $event.stopPropagation();
                    $scope.datepickeradmission = {'opened': true};
                };

                $scope.changeProbAdmissionDate=function(probAdmissionDate) {
                    $scope.diagnosisTreatmentDto.dateOfAdmission = formatDate(probAdmissionDate);
                    //console.log("qwqe############"+$scope.diagnosisTreatmentDto.dateOfAdmission);
                };

                $scope.hcpServiceDetails = [];

                $scope.checkIfAllApprovedAmountEntered = function () {
                    for(var i=0; i < $scope.createUpdateDto.claimantPolicyDetailDto.coverageBenefitDetails.length ; i++){
                        var coverage = $scope.createUpdateDto.claimantPolicyDetailDto.coverageBenefitDetails[i];
                        for(var j = 0 ; j < coverage.benefitDetails.length; j++){
                            var benefit = coverage.benefitDetails[j];
                            if(benefit.approvedAmount == null || angular.isUndefined(benefit.approvedAmount) || benefit.approvedAmount == '' || benefit.approvedAmount == 0 || (coverage.balanceAmount < benefit.approvedAmount)){
                                return false;
                            }
                        }
                    }
                    return true;
                };

                $scope.getHCPServiceDetails = function(){
                    $http.get("/pla/grouphealth/claim/cashless/preauthorizationrequest/getallrelevantservices/" + preAuthorizationId).success(function (data, status, headers, config) {
                        $scope.hcpServiceDetails = data;
                    }).error(function (response, status, headers, config) {
                    });
                };
                //$scope.coverageDetailDto.approvedAmount='';
                $scope.underwriterApprove = function () {
                    if (!$scope.checkIfAllApprovedAmountEntered()) {
                        $scope.approvepopupModal();

                    }
                    else {
                        $.when($scope.constructCommentDetails()).done(function () {
                            $http({
                                url: '/pla/grouphealth/claim/cashless/preauthorizationrequest/underwriter/approve',
                                method: 'POST',
                                data: $scope.createUpdateDto
                            }).success(function (response, status, headers, config) {
                                if (status === 200) {
                                    $http.get('/pla/grouphealth/claim/cashless/preauthorizationrequest/getpreauthorizationclaimantdetailcommandfrompreauthorizationrequestid?preAuthorizationId=' + preAuthorizationId)
                                        .success(function (response, status, headers, config) {
                                            $scope.createUpdateDto = response;
                                            if (status == "200") {
                                                setTimeout(function () {
                                                    window.location.href = '/pla/grouphealth/claim/cashless/preauthorizationrequest/underwriter/getdefaultlistofunderwriterlevels/'+$scope.levelOfUnderwriter;
                                                }, 2000);
                                            }
                                        }).error(function (response, status, headers, config) {
                                    });
                                }
                            }).error(
                                function (status) {
                                    //console.log(status);
                                }
                            );
                        });
                    };
                };

                $scope.myModal = false;
                $scope.toggleModal = function(){
                    $("#myModal").modal('show');
                };
                $scope.approveModal = false;
                $scope.approvepopupModal = function(){
                    $("#approveModal").modal('show');
                };
                $scope.underwriterReject = function () {
                    if (!$scope.comment.comments) {
                        $scope.message = "Comment is mandatory to reject Pre-Authorization.";
                        $scope.toggleModal();
                    } else {
                        $http.get('/pla/grouphealth/claim/cashless/preauthorizationrequest/underwriter/checkifpreauthorizationrejectionemailsent/' + preAuthorizationId)
                            .success(function (response) {
                                if (response.data === true) {
                                    $scope.rejectPreAuthorization();
                                }
                                else{
                                    var win = window.open('/pla/grouphealth/claim/cashless/preauthorizationrequest/underwriter/getpreauthorizationrejectionletter/' + preAuthorizationId, "_blank", "toolbar=no,resizable=no," +
                                        "scrollable=no,menubar=no,personalbar=no,dependent=yes,dialog=yes,split=no,titlebar=no,resizable=no,location=no,left=100px");
                                    var timer = setInterval(function () {
                                        if (win.closed) {
                                            clearInterval(timer);
                                            $http.get('/pla/grouphealth/claim/cashless/preauthorizationrequest/underwriter/checkifpreauthorizationrejectionemailsent/' + preAuthorizationId)
                                                .success(function (response) {
                                                    if (response.data === true) {
                                                        $scope.rejectPreAuthorization();
                                                    } else{
                                                        $scope.message = " Please email the rejection letter.";
                                                        $scope.toggleModal();
                                                    }
                                                }).error(function (response) {
                                            });
                                        }
                                    }, 500);
                                }
                            }).error()
                    }
                };

                $scope.rejectPreAuthorization = function(){
                    $.when($scope.constructCommentDetails()).done(function () {
                        $http({
                            url: '/pla/grouphealth/claim/cashless/preauthorizationrequest/underwriter/reject',
                            method: 'POST',
                            data: $scope.createUpdateDto
                        }).success(function (response, status, headers, config) {
                            if (status === 200) {
                                $http.get('/pla/grouphealth/claim/cashless/preauthorizationrequest/getpreauthorizationclaimantdetailcommandfrompreauthorizationrequestid?preAuthorizationId=' + preAuthorizationId)
                                    .success(function (response, status, headers, config) {
                                        $scope.createUpdateDto = response;
                                        if (status == 200) {
                                            setTimeout(function () {
                                                window.location.href = '/pla/grouphealth/claim/cashless/preauthorizationrequest/underwriter/getdefaultlistofunderwriterlevels/'+$scope.levelOfUnderwriter;
                                            }, 2000);
                                        }
                                    }).error(function (response, status, headers, config) {
                                });
                            }
                        }).error(
                            function (status) {
                                //console.log(status);
                            }
                        );
                    });
                };

                $scope.rejectionEmailSent = false;
                $scope.checkRejectionEmailSent = function(){
                    $http.get('/pla/grouphealth/claim/cashless/preauthorizationrequest/underwriter/checkifpreauthorizationrejectionemailsent/' + preAuthorizationId)
                        .success(function(response) {
                            if(response.data === true){
                                $scope.rejectionEmailSent = true;
                            }
                        }).error(function(response){
                        $scope.rejectionEmailSent = false;
                    });
                };



                $scope.underwriterReturn = function () {
                    if (!$scope.comment.comments) {
                        $scope.message = "Comment is mandatory to return Pre-Authorization.";
                        $scope.toggleModal();
                    } else{
                        var requirementAdded = $scope.isRequirementAdded($scope.documentList);
                        if(requirementAdded) {
                            $http.get('/pla/grouphealth/claim/cashless/preauthorizationrequest/underwriter/checkifpreauthorizationrequirementemailsent/' + preAuthorizationId)
                                .success(function(response){
                                    if(response.data === true){
                                        $scope.returnPreAuthorization();
                                    } else{
                                        var win = window.open('/pla/grouphealth/claim/cashless/preauthorizationrequest/underwriter/getaddrequirementrequestletter/'+preAuthorizationId,"_blank","toolbar=no,resizable=no," +
                                            "scrollable=no,menubar=no,personalbar=no,dependent=yes,dialog=yes,split=no,titlebar=no,resizable=no,location=no,left=100px");
                                        var timer = setInterval(function(){
                                            if(win.closed){
                                                clearInterval(timer);
                                                $http.get('/pla/grouphealth/claim/cashless/preauthorizationrequest/underwriter/checkifpreauthorizationrequirementemailsent/' + preAuthorizationId)
                                                    .success(function(response){
                                                        if(response.data === true){
                                                            $scope.returnPreAuthorization();
                                                        } else{
                                                            $scope.message = " Please email the requirement letter.";
                                                            $scope.toggleModal();
                                                        }
                                                    }).error();
                                            }
                                        }, 500);
                                    }
                                }).error();
                        } else {
                            $scope.returnPreAuthorization();
                        }
                    }
                };

                $scope.returnPreAuthorization = function(){
                    $.when($scope.constructCommentDetails()).done(function () {
                        $http({
                            url: '/pla/grouphealth/claim/cashless/preauthorizationrequest/underwriter/return',
                            method: 'POST',
                            data: $scope.createUpdateDto
                        }).success(function (response, status, headers, config) {
                            if (status === 200) {
                                $http.get('/pla/grouphealth/claim/cashless/preauthorizationrequest/getpreauthorizationclaimantdetailcommandfrompreauthorizationrequestid?preAuthorizationId=' + preAuthorizationId)
                                    .success(function (response, status, headers, config) {
                                        $scope.createUpdateDto = response;
                                        if (status == 200) {
                                            setTimeout(function () {
                                                window.location.href = '/pla/grouphealth/claim/cashless/preauthorizationrequest/underwriter/getdefaultlistofunderwriterlevels/'+$scope.levelOfUnderwriter;
                                            }, 2000);
                                        }
                                    }).error(function (response, status, headers, config) {
                                });
                            }
                        }).error(
                            function (status) {
                                //console.log(status);
                            }
                        );
                    });
                };

                $scope.isRequirementAdded = function (documentList) {
                    for (var i = 0; i < documentList.length; i++) {
                        var document = documentList[i];
                        if (document.fileName == null || document.content == null) {
                            return true;
                        }
                    }
                    return false;
                };

                $scope.underwriterRouteSenior = function () {
                    if (!$scope.comment.comments) {
                        $scope.message = "Comment is mandatory to route Pre-Authorization to senior Underwriter.";
                        $scope.toggleModal();
                    } else {
                        $.when($scope.constructCommentDetails()).done(function () {
                            $http({
                                url: '/pla/grouphealth/claim/cashless/preauthorizationrequest/underwriter/routetoseniorunderwriter',
                                method: 'POST',
                                data: $scope.createUpdateDto
                            }).success(function (response, status, headers, config) {
                                if (status === 200) {
                                    $http.get('/pla/grouphealth/claim/cashless/preauthorizationrequest/getpreauthorizationclaimantdetailcommandfrompreauthorizationrequestid?preAuthorizationId=' + preAuthorizationId)
                                        .success(function (response, status, headers, config) {
                                            $scope.createUpdateDto = response;
                                            if (status == 200) {
                                                setTimeout(function () {
                                                    window.location.href = '/pla/grouphealth/claim/cashless/preauthorizationrequest/underwriter/getdefaultlistofunderwriterlevels/LEVEL1';
                                                }, 1000);
                                            }
                                        }).error(function (response, status, headers, config) {
                                    });
                                }
                            }).error(
                                function (status) {
                                    //console.log(status);
                                }
                            );
                        });
                    }
                };

                $scope.addRequirement = function () {
                    $.when($scope.constructCommentDetails()).done(function(){
                        $http({
                            url: '/pla/grouphealth/claim/cashless/preauthorizationrequest/underwriter/addrequirement',
                            method: 'POST',
                            data: $scope.createUpdateDto
                        }).success(function(response, status, headers, config) {
                            if(status === 200) {
                                $http.get('/pla/grouphealth/claim/cashless/preauthorizationrequest/getpreauthorizationclaimantdetailcommandfrompreauthorizationrequestid?preAuthorizationId=' + preAuthorizationId)
                                    .success(function (response, status, headers, config) {
                                        $scope.createUpdateDto = response;
                                        $http.get("/pla/grouphealth/claim/cashless/preauthorizationrequest/getmandatorydocuments/" + clientId + "/" + preAuthorizationId).success(function (response, status, headers, config) {
                                            $scope.documentList = response;
                                            $scope.getAllDocuments();
                                        });
                                    }).error(function (response, status, headers, config) {
                                });
                            }
                        }).error(
                            function(status){
                                //console.log(status);
                            }
                        );
                        //console.log($scope.createUpdateDto);
                    });
                };

                $scope.constructCommentDetails = function() {
                    if ($scope.comment) {
                        if ($scope.createUpdateDto.commentDetails) {
                            $scope.createUpdateDto.commentDetails.push($scope.comment);
                        } else {
                            $scope.createUpdateDto.commentDetails = new Array($scope.comment);
                        }
                    }
                };

                $scope.populateDocumentSelected = function(data){
                    $scope.additionalDocumentAskedFor = {};
                    console.log(data);
                    if (data.originalObject) {
                        if ($scope.createUpdateDto.additionalRequiredDocuments.length > 0) {
                            $scope.additionalDocumentAskedFor.documentCode = data.originalObject.documentCode;
                            $scope.additionalDocumentAskedFor.documentName = data.originalObject.documentName;
                            $scope.createUpdateDto.additionalRequiredDocuments.push($scope.additionalDocumentAskedFor);
                        } else {
                            $scope.additionalDocumentAskedFor.documentCode = data.originalObject.documentCode;
                            $scope.additionalDocumentAskedFor.documentName = data.originalObject.documentName;
                            $scope.createUpdateDto.additionalRequiredDocuments = new Array($scope.additionalDocumentAskedFor);
                        }
                    }
                };
                var mode = getQueryParameter("mode");
                if (mode == 'view'){
                    $scope.isViewMode=true;

                }

                $scope.disableHtn = true;
                //$scope.createUpdateDto.illnessDetailDto.htndetail=$scope.createUpdateDto.illnessDetailDto.htndetails;
                $scope.$watch(
                    function(){
                        return {htn: $scope.createUpdateDto.illnessDetailDto.htn, htnDetails: $scope.createUpdateDto.illnessDetailDto.htndetails};
                    },
                    function(newVal, oldVal) {
                        if(newVal.htn === 'No'){
                            $scope.disableHtn = true;
                            $scope.createUpdateDto.illnessDetailDto.htndetails='';

                        }
                        else {
                            //$scope.createUpdateDto.illnessDetailDto.htndetails=$scope.createUpdateDto.illnessDetailDto.htndetail;
                            $scope.disableHtn = false;
                        }
                    },
                    true
                );

                $scope.disableIhd = true;
                $scope.$watch(
                    function(){
                        return {idh: $scope.createUpdateDto.illnessDetailDto.idhHOD, htnDetails: $scope.createUpdateDto.illnessDetailDto.ihdhoddetails};
                    },
                    function(newVal, oldVal) {
                        if(newVal.idh === 'No'){
                            $scope.disableIhd = true;
                            $scope.createUpdateDto.illnessDetailDto.ihdhoddetails='';
                        }
                        else {
                            $scope.disableIhd = false;
                        }
                    },
                    true
                );

                $scope.disdibetes= true;
                $scope.$watch(
                    function(){
                        return {Dibetes: $scope.createUpdateDto.illnessDetailDto.diabetes, diabetesDetails: $scope.createUpdateDto.illnessDetailDto.diabetesDetails};
                    },
                    function(newVal, oldVal) {
                        if(newVal.Dibetes === 'No'){
                            $scope.disdibetes = true;
                            $scope.createUpdateDto.illnessDetailDto.diabetesDetails='';
                        }
                        else {
                            $scope.disdibetes = false;
                        }
                    },
                    true
                );

                $scope.disAsthma= true;
                $scope.$watch(
                    function(){
                        return {Asthma: $scope.createUpdateDto.illnessDetailDto.asthmaCOPDTB, asthmaCOPDTBDetails: $scope.createUpdateDto.illnessDetailDto.asthmaCOPDTBDetails};
                    },
                    function(newVal, oldVal) {
                        if(newVal.Asthma === 'No'){
                            $scope.disAsthma = true;
                            $scope.createUpdateDto.illnessDetailDto.asthmaCOPDTBDetails='';
                        }
                        else {
                            $scope.disAsthma = false;
                        }
                    },
                    true
                );

                $scope.disStd= true;
                $scope.$watch(
                    function(){
                        return {Std: $scope.createUpdateDto.illnessDetailDto.stdhivaids, stdhivaidsdetails: $scope.createUpdateDto.illnessDetailDto.stdhivaidsdetails};
                    },
                    function(newVal, oldVal) {
                        if(newVal.Std === 'No'){
                            $scope.disStd = true;
                            $scope.createUpdateDto.illnessDetailDto.stdhivaidsdetails='';
                        }
                        else {
                            $scope.disStd = false;
                        }
                    },
                    true
                );

                $scope.disArthiritis= true;
                $scope.$watch(
                    function(){
                        return {Arthiritis: $scope.createUpdateDto.illnessDetailDto.arthritis, stdhivaidsdetails: $scope.createUpdateDto.illnessDetailDto.arthritisDetails};
                    },
                    function(newVal, oldVal) {
                        if(newVal.Arthiritis === 'No'){
                            $scope.disArthiritis = true;
                            $scope.createUpdateDto.illnessDetailDto.arthritisDetails='';
                        }
                        else {
                            $scope.disArthiritis = false;
                        }
                    },
                    true
                );

                $scope.disCancer= true;
                $scope.$watch(
                    function(){
                        return {Cancer: $scope.createUpdateDto.illnessDetailDto.cancerTumorCyst, cancerTumorCystDetail: $scope.createUpdateDto.illnessDetailDto.cancerTumorCystDetails};
                    },
                    function(newVal, oldVal) {
                        if(newVal.Cancer === 'No'){
                            $scope.disCancer = true;
                            $scope.createUpdateDto.illnessDetailDto.cancerTumorCystDetails='';
                        }
                        else {
                            $scope.disCancer = false;
                        }
                    },
                    true
                );

                $scope.dispshyciatric= true;
                $scope.$watch(
                    function(){
                        return {pshyciatric: $scope.createUpdateDto.illnessDetailDto.psychiatricCondition, psychiatricConditionDetails: $scope.createUpdateDto.illnessDetailDto.psychiatricConditionDetails};
                    },
                    function(newVal, oldVal) {
                        if(newVal.pshyciatric === 'No'){
                            $scope.dispshyciatric = true;
                            $scope.createUpdateDto.illnessDetailDto.psychiatricConditionDetails='';
                        }
                        else {
                            $scope.dispshyciatric = false;
                        }
                    },
                    true
                );

                $scope.disAlcohol= true;
                $scope.$watch(
                    function(){
                        return {Alcohol: $scope.createUpdateDto.illnessDetailDto.alcoholDrugAbuse, alcoholDrugAbuseDetails: $scope.createUpdateDto.illnessDetailDto.alcoholDrugAbuseDetails};
                    },
                    function(newVal, oldVal) {
                        if(newVal.Alcohol === 'No'){
                            $scope.disAlcohol = true;
                            $scope.createUpdateDto.illnessDetailDto.alcoholDrugAbuseDetails='';
                        }
                        else {
                            $scope.disAlcohol = false;
                        }
                    },
                    true
                );

                if(false){
                    $('#plaModal').modal({backdrop: 'static', keyboard: false})

                }
                $scope.ok = function(){
                    window.location.href = '/pla/grouphealth/claim/cashless/claim/underwriter/getdefaultlistofunderwriterlevels/LEVEL1';
                }




            }])
})(angular);
function formatDate(date) {
    var d = new Date(date),
        month = '' + (d.getMonth() + 1),
        day = '' + d.getDate(),
        year = d.getFullYear();

    if (month.length < 2) month = '0' + month;
    if (day.length < 2) day = '0' + day;

    return [year, month, day].join('-');
}
