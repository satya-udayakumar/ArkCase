'use strict';

// Authentication service for user variables
angular.module('services').factory('ResourceLoaderService', ['$q', '$http',
    function ($q, $http) {
        return {
            loadResource: function (lang, module) {
                return $http.get('arkcase/modules_config/config/modules/'+  module +'/resources/' + lang + '.json');
            }
        }
    }
]);