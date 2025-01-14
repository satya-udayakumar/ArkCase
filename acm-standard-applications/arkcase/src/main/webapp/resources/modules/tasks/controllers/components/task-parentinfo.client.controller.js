'use strict';

angular.module('tasks').controller(
    'Tasks.ParentInfoController',
    ['$scope', '$stateParams', '$translate', 'UtilService', 'ConfigService', 'ObjectService', 'Case.InfoService', 'Complaint.InfoService', 'Task.InfoService', 'CostTracking.InfoService', 'TimeTracking.InfoService', 'Object.ModelService', 'LookupService', 'Helper.ObjectBrowserService', 'Consultation.InfoService',
        'MessageService', function ($scope, $stateParams, $translate, Util, ConfigService, ObjectService, CaseInfoService, ComplaintInfoService, TaskInfoService, CostTrackingInfoService, TimeTrackingInfoService, ObjectModelService, LookupService, HelperObjectBrowserService, ConsultationInfoService, MessageService) {

        new HelperObjectBrowserService.Component({
            moduleId: "tasks",
            componentId: "parentinfo",
            scope: $scope,
            stateParams: $stateParams,
            retrieveObjectInfo: TaskInfoService.getTaskInfo,
            validateObjectInfo: TaskInfoService.validateTaskInfo,
            onObjectInfoRetrieved: function (objectInfo) {
                onObjectInfoRetrieved(objectInfo);
            }
        });

        LookupService.getUsers().then(function (users) {
            var options = [];
            _.each(users, function (user) {
                options.push({
                    object_id_s: user.object_id_s,
                    name: user.name
                });
            });
            $scope.assignableUsers = options;
            return users;
        });

        $scope.onClickTitle = function () {
            if ($scope.parentCaseInfo) {
                ObjectService.showObject(ObjectService.ObjectTypes.CASE_FILE, $scope.parentCaseInfo.id);
            } else if ($scope.parentComplaintInfo) {
                ObjectService.showObject(ObjectService.ObjectTypes.COMPLAINT, $scope.parentComplaintInfo.complaintId);
            } else if ($scope.parentCostsheetInfo) {
                ObjectService.showObject(ObjectService.ObjectTypes.COSTSHEET, $scope.parentCostsheetInfo.id);
            } else if ($scope.parentTimesheetInfo) {
                ObjectService.showObject(ObjectService.ObjectTypes.TIMESHEET, $scope.parentTimesheetInfo.id);
            } else if ($scope.parentConsultationInfo) {
                ObjectService.showObject(ObjectService.ObjectTypes.CONSULTATION, $scope.parentConsultationInfo.id);
            } else {
                $log.error('parentCaseInfo is undefined, cannot redirect to the parent case');
            }
        };

        var onObjectInfoRetrieved = function (objectInfo) {
            $scope.objectInfo = objectInfo;

            if (Util.isEmpty($scope.objectInfo.parentObjectId)) {
                return;
            }

            if (ObjectService.ObjectTypes.CASE_FILE == $scope.objectInfo.parentObjectType) {
                var caseInfo = {
                    id: $scope.objectInfo.parentObjectId
                };
                CaseInfoService.resetCaseInfo(caseInfo);
                CaseInfoService.getCaseInfo($scope.objectInfo.parentObjectId).then(function (caseInfo) {
                    $scope.parentCaseInfo = caseInfo;
                    $scope.owningGroup = ObjectModelService.getGroup(caseInfo);
                    $scope.assignee = ObjectModelService.getAssignee(caseInfo);
                    return caseInfo;
                });
            } else if (ObjectService.ObjectTypes.CONSULTATION == $scope.objectInfo.parentObjectType) {
                ConsultationInfoService.getConsultationInfo($scope.objectInfo.parentObjectId).then(function (consultationInfo) {
                    $scope.parentConsultationInfo = consultationInfo;
                    $scope.owningGroup = ObjectModelService.getGroup(consultationInfo);
                    $scope.assignee = ObjectModelService.getAssignee(consultationInfo);
                    return consultationInfo;
                });
            } else if (ObjectService.ObjectTypes.COMPLAINT == $scope.objectInfo.parentObjectType) {
                ComplaintInfoService.getComplaintInfo($scope.objectInfo.parentObjectId).then(function (complaintInfo) {
                    $scope.parentComplaintInfo = complaintInfo;
                    $scope.owningGroup = ObjectModelService.getGroup(complaintInfo);
                    $scope.assignee = ObjectModelService.getAssignee(complaintInfo);
                    return complaintInfo;
                });
            } else if (ObjectService.ObjectTypes.COSTSHEET == $scope.objectInfo.parentObjectType) {
                CostTrackingInfoService.getCostsheetInfo($scope.objectInfo.parentObjectId).then(function (costsheetInfo) {
                    $scope.parentCostsheetInfo = costsheetInfo;
                    $scope.costsheetApprover = ObjectModelService.getParticipantByType(costsheetInfo, "approver");
                    return costsheetInfo;
                });
            } else if (ObjectService.ObjectTypes.TIMESHEET == $scope.objectInfo.parentObjectType) {
                TimeTrackingInfoService.getTimesheetInfo($scope.objectInfo.parentObjectId).then(function(timesheetInfo) {
                    $scope.parentTimesheetInfo = timesheetInfo;
                    $scope.timesheetApprover = ObjectModelService.getParticipantByType(timesheetInfo, "approver");
                    return timesheetInfo;
                });
            }

                        var parentObjectType = $scope.objectInfo.parentObjectType;
                        var parentObjectId = $scope.objectInfo.parentObjectId;
                        var parentObjectName = $scope.objectInfo.parentObjectName;
                        var eventName = "object.changed/" + parentObjectType + "/" + parentObjectId;
                        var objectTypeString = $translate.instant('common.objectTypes.' + parentObjectType);
                        if (!objectTypeString) {
                            objectTypeString = parentObjectType;
                        }
                        $scope.$bus.subscribe(eventName, function(data) {
                            MessageService.info(objectTypeString + " with number " + parentObjectName + " was updated.");
                            $scope.$emit('report-object-refreshed', $stateParams.id);
                        });
                    };
                } ]);