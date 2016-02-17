
var searchPreAuthorizationModule = (function(){
    var PreAuthorizationServices = {};
    PreAuthorizationServices.PreAuthorizationSelected = undefined;
    this.PreAuthorizationName = null;
    PreAuthorizationServices.getTheItemSelected = function(ele){
        this.groupHealthCashlessClaimId=$(ele).val();
        this.clientId=$(ele).attr("clientId");
        $("#PreAuthorization-view").prop("disabled","");
        $("#PreAuthorization-create").prop("disabled","");
        $(".btn-disabled").attr("disabled", false);
        this.PreAuthorizationName = $(ele).parent().find('input[type=hidden]').val();
    };
    PreAuthorizationServices.updatePreAuthorization = function () {
        var  clientId =this.clientId;
        var  groupHealthCashlessClaimId =this.groupHealthCashlessClaimId;
        window.location.href = "/pla/grouphealth/claim/cashless/claim/updateviewservicemismatch?groupHealthCashlessClaimId=" + groupHealthCashlessClaimId +"&clientId="+ clientId +"&mode=edit";
    };

    PreAuthorizationServices.viewPreAuthorization =  function(){//activate deactive when click radio button
        var  clientId =this.clientId;
        var  preAuthorizationId =this.PreAuthorizationSelected;
        window.location.href = "/pla/grouphealth/claim/cashless/updateviewservicemismatch?groupHealthCashlessClaimId=" + groupHealthCashlessClaimId +"&clientId="+ clientId+"&mode=view";


    };

    PreAuthorizationServices.emailPolicy = function () {
        var  preAuthorizationId =this.PreAuthorizationSelected;
        //  window.location.href = "/pla/grouplife/policy/viewpolicy?policyId=" + policyId  + "&mode=view";

        window.open('/pla/grouplife/policy/openemailpolicy/'+preAuthorizationId,"_blank","toolbar=no,resizable=no," +
        "scrollable=no,menubar=no,personalbar=no,dependent=yes,dialog=yes,split=no,titlebar=no,resizable=no,location=no,left=100px");

    };

    PreAuthorizationServices.reload = function(){
        window.location.reload();
    };

    return PreAuthorizationServices;
})();