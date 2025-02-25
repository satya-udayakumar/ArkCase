'use strict';

angular.module('admin').controller(
    'Admin.LdapUserManagementController',
    ['$scope', '$q', '$modal', 'Admin.LdapUserManagementService', 'LookupService', 'MessageService', 'Acm.StoreService', 'UtilService', '$log', '$translate', 'Admin.LdapConfigService', 'Dialog.BootboxService',
        function ($scope, $q, $modal, LdapUserManagementService, LookupService, MessageService, Store, Util, $log, $translate, LdapConfigService, DialogService) {

            $scope.cloneUser = cloneUser;
            $scope.onObjSelect = onObjSelect;
            $scope.onAuthRoleSelected = onAuthRoleSelected;
            $scope.initUser = initUser;
            $scope.fillList = fillList;
            $scope.deleteUser = deleteUser;
            //scroll functions
            $scope.userScroll = userScroll;
            $scope.unauthorizedScroll = unauthorizedScroll;
            $scope.authorizedScroll = authorizedScroll;
            $scope.retrieveDataScroll = retrieveDataScroll;
            //filter functions
            $scope.userManagementFilter = userManagementFilter;
            $scope.userUnauthorizedFilter = userUnauthorizedFilter;
            $scope.userAuthorizedFilter = userAuthorizedFilter;
            $scope.retrieveDataFilter = retrieveDataFilter;

            function isUserManagementEnabled(directories, directory) {
                var dirData = _.find(directories, function (data) {
                    if(data["ldapConfig.id"] === directory){
                        return data;
                    }
                });
                return dirData ? dirData["ldapConfig.enableEditingLdapUsers"] : false;
            }

            var currentAuthGroups;
            var selectedUser;
            var userPageSize = 50;
            function initializeData() {
                $scope.makePaginationRequest = true;
                $scope.userFilterWord = null;
                $scope.unauthorizedFilterWord = null;
                $scope.authorizedFilterWord = null;
                $scope.lastSelectedUser = "";
                $scope.userData = {
                    "chooseObject": [],
                    "selectedNotAuthorized": [],
                    "selectedAuthorized": []
                };
                $scope.scrollLoadData = {
                    "loadObjectsScroll": $scope.userScroll,
                    "loadUnauthorizedScroll": $scope.unauthorizedScroll,
                    "loadAuthorizedScroll": $scope.authorizedScroll
                };
                $scope.filterData = {
                    "objectsFilter": $scope.userManagementFilter,
                    "unauthorizedFilter": $scope.userUnauthorizedFilter,
                    "authorizedFilter": $scope.userAuthorizedFilter
                };
            }
            initializeData();

            $scope.selectDirectory = function (directoryName) {
                initializeData();
                initUser(userPageSize, directoryName);
            };

            var directoriesConfigPromise = LdapConfigService.retrieveDirectories();

            directoriesConfigPromise.then(function(directories) {
                $scope.directories = Object.keys(directories.data).sort();
                $scope.directoryName = $scope.directoryName ? $scope.directoryName : $scope.directories[0];
                $scope.initUser(userPageSize, $scope.directoryName);
            });

            function initUser(userNumber, directoryName) {
                var userRequestInfo = {};
                userRequestInfo.n = Util.isEmpty(userNumber) ? 50 : userNumber;
                userRequestInfo.start = $scope.userData.chooseObject.length;
                if ($scope.makePaginationRequest) {
                    LdapUserManagementService.getNUsers(userRequestInfo, directoryName).then(function(response) {
                        $scope.fillList($scope.userData.chooseObject, response.data.response.docs);
                        if (_.isEmpty($scope.lastSelectedUser)) {
                            $scope.lastSelectedUser = $scope.userData.chooseObject[0];
                            $scope.onObjSelect($scope.lastSelectedUser);
                        }
                        $scope.makePaginationRequest = response.data.response.numFound > $scope.userData.chooseObject.length;
                    });
                }
            }

            //callback function when user is selected
            function onObjSelect(selectedObject) {
                $scope.userData.selectedAuthorized = [];
                $scope.userData.selectedNotAuthorized = [];

                if (!_.isEmpty($scope.userData.chooseObject)) {
                    var data = {};
                    data.member = selectedObject;
                    $scope.lastSelectedUser = selectedObject;
                    selectedUser = selectedObject;
                    currentAuthGroups = [];
                    data.isAuthorized = false;
                    var unAuthorizedGroupsForUser = LdapUserManagementService.getGroupsForUser(data);
                    data.isAuthorized = true;
                    var authorizedGroupsForUser = LdapUserManagementService.getGroupsForUser(data);
                    $q.all([ authorizedGroupsForUser, unAuthorizedGroupsForUser ]).then(function(result) {
                        _.forEach(result[0].data.response.docs, function(group) {
                            var authObject = {};
                            authObject.key = group.name;
                            authObject.name = group.name;
                            $scope.userData.selectedAuthorized.push(authObject);
                            currentAuthGroups.push(authObject.key);
                        });
                        _.forEach(result[1].data.response.docs, function(group) {
                            var authObject = {};
                            authObject.key = group.name;
                            authObject.name = group.name;
                            $scope.userData.selectedNotAuthorized.push(authObject);
                        });
                    });
                }
            }

            //callback function when groups are moved
            function onAuthRoleSelected(selectedObject, authorized, notAuthorized) {
                var toBeAdded = [];
                var toBeRemoved = [];
                var deferred = $q.defer();

                //get roles which needs to be added
                _.forEach(authorized, function(group) {
                    if (currentAuthGroups.indexOf(group.key) === -1) {
                        toBeAdded.push(group.key);
                    }
                });
                _.forEach(notAuthorized, function(group) {
                    if (currentAuthGroups.indexOf(group.key) !== -1) {
                        toBeRemoved.push(group.key);
                    }
                });
                //perform adding on server
                if (toBeAdded.length > 0) {
                    currentAuthGroups = currentAuthGroups.concat(toBeAdded);

                    LdapUserManagementService.addGroupsToUser(selectedObject.key, toBeAdded, selectedObject.directory).then(function(data) {
                        deferred.resolve(data);
                    }, function(error) {
                        //error adding group
                        deferred.reject(error);
                    });
                    return deferred.promise;
                }

                if (toBeRemoved.length > 0) {
                    _.forEach(toBeRemoved, function(element) {
                        currentAuthGroups.splice(currentAuthGroups.indexOf(element), 1);
                    });

                    LdapUserManagementService.removeGroupsFromUser(selectedObject.key, toBeRemoved, selectedObject.directory).then(function(data) {
                        deferred.resolve(data);
                    }, function(error) {
                        //error adding group
                        deferred.reject(error);
                    });
                    return deferred.promise;
                }
                return deferred.promise;
            }

            function openCloneUserModal(userForm, usernameError) {

                return $modal.open({
                    animation: $scope.animationsEnabled,
                    templateUrl: 'modules/admin/views/components/security.organizational-hierarchy.create-user.dialog.html',
                    controller: [ '$scope', '$modalInstance', 'UtilService', function($scope, $modalInstance, Util) {
                        $scope.enableUsernameEdit = true;
                        $scope.header = "admin.security.organizationalHierarchy.createUserDialog.addLdapMember.title";
                        $scope.okBtn = "admin.security.organizationalHierarchy.createUserDialog.addLdapMember.btn.ok";
                        $scope.cancelBtn = "admin.security.organizationalHierarchy.createUserDialog.addLdapMember.btn.cancel";
                        $scope.user = userForm;
                        $scope.usernameError = usernameError;
                        $scope.data = {
                            "user": $scope.user,
                            "selectedUser": selectedUser
                        };

                        $scope.clearUsernameError = function() {
                            if ($scope.error) {
                                $scope.error = '';
                            }
                        };

                        $scope.ok = function() {
                            $modalInstance.close($scope.data);
                        };
                    } ],
                    size: 'md',
                    backdrop: 'static'
                });
            }

            function onCloneUser(data, deferred) {
                LdapUserManagementService.cloneUser(data).then(function(response) {
                    // add the new user to the list
                    var element = {};
                    element.name = response.data.fullName;
                    element.key = response.data.userId;
                    element.directory = response.data.userDirectoryName;
                    $scope.userData.chooseObject.push(element);

                    //add the new user to cache store
                    var cacheUsers = new Store.SessionData(LookupService.SessionCacheNames.USERS);
                    var users = cacheUsers.get();
                    users.push(element);
                    cacheUsers.set(users);

                    MessageService.succsessAction();
                }, function(error) {
                    //error adding user
                    if (error.data.message) {
                        var usernameError;
                        var onAdd = function(data) {
                            return onCloneUser(data);
                        };
                        if (error.data.field == 'username') {
                            usernameError = error.data.message;
                            openCloneUserModal(error.data.extra.user, usernameError).result.then(onAdd, function() {
                                deferred.reject("cancel");
                                return {};
                            });
                        } else {
                            MessageService.error(error.data.message);
                        }
                    }
                    else {
                        MessageService.errorAction();
                    }
                });
            }

            function cloneUser() {
                directoriesConfigPromise.then(function (directories) {
                    var userManagementEnabled = isUserManagementEnabled(directories.data, selectedUser.directory);
                    if (userManagementEnabled) {
                        var modalInstance = openCloneUserModal({}, "");
                        var deferred = $q.defer();
                        modalInstance.result.then(function (data) {
                            onCloneUser(data, deferred);
                        }, function () {
                            // Cancel button was clicked
                        });
                    } else {
                        DialogService.alert($translate.instant("admin.security.ldap.user.management.alertMessage"));
                    }
                })
            }

            function fillList(listToFill, data) {
                _.forEach(data, function(obj) {
                    var element = {};
                    element.name = obj.name;
                    element.key = obj.object_id_s;
                    element.directory = obj.directory_name_s;
                    listToFill.push(element);
                });
            }

            function deleteUser() {
                LdapUserManagementService.deleteUser(selectedUser).then(function() {
                    var cacheUsers = new Store.SessionData(LookupService.SessionCacheNames.USERS);
                    var users = cacheUsers.get();
                    var cacheKeyUser = _.find(users, {
                        'object_id_s': selectedUser.key
                    });
                    cacheUsers.remove(cacheKeyUser);

                    $scope.userData.chooseObject = _.reject($scope.userData.chooseObject, function(element) {
                        return element.key === selectedUser.key;
                    });

                    $scope.lastSelectedUser = $scope.userData.chooseObject[0];
                    MessageService.succsessAction();
                }, function() {
                    MessageService.errorAction();
                });
            }

            function userScroll() {
                if (Util.isEmpty($scope.userFilterWord)) {
                    $scope.initUser(userPageSize, $scope.directoryName);
                } else {
                    // Retrieves the next page of filtered data since the user panel filter is set
                    var data = {};
                    data.filterWord = $scope.userFilterWord;
                    data.n = userPageSize;
                    data.start = $scope.userData.chooseObject.length;
                    $scope.retrieveDataScroll(data, "getUsersFiltered", "chooseObject");
                }
            }

            function retrieveDataScroll(data, methodName, panelName) {
                LdapUserManagementService[methodName](data).then(function(response) {
                    if (_.isArray(response.data)) {
                        $scope.fillList($scope.userData[panelName], response.data);
                    } else {
                        $scope.fillList($scope.userData[panelName], response.data.response.docs);
                    }
                    if (panelName === "selectedAuthorized") {
                        currentAuthGroups = [];
                        _.forEach($scope.userData[panelName], function(obj) {
                            currentAuthGroups.push(obj.key);
                        });
                    }
                }, function() {
                    $log.error('Error during calling the method ' + methodName);
                });
            }

            function unauthorizedScroll() {
                var data = {};
                data.member = $scope.lastSelectedUser;
                data.start = $scope.userData.selectedNotAuthorized.length;
                data.isAuthorized = false;
                if (Util.isEmpty($scope.unauthorizedFilterWord)) {
                    $scope.retrieveDataScroll(data, "getGroupsForUser", "selectedNotAuthorized");
                } else {
                    // Retrieves the next page of filtered data since the unauthorized panel filter is set
                    data.filterWord = $scope.unauthorizedFilterWord;
                    $scope.retrieveDataScroll(data, "getGroupsFiltered", "selectedNotAuthorized");
                }
            }

            function authorizedScroll() {
                var data = {};
                data.member = $scope.lastSelectedUser;
                data.start = $scope.userData.selectedAuthorized.length;
                data.isAuthorized = true;
                if (Util.isEmpty($scope.authorizedFilterWord)) {
                    $scope.retrieveDataScroll(data, "getGroupsForUser", "selectedAuthorized");
                } else {
                    // Retrieves the next page of filtered data since the authorized panel filter is set
                    data.filterWord = $scope.authorizedFilterWord;
                    $scope.retrieveDataScroll(data, "getGroupsFiltered", "selectedAuthorized");
                }
            }

            function retrieveDataFilter(data, methodName, panelName) {
                if (methodName === 'getNUsers') { // The getNUsers method requires the directoryName
                    LdapUserManagementService.getNUsers(data, $scope.directoryName).then(function(response) {
                        $scope.userData[panelName] = [];
                        $scope.fillList($scope.userData[panelName], response.data.response.docs);
                        $scope.onObjSelect($scope.userData.chooseObject[0]);
                        $scope.makePaginationRequest = response.data.response.numFound > $scope.userData.chooseObject.length;
                    }, function() {
                        $log.error('Error during calling the method ' + methodName);
                    });
                } else {
                    LdapUserManagementService[methodName](data).then(function(response) {
                        $scope.userData[panelName] = [];
                        if (_.isArray(response.data)) {
                            $scope.fillList($scope.userData[panelName], response.data);
                        } else {
                            $scope.fillList($scope.userData[panelName], response.data.response.docs);
                        }
                        if (methodName == "getNUsers" || methodName == "getUsersFiltered") {
                            $scope.onObjSelect($scope.userData.chooseObject[0]);
                        }
                    }, function() {
                        $log.error('Error during calling the method ' + methodName);
                    });
                }
            }

            function userManagementFilter(data) {
                $scope.userFilterWord = data.filterWord;
                if (Util.isEmpty(data.filterWord)) {
                    data.n = Util.isEmpty(data.n) ? 50 : data.n;
                    $scope.retrieveDataFilter(data, "getNUsers", "chooseObject");
                } else {
                    $scope.retrieveDataFilter(data, "getUsersFiltered", "chooseObject");
                }
            }

            function userUnauthorizedFilter(data) {
                data.member = $scope.lastSelectedUser;
                data.isAuthorized = false;
                $scope.unauthorizedFilterWord = data.filterWord;
                if (Util.isEmpty(data.filterWord)) {
                    data.n = Util.isEmpty(data.n) ? 50 : data.n;
                    $scope.retrieveDataFilter(data, "getGroupsForUser", "selectedNotAuthorized");
                } else {
                    $scope.retrieveDataFilter(data, "getGroupsFiltered", "selectedNotAuthorized");
                }
            }

            function userAuthorizedFilter(data) {
                data.member = $scope.lastSelectedUser;
                data.isAuthorized = true;
                $scope.authorizedFilterWord = data.filterWord;
                if (Util.isEmpty(data.filterWord)) {
                    data.n = Util.isEmpty(data.n) ? 50 : data.n;
                    $scope.retrieveDataFilter(data, "getGroupsForUser", "selectedAuthorized");
                } else {
                    $scope.retrieveDataFilter(data, "getGroupsFiltered", "selectedAuthorized");
                }
            }

            // Add method for AFDP-6803 to customize ok button
            $scope.deleteUserConfirm = function deleteUserConfirm() {
                directoriesConfigPromise.then(function (directories) {
                    var userManagementEnabled = isUserManagementEnabled(directories.data, selectedUser.directory);
                    if(userManagementEnabled){
                        bootbox.confirm({
                            message: $translate.instant("admin.security.ldap.user.management.deleteUserMsg"),
                            buttons: {
                                confirm:{
                                    label: $translate.instant("admin.security.ldap.user.management.deleteUserBtn"),
                                    className: "btn btn-danger"
                                },
                             cancel: {
                                label: $translate.instant("admin.security.ldap.user.management.cancelBtn")
                            }
                            },
                            callback: function(result){
                                if (result) {
                                    deleteUser();
                                }
                            }
                        })
                    }else {
                        DialogService.alert($translate.instant("admin.security.ldap.user.management.alertMessage"));
                    }
                });
            }

        } ]);
