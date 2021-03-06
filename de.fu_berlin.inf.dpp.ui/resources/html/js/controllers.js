var app = angular.module('app', ['ui.bootstrap', 'ng-context-menu']);

app.controller('ToolbarController', function ($scope) {
    $scope.connectButtonText = "Connect";

    $scope.connected = false;

    $scope.buttonDisabled = false;

    $scope.accounts = [];

    //TODO function name, maybe toggle connect?
    $scope.connect = function () {
        if (!$scope.connected) {
            if ($scope.accounts.length > 0) {
                $scope.connectUser($scope.accounts[0]);
            } else {
                alert("Cannot connect because no account is configured.");
            }
        } else {
            __java_disconnect();
        }
    };

    $scope.showConnect = function () {
        $scope.connectButtonText = "Connect";
        $scope.connected = false;
        $scope.buttonDisabled = false;
    };

    $scope.showDisconnect = function () {
        $scope.connectButtonText = "Disconnect";
        $scope.connected = true;
        $scope.buttonDisabled = false;
    };

    $scope.showConnecting = function () {
        $scope.connectButtonText = "Connecting...";
        $scope.buttonDisabled = true;
    };

    $scope.showDisconnecting = function () {
        $scope.connectButtonText = "Disconnecting...";
        $scope.buttonDisabled = true;
    };

    $scope.connectUser = function (account) {
        __java_connect(JSON.stringify(account));
    };

    $scope.showAddAccountWizard = function () {
        __java_showAddAccountWizard();
    };

    $scope.showAddContactWizard = function () {
        __java_showAddContactWizard();
    };
});

app.controller('ContactListCtrl', function ($scope) {
    $scope.contacts = [];

    $scope.root = null;

    $scope.selected = null;

    $scope.add = function (contact, presence, addition) {
        $scope.contacts.push({name: contact, presence: presence, addition: addition})
    };

    $scope.displayRoot = function (account) {
        $scope.root = account.username + '@' + account.domain;
    };

    $scope.clearAll = function () {
        $scope.contacts = [];
    };

    $scope.selectContact = function (name) {
        $scope.selected = name;
    };

    $scope.renameContact = function () {
        __java_renameContact($scope.selected);
    };

    $scope.deleteContact = function () {
        __java_deleteContact($scope.selected);
    };
});

__angular_setAccountList = function (accountList) {
    var exposedScope = angular.element(document.getElementById('toolbar')).scope();
    exposedScope.$apply(function () {
        exposedScope.accounts = accountList;
    })

};

__angular_displayContactList = function (contactList) {
    var exposedScope = angular.element(document.getElementById('contact-list')).scope();
    exposedScope.$apply(exposedScope.displayRoot(contactList.account));
    exposedScope.$apply(exposedScope.clearAll());
    contactList.contactList.forEach(function (contact) {
        exposedScope.$apply(exposedScope.add(contact.displayName, contact.presence, contact.addition));
    });
};

__angular_setIsConnected = function (connected) {
    var exposedScope = angular.element(document.getElementById('toolbar')).scope();
    if (connected) {
        exposedScope.$apply(exposedScope.showDisconnect());
    } else {
        exposedScope.$apply(exposedScope.showConnect());
    }
};

__angular_setIsConnecting = function () {
    var exposedScope = angular.element(document.getElementById('toolbar')).scope();
    exposedScope.$apply(exposedScope.showConnecting());
};

__angular_setIsDisconnecting = function () {
    var exposedScope = angular.element(document.getElementById('toolbar')).scope();
    exposedScope.$apply(exposedScope.showDisconnecting());
};