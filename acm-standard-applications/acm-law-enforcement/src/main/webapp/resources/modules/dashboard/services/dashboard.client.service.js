'use strict';

angular.module('dashboard').factory('Dashboard.DashboardService', ['$resource',
    function ($resource) {
        return $resource('', {}, {
            getConfig: {
                method: 'GET',
                url: 'api/v1/plugin/dashboard/get',
                params: {
                    moduleName: "@moduleName"
                },
                data: ''
            },

            queryCasesByQueue: {
                method: 'GET',
                url: 'api/v1/plugin/casefile/number/by/queue',
                isArray: false,
                data: ''
            },

            queryCasesByStatus: {
                method: 'GET',
                url: 'api/v1/plugin/casebystatus/:period',
                isArray: true,
                data: ''
            },

            queryNewComplaints: {
                method: 'GET',
                url: 'api/v1/plugin/complaint/list/lastMonth',
                isArray: true,
                data: ''
            },

            queryMyTasks: {
                method: 'GET',
                url: 'api/v1/plugin/task/forUser/:userId',
                isArray: true,
                data: ''
            },

            queryMyComplaints: {
                method: 'GET',
                url: 'api/v1/plugin/complaint/forUser/:userId',
                isArray: true,
                data: ''
            },

            queryMyCases: {
                method: 'GET',
                url: 'api/v1/plugin/search/advancedSearch?q=assignee_id_lcs\\::userId+' +
                'AND+object_type_s\\:CASE_FILE+' +
                'AND+NOT+status_lcs\\:DRAFT&start=:startWith&n=:pageSize&s=:sortBy :sortDir',
                isArray: false,
                data: ''
            },

            queryTeamWorkload: {
                method: 'GET',
                url: 'api/v1/plugin/task/list/:due',
                isArray: true,
                data: ''
            },

            getWidgetsPerRoles: {
                method: 'GET',
                url: 'api/latest/plugin/dashboard/widgets/get',
                isArray: true,
            },

            saveConfig: {
                method: 'POST',
                url: 'api/v1/plugin/dashboard/set'
            }
        })
    }
]);