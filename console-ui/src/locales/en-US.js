/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

const I18N_CONF = {
  Header: {
    home: 'HOME',
    docs: 'DOCS',
    blog: 'BLOG',
    community: 'COMMUNITY',
    enterprise: 'ENTERPRISE EDITION',
    languageSwitchButton: '中',
    logout: 'logout',
    changePassword: 'modify password',
    passwordRequired: 'password should not be empty',
    usernameRequired: 'username should not be empty',
  },
  Login: {
    login: 'Login',
    internalSysTip1: 'Internal system.',
    internalSysTip2: 'Not exposed to the public network',
    submit: 'Submit',
    pleaseInputUsername: 'Please input username',
    pleaseInputPassword: 'Please input password',
    invalidUsernameOrPassword: 'invalid username or password',
    productDesc:
      'an easy-to-use dynamic service discovery, configuration and service management platform for building cloud native applications',
  },
  MainLayout: {
    nacosName: 'NACOS',
    nacosMode: 'MODE',
    doesNotExist: 'The page you visit does not exist',
    configurationManagementVirtual: 'ConfigManagement',
    configurationManagement: 'Configurations',
    configdetail: 'Configuration Details',
    configsync: 'Synchronize Configuration',
    configeditor: 'Edit Configuration',
    newconfig: 'Create Configuration',
    historyRollback: 'Historical Versions',
    configRollback: 'Configuration Rollback',
    historyDetail: 'History Details',
    listeningToQuery: 'Listening Query',
    serviceManagementVirtual: 'ServiceManagement',
    serviceManagement: 'Service List',
    subscriberList: 'Subscribers',
    serviceDetail: 'Service Details',
    namespace: 'Namespace',
    clusterManagementVirtual: 'ClusterManagement',
    clusterManagement: 'Cluster Node List',
    authorityControl: 'Authority Control',
    userList: 'User List',
    roleManagement: 'Role Management',
    privilegeManagement: 'Privilege Management',
    consoleClosed: 'Console Closed',
    settingCenter: 'Setting Center',
  },
  Password: {
    passwordNotConsistent: 'The passwords are not consistent',
    passwordRequired: 'password should not be empty',
    pleaseInputOldPassword: 'Please input original password',
    pleaseInputNewPassword: 'Please input new password',
    pleaseInputNewPasswordAgain: 'Please input new password again',
    oldPassword: 'Original password',
    newPassword: 'New password',
    checkPassword: 'Check password',
    changePassword: 'modify password',
    invalidPassword: 'Invalid original password',
    modifyPasswordFailed: 'Modify password failed',
  },
  NameSpace: {
    public_tips: 'public namespace ID is empty by default',
    namespace: 'Namespaces',
    prompt: 'Notice',
    namespaceDetails: 'Namespace details',
    namespaceName: 'Name',
    namespaceID: 'ID',
    configuration: 'Number of Configurations',
    description: 'Description',
    removeNamespace: 'Remove the namespace',
    confirmDelete: 'Sure you want to delete the following namespaces?',
    configurationManagement: 'Configurations',
    removeSuccess: 'Remove the namespace success',
    deletedSuccessfully: 'Deleted successfully',
    deletedFailure: 'Delete failed',
    namespaceDelete: 'Delete',
    details: 'Details',
    edit: 'Edit',
    namespacePublic: 'public(to retain control)',
    pubNoData: 'No results found.',
    namespaceAdd: 'Create Namespace',
    namespaceNames: 'Namespaces',
    namespaceNumber: 'Namespace ID',
    namespaceOperation: 'Actions',
    refresh: 'Refresh',
  },
  ServiceList: {
    serviceList: 'Service List',
    serviceName: 'Service Name',
    serviceNamePlaceholder: 'Enter Service Name',
    hiddenEmptyService: 'Hidden Empty Service',
    query: 'Search',
    pubNoData: 'No results found.',
    columnServiceName: 'Service Name',
    groupName: 'Group Name',
    groupNamePlaceholder: 'Enter Group Name',
    columnClusterCount: 'Cluster Count',
    columnIpCount: 'Instance Count',
    columnHealthyInstanceCount: 'Healthy Instance Count',
    columnTriggerFlag: 'Trigger Protection Threshold',
    operation: 'Operation',
    detail: 'Details',
    sampleCode: 'Code Example',
    deleteAction: 'Delete',
    prompt: 'Confirm',
    promptDelete: 'Do you want to delete the service?',
    create: 'Create Service',
    subscriber: 'Subscriber',
  },
  SubscriberList: {
    subscriberList: 'Subscriber List',
    serviceName: 'Service Name',
    serviceNamePlaceholder: 'Enter Service Name',
    groupName: 'Group Name',
    groupNamePlaceholder: 'Enter Group Name',
    query: 'Search',
    pubNoData: 'No results found.',
    address: 'Address',
    clientVersion: 'Client Version',
    appName: 'Application Name',
    searchServiceNamePrompt: 'Service name required!',
  },
  ClusterNodeList: {
    clusterNodeList: 'Node List',
    nodeIp: 'NodeIp',
    nodeIpPlaceholder: 'Please enter node Ip',
    query: 'Search',
    pubNoData: 'No results found.',
    nodeState: 'NodeState',
    extendInfo: 'NodeMetaData',
    operation: 'Operation',
    leave: 'Leave',
    leaveSucc: 'Leave successfully',
    leaveFail: 'Leave failed',
    leavePrompt: 'prompt',
    confirm: 'Confirm',
    confirmTxt: 'Confirm that you want to go offline this cluster node?',
  },
  EditClusterDialog: {
    updateCluster: 'Update Cluster',
    checkType: 'Check Type',
    checkPort: 'Check Port',
    useIpPortCheck: 'Use port of IP',
    checkPath: 'Check Path',
    checkHeaders: 'Check Headers',
    metadata: 'Metadata',
  },
  ServiceDetail: {
    serviceDetails: 'Service Details',
    back: 'Back',
    editCluster: 'Edit Cluster',
    cluster: 'Cluster',
    metadata: 'Metadata',
    selector: 'Selector',
    type: 'Type',
    registerLevel: 'Register Level',
    groupName: 'Group Name',
    protectThreshold: 'Protect Threshold',
    serviceName: 'Service Name',
    editService: 'Edit Service',
    InstanceFilter: {
      title: 'Metadata Filter',
      addFilter: 'Add Filter',
      clear: 'Clear',
    },
  },
  EditServiceDialog: {
    createService: 'Create Service',
    updateService: 'Edit Service',
    serviceName: 'Service Name',
    metadata: 'Metadata',
    groupName: 'Group Name',
    type: 'Type',
    registerLevel: 'Register Level',
    typeLabel: 'Label',
    typeNone: 'None',
    selector: 'Selector',
    protectThreshold: 'Protect Threshold',
    serviceNameRequired: 'Please enter a service name',
    protectThresholdRequired: 'Please enter a protect threshold',
  },
  InstanceFilter: {
    title: 'Metadata Filter',
    addFilter: 'Add Filter',
    clear: 'Clear',
  },
  InstanceTable: {
    operation: 'Operation',
    port: 'Port',
    weight: 'Weight',
    healthy: 'Healthy',
    metadata: 'Metadata',
    editor: 'Edit',
    offline: 'Offline',
    online: 'Online',
    ephemeral: 'Ephemeral',
  },
  EditInstanceDialog: {
    port: 'Port',
    weight: 'Weight',
    metadata: 'Metadata',
    updateInstance: 'Update Instance',
    whetherOnline: 'Whether Online',
  },
  ListeningToQuery: {
    success: 'Success',
    failure: 'Failure',
    configuration: 'Configuration',
    pubNoData: 'No results found.',
    listenerQuery: 'Listening Query',
    queryDimension: 'Dimension',
    pleaseEnterTheDataId: 'Enter Data ID',
    dataIdCanNotBeEmpty: 'Data ID cannot be empty',
    pleaseInputGroup: 'Enter Group',
    groupCanNotBeEmpty: 'Group cannot be empty',
    pleaseInputIp: 'Enter IP',
    query: 'Search',
    articleMeetRequirements: 'configuration items.',
  },
  HistoryRollback: {
    details: 'Details',
    rollback: 'Roll Back',
    pubNoData: 'No results found.',
    toConfigure: 'Historical Versions (Configuration record is retained for 30 days.)',
    dataId: 'Enter Data ID',
    dataIdCanNotBeEmpty: 'Data ID cannot be empty',
    group: 'Enter Group',
    groupCanNotBeEmpty: 'Group cannot be empty',
    query: 'Search',
    articleMeetRequirements: 'configuration items.',
    lastUpdateTime: 'Last Modified At',
    operator: 'Operator',
    operation: 'Operation',
    compare: 'Compare',
    historyCompareTitle: 'History Compare',
    historyCompareLastVersion: 'Lasted Release Version',
    historyCompareSelectedVersion: 'Selected Version',
  },
  HistoryDetail: {
    historyDetails: 'History Details',
    update: 'Update',
    insert: 'Insert',
    deleteAction: 'Delete',
    recipientFrom: 'Collapse',
    moreAdvancedOptions: 'Advanced Options',
    home: 'Application',
    actionType: 'Action Type',
    operator: 'Operator',
    sourceIp: 'Source IP',
    configureContent: 'Configuration Content',
    back: 'Back',
    namespace: 'Namespace',
  },
  DashboardCard: {
    importantReminder0: 'Important reminder',
    viewDetails1: 'view details',
  },
  ConfigurationManagement: {
    exportBtn: 'Export',
    questionnaire2: 'questionnaire',
    ad:
      'a ACM front-end monitoring questionnaire, the time limit to receive Ali cloud voucher details shoved stamp: the',
    noLongerDisplay4: 'no longer display',
    createConfiguration: 'Create Configuration',
    removeConfiguration: 'Delete Configuration',
    sureDelete: 'Are you sure you want to delete the following configuration?',
    environment: 'Region',
    configurationManagement: 'Configurations',
    details: 'Details',
    sampleCode: 'Code Example',
    edit: 'Edit',
    deleteAction: 'Delete',
    more: 'More',
    version: 'Historical Versions',
    listenerQuery: 'Configuration Listening Query',
    failedEntry: 'Failed Entry',
    successfulEntry: 'Successful Entry',
    unprocessedEntry: 'Unprocessed Entry',
    pubNoData: 'No results found.',
    configurationManagement8: 'Configuration Management',
    queryResults: 'Found',
    articleMeetRequirements: 'configuration items',
    fuzzydMode: 'Default fuzzy query mode',
    fuzzyd: "Add wildcard '*' for fuzzy query",
    defaultFuzzyd: 'Default fuzzy query mode opened',
    fuzzyg: "Add wildcard '*' for fuzzy query",
    defaultFuzzyg: 'Default fuzzy query mode opened',
    query: 'Search',
    advancedQuery9: 'Advanced Query',
    app1: 'Enter App Name\n',
    tags: 'Tags',
    pleaseEnterTag: 'Enter Tag',
    configDetailLabel: 'DetailSearch',
    configDetailH: 'search config detail',
    application: 'Application',
    operation: 'Operation',
    export: 'Export query results',
    newExport: 'New version export query results',
    import: 'Import',
    uploadBtn: 'Upload File',
    importSucc: 'The import was successful',
    importAbort: 'Import abort',
    importSuccBegin: 'The import was successful,with ',
    importSuccEnd: 'configuration items imported',
    importFail: 'Import failed',
    importFail403: 'Unauthorized!',
    importDataValidationError: 'No legitimate data was read, please check the imported data file.',
    metadataIllegal: 'The imported metadata file is illegal',
    namespaceNotExist: 'namespace does not exist',
    abortImport: 'Abort import',
    skipImport: 'Skip',
    overwriteImport: 'Overwrite',
    importRemind:
      'File upload will be imported directly into the configuration, please be careful!',
    samePreparation: 'Same preparation',
    targetNamespace: 'Target namespace',
    conflictConfig: 'Conflict-detected configuration items',
    importSuccEntries: 'Successful entries: ',
    failureEntries: 'Failure entries',
    unprocessedEntries: 'Unprocessed entries',
    unrecognizedEntries: 'Unrecognized entries',
    skippedEntries: 'skipped entries',
    exportSelected: 'Export selected configs',
    newExportSelected: 'New version export selected configs',
    clone: 'Clone',
    exportSelectedAlertTitle: 'Export config',
    exportSelectedAlertContent: 'please select the configuration to export',
    cloneSucc: 'The clone was successful',
    cloneAbort: 'Clone abort',
    cloneSuccBegin: 'The clone was successful,with ',
    cloneSuccEntries: 'Successful entries: ',
    cloneSuccEnd: 'configuration items cloned',
    cloneFail: 'Clone failed',
    getNamespaceFailed: 'get the namespace failed',
    getNamespace403: 'Without permission to access ${namespaceName} namespace!',
    startCloning: 'Start Clone',
    cloningConfiguration: 'Clone config',
    source: 'Source ',
    configurationNumber: 'Items',
    target: 'Target',
    selectNamespace: 'Select Namespace',
    selectedEntry: '| Selected Entry',
    cloneSelectedAlertTitle: 'Clone config',
    cloneSelectedAlertContent: 'please select the configuration to clone',
    delSelectedAlertTitle: 'Delete config',
    delSelectedAlertContent: 'please select the configuration to delete',
    delSuccessMsg: 'delete successful',
    cloneEditableTitle: 'Modify Data Id and Group (optional)',
    authFail: 'Auth failed',
    copyNamespaceID: 'Copy namespace ID',
  },
  NewConfig: {
    newListingMain: 'Create Configuration',
    newListing: 'Create Configuration',
    publishFailed: 'Publish failed. Make sure parameters are entered correctly.',
    publishFailed403: 'Publish failed. No permission to create Configuration',
    doNotEnter: 'Illegal characters not allowed',
    newConfig: 'Data ID cannot be empty.',
    dataIdIsNotEmpty: 'Data ID cannot exceed 255 characters in length',
    groupPlaceholder: 'Enter your group name',
    moreAdvanced: 'Group cannot be empty',
    groupNotEmpty: 'Group ID cannot exceed 127 characters in length',
    annotation:
      'Notice: You are going to add configuration to a new group, please make sure that the version of Pandora which clients are using is higher than 3.4.0, otherwise this configuration may be unreadable to clients.',
    dataIdLength: 'Collapse',
    collapse: 'Advanced Options',
    tags: 'Tags',
    pleaseEnterTag: 'Enter Tag',
    groupIdCannotBeLonger: 'Application',
    description: 'Description',
    targetEnvironment: 'Format',
    configurationFormat: 'Configuration Content',
    configureContentsOf: 'Press F1 to view in full screen',
    fullScreen: 'Press Esc to exit',
    escExit: 'Publish',
    release: 'Back',
    confirmSyanx: 'The configuration information may has a syntax error. Are you sure to submit?',
    dataIdExists: 'Configuration already exists. Enter a new Data ID and Group name.',
    dataRequired: 'Data cannot be empty, submission failed',
    namespace: 'Namespace',
  },
  CloneDialog: {
    terminate: 'Terminate',
    skip: 'Skip',
    cover: 'Cover',
    getNamespaceFailed: 'get the namespace failed',
    selectedEntry: '| Selected Entry',
    homeApplication: 'Home Application',
    tags: 'tags',
    startCloning: 'Start Clone',
    source: 'Source ',
    configurationNumber: 'Items',
    target: 'Target',
    conflict: 'Conflict',
    selectNamespace: 'Select Namespace',
    configurationCloning: 'Clone（',
  },
  DeleteDialog: {
    confManagement: 'Configuration Management',
    determine: 'OK',
    deletetitle: 'Delete Configuration',
    deletedSuccessfully: 'Configuration deleted',
    deleteFailed: 'Deleting configuration failed',
  },
  DiffEditorDialog: {
    publish: 'Publish',
    back: 'Back',
  },
  ConfigEditor: {
    official: 'Official',
    production: 'Production',
    beta: 'BETA',
    wrong: 'Error',
    submitFailed: 'Cannot be empty, submit failed',
    toedittitle: 'Edit Configuration',
    newConfigEditor: 'New Config Editor',
    toedit: 'Edit Configuration',
    vdchart: 'Illegal characters not allowed',
    recipientFrom: 'Data ID cannot be empty',
    homeApplication: 'Group name cannot be empty',
    collapse: 'Collapse',
    groupNotEmpty: 'Advanced Options',
    tags: 'Tags',
    pleaseEnterTag: 'Enter Tag',
    targetEnvironment: 'Application',
    description: 'Description',
    format: 'Format',
    configcontent: 'Configuration Content',
    escExit: 'Press F1 to view in full screen',
    releaseBeta: 'Press Esc to exit ',
    release: 'Beta Publish',
    stopPublishBeta: 'Stop Beta',
    betaPublish: 'Beta Publish',
    betaSwitchPrompt: 'Not checked by default.',
    publish: 'Publish',
    back: 'Back',
    codeValErrorPrompt: 'Configuration information may have syntax errors. Are you sure to submit?',
    dialogTitle: 'Content Comparison',
    dialogCurrentArea: 'Current Value',
    dialogOriginalArea: 'Original Value',
    publishFailed403: 'Publish failed. No operation permission',
    publishCasFailed: 'Publish failed. Changes configuration conflict',
    namespace: 'Namespace',
  },
  EditorNameSpace: {
    notice: 'Notice',
    pleaseDo: 'Illegal characters not allowed',
    publicSpace: 'OK',
    confirmModify: 'Edit Namespace',
    editNamespace: 'Loading...',
    load: 'Namespace',
    namespace: 'Namespace cannot be empty',
    namespaceDesc: 'Namespace description cannot be empty',
    description: 'Description',
  },
  ExportDialog: {
    selectedEntry: '| Selected Entry',
    application: 'Application',
    tags: 'Tags',
    exportBtn: 'Export',
    exportConfiguration: 'Export ( ',
    source: 'Source',
    items: 'Items',
  },
  ImportDialog: {
    terminate: 'Terminate',
    skip: 'Skip',
    overwrite: 'Overwrite',
    zipFileFormat: 'Only upload. zip file format',
    uploadFile: 'Upload File',
    importLabel: 'Import ( ',
    target: 'Target',
    conflict: 'Conflict',
    beSureExerciseCaution: 'Caution: data will be imported directly after uploading.',
  },
  ShowCodeing: {
    sampleCode: 'Sample Code',
    loading: 'Loading...',
  },
  SuccessDialog: {
    title: 'Configuration Management',
    determine: 'OK',
    failure: 'Failed',
  },
  ConfigSync: {
    error: 'Error',
    syncConfigurationMain: 'Synchronize Configuration',
    syncConfiguration: 'Synchronize Configuration Successfully',
    advancedOptions: 'Advanced Options',
    collapse: 'Collapse',
    home: 'Application：',
    region: 'Region：',
    configuration: 'Configuration Content：',
    target: 'Target Region：',
    sync: 'Synchronize',
    back: 'Back',
  },
  NewNameSpace: {
    norepeat: 'Duplicate namespace. Please enter a different name.',
    notice: 'Notice',
    input: 'Illegal characters not allowed',
    ok: 'OK',
    cancel: 'Cancel',
    newnamespce: 'Create Namespace',
    loading: 'Loading...',
    name: 'Namespace',
    namespaceId: 'Namespace ID(automatically generated if not filled)',
    namespaceIdTooLong: 'The namespace ID length cannot exceed 128',
    namespacenotnull: 'Namespace cannot be empty',
    namespacedescnotnull: 'Namespace description cannot be empty',
    description: 'Description',
    namespaceIdAlreadyExist: 'namespaceId already exist',
    newnamespceFailedMessage:
      'namespaceId format is incorrect/namespaceId length greater than 128/namespaceId already exist',
  },
  NameSpaceList: {
    notice: 'Notice',
  },
  ConfigDetail: {
    official: 'Official',
    error: 'Error',
    configurationDetails: 'Configuration Details',
    collapse: 'Collapse',
    more: 'Advanced Options',
    home: 'Application',
    tags: 'Tags',
    description: 'Description',
    betaRelease: 'Beta Publish',
    configuration: 'Configuration Content',
    back: 'Back',
    versionComparison: 'Version Comparison',
    dialogCurrentArea: 'Current Version',
    dialogOriginalArea: 'Previous Version',
    configComparison: 'Config Comparison',
    dialogCurrentConfig: 'Current Config',
    dialogComparedConfig: 'Compared Config',
    configComparisonTitle: 'Select Config',
    dataIdInput: 'Please Enter Data Id',
    groupInput: 'Please Enter Group',
    namespaceSelect: 'Please Select namespace',
    configNotFind: 'The Configuration is not found, Please select again',
    namespace: 'Namespace',
  },
  ConfigRollback: {
    rollBack: 'Roll Back',
    determine: 'Are you sure you want to roll back',
    followingConfiguration: 'the following configuration?',
    configurationRollback: 'Configuration Rollback',
    collapse: 'Collapse',
    more: 'Advanced Options',
    home: 'Application',
    actionType: 'Action Type',
    configuration: 'Configuration Content',
    back: 'Back',
    rollbackSuccessful: 'Rollback Successful',
    rollbackDelete: 'Delete',
    update: 'Update',
    insert: 'Insert',
    additionalRollbackMessage: 'This operation will delete the below config!',
    namespace: 'Namespace',
  },
  UserManagement: {
    userManagement: 'User Management',
    createUser: 'Create user',
    resetPassword: 'Edit',
    deleteUser: 'Delete',
    deleteUserTip: 'Do you want to delete this user?',
    username: 'Username',
    password: 'Password',
    operation: 'Operation',
    refresh: 'Refresh',
    query: 'Search',
    fuzzydMode: 'Default fuzzy query mode',
    defaultFuzzyd: 'Default fuzzy query mode opened',
    fuzzyd: "Add wildcard '*' for fuzzy query",
  },
  NewUser: {
    createUser: 'Create user',
    username: 'Username',
    password: 'Password',
    rePassword: 'Repeat',
    usernamePlaceholder: 'Please Enter Username',
    passwordPlaceholder: 'Please Enter Password',
    rePasswordPlaceholder: 'Please Enter Repeat Password',
    usernameError: 'User name cannot be empty!',
    passwordError: 'Password cannot be empty!',
    rePasswordError: 'Repeat Password cannot be empty!',
    rePasswordError2: 'Passwords are inconsistent!',
  },
  PasswordReset: {
    resetPassword: 'Password Reset',
    username: 'Username',
    password: 'Password',
    rePassword: 'Repeat',
    passwordPlaceholder: 'Please Enter Password',
    rePasswordPlaceholder: 'Please Enter Repeat Password',
    passwordError: 'Password cannot be empty!',
    rePasswordError: 'Repeat Password cannot be empty!',
    rePasswordError2: 'Passwords are inconsistent!',
  },
  RolesManagement: {
    roleManagement: 'Role management',
    bindingRoles: 'Binding roles',
    role: 'Role',
    username: 'Username',
    operation: 'Operation',
    deleteRole: 'Delete',
    deleteRoleTip: 'Do you want to delete this role?',
    refresh: 'Refresh',
    fuzzydMode: 'Default fuzzy query mode',
    defaultFuzzyd: 'Default fuzzy query mode opened',
    fuzzyd: "Add wildcard '*' for fuzzy query",
    query: 'Search',
  },
  NewRole: {
    bindingRoles: 'Binding roles',
    username: 'Username',
    role: 'Role',
    usernamePlaceholder: 'Please Enter Username',
    rolePlaceholder: 'Please Enter Role',
    usernameError: 'User name cannot be empty!',
    roleError: 'Role cannot be empty!',
  },
  PermissionsManagement: {
    privilegeManagement: 'Permissions Management',
    addPermission: 'Add Permission',
    role: 'Role',
    resource: 'Resource',
    action: 'Action',
    operation: 'Operation',
    deletePermission: 'Delete',
    deletePermissionTip: 'Do you want to delete this permission?',
    readOnly: 'read only',
    writeOnly: 'write only',
    readWrite: 'Read and write',
    refresh: 'Refresh',
    fuzzydMode: 'Default fuzzy query mode',
    defaultFuzzyd: 'Default fuzzy query mode opened',
    fuzzyd: "Add wildcard '*' for fuzzy query",
    query: 'Search',
  },
  NewPermissions: {
    addPermission: 'Add Permission',
    role: 'Role',
    resource: 'Resource',
    action: 'Action',
    resourcePlaceholder: 'Please select resources',
    rolePlaceholder: 'Please enter Role',
    actionPlaceholder: 'Please select Action',
    resourceError: 'Resource cannot be empty!',
    roleError: 'Role cannot be empty!',
    actionError: 'Action cannot be empty!',
    readOnly: 'read only',
    writeOnly: 'write only',
    readWrite: 'Read and write',
  },
  Components: {
    copySuccessfully: 'Success copied!',
  },
  SettingCenter: {
    settingTitle: 'Setting Center',
    settingTheme: 'Themes',
    settingLight: 'light',
    settingDark: 'dark',
    settingLocale: 'Language',
    settingSubmit: 'Apply',
  },
};

export default I18N_CONF;
