'use strict';
/**
 * @ngdoc controller
 * @name dashboard.my-consultations.controller:Dashboard.MyConsultationsController
 *
 * @description
 *
 * {@link /acm-standard-applications/arkcase/src/main/webapp/resources/modules/dashboard/controllers/components/my-consultations.client.controller.js modules/dashboard/controllers/components/my-consultations.client.controller.js}
 *
 * Loads consultations in the "My Consultations" widget.
 */
angular.module('dashboard.my-consultations').controller('Dashboard.MyConsultationsController', [ '$scope', '$translate', 'config', 'Authentication', 'Dashboard.DashboardService', 'Task.AlertsService', 'Util.DateService', 'ConfigService', 'params', 'UtilService', function($scope, $translate, config, Authentication, DashboardService, TaskAlertsService, UtilDateService, ConfigService, params, Util) {
    var vm = this;
    vm.config = null;
    var userInfo = null;
    //var userGroups = null;
    var userGroupList = null;

    if (!Util.isEmpty(params.description)) {
        $scope.$parent.model.description = " - " + params.description;
    } else {
        $scope.$parent.model.description = "";
    }

    ConfigService.getComponentConfig("dashboard", "myConsultations").then(function(config) {
        vm.config = config;
        vm.gridOptions.columnDefs = config.columnDefs;
        vm.gridOptions.enableFiltering = config.enableFiltering;
        vm.gridOptions.paginationPageSizes = config.paginationPageSizes;
        vm.gridOptions.paginationPageSize = paginationOptions.pageSize;

        Authentication.queryUserInfo().then(function(responseUserInfo) {
            userInfo = responseUserInfo;
            var userGroups = _.filter(responseUserInfo.authorities, function(userGroup) {
                return _.startsWith(userGroup, 'ROLE') == false;
            });
            userGroupList = userGroups.join("\" OR \"");
            userGroupList = "(\"" + userGroupList + "\")";
            userGroupList = encodeURIComponent(userGroupList);

            getPage();
            return userInfo;
        });
    });

    var paginationOptions = {
        pageNumber: 1,
        pageSize: 5,
        sortBy: 'id',
        sortDir: 'desc'
    };

    //Get the user's defined options from the Config.
    if (config.paginationPageSize) {
        paginationOptions.pageSize = parseInt(config.paginationPageSize);
    } else {
        //defaults the dropdown value on edit UI to the default pagination options
        config.paginationPageSize = "" + paginationOptions.pageSize + "";
    }

    /**
     * @ngdoc method
     * @name openViewer
     * @methodOf dashboard.my-consultations.controller:Dashboard.MyConsultationsController
     *
     * @param {Object} data from the current row of the ui-grid (including the file id)
     *
     * @description
     * This method opens the selected file in the snowbound viewer
     */
    vm.openViewer = function(rowData) {
        if (rowData && rowData.entity.object_id_s) {
            window.open(window.location.href.split('!')[0] + '!/consultations/' + rowData.entity.object_id_s + '/main', '_self');
        }
    };

    var rowTmpl = '<div ng-class="{\'overdue\':row.entity.isOverdue, \'deadline\':row.entity.isDeadline}"><div ng-repeat="(colRenderIndex, col) in colContainer.renderedColumns track by col.colDef.name" class="ui-grid-cell" ng-class="{ \'ui-grid-row-header-cell\': col.isRowHeader }" ui-grid-cell></div></div>';

    vm.gridOptions = {
        enableColumnResizing: true,
        enableRowSelection: true,
        enableSelectAll: false,
        enableRowHeaderSelection: false,
        useExternalPagination: true,
        useExternalSorting: true,
        multiSelect: false,
        noUnselect: false,
        columnDefs: [],
        rowTemplate: rowTmpl,
        onRegisterApi: function(gridApi) {
            vm.gridApi = gridApi;
            gridApi.core.on.sortChanged($scope, function(grid, sortColumns) {
                if (sortColumns.length == 0) {
                    paginationOptions.sort = null;
                } else {
                    paginationOptions.sortBy = sortColumns[0].name;
                    paginationOptions.sortDir = sortColumns[0].sort.direction;
                }
                getPage();
            });
            gridApi.pagination.on.paginationChanged($scope, function(newPage, pageSize) {
                paginationOptions.pageNumber = newPage;
                paginationOptions.pageSize = pageSize;
                getPage();
            });
        }
    };

    function getPage() {
        DashboardService.queryMyConsultations({
            userId: userInfo.userId,
            userGroupList: userGroupList,
            sortBy: paginationOptions.sortBy,
            sortDir: paginationOptions.sortDir,
            startWith: (paginationOptions.pageNumber - 1) * paginationOptions.pageSize,
            pageSize: paginationOptions.pageSize
        }, function(data) {
            vm.gridOptions.data = [];
            vm.gridOptions.totalItems = data.response.numFound;

            _.forEach(data.response.docs, function(value) {
                value.status_lcs = value.status_lcs.toUpperCase();

                if (Util.goodValue(value.dueDate_tdt)) {
                    value.dueDate_tdt = UtilDateService.isoToLocalDateTime(value.dueDate_tdt);
                }

                //calculate to show alert icons if consultations is in overdue or deadline is approaching
                value.isOverdue = TaskAlertsService.calculateOverdue(value.dueDate_tdt);
                value.isDeadline = TaskAlertsService.calculateDeadline(value.dueDate_tdt);

                vm.gridOptions.data.push(value);
            });
        });
    }
} ]);
