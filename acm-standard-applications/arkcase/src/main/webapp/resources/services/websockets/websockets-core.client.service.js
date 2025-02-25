'use strict';

angular.module("services").factory("WebSocketsListener", ['$q', '$timeout', 'Websockets.MessageHandler', 'Authentication', function ($q, $timeout, messageHandler, Authentication) {

    var user;
    Authentication.queryUserInfo().then(function(userInfo) {
        user = userInfo;
        return userInfo;
    });

    var service = {
        //The number of milliseconds to delay before attempting to reconnect.
        RECONNECT_INTERVAL: 1000,
        //The rate of increase of the reconnect delay. Allows reconnect attempts to back off when problems persist.
        RECONNECT_DELAY: 2,
        RECONNECT_ATTEMPTS: 1,
        MAX_RECONNECT_ATTEMPTS: 1,
        //The maximum number of milliseconds to delay a reconnection attempt
        MAX_RECONNECT_INTERVAL: 30000,
        SOCKET_URL: "/arkcase/stomp",
        LISTEN_TOPIC_OBJECTS: "/topic/objects/changed",
        GENERIC_TOPIC: "/topic/generic/",
        SCHEDULED_JOBS_TOPIC: "/topic/jobStatus",
        CONFIGURATION_UPDATED_TOPIC: "/topic/configuration/updated",
        UPLOAD_FILE_MANAGER_QUEUE: "/queue/Consumer.[CLIENT_ID].VirtualTopic.UploadFileManager:[USERNAME]",
        MESSAGE_BROKER: "/app/print-message",
        shouldStart: true,
        socket: {
            client: null,
            stomp: null
        },

        connect: function () {
            if (this.isConnected()) {
                return;
            }
            console.log("WS init");
            this.socket.client = new SockJS(this.SOCKET_URL);
            this.socket.stomp = Stomp.over(this.socket.client);
            //this.socket.stomp.debug = null; //frequently need to disable stomp msg for debugging, please do not delete this line
            this.socket.stomp.connect({}, connectCallback(this), errorCallback(this));
        },

        send: function(message, destination) {
            if (!this.isConnected()) {
                return;
            }
            var id = Math.floor(Math.random() * 1000000);
            this.socket.stomp.send(destination, {
                priority: 9
            }, JSON.stringify(message));
        },

        disconnect: function() {
            if (this.isConnected()) {
                console.log("WS close");
                this.socket.stomp.disconnect();
            }
        },

        isConnected: function() {
            return this.socket.stomp && this.socket.stomp.connected;
        }
    };

    var connectCallback = function(target) {
        return function(frame) {
            target.RECONNECT_ATTEMPTS = 0;
            target.socket.stomp.subscribe(target.LISTEN_TOPIC_OBJECTS, function(data) {
                var message = JSON.parse(data.body);
                $timeout(function() {
                    messageHandler.handleMessage(message);
                    //4 seconds delay so solr can index the object
                }, 4000);
            });
            target.socket.stomp.subscribe(target.GENERIC_TOPIC + user.userId, function (data) {
                var message = JSON.parse(data.body);
                messageHandler.handleGenericMessage(message);
            });
            target.socket.stomp.subscribe(target.SCHEDULED_JOBS_TOPIC, function (data) {
                var message = JSON.parse(data.body);
                messageHandler.handleScheduledJobStatusMessage(message);
            });

            target.socket.stomp.subscribe(target.CONFIGURATION_UPDATED_TOPIC, function (data) {
                var message = JSON.parse(data.body);
                $timeout(function () {
                    messageHandler.handleConfigurationUpdatedMessage(message);
                    // 2 seconds delay so the changes can be saved
                }, 2000);
            });

            var username = user.userId.replace(/\./g, "_DOT_").replace(/@/g, "_AT_");
            var destination = target.UPLOAD_FILE_MANAGER_QUEUE.replace("[CLIENT_ID]", username).replace("[USERNAME]", username);
            target.socket.stomp.subscribe(destination, function (data) {
                var message = JSON.parse(data.body);
                messageHandler.handleGenericMessage(message);
            });
        }
    };

    var errorCallback = function(target) {
        return function(error) {
            console.log("WS error", error);
            if (target.RECONNECT_ATTEMPTS) {
                if (target.MAX_RECONNECT_ATTEMPTS && target.RECONNECT_ATTEMPTS > target.MAX_RECONNECT_ATTEMPTS) {
                    return;
                }
            }
            var timeoutMax = target.RECONNECT_INTERVAL * Math.pow(target.RECONNECT_DELAY, target.RECONNECT_ATTEMPTS);
            var timeoutMin = target.RECONNECT_INTERVAL * (Math.pow(target.RECONNECT_DELAY, target.RECONNECT_ATTEMPTS) - 1);
            var timeout = Math.random() * (timeoutMax - timeoutMin) + timeoutMin;
            setTimeout(function() {
                target.RECONNECT_ATTEMPTS++;
                target.connect();
            }, timeout > target.MAX_RECONNECT_INTERVAL ? target.MAX_RECONNECT_INTERVAL : timeout);
        }
    };

    return service;

} ]).run(function(WebSocketsListener) {
    if (WebSocketsListener.shouldStart) {
        WebSocketsListener.connect();
    }
});
