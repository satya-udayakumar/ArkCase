'use strict';

angular.module('cases').controller('Cases.AddAppealDispositionCategoriesModalController',
    ['$scope', '$modal', '$modalInstance', 'params', '$q', 'UtilService', 'Object.LookupService', 'Case.ExemptionService', '$translate', 'MessageService',
        function ($scope, $modal, $modalInstance, params, $q, Util, ObjectLookupService, CaseExemptionService, $translate, MessageService) {

            $scope.isDispositionRequired = params.isDispositionRequired;
            $scope.caseId = params.caseId;

            $scope.isCustomDisabled = true;

            $scope.objectInfo = {};

            ObjectLookupService.getLookupByLookupName('appealDispositionType').then(function (appealDispositionType) {
                $scope.dispositionCategoriesLookup = appealDispositionType;
                if (!Util.isEmpty(params.disposition)) {
                    $scope.objectInfo.disposition = params.disposition;
                } else {
                    $scope.objectInfo.disposition = $scope.dispositionCategoriesLookup[0].key;
                }

                // Can only select reasons when the disposition is closed (UI label is "Closed for Other Reasons")
                $scope.showDispositionReasons = (params.disposition === 'closed');
            });

            ObjectLookupService.getAppealDispositionReasons().then(function (appealDispositionReasons) {
                $scope.appealDispositionReasonsLookup = appealDispositionReasons;
            });

            ObjectLookupService.getAppealOtherReasons().then(function (appealOtherReasons) {
                $scope.otherReasonsLookup = appealOtherReasons;

                if (!Util.isEmpty(params.otherReason)) {
                    var found = _.find(appealOtherReasons, {
                        key: params.otherReason
                    });
                    if (found) {
                        $scope.objectInfo.otherReason = params.otherReason;
                    } else {
                        $scope.objectInfo.otherReason = 'custom';
                        $scope.customOtherReason = params.otherReason;
                    }

                } else {
                    $scope.objectInfo.otherReason = $scope.otherReasonsLookup[0].key;
                }

                if (Util.isEmpty($scope.customOtherReason)) {
                    $scope.isCustomDisabled = true;
                } else {
                    $scope.isCustomDisabled = false;
                }
            });

            if (!Util.isArrayEmpty(params.dispositionReasons)) {
                $scope.objectInfo.dispositionReasons = angular.copy(params.dispositionReasons);
            } else {
                $scope.objectInfo.dispositionReasons = [];
            }

            var otherExistsInReason = _.some($scope.objectInfo.dispositionReasons, function (value) {
                return value.reason === 'other';
            });

            if (otherExistsInReason) {
                $scope.isOtherRequired = true;
            } else {
                $scope.isOtherRequired = false;
            }

            var promiseExemptionCodes = CaseExemptionService.getExemptionCode(params.caseId, 'CASE_FILE');
            $q.all([promiseExemptionCodes]).then(function (data) {
                $scope.hasExemptionCodes = data[0].data.length > 0 ? true : false;
            });

            $scope.changeDispositionCategory = function () {

                // Can only select reasons when the disposition is closed (UI label is "Closed for Other Reasons")
                $scope.showDispositionReasons = ($scope.objectInfo.disposition === 'closed');
                $scope.isReasonRequired = $scope.showDispositionReasons && Util.isArrayEmpty($scope.objectInfo.dispositionReasons);

                if ($scope.isReasonRequired) {
                    $scope.reasonsMessage = "Enter at least one reason";
                } else {
                    $scope.reasonsMessage = null;
                }
            };

            $scope.onReasonSelected = function (reason) {
                if (reason) {
                    var reasonExists = _.some($scope.objectInfo.dispositionReasons, function (value) {
                        return value.reason === reason;
                    });

                    if (!reasonExists) {
                        var dispositionReason = {
                            reason: reason,
                            caseId: $scope.caseId,
                            requestType: 'Appeal'
                        };

                        $scope.objectInfo.dispositionReasons.push(dispositionReason);

                        $scope.isReasonRequired = false;

                        if (reason === 'other') {
                            $scope.isOtherRequired = true;
                        } else {
                            $scope.isOtherRequired = false;
                        }
                    } else {
                        _.forEach($scope.objectInfo.dispositionReasons, function (disReason, i) {
                            if (Util.compare(disReason.reason, reason)) {
                                $scope.objectInfo.dispositionReasons.splice(i, 1);
                                return false;
                            }
                        });

                        $scope.changeDispositionCategory();

                        //if other was unchecked in reasons
                        if (reason === 'other') {
                            $scope.isOtherRequired = false;
                            $scope.objectInfo.otherReason = null;
                            $scope.isCustomDisabled = true;
                            $scope.customOtherReason = null;
                        }
                    }

                    if ($scope.isReasonRequired) {
                        $scope.reasonsMessage = "Enter at least one reason";
                    } else {
                        $scope.reasonsMessage = null;
                    }
                }
            };

            $scope.isChecked = function (reason) {
                return _.some($scope.objectInfo.dispositionReasons, function (value) {
                    return value.reason === reason;
                });
            };

            $scope.changeOtherReason = function () {
                if (Util.isEmpty($scope.objectInfo.otherReason)) {
                    var otherExistsInReason = _.some($scope.objectInfo.dispositionReasons, function (value) {
                        return value.reason === 'other';
                    });

                    if (otherExistsInReason) {
                        $scope.isOtherRequired = true;
                    } else {
                        $scope.isOtherRequired = false;
                    }
                }
                if ($scope.objectInfo.otherReason === 'custom') {
                    $scope.isCustomDisabled = false;
                } else {
                    $scope.isCustomDisabled = true;
                    $scope.customOtherReason = null;
                }
            };

            $scope.onClickOk = function () {
                var disposition = _.find($scope.dispositionCategoriesLookup, {
                    key: $scope.objectInfo.disposition
                });

                if ($scope.objectInfo.disposition === 'affirmed' || $scope.objectInfo.disposition === 'partially-affirmed') {
                    if (Util.isArrayEmpty($scope.objectInfo.dispositionReasons) && !$scope.hasExemptionCodes) {
                        MessageService.info($translate.instant("core.lookups.info.validationInfo"));
                        $modalInstance.dismiss();
                    }
                }

                // Removes reasons if saving a disposition other than closed (UI label is "Closed for Other Reasons")
                if ($scope.objectInfo.disposition !== 'closed') {
                    if (!Util.isArrayEmpty($scope.objectInfo.dispositionReasons)) {
                        $scope.objectInfo.dispositionReasons = [];
                    }
                    $scope.objectInfo.otherReason = null;
                }

                var data = {
                    disposition: $scope.objectInfo.disposition,
                    dispositionValue: $translate.instant(disposition.value),
                    otherReason: $scope.objectInfo.otherReason,
                    dispositionReasons: $scope.objectInfo.dispositionReasons,
                    showDispositionReasonsFlag: $scope.showDispositionReasons
                };

                if ($scope.objectInfo.otherReason === 'custom') {
                    data.otherReason = $scope.customOtherReason;
                }

                $modalInstance.close(data);
            };

            $scope.onClickCancel = function () {
                $modalInstance.dismiss('Cancel');
            };
        }]);