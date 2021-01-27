'use strict';

angular.module('cases').controller('Cases.SuggestedCasesController', ['$scope', '$translate', '$stateParams', 'Helper.UiGridService', 'UtilService', 'Helper.ObjectBrowserService', 'Case.InfoService', 'SuggestedObjectsService',
    function ($scope, $translate, $stateParams, HelperUiGridService, Util, HelperObjectBrowserService, CaseInfoService, SuggestedObjectsService) {


        new HelperObjectBrowserService.Component({
            scope: $scope,
            stateParams: $stateParams,
            moduleId: "cases",
            componentId: "suggestedCases",
            retrieveObjectInfo: CaseInfoService.getCaseInfo,
            validateObjectInfo: CaseInfoService.validateCaseInfo,
            onConfigRetrieved: function (componentConfig) {
                return onConfigRetrieved(componentConfig);
            },
            onObjectInfoRetrieved: function(objectInfo) {
                onObjectInfoRetrieved(objectInfo);
            }
        });

        var gridHelper = new HelperUiGridService.Grid({
            scope: $scope
        });
        var onConfigRetrieved = function(config) {

            $scope.config = config;
            $scope.objectTypeValue = config.objectTypeValue;

            gridHelper.setColumnDefs(config);
            gridHelper.setBasicOptions(config);
            gridHelper.disableGridScrolling(config);

            $scope.gridOptions = {
                enableColumnResizing: true,
                enableRowSelection: true,
                enableRowHeaderSelection: false,
                multiSelect: false,
                noUnselect: false,
                columnDefs: $scope.config.columnDefs,
                paginationPageSizes: $scope.config.paginationPageSizes,
                paginationPageSize: $scope.config.paginationPageSize,
                totalItems: 0,
                data: []
            };
        };

        function retrieveGridData(){
            SuggestedObjectsService.getSuggestedObjects($scope.objectInfo.title, "CASE_FILE", $scope.objectInfo.id).then(function (data) {
                $scope.suggestedCases = data.data;
                $scope.gridOptions = $scope.gridOptions || {};
                $scope.gridOptions.data = $scope.suggestedCases;
                $scope.gridOptions.totalItems = $scope.suggestedCases.length;
            });
        }

        $scope.getObjectTypeValue = function (key) {
            return $scope.objectTypeValue[key];
        };

        var onObjectInfoRetrieved = function(objectInfo) {
            $scope.objectInfo = objectInfo;
            retrieveGridData();
        };

        $scope.onClickObjLink = function(event, rowEntity) {
            event.preventDefault();

            var targetType = Util.goodMapValue(rowEntity, "type");
            var targetId = Util.goodMapValue(rowEntity, "id");
            gridHelper.showObject(targetType, targetId);
        };
       
    }]);