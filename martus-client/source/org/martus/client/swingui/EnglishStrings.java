/*

Martus(TM) is a trademark of Beneficent Technology, Inc. 
This software is (c) Copyright 2001-2015, Beneficent Technology, Inc.

Martus is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later
version with the additions and exceptions described in the
accompanying Martus license file entitled "license.txt".

It is distributed WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, including warranties of fitness of purpose or
merchantability.  See the accompanying Martus License and
GPL license for more details on the required license terms
for this software.

You should have received a copy of the GNU General Public
License along with this program; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA 02111-1307, USA.

*/

package org.martus.client.swingui;

import org.martus.common.bulletin.Bulletin;

public class EnglishStrings
{
	public static String strings[] = {
"wintitle:main=Martus Information Management and Data Collection Framework",
"wintitle:create=Create Record",
"wintitle:options=Options",
"wintitle:HelpDefaultDetails=Help on Record Details Field Default Content",
"wintitle:MartusSignIn=Martus SignIn",
"wintitle:MartusSignInValidate=Validate User",
"wintitle:MartusSignInRetypePassword=Confirm Password",
"wintitle:ServerCompliance=Server Compliance Statement",
"wintitle:ErrorBackingupKeyPair=Error",
"wintitle:askToBackupKeyPair=Key Backup Needed",
"wintitle:BulletinDetailsDialog=Record Details",
"wintitle:IncompatibleMtfVersion=Incompatible Translation Version",
"wintitle:DuplicateLabelsInCustomTemplate=Warning: Duplicate Labels Found",
"wintitle:ImportProgress=Importing Records",
"wintitle:ExportProgress=Exporting Records",
"wintitle:FileDialogExportPublicKey=Export Public Account ID",

// NOTE: With confirm is for swing; without is for javafx
"wintitle:confirmsend=Confirm Save Record",
"wintitle:send=Confirm Save Record",

"wintitle:confirmretrieve=Confirm Retrieve Records",
"wintitle:confirmRemoveAttachment=Confirm Remove Attachments",
"wintitle:confirmOverWriteExistingFile=Confirm Replace Existing File",
"wintitle:confirmCancelModifyBulletin=Cancel Edit",
"wintitle:confirmWarningSwitchToNormalKeyboard=Security Warning",
"wintitle:confirmCloneBulletinAsMine=Confirm Create Copy of Someone Else's Record",
"wintitle:confirmUploadReminder=Upload Reminder",
"wintitle:confirmDraftUploadReminder=Unsent Draft Reminder",
"wintitle:confirmRedoWeakPassword=Weak Password Warning",
"wintitle:confirmResetDefaultDetails=Reset Contents",
"wintitle:confirmNotYourBulletinViewAttachmentAnyways=Attachment Warning",
"wintitle:confirmServerComplianceFailed=Server Compliance Statement",
"wintitle:confirmWarnMissingOrCorruptAccountMapSignatureFile=Accountmap Signature File Missing Or Corrupt",
"wintitle:confirmWarnMissingAccountMapFile=Accountmap File Missing Or Corrupt",
"wintitle:confirmCancelShareBackup=Key Backup",
"wintitle:confirmEnterCustomFields=Customize Fields",
"wintitle:confirmUndoCustomFields=Customize Fields",
"wintitle:confirmCancelShareRecover=Cancel Restore",
"wintitle:confirmRecoveredKeyShareFailedTryAgain=Restore Failed",
"wintitle:confirmErrorRecoverIvalidFileName=Invalid File",
"wintitle:confirmBackupKeyShareVerifyDisks=Verify Disks",
"wintitle:confirmCancelShareVerify=Cancel Verification",
"wintitle:confirmEditBulletinWithUnknownTags=Modify Record",
"wintitle:confirmExportUnknownTags=Export Records",
"wintitle:confirmKeyPairFileExistsOverWrite=Key file Exists",
"wintitle:confirmQuickEraseOutboxNotEmpty=Unsent Records Reminder",
"wintitle:confirmCancelBackupRecovery=Cancel Backup Restore",
"wintitle:confirmUnableToRecoverFromBackupFile=Unable to Restore from Backup File",
"wintitle:confirmWarningPathChosenMayNotBeRemoveable=Questionable Media Chosen",
"wintitle:confirmBackupKeyPairInformation=Key Backup",
"wintitle:confirmhelpStringNotFound=Text Not Found",
"wintitle:confirmNeedsFolderMigration=Folder Migration Required",
"wintitle:confirmNeedsBulletinVersioningMigration=Record Versioning Migration Required",
"wintitle:confirmUnAuthoredBulletinDeleteBeforePaste=Record Already Exists",
"wintitle:confirmRemoveMartus=Delete All Data and Remove Martus",
"wintitle:confirmDeleteMyData=Delete My Data",
"wintitle:confirmNewerConfigInfoFileFound=Release Warning",
"wintitle:confirmdeletefolder=Confirm Delete Folder",
"wintitle:confirmRetrieveNewerVersions=Confirm Retrieving Newer Versions",
"wintitle:confirmDateRageInvalid=Date Range Invalid",
"wintitle:confirmPrintAllPrivateData=Print All Private Data",
"wintitle:confirmCancelRetrieve=Cancel Retrieve",
"wintitle:confirmReportIncludePrivate=All Data Private",
"wintitle:confirmSealSelectedBulletins=Seal Records",
"wintitle:confirmXmlSchemaNewerImportAnyway=Import Records",
"wintitle:confirmImportingCustomizationUnknownSigner=Template Manager",

"wintitle:notifyAddContact=Add Contact",
"wintitle:notifyVerifyContact=Verify Contact",
"wintitle:notifyImportTemplate=Import Template",
"wintitle:notifyContactsNoServer=Contacts Server",

"wintitle:notifyDropErrorBulletinExists=Cannot Move Record(s)",
"wintitle:notifyDropErrorBulletinOlder=Cannot Move Record(s)",
"wintitle:notifyDropErrors=Error Moving Record(s)",
"wintitle:notifyPasteErrorBulletinAlreadyExists=Cannot Paste Record(s)",
"wintitle:PasteErrorBulletinOlder=Cannot Paste Record(s)",
"wintitle:notifyPasteError=Error Pasting Record(s)",
"wintitle:notifyretrievenothing=Retrieve Records",
"wintitle:notifyretrievenoserver=Retrieve Records",
"wintitle:notifyDeleteServerDraftsWorked=Delete Drafts From Server",
"wintitle:notifyDeleteServerDraftsNone=Delete Drafts From Server",
"wintitle:notifyDeleteServerDraftsFailed=Delete Drafts From Server",
"wintitle:notifypasswordsdontmatch=Invalid Setup Information",
"wintitle:notifyusernamessdontmatch=Invalid Setup Information",
"wintitle:notifyUserNameBlank=Invalid Setup Information",
"wintitle:notifyPasswordInvalid=Invalid Setup Information",
"wintitle:notifyPasswordMatchesUserName=Invalid Setup Information",
"wintitle:notifyincorrectsignin=Incorrect Signin",
"wintitle:notifyuploadrejected=Error Sending Record",
"wintitle:notifycorruptconfiginfo=Error Loading Configuration File",
"wintitle:notifymagicwordrejected=Upload Permission Rejected",
"wintitle:notifyRewriteKeyPairFailed=Error Changing Username or Password",
"wintitle:notifyRewriteKeyPairSaved=Changed Username or Password",
"wintitle:notifyUnableToViewAttachment=Viewing Attachment Failed",
"wintitle:notifyUnableToSaveAttachment=Saving Attachment Failed",
"wintitle:notifySearchFailed=Search Results",
"wintitle:notifySearchFound=Search Results",
"wintitle:notifyServerError=Server Error",
"wintitle:notifyFoundOrphans=Recovered Lost Records",
"wintitle:notifyFoundDamagedBulletins=Detected Damaged Records",
"wintitle:notifyErrorSavingState=Error Saving State",
"wintitle:notifyErrorSavingFile=Error Saving File",
"wintitle:notifyErrorBackingupKeyPair=Error Verifying Key Pair",
"wintitle:notifyExportMyPublicKey=Public Account Key Exported",
"wintitle:notifyPublicInfoFileError=Error Importing Public Information",
"wintitle:notifyErrorSavingConfig=Error Saving Configuration File",
"wintitle:notifyAuthenticateServerFailed=Security Alert!",
"wintitle:notifyUnexpectedError=Unexpected Error",
"wintitle:notifyInvalidServerName=Invalid Server Name or IP Address",
"wintitle:notifyInvalidServerCode=Invalid Server Public Code",
"wintitle:notifyServerInfoInvalid=Server Response Invalid",
"wintitle:notifyConfigNoServer=Unable to Connect to Server",
"wintitle:notifyServerCodeWrong=Incorrect Server Public Code",
"wintitle:notifyRememberPassword=Remember Your Password",
"wintitle:notifyDamagedBulletinMovedToDiscarded=Moved Damaged Record",
"wintitle:notifyUploadFailedBulletinNotSentToServer=Unable to Send Record To Server",
"wintitle:notifyPreviewOneBulletinOnly=Preview One Record Only",
"wintitle:notifyPreviewNoBulletinsSelected=No Record Selected",
"wintitle:notifyRetrievePreviewNotAvailableYet=Unable To Preview",
"wintitle:notifyRetrievedOnlySomeSummaries=Error During Retrieve",
"wintitle:notifyConfirmCorrectDefaultDetailsData=Confirm Correct Default Details Content",
"wintitle:notifyExportComplete=Export Records",
"wintitle:notifyExportCompleteMissingAttachments=Export Records",
"wintitle:notifyErrorDuringExit=Error During Exit",

"wintitle:notifyErrorWritingFile=Error Writing File",
"wintitle:notifyErrorReadingFile=Error Reading File",
"wintitle:notifyExportZeroBulletins=No Records Selected",
"wintitle:notifyPrintZeroBulletins=No Records Selected",
"wintitle:notifyNoGridRowSelected=No Row Selected",
"wintitle:notifyNoImportFileSpecified=No Folder Specified",

"wintitle:notifyUserRejectedServerCompliance=Server Compliance Statement",
"wintitle:notifyExistingServerRemoved=Server Removed",
"wintitle:notifyErrorSavingBulletin=Error Saving",
"wintitle:notifyExportFolderEmpty=Error Exporting Folder",
"wintitle:notifyErrorBackingUpKeyShare=Error Backing Up Key",				
"wintitle:notifyRecoveryProcessKeyShare=Restore Account from Key Backup",				
"wintitle:notifyRecoveredKeyShareSucceededNewUserNamePasswordRequired=Restore Succeeded",				
"wintitle:notifyVerifyKeyPairSharePassed=Verification Succeeded",				
"wintitle:notifyRecoveryOfKeyShareComplete=Restore Complete",
"wintitle:notifyOperationCompleted=Finished",
"wintitle:notifycontactRejected=Error Sending ContactInfo",
"wintitle:notifyUserAlreadyExists=User Already Exists",
"wintitle:notifyRecoveryProcessBackupFile=Restore Key",
"wintitle:notifyRecoveryOfKeyPairComplete=Successful Key Restore",
"wintitle:notifyErrorRecoveringAccountDirectory=Error During Restore",
"wintitle:notifyServerSSLNotResponding=Unable to connect",
"wintitle:notifyAlreadyRunning=Martus Already Running",
"wintitle:notifyFilesWillNotBeDeleted=Files Not Deleted",
"wintitle:notifyFolderMigrationFailed=Folder Migration Error",
"wintitle:notifyResendErrorNotAuthorizedToSend=Not Authorized To Send",
"wintitle:notifyResendError=Error Sending Record",
"wintitle:notifyErrorRenameFolder=Unable to Rename Folder",
"wintitle:notifyErrorRenameFolderExists=Unable to Rename Folder",
"wintitle:notifyAlreadyViewingThisVersion=Record Version Already Being Viewed",
"wintitle:notifyBulletinVersionNotInSystem=Record Version Not Found",
"wintitle:notifyErrorExportingCustomizationTemplate=Error Exporting Template",
"wintitle:notifyErrorImportingCustomizationTemplate=Error Importing Template",
"wintitle:notifyErrorImportingCustomizationTemplateFuture=Error Importing Template",
"wintitle:notifyImportingCustomizationTemplateSuccess=Importing Template Succeeded",
"wintitle:notifyCreatingFieldSpecCache=Missing List of Fields",
"wintitle:notifyRetrieveError=Retrieve Error",
"wintitle:notifyRetrieveInProgress=Retrieve In Progress",
"wintitle:notifyRetrieveFileDataVersionError=Warning",
"wintitle:notifyRetrieveFileError=Error",
"wintitle:notifyImportComplete=Import Complete",
"wintitle:notifyErrorImportingBulletins=Error Importing Records",
"wintitle:notifyErrorExportingBulletins=Error Exporting Records",
"wintitle:notifyImportMissingAttachments=Warning: Missing Attachments",
"wintitle:notifyImportBulletinsNotImported=Warning: Records Not Imported",
"wintitle:notifyNoReportFieldsSelected=Create Report",
"wintitle:notifyNotValidReportFormat=Reports",
"wintitle:notifyReportFormatIsOld=Reports",
"wintitle:notifyReportFormatIsTooNew=Reports",
"wintitle:notifyReportFormatDifferentLanguage=Reports",
"wintitle:notifyViewAttachmentNotAvailable=Unable to View Attachment",
"wintitle:notifySealSelectedZeroBulletinsOurs=Seal Records",
"wintitle:notifyErrorImportingBulletinsTooOld=Import Record(s)",
"wintitle:notifyErrorImportingBulletinsTooNew=Import Record(s)",
"wintitle:notifyErrorSavingDictionary=Error",
"wintitle:notifyErrorLoadingDictionary=Error",
"wintitle:notifyErrorUpdatingDictionary=Error",
"wintitle:BulletinContacts=Record Contacts",

"wintitle:FileDialogImportTemplate=Import Form Template",
"wintitle:FileDialogRecoverSharedKeyPair=Restore Account from Key Backup",

"wintitle:SelectTemplate=Template Selector",
"field:confirmOkToSwitchTemplate=Changing templates will replace the old template with a new one and may remove existing data from this record.",
"field:DisplayableDefaultFormTemplateName=Martus Standard Fields",

"wintitle:notifyUnableToLoadCurrentTemplate=Error",
"field:notifyUnableToLoadCurrentTemplatecause=Unable to restore the current form template, so the default template will be used.",

"wintitle:Templates=Template Manager",
"field:confirmDeleteTemplate=Are you sure you want to delete this template?\n#Name#",

"wintitle:notifyContactKeyIsOurself=Contact Account Is Yourself",
"field:notifyContactKeyIsOurselfcause=You cannot add yourself as a contact.",
"wintitle:notifyContactKeyAlreadyExists=Contact Account Already Exists",
"field:notifyContactKeyAlreadyExistscause=You have already added this contact.",
"wintitle:notifyUnableToRetrieveContactFromServer=Unable To Retrieve Contact Info",
"field:notifyUnableToRetrieveContactFromServercause=This token is not recognized by this server.",

"wintitle:notifyTemplateSavedToServer=Save Template To Server",
"field:notifyTemplateSavedToServercause=Successfully saved form template to your account on the server.",
"wintitle:notifyServerNotAvailable=Save Template To Server",
"field:notifyServerNotAvailablecause=Server not available.",
"wintitle:notifyErrorSavingTemplateToServer=Save Template To Server",
"field:notifyErrorSavingTemplateToServercause=Unexpected error saving template to server.",
"wintitle:notifyServerNotCompatible=Server Error",
"field:notifyServerNotCompatiblecause=This server does not support this feature.",

"wintitle:notifyErrorGettingCompliance=Error Getting Compliance Statement",
"field:notifyErrorGettingCompliancecause=An error occured while trying to connect to the server.",

"wintitle:notifyErrorServerOffline=Unable to Connect to Server",
"field:notifyErrorServerOfflinecause=Unable to connect to the server.",

"wintitle:notifyErrorNetworkOffline=Network Offline",
"field:notifyErrorNetworkOfflinecause=Martus is currently in offline mode.",

"wintitle:notifyErrorServerConnection=Server error",
"field:notifyErrorServerConnectioncause=The request was unsuccessful, due to a network error.",

"wintitle:notifySyncDisabledDueToError=Error",
"field:notifySyncDisabledDueToErrorcause=Automatic syncing has been disabled due to an unexpected error",

"wintitle:confirmShowOnMapBypassesTor=Show On Map",
"field:confirmShowOnMapBypassesTorcause=Tor is enabled, but requesting a map will bypass it.",
"field:confirmShowOnMapBypassesToreffect=If you continue, anyone monitoring you could see your map request.",

"wintitle:inputservermagicword=Request Upload Permission",
"wintitle:inputCustomFields=Customize Fields",
"wintitle:inputGetShareFileName=Key Backup",

//NOTE: Without notify is for swing; with notify is for javafx 
"wintitle:ErrorDateInFuture=Date Invalid",
"wintitle:ErrorDateRangeInverted=Date Range Invalid",
"wintitle:ErrorDateTooEarly=Date Too Early",
"wintitle:ErrorDateTooLate=Date Too Late",
"wintitle:notifyErrorDateInFuture=Date Invalid",
"wintitle:notifyErrorDateRangeInverted=Date Range Invalid",
"wintitle:notifyErrorDateTooEarly=Date Too Early",
"wintitle:notifyErrorDateTooLate=Date Too Late",

"wintitle:setupcontact=Martus Setup Contact Information",
"wintitle:BulletinTemplate=Details Field Default Content",
"wintitle:about=About Martus",
"wintitle:Help=Martus Help",
"wintitle:RetrieveMySealedBulletinProgress=Retrieving Records",
"wintitle:RetrieveMyDraftBulletinProgress=Retrieving Records",
"wintitle:RetrieveMySealedBulletinSummaries=Retrieving Record Summaries",
"wintitle:RetrieveMyDraftBulletinSummaries=Retrieving Record Summaries",
"wintitle:ConfigServer=Server Configuration",
"wintitle:ServerSelectionResults=Server Configuration Results",
"wintitle:search=Search",
"wintitle:BulletinPreview=Record Preview",
"wintitle:ServerNews=Server News: Message #CurrentNewsItem# of #MaxNewsItems#",
"wintitle:ExportBulletins=Export Records",
"wintitle:SaveShareKeyPair=Saving Backup Disk",
"wintitle:SaveRecoverShareKeyPairOf=of",
"wintitle:BackupSecretShareCompleteInformation=Backup Complete",				
"wintitle:RecoverShareKeyPair=Restore from Backup Disk",				
"wintitle:ErrorPreviousBackupShareExists=Previous File Exists",
"wintitle:ErrorRecoverNoAppropriateFileFound=No Appropriate File Found",
"wintitle:ErrorRecoverShareDisk=Key backup File Error",				
"wintitle:ErrorVerifyingKeyPairShare=Verification Error",				
"wintitle:VerifyingKeyPairShare=Verifying Disk",
"wintitle:RemoveServer=Remove Server",
"wintitle:BackupKeyPairToSecretShareInformation=Backup Key Information ",

// NOTE: Without notify is for swing; with notify is for javafx 
"wintitle:ErrorAttachmentMissing=Attachment Missing",
"wintitle:ErrorRequiredFieldBlank=Required Field Blank",
"wintitle:notifyErrorAttachmentMissing=Attachment Missing",
"wintitle:notifyErrorRequiredFieldBlank=Required Field Blank",

"wintitle:helpStringNotFound=Search Text Not Found",
"wintitle:CreateCustomFieldsHelp=Help on Creating Custom Fields",
"wintitle:ErrorCustomFields=Customize Fields Error",
"wintitle:SetFolderOrder=Folder Order",
"wintitle:FancySearchHelp=Search Help",
"wintitle:ConfigureSpellCheck=Configure Spell Checking",

"wintitle:warningDeleteSingleBulletin=Confirm Delete Record",
"wintitle:warningDeleteMultipleBulletins=Confirm Delete Records",

"wintitle:RunOrCreateReport=Reports",
"wintitle:ReportChooseSortFields=Reports",
"wintitle:FileDialogSelectReport=Run Report",
"button:FileDialogOkSelectReport=Select",
"wintitle:ChooseReportFields=Create Report",
"wintitle:OrganizeReportFields=Organize Report Fields",
"wintitle:PrintOptions=Print Options",
"wintitle:DeleteMyDataFromThisComputer=Delete My Data From This Computer",
"wintitle:RemoveMartsFromThisComputer=Remove Martus From This Computer",
"wintitle:ResendBulletins=Resend Records",
"wintitle:ImportBulletins=Import Records",
"wintitle:FileDialogImportBulletin=Import Record",

"wintitle:LoadSavedSearch=Load Saved Search",
"wintitle:FileDialogSaveSearch=Save Search",
"wintitle:SearchProgress=Searching...",
"wintitle:ReportSearchProgress=Searching...",
"wintitle:LoadingFieldValuesFromAllBulletins=Loading Values...",
"wintitle:CreateCaseIncidentProject=Create #FolderName#",
"wintitle:RenameCaseIncidentProject=Rename #FolderName#",
"wintitle:DeleteCaseIncidentProject=Delete #FolderName#",
"field:CreatingAccount=Please wait. Your account's encryption key is being created.",
"field:LoadingTemplates=Please wait while we retrieve a list of templates from the server.",
"field:FindAccountByToken=Please wait while we find this account on the server.",
"field:RetrievingRecordSummariesFromServer=Please wait while we retrieve records from the server.",
"field:moveToCaseProjectIncidents=Select one or more #FolderName#, to move selected items to.",
"field:VerifyPublicCodeNewAndOld=Please visually verify either public code below to ensure you are adding the person you think you are adding.",
"field:VerifyPublicCode=A public code is a unique identifier that can help you verify that a contact that you are adding is who they say they are.\n\nYou can visually verify the public code below. If you don’t know your contact’s public code, you can always perform the verification later.\n\nVerification is an important step that only needs to be performed once.",
"field:ContactAlreadyExistsAs=This contact currently has the name: '#Name#'",

"field:notifyShowTokenAndPublicCodecause=Access Token: #Token#\nPublic Code: #PublicCode#",
"wintitle:notifyShowTokenAndPublicCode=Account Token and Public Code",

"field:AttemptToConnectToServerAndGetCompliance=Attempting to connect to server",
"field:AttemptToConnectToServer=Attempting to connect to server",
"wintitle:notifyAdvanceServerNotResponding=Server Not Available",
"field:notifyAdvanceServerNotRespondingcause=A connection to the server was not made. This could be because the server is not available, you have entered an incorrect IP, the server is blocked, or your Internet connection is down.",
"wintitle:ServerNotRespondingSaveConfiguration=Server Not Available",
"field:ServerNotRespondingSaveConfiguration=A connection to the server was not made. This could be because the server is not available, the server is blocked, or your Internet connection is down. Would you like to save this server configuration for later?",
"field:ConnectingToServerToRetrieveToken=Attempting to connect to the server and retrieve your Token.",

"wintitle:ExitManageContacts=Exit Manage Contacts",
"field:ExitManageContacts=Your changes will be lost, are you sure you want to do this?",
"field:confirmExitManageContactseffect=Your changes will be lost, are you sure you want to do this?",

"wintitle:Trash=Trash",
"wintitle:EmptyTrash=Empty Trash",
"field:EmptyTrashConfirmation=This will permanently remove these records from your local computer. This does not affect the records on the server.",
"field:EmptyTrashConfirmationItemsInOtherFolders=Note: some records here are linked in other #Cases# and those copies will not be permanently removed.",
"field:ContactNamesSeparator=,",
"field:notifyLegacyPublicIsPrivateMessagecause=Note: Any record data made public using previous versions of Martus will no longer be public in the new Martus system.",

"tooltip:ShowTrash=Trash",
"tooltip:CaseSettings=Settings",
"tooltip:DeleteCase=Delete",
"tooltip:AddCase=Add",
"tooltip:EmptyTrash=Empty Trash",
"tooltip:DeleteItem=Delete",
"tooltip:MoveItem=Move",
"tooltip:CopyItem=Copy",
"tooltip:ExportItem=Export",
"tooltip:Templates=Template Manager",
"tooltip:TemplateSelector=Template Selector",
"tooltip:AddContact=Add Contacts",
"tooltip:ServerSync=Sync Manager",
"tooltip:ImportBulletin=Import Record",
"tooltip:NeverDeleteFromServer=Check to prohibit deletion of your records from the Martus server.",
"tooltip:CreateNewRecord=Create new record",
"tooltip:ServerCurrentlyOn=Server ON: Click to turn Server off",
"tooltip:ServerCurrentlyOff=Server OFF: Click to turn Server on",
"tooltip:TorCurrentlyOn=Tor ON: Click to turn Tor off",
"tooltip:TorCurrentlyOff=Tor OFF: Click to turn Tor on",
"tooltip:QuickSearch=Search",
"tooltip:Help=Help",

"button:export=Export",
"button:Help=Help",
"button:create=Create",
"button:search=Search",
"button:print=Print",
"button:send=Share",
"button:savedraft=Save",
"button:ShowMap=Show on Map",
"button:ShowImage=Return to image",

"button:saveBulletin=Save",
"button:versionBulletin=Version",
"button:shareBulletin=Share",

"button:InputServerNameOk=OK",
"button:inputsearchok=Search",
"button:inputCustomFieldsok=OK",
"button:inputGetShareFileNameok=OK",

"button:close=Close",
"button:customDefault=Restore Defaults",
"button:customHelp=Help",
"button:customImport=Template Manager",
"button:customExport=Export Template",
"button:customSendToServer=Send Template to Server",

"button:save=Save",
"button:retrieve=Retrieve",
"button:DeleteServerDrafts=Delete",
"button:checkall=Check All",
"button:uncheckall=Uncheck All",
"button:addattachment=Add",
"button:removeattachment=Remove",
"button:attachmentLabel=Name",
"button:attachmentSize=Size(Kb)",
"button:saveattachment=Save",
"button:viewattachment=View",
"button:hideattachment=Hide",
"button:VirtualKeyboardSwitchToNormal=Switch to using regular keyboard",
"button:VirtualKeyboardSwitchToVirtual=Switch to using on-screen keyboard",
"button:DownloadableSummaries=Show records that are only on the server.",
"button:AllSummaries=Show all records on this server and on this computer.",
"button:Preview=Preview",
"button:Delete=Delete",
"button:modify=Modify",
"button:Back=Back",
"button:Continue=Continue",
"button:ResetContents=Reset Contents",
"button:ServerComplianceAccept=Accept",
"button:ServerComplianceReject=Reject",
"button:SignIn=Sign In",
"button:NewAccountTab=New Account",
"button:RecoverAccountTab=Restore Account",
"button:RecoverAccountByShare=Restore account from multiple disks",
"button:RecoverAccountByBackup=Restore account from backup key file",
"field:CreateNewAccount=Want to create a new Martus account?  Click here.",
"button:BulletinDetails=Record Details",
"button:ViewPreviousBulletinVersion=View Selected Version",
"button:RetrieveAllVersions=Retrieve all record versions",
"button:RetrieveLatestBulletinRevisionOnly=Retrieve latest record version only",
"button:FolderOrderUp=Up",
"button:FolderOrderDown=Down",
"button:CloseHelp=Close Help",
"button:SearchFinalBulletinsOnly=Only search most recent version of records",
"button:SearchSameRowsOnly=Match multi-item list details in the search screen to a single item. See the search Help screen for additional guidance on this advanced option.",
"button:DeleteSelectedGridRow=Delete Selected Row",
"button:InsertEmptyGridRow=Insert Row",
"button:AppendEmptyGridRow=Append Row",
"button:PopUpTreeChoose=Choose Field",
"button:RunReport=Use Existing Report Format",
"button:CreateTabularReport=Create New Tabular Report",
"button:CreatePageReport=Create New Page Report",
"button:SelectReport=Select",
"button:FieldLabel=Field Label",
"button:FieldType=Type",
"button:FieldTag=Tag",
"button:SaveSearch=Save This Search",
"button:LoadSearch=Load Previous Search",
"button:LoadSearchOkButton=Load",
"button:LoadFieldValuesFromAllBulletins=Load all possible values for selected field",

"button:AddTemplate=Add",

"button:AddFieldToReport=Add",
"button:RemoveFieldFromReport=Remove",
"button:MoveFieldUpInReport=Move Up",
"button:MoveFieldDownInReport=Move Down",

"button:ShowGridExpanded=Show Expanded",
"button:ShowGridNormal=Show as Grid",

"button:ServerSyncRecordLocation=Location",
"button:ServerSyncRecordTitle=Title",
"button:ServerSyncRecordAuthor=Author",
"button:ServerSyncRecordLastModified=Last Saved",
"button:ServerSyncRecordSize=Size (Kb)",
"button:HaveServerDefaultToOn=Have Server Default to On? (Applied on login)",
"button:AutomaticallyDownloadFiles=Automatically Download Files from Server?",
"button:ExportAccountPublicKey=Export Public Account Key",
"button:ExportMultiKey=Export Multi-Part Key",

"field:RecordLocationLocal=Local",
"field:RecordLocationServer=Server",
"field:RecordLocationServerTab=Server",
"field:serverWithColon=Server ",
"field:RecordLocationBothLocalAndServer=Both",
"field:RecordLocationUnknown=Unknown",
"field:NoServerSyncDataInTable=No Records available",
"wintitle:notifySyncUnableToDownloadLocalFiles=Download Information",
"field:notifySyncUnableToDownloadLocalFilescause=The following records were unable to be downloaded since they were only found locally.  The remaining records will be downloaded in the background.",
"wintitle:notifySyncUnableToUploadServerFiles=Upload Information",
"field:notifySyncUnableToUploadServerFilescause=The following records were unable to be uploaded since they were only found on the server or you do not have permission to upload these as your own.  The remaining records will be uploaded in the background.",
"wintitle:notifySyncUnableToDeleteLocalOnlyOrImmutableFiles=Download Information",
"field:notifySyncUnableToDeleteLocalOnlyOrImmutableFilescause=The following records were unable to be deleted since they were only found locally or you do not have permission to delete them off the server.  The remaining records will be deleted.",

"button:IncludePrivateBulletins=Include Private Data",

"menu:file=File",
"menu:CreateNewBulletin=Create New Record",
"menu:printBulletin=Print Record(s)",
"menu:printButton=Print",
"menu:Analysis=Analysis",
"menu:Reports=Reports",
"menu:Charts=Charts",
"menu:ExportBulletins=Export Records",
"menu:ImportBulletins=Import Records",
"menu:ExportFolder=Export Folder",
"menu:exit=Exit",
"menu:edit=Edit",
"menu:search=Advanced Search",
"menu:modifyBulletin=Modify Record",
"menu:SelectAllBulletins=Select All Records",
"menu:CutBulletins=Cut Record(s)",
"menu:CopyBulletins=Copy Record(s)",
"menu:PasteBulletins=Paste Record(s)",
"menu:DiscardBulletins=Discard Record(s)",
"menu:DeleteBulletins=Delete Record(s)",
"menu:SealSelectedBulletins=Seal Record(s)",
"menu:ResendBulletins=Resend Record(s)",
"menu:folders=Folders",
"menu:CreateNewFolder=Create New Folder",
"menu:RenameFolder=Rename Folder",
"menu:DeleteFolder=Delete Folder",
"menu:OrganizeFolders=Organize Folders",
"menu:server=Server",
"menu:SelectServer=Select Martus Server",
"menu:options=Options",
"menu:Preferences=Preferences",
"menu:contactinfo=Contact Information",
"menu:DefaultDetailsFieldContent=Default Details Field Content",
"menu:ConfigureSpellCheck=Spell Checking",
"menu:changeUserNamePassword=Change Username or Password",
"menu:tools=Tools",
"menu:QuickEraseDeleteMyDataOnly=Delete My Data",
"menu:QuickEraseRemoveMartus=Delete All Data And Remove Martus",
"menu:BackupMyKeyPairFile=Backup My Key",
"menu:ExportMyPublicKey=Export My Public Account ID",
"menu:CustomFields=Customize Fields",
"menu:help=Help",
"menu:helpMessage=Help",
"menu:about=About Martus",
"menu:ViewMyAccountDetails=View My Account Details",
"menu:cut=Cut",
"menu:copy=Copy",
"menu:paste=Paste",
"menu:delete=Delete",
"menu:selectall=Select All",
"menu:RemoveServer=Remove Martus Server",
"menu:MoreSpellingSuggestions=More Suggestions",
"menu:AddToDictionary=Add '#NewWord#' to User Dictionary",

"field:translationVersion=English",
"field:aboutDlgVersionInfo=software version:",
"field:aboutDlgMlpDateInfo=Language Pack Date",
"field:aboutDlgTranslationVersionInfo=Translation version:",
"field:aboutDlgBuildDate=Built on:",
"field:aboutDlgDisclaimer=Martus comes with ABSOLUTELY NO WARRANTY, and is made available under license terms in the file named license.txt in the Martus directory. This is free software, and you are welcome to redistribute it under certain conditions discussed in license.txt. ",
"field:aboutDlgCredits=Martus Software is developed by Benetech in partnership with the Information Program of the Open Society Institute, Aspiration, and the John D. and Catherine T. MacArthur Foundation.",
"field:aboutDlgThirdParty=This product includes software developed by the Apache Software Foundation, Bouncy Castle, IBM, JH Labs, JOrtho, JUnit, The Mozilla Foundation, Logi Ragnarsson, Object Refinery Limited, and Subgraph [Orchid].",

"field:status=Status",

"field:BulletinSize=Size (Kb)",

"field:Author=Author ",
"field:Organization=Organization ",
"field:TemplateDetails=Details",
"field:MayBeDamaged=Warning: Portions may be missing or damaged",
"field:NotAuthorizedToViewPrivate=The author has not given you permission to view this record.",
"field:BulletinHasUnknownStuff=Warning: Some information in this record is not visible",
"field:retrieveflag=Retrieve?",
"field:DeleteFlag=Delete?",
"field:waitingForBulletinsToLoad=Loading Martus.  Please wait...",
"field:HelpDefaultDetails=Enter questions, details, or other information your organization wants to have answered in future records created.",
"field:HelpExampleDefaultDetails=Example:",
"field:HelpExample1DefaultDetails=Were there any witnesses?",
"field:HelpExample2DefaultDetails=List Victim Names and Ages:",
"field:HelpExampleEtcDefaultDetails=etc...",
"field:PublicInformationFiles=Public Information Files (*.mpi)",
"field:XmlFileFilter=XML Files (*.xml)",
"field:NormalKeyboardMsg1=Remember: Entering your password using the regular keyboard may reduce security.",
"field:NormalKeyboardMsg2=For maximum security switch to the on-screen keyboard.",
"field:RetrieveSummariesMessage=All records retrieved will still remain on the server.\nYou can only retrieve records that are not currently on your computer.",
"field:DeleteServerDraftsMessage=You can only delete draft records from the server that are not currently on your computer.",
"field:ContactInfoRequiredFields=This information identifies your organization.\nYou may enter either an Author, Organization, or both, and both are shown in every record you create.",
"field:ContactInfoFutureUseOfFields=This information is optional and is used to auto-fill your records for your convenience. It is stored on your local computer and not sent to the Martus server.",
"field:UploadingSealedBulletin=Sending Record",
"field:UploadingDraftBulletin=Sending Record",
"field:StatusReady=Server Ready",
"field:StatusRetrieving=Receiving Records",
"field:StatusConnecting=Connecting to server",
"field:RetrieveMyDraftBulletinProgress=Retrieving My Draft Records",
"field:NoServerAvailableProgressMessage=Server Not Available",
"field:ServerNotConfiguredProgressMessage=Server needs to be configured.",
"field:OfflineModeProgressMessage=Offline",
"field:UploadFailedProgressMessage=Upload Failed",
"field:ChunkProgressStatusMessage=Download Progress",
"field:TorStatusActive=Embedded Tor Active",
"field:TorStatusInitializing=Embedded Tor Initializing",
"field:TorStatusDisabled=Embedded Tor Disabled",
"field:OnlineHelpMessage=Details:",
"field:OnlineHelpTOCMessage=Topics:",
"field:DefaultDetailFiles=Default Details",
"field:ServerComplianceDescription=The server you have selected has provided the following statement describing its compliance with the official guidelines for the secure and reliable operation of a Martus server.  You can accept or reject this server based on its compliance statement.",
"field:ServerComplianceChangedDescription=The current server has updated its statement describing its compliance with the official guidelines for the secure and reliable operation of a Martus server.  The new statement appears below. You can accept or reject this server based on its compliance statement.",
"field:RecoverShareKeyPair=Another part of the shared key is required to complete this process.",
"field:ErrorPreviousBackupShareExists=A previous backup file exists.  You must save each file on its own removable media disk.",		
"field:ErrorRecoverNoAppropriateFileFound=No appropriate backup file was found on this disk, please try a different disk.",
"field:ErrorRecoverShareDisk=An error occurred reading this key backup file.",
"field:ErrorVerifyingKeyPairShare=An error occurred verifying this key backup file.",
"field:RecoverAccount=Restore your existing account from a backup file.",
"field:ErrorBackingupKeyPair=Unable to back up the key file on this disk.",
"field:BackupKeyPairToSecretShareInformation=This method breaks up your key into #TotalNumberOfFilesInBackup# pieces, any #MinimumNumberOfFilesNeededForRecovery# of which will be required to reconstruct the key without requiring a password. If you forget your login name or your password, this method is the only way you can restore your key.\n\nOnce the #TotalNumberOfFilesInBackup# files have been written to removable media disks, you should distribute them to #TotalNumberOfFilesInBackup# different people whom you will remember, but each of whom doesn't know to whom you have given the other files. Do not store any of the disks together.\n",
"field:BulletinWasSent=Sent",
"field:" + Bulletin.PSEUDOFIELD_WAS_SENT + "=Sent",
"field:preferencesAdjustThai=Automatically adjust legacy Thai dates",
"field:preferencesAdjustPersian=Automatically adjust legacy Afghan/Persian dates",
"field:preferencesUseZawgyi=Enable Zawgyi font to display Burmese",
"field:preferencesAllPrivate=Prevent creating public records",
"field:PreferencesUseInternalTor=Use embedded Tor (Using Tor can improve security but may be slower than not using Tor)",
"field:PreferencesWhyUseTor=Turn Tor ON if you wish to hide that you are connecting to a Martus server. This may help reach the Martus servers if they are blocked from your location.",
"field:CustomizationTemplateFileFilter=Customization Template Files (*.mct)",
"field:SearchGridHeaderField=Field(s) to search",
"field:SearchGridHeaderOp=Compare how?",
"field:SearchGridHeaderValue=Search for...",
"field:SearchOpContains=contains",
"field:SearchAnyField=--Any Field--",
"field:DuplicateLabelsInCustomTemplate=The following labels appear more than once in your template.",
"field:DuplicateLabels=Labels",
"field:DuplicateLabelsInCustomTemplateContinue=Do you want to save this template with the duplicate labels?",
"field:PrintPrivateDataMessage=You have the option to include or exclude the selected record's private information in this print out. " +
	"If you choose to print private data, it will be visible to anyone who sees the print out.",
"button:PrintOnlyPublic=Print only public information",
"button:PrintPublicAndPrivate=Include private information",
"field:PrintToPrinterOrDisk=You can print the record contents to a printer or to a file.",
"button:PrintToDisk=Print to a file",
"field:DefaultPrintToDiskFileName=records.html",
"wintitle:notifyPrintToDiskComplete=Print To Report File",
"field:notifyPrintToDiskCompletecause=Successfully saved Report",
"wintitle:notifyPrintCompleted=Reports",
"field:notifyPrintCompletedcause=Printing Complete",
"field:OrganizeReportFields=Add the fields you want to appear in your report, and then arrange them in the order you want them to be printed.",
"field:SkippingBulletinsNotOurs=Note: Records authored by other accounts cannot be changed using this command, " +
	"so they are not included in the list above.",
"field:DownloadTemplateFromMartusUser=Download from Another Martus User",
"field:DownloadTemplateFromMyContacts=Download from my Contacts",
"field:ExportMultiKeyNote=Note: This is the only way you can restore access to your account if you forget your password.",	
"field:ContactVerifyNow=Verify Now",
"field:ContactVerified=Verified",
"field:AuthorizedToReadNotInContacts=#PublicCode# (not currently in contacts)",
"field:AccountIdWithPublicCode=#AccountId# #PublicCode#",
"field:HistoryVersion=#VersionNumber#: #DateSaved# : #Title#",
"menu:Account=Account",
"field:AuthorNicknameFieldDescription=Name here if available otherwise blank",
"field:AuthorPublicCode=Author Public Code ",
"field:BulletinID=Record ID ",
"field:DateCreated=Date Created",
"field:LastModified=Last Modified",
"field:EnterNewName=Enter New Name ",

"wintitle:IncludePrivateData=Include Private Data",
"field:IncludePrivateData=#TotalBulletins# records were selected for printing.  #AllPrivateBulletins# of them are all private.\n What would you like to include when printing?",
"button:PublicAndPrivateData=Both Public and Private Data",
"button:PublicOnly=Public Data Only",
"button:SelectAll=Select All",
"button:Encrypted=Encrypted?",
"button:IncludeAttachments=Include attachments",
"button:RemoveFromExistingCase=Remove from #FolderName#?",
"button:Templates=Template Manager",
"button:SaveChanges=Save Changes",
"button:Upload=Upload",
"button:Download=Download",
"button:DeleteFromServer=Delete from Server",

"field:Location=Location ",
"field:OnServer=On Server",
"field:Edit=Edit",
"field:AssistanceWithMartusPart1=Martus help documentation is available at",
"field:AssistanceWithMartusPart1B=or inside C:\\Martus\\Docs on Windows",
"field:AssistanceWithMartusPart1C=and the Martus Documentation folder inside the Martus DMG on Mac.",
"field:AssistanceWithMartusPart2=For additional assistance, please email",
"field:All=All",
"field:LocalOnly=Local Only",
"field:ServerOnly=Server Only",
"field:MyRecords=My Records",
"field:SharedWithMe=Shared With Me",
"field:TemplateManager=Template Manager",
"field:AvailableTemplates=Available Templates",
"field:Name=Name",
"field:Delete=Delete",
"field:Upload=Upload",
"field:Export=Export",
"field:AddNewTemplate=Add New Template",
"field:Import=Import ",
"field:TemplateSelector=Template Selector",
"field:SystemTab=System",
"field:TorTab=Tor",
"field:CurrentConnection=Current Connection",
"field:DefaultServerConnection=Default Server ",
"field:ConnectToDefault=Connect to Default",
"field:AdvancedSettings=Advanced Server Settings ",
"field:ServerIP=IP Address",
"field:ServerPreferences=Server Preferences",
"field:Download=Download",
"field:SelectLanguage=Select Language ",
"field:Calendar=Calendar",
"field:DateDelimiter=Date Delimiter ",
"field:DateFormatSequence=Date Format Sequence ",
"field:CalendarType=Calendar Type ",
"field:RecordSettings=Record Settings",
"field:RecordDetails=Record Details",
"field:DoNotShowAgainThisSession=Do not show this message again during this session",
"field:NotAvailable=(n/a)",
"wintitle:PrintPreview=Print Preview",
"button:PrintToPrinter=Print To Printer",
"button:PrintToFile=Print To File",
"button:FromContact=From Another Martus User",
"button:ImportFromFile=Import From File",
"button:Choose=Choose",
"button:ConnectToServer=Connect to Server",
"button:ChangeUsernameAndPassword=Change Username and Password",

"button:CreateChart=Create a new chart template",
"wintitle:CreateChart=Charts",
"field:ChartPrivateFieldsNotice=Please note that any Martus record data in charts will not be encrypted, \nand anyone who gets a copy of the chart output will be able to read all the data. \nUse caution when selecting a private field.",
"field:ChartType=Type of Chart",
"field:ChartTypeBar=Bar Chart",
"field:ChartType3DBar=3D Bar Chart",
"field:ChartTypePie=Pie Chart",
"field:ChartTypeLine=Line Chart (records over time)",
"field:ChartItemLabelBlank=(Missing field or data)",
"field:ChartFieldToCount=Field to count",
"field:ChartSubtitle=Subtitle (optional)",
"field:ChartTitle=Martus Records by #SelectedField#",
"field:ChartCreatedOn=Chart produced #Date#",
"field:ChartSeriesTitle=Martus Record Counts",
"field:ChartYAxisTitle=Martus Record Count",
"field:ChartPieSliceLabel=#Count# records = #Percent# of Martus records",
"field:ChartSelectedBulletinsDisclaimerBar=Note: Chart shows number of Martus records matching search criteria.",
"field:ChartSelectedBulletinsDisclaimerPie=Note: Chart shows % of Martus records matching search criteria.",
"field:DefaultPrintChartToDiskFileName=chart.jpeg",
"wintitle:notifyChartCompleted=Charts",
"field:notifyChartCompletedcause=Printing complete",
"wintitle:notifyChartUnknownError=Charts",
"field:notifyChartUnknownErrorcause=An unknown error has occurred",
"wintitle:notifyRestartMartusForLanguageChange=Martus Restart Required",
"field:notifyRestartMartusForLanguageChangecause=Please restart Martus to see the language you have chosen.",

"wintitle:PreparingBulletins=Preparing Records...",
"field:PreparingBulletins=Preparing Records...",

"wintitle:SealingSelectedBulletins=Sealing Records...",
"field:SealingSelectedBulletins=Sealing Records...",

"wintitle:Export=Export",
"button:ExportTo=Send To",

"field:AddingPermissionsToBulletins=Updating access...",
"wintitle:FileDialogAddAttachment=Add Attachment",
"button:FileDialogOkAddAttachment=Add",

"wintitle:FileDialogRestoreFromKeyPair=Restore Account From KeyPair",
"button:FileDialogOkRestoreFromKeyPair=Restore",

"button:FileDialogOkImportMBA=Import",

"button:FileDialogOkImportCustomization=Import",

"button:FileDialogOkImportBulletins=Import",

"button:FileDialogOkRecoverSharedKeyPair=Recover",

"wintitle:FileDialogSaveKeyPair=Backup Key File",

"wintitle:FileSaveDialogExport=Export Records to which file?",
"wintitle:FolderSelectDialogExport=Export to which folder?",

"wintitle:FileDialogExportCustomization=Export Customization Template",

"wintitle:AddTemplate=Add Form Template",

"field:VirtualUserNameDescription=(Enter using regular keyboard)",
"field:VirtualPasswordDescription=Enter Password using mouse with on-screen keyboard below",
"field:VirtualKeyboardKeys=ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890-+=!@#$%^&*()_,.[]{}<>\\/?|;:~",
"field:VirtualKeyboardSpace=Space",
"field:VirtualKeyboardBackSpace=Back Space",

"field:ExportBulletinMba=Exporting encrypted Item",
"field:ExportBulletinXml=Exporting non-encrypted Items",
"field:BulletinDetails=Record Details",
"wintitle:BulletinDetails=Record Details",

"field:confirmquestion=Are you sure you want to continue?",
"field:confirmsendcause=You have chosen to save a completed record.",
"field:confirmsendeffect=This will permanently seal the record and you will not be allowed to make any further modifications to it except by creating a new version of the record.",
"field:confirmdeletefoldercause=You have chosen to permanently delete a folder.  ",
"field:confirmdeletefoldereffect=Any records in the folder will be moved to Trash.  ",
"field:confirmretrievecause=You have chosen to retrieve all records from the Martus server.  ",
"field:confirmretrieveeffect=This will restore all the discarded records that were sent to the server.  ",
"field:confirmRemoveAttachmentcause=You have chosen to remove the selected attachments from this record.",
"field:confirmRemoveAttachmenteffect=The selected attachments will be permanently removed from this record.",
"field:confirmOverWriteExistingFilecause=A file already exists with that name.",
"field:confirmOverWriteExistingFileeffect=The existing file will be deleted and replaced with the new information.",
"field:confirmCancelModifyBulletincause=You have chosen to cancel modifying this record.",
"field:confirmCancelModifyBulletineffect=Canceling will discard any changes made to this record.",
"field:confirmWarningSwitchToNormalKeyboardcause=Warning! Using the regular keyboard to enter your password greatly reduces the security of the Martus system, and could make it easier for an attacker to view your private data.",
"field:confirmCloneBulletinAsMinecause=You have chosen to modify a record that was created by someone else.",
"field:confirmUploadRemindercause=Please Note: There are records that have not been sent to a server.  Do you still want to exit?",
"field:confirmUploadRemindereffect=Clicking on Yes will exit Martus, and leave the unsent records.  They will be sent the next time you run Martus and connect to a server.",
"field:confirmDraftUploadRemindercause=Please Note: There are draft records that have been modified and not yet sent to a server.  Do you still want to exit?",
"field:confirmDraftUploadRemindereffect=Clicking on Yes will exit Martus, and leave the unsent records.  They will be sent the next time you run Martus and connect to a server.",
"field:confirmRedoWeakPasswordcause=The password you chose has fewer than 15 characters and less than 2 non-alphanumeric characters. We recommend choosing a stronger password.",
"field:confirmRedoWeakPasswordeffect=Clicking on Yes will continue with the password you just entered.",
"field:confirmResetDefaultDetailscause=This will reset the current Default Details contents to the original contents. After resetting the contents, you should review them to be sure they are correct.",
"field:confirmResetDefaultDetailseffect=After you review the contents, you can accept them, modify them, or press cancel to keep the existing default details.",
"field:confirmNotYourBulletinViewAttachmentAnywayscause=Warning: the record you are currently viewing is not yours.  Attachments can contain viruses, or malicious programs harmful to your computer.",
"field:confirmNotYourBulletinViewAttachmentAnywayseffect=Clicking on Yes will #action# the attachment. Clicking on No will cancel this action.",
"field:confirmServerComplianceFailedcause=Martus software is unable to determine whether this server complies with the official guidelines for operating a secure and reliable Martus server.",
"field:confirmServerComplianceFailedeffect=Benetech recommends that you do not use this server until a compliance statement is made available for you to read.  Continuing will select this non-compliant server anyway.",
"field:confirmWarnMissingOrCorruptAccountMapSignatureFilecause=Warning: acctmap.txt.sig file is missing or is corrupt in your account's packets directory.  If you have just upgraded to a new release of Martus this warning is harmless and you should click on No.  Otherwise this may indicate someone has tried to tamper with your data or could be caused by a hardware error.",		
"field:confirmWarnMissingOrCorruptAccountMapSignatureFileeffect=Clicking on Yes will try to generate a new acctmap.txt.sig file and continue to load Martus.  Clicking on No will exit Martus.",
"field:confirmWarnMissingAccountMapFilecause=Warning: acctmap.txt file in your account's packets directory is missing or is corrupt.  This may indicate someone has tried to tamper with your data or could be caused by a hardware error.",		
"field:confirmWarnMissingAccountMapFileeffect=Clicking on Yes will delete all of your records and continue to load Martus.  Clicking on No will exit Martus so you can try to recover or repair this file manually.",
"field:confirmReportIncludePrivatecause=There are no records with public data. Include private data?",
"field:confirmReportIncludePrivateeffect=Press 'Include Private Data' to continue with private data included, or press 'Cancel' to exit and return to Martus.",

"field:confirmCancelShareBackupcause=Do you wish to cancel this backup?",
"field:confirmCancelShareBackupeffect=By choosing Yes, you will exit this backup.",
"field:confirmEnterCustomFieldscause=You have chosen to customize the fields that will be used in any new records created by this account.",
"field:confirmEnterCustomFieldseffect=Customizing fields is an advanced operation. You should only proceed if you are certain that you understand the feature.",
"field:confirmUndoCustomFieldscause=You have chosen to restore the standard set of Martus fields.",
"field:confirmUndoCustomFieldseffect=New records created with this account will not have any custom fields.",
"field:confirmCancelShareRecovercause=Do you wish to cancel the key restore process?",
"field:confirmCancelShareRecovereffect=By choosing Yes, you will exit Martus.",
"field:confirmRecoveredKeyShareFailedTryAgaincause=Key Restore failed, we suggest you try again with a different set of files. Choose Yes to try again, No to exit Martus.",
"field:confirmRecoveredKeyShareFailedTryAgaineffect=Choosing Yes will begin the restore process again.",
"field:confirmErrorRecoverIvalidFileNamecause=The file you chose is not part of a key backup set.  These files are named '<name you picked>-#.dat' where # is the sequence number in the disk set previously created.",
"field:confirmErrorRecoverIvalidFileNameeffect=Choosing yes will bring up the file selection dialog again so you may pick a different file.",
"field:confirmBackupKeyShareVerifyDiskscause=We strongly recommend that you now verify all disks to make sure the data was written correctly.",
"field:confirmBackupKeyShareVerifyDiskseffect=By choosing Yes, the verification process will begin.",
"field:confirmCancelShareVerifycause=Do you wish to cancel the verification?",
"field:confirmCancelShareVerifyeffect=By choosing Yes, you will exit this verification step.",
"field:confirmEditBulletinWithUnknownTagscause=The record you have chosen to modify contains information that this version of Martus cannot understand. It may have been created by a newer release of Martus, so you should ensure that you are running the latest release.",
"field:confirmEditBulletinWithUnknownTagseffect=If you copy this record, the unknown information will be lost.",
"field:confirmExportUnknownTagscause=One or more of the records you have chosen to export contain information that this version of Martus cannot understand. They may have been created by a newer release of Martus, so you should ensure that you are running the latest release.",
"field:confirmExportUnknownTagseffect=If you continue with the export, any unknown information will not be exported.",
"field:confirmQuickEraseOutboxNotEmptycause=Please Note: There are records that have not been sent to a server. If you continue these records will be lost.",
"field:confirmQuickEraseOutboxNotEmptyeffect=Clicking on Yes will bring you to a confirmation dialog, Clicking on No will return you to Martus without erasing anything.",
"field:confirmKeyPairFileExistsOverWritecause=A key file already exists for this account.  Do you wish to overwrite it?",
"field:confirmKeyPairFileExistsOverWriteeffect=Clicking on Yes will delete the old key file and replace it with your restored key.",
"field:confirmCancelBackupRecoverycause=Do you wish to cancel the key  restore process?",
"field:confirmCancelBackupRecoveryeffect=By choosing Yes, you will exit Martus.",
"field:confirmUnableToRecoverFromBackupFilecause=Key restore failed, we suggest you try again with a different backup file.",
"field:confirmUnableToRecoverFromBackupFileeffect=Choose Yes to begin the restore process again, No to exit Martus.",
"field:confirmWarningPathChosenMayNotBeRemoveablecause=Are you sure the directory you chose to save your backup files to is removable?  You cannot save all #TotalNumberOfFilesInBackup# files to the same location without inserting a new disk for each backup file.",
"field:confirmWarningPathChosenMayNotBeRemoveableeffect=Choosing Yes will begin the backup process as instructed.",
"field:confirmBackupKeyPairInformationcause=Save your password encrypted key backup file to a single removable medium and store it in a safe place. You can use this backup file, with your username and password, to restore your data.",		
"field:confirmBackupKeyPairInformationeffect=Choosing Yes will begin the backup process.",		
"field:confirmhelpStringNotFoundcause=The text \"#SearchString#\" was not found.  Do you wish to search from the beginning?",
"field:confirmhelpStringNotFoundeffect=Clicking on Yes will search for \"#SearchString#\" from the beginning.",
"field:confirmgeneralBackupKeyPairMsgcause=If you lose your key, and you do not have a copy, you will not be able to open any of your records.",
"field:confirmgeneralBackupKeyPairMsgeffect=Selecting Yes will guide you through the backup procedure, selecting No will skip this important step.",
"field:confirmbackupIncompleteEncryptedNeeded=A single encrypted backup file of your key has yet to be created.",
"field:confirmbackupIncompleteImprovedShareNeeded=Martus has improved the security of the multiple pieces key backup.  To keep your account secure and enable you to access your data if you forget your password, please create a new set of key backup files, and delete/destroy the old ones.",
"field:confirmbackupIncompleteShareNeeded=A multiple disk backup of your key has yet to be created.",
"field:confirmNeedsFolderMigrationcause=This account was created with an older release of Martus, which used a different set of folders. The folders need to be migrated to the new format. The migration is automatic, fast, and safe. You cannot run this release of Martus unless you allow this migration.",
"field:confirmNeedsFolderMigrationeffect=Answering Yes will allow the migration to proceed, answering No will exit Martus.",
"field:confirmNeedsBulletinVersioningMigrationcause=This account was created with an older release of Martus, which was unaware of multiple sealed record versions. We need to update your system to recognize records with versions.  The migration is automatic, fast, and safe. You cannot run this release of Martus unless you allow this migration.",
"field:confirmNeedsBulletinVersioningMigrationeffect=Answering Yes will allow the migration to proceed, answering No will exit Martus.",

"field:confirmUnAuthoredBulletinDeleteBeforePastecause=The record entitled \"#Title#\" already exists in this system.  Do you wish to delete the old record and replace it with this record?",
"field:confirmUnAuthoredBulletinDeleteBeforePasteeffect=Answering Yes will replace the old record with this record.  Answering on No will leave the original record and skip this file.",
"field:confirmRemoveMartuscause=You are about to delete all Martus data from this computer.",
"field:confirmRemoveMartuseffect=Answering Yes will delete all data, uninstall Martus and exit the program.  Answering No will return you to Martus with nothing deleted.",
"field:confirmDeleteMyDatacause=You are about to delete your Martus data from this computer.",
"field:confirmDeleteMyDataeffect=Answering Yes will delete your data and exit Martus.  Answering No will return you to Martus with nothing deleted.",
"field:confirmNewerConfigInfoFileFoundcause=Warning: It appears that you are trying to run an older release of Martus.  Running this release of Martus may result in reduced functionality, and some configuration settings created in the newer version will be ignored. We recommend that you upgrade your software.",  
"field:confirmNewerConfigInfoFileFoundeffect=Answering Yes will continue to use this older release of Martus.  Answering No will exit Martus so you can upgrade to the latest release.",
"field:confirmRetrieveNewerVersionscause=The following record(s) selected for retrieval are newer versions of records already on this computer:\n\n#Titles#",
"field:confirmRetrieveNewerVersionseffect=Each older version will be replaced by the newer version from the server.",
"field:confirmPrintAllPrivateDatacause=One or more records will not be printed because all the information is private, and you marked the \"Print only public information\" box.",
"field:confirmPrintAllPrivateDataeffect=Answering '#PrintBack#' will allow you to return to the previous dialog so you can mark the \"Include private information\" box. " +
	"Answering '#PrintContinue#' will print only the public information in the records and skip those records which are all private.",
"field:confirmCancelRetrievecause=Records are currently being retrieved from the server.",
"field:confirmCancelRetrieveeffect=This operation will cancel the current retrieval, so some of the requested records may not be retrieved.",

"field:confirmDateRageInvalidcause=The date range you entered is invalid because the end date occurs before the begin date.",
"field:confirmDateRageInvalideffect=Answering Yes will take you back to the '#FieldLabel#' date range to fix the problem.  Answering No will swap the begin and end dates so they are in order.",

"field:confirmSealSelectedBulletinscause=This will seal all the currently selected draft records.",
"field:confirmSealSelectedBulletinseffect=Any selected records that are already sealed will remain unchanged.",

"field:confirmXmlSchemaNewerImportAnywaycause=This XML file was created by a newer version of Martus.",
"field:confirmXmlSchemaNewerImportAnywayeffect=If you continue with the import, some information in the record(s) might not be imported.",

"field:confirmImportingCustomizationUnknownSignercause=This template was not created by a known account. You should carefully review the fields to make sure the template is suitable. You will be able to cancel the template editing to restore the earlier template.",
"field:confirmImportingCustomizationUnknownSignereffect=To see the template contents, continue with the import.",
"wintitle:TemplateEditor=Template Editor",
"field:ImportTemplateWhichAlreadyExists=A template by that name already exists. Enter a new title for this template or leave the title the same to replace the old template with this new one.",
"field:ImportTemplateNoName=This template currently does not have a name.  Enter a new title for this template.",

"wintitle:confirmUploadPublicTemplateToServerWarning=Send Template to Server",
"field:confirmUploadPublicTemplateToServerWarningcause=Anyone with your access token can download this template from the server.",
"field:confirmUploadPublicTemplateToServerWarningeffect=Select Yes to make this template’s details publicly available on the server.",
"field:WhichTemplate=Selected template: #TemplateTitle#",


"field:notifyDropErrorBulletinExistscause=One or more records cannot be moved to that folder, because it already exists in that folder.",
"field:notifyDropErrorBulletinOldercause=One or more records cannot be moved to that folder, because a newer version of this record already exists.",
"field:notifyDropErrorscause=An unexpected error occurred while moving the record(s). One or more files may be damaged.",
"field:notifyPasteErrorBulletinAlreadyExistscause=One or more records cannot be pasted in that folder, because they already exist in this folder.",
"field:notifyPasteErrorBulletinOldercause=One or more records cannot be pasted in that folder, because a newer version of this record already exists.",
"field:notifyPasteErrorcause=An unexpected error occurred while pasting the record(s). One or more files may be damaged.",
"field:notifyretrievenothingcause=No records were selected",
"field:notifyDeleteServerDraftsWorkedcause=All of the selected draft records have been deleted from the server.",
"field:notifyDeleteServerDraftsFailedcause=Error: Unable to delete all of those draft records from the server. Some of them may have been deleted.",
"field:notifyDeleteServerDraftsNonecause=No records were selected",
"field:notifyretrievenoservercause=The current server is not responding or may need to be configured from the server menu.",
"field:notifyContactsNoServercause=The current server is not responding or may need to be configured from the server menu.",
"field:notifypasswordsdontmatchcause=Warning: You must enter the same password you entered on the previous screen.",
"field:notifyusernamessdontmatchcause=Warning: You must enter the same username you entered on the previous screen.",
"field:notifyUserNameBlankcause=Username must not be blank",
"field:notifyPasswordInvalidcause=Not a valid password, passwords must be at least 8 characters long, 15 recommended.",
"field:notifyPasswordMatchesUserNamecause=Your password can not be your username",
"field:notifyincorrectsignincause=Username or password incorrect",
"field:notifyuploadrejectedcause=The current Martus Server has refused to accept a record",
"field:notifycorruptconfiginfocause=The configuration file may be corrupted",
"field:notifymagicwordrejectedcause=The Server has rejected your request. The magic word is probably not correct.",
"field:notifyRewriteKeyPairFailedcause=An error occurred.  Unable to change username or password.  You may need to restore your backup key file.",
"field:notifyRewriteKeyPairSavedcause=Successfully saved your new username and password.",
"field:notifyUnableToSaveAttachmentcause=Unable to save the selected attachment for some reason.  Try saving it to a different file.",
"field:notifyUnableToViewAttachmentcause=Unable to view the selected attachment for some reason.",
"field:notifySearchFailedcause=Sorry, no records were found.",
"field:notifyErrorDuringExitcause=An error has occurred, which may have prevented Martus from saving some configuration information. You may see a warning next time you log in.",

"field:notifyServerErrorcause=Server Error, the server may be down, please try again later",
"field:notifyFoundOrphanscause=One or more records were not in any folder. These lost records have been placed into the Recovered Records folder.",
"field:notifyFoundDamagedBulletinscause=One or more records were severely damaged, and cannot be displayed. If these records were backed up to a server, you may be able to retrieve undamaged copies from there.",
"field:notifyErrorSavingStatecause=Unable to save current screen layout.",
"field:notifyExportMyPublicKeycause=The following file has been exported: #Filename#",
"field:notifyPublicInfoFileErrorcause=The file does not contain valid public information.",
"field:notifyErrorSavingConfigcause=Unable to save configuration file.",
"field:notifyErrorSavingFilecause=Unable to save the file. This could be because the destination is readonly or full, or because of a hardware error.",
"field:notifyErrorBackingupKeyPair=Unable to verify the backup. Please try again, possibly to a different destination.",
"field:notifyAuthenticateServerFailedcause=Martus could not authenticate the server. The server may have been compromised.  Please verify your server configuration and contact the server operator.",
"field:notifyUnexpectedErrorcause=An unexpected error has occurred. Please report this problem to martus@benetech.org.",
"field:notifyInvalidServerNamecause=You must have a server name or IP address.",
"field:notifyInvalidServerCodecause=You must have a server public code.",
"field:notifyServerInfoInvalidcause=The Server has responded with invalid account information.",
"field:notifyConfigNoServercause=The selected server is not responding. Before you choose a server, you must be connected to the internet, and that server must be available.",
"field:notifyServerCodeWrongcause=The Server Public Code does not match the one you entered.",
"field:notifyRememberPasswordcause=Please remember your username and password. It cannot be recovered.",
"field:notifyDamagedBulletinMovedToDiscardedcause=An error occurred during upload, and the damaged record'#BulletinTitle#' has been moved to the Damaged Records folder.",
"field:notifyUploadFailedBulletinNotSentToServercause=An error occurred during upload, and the record:'#BulletinTitle#' has not been sent to the server.  You may try and resend the record later.",
"field:notifyPreviewOneBulletinOnlycause=You may only preview one record at a time.  Please only select one record to preview.",
"field:notifyPreviewNoBulletinsSelectedcause=No record selected.  Please select the record you wish to preview.",
"field:notifyRetrievePreviewNotAvailableYetcause=Preview not yet available for this record. Please wait until the information has been retrieved from the server, and then try again.",
"field:notifyRetrievedOnlySomeSummariescause=Errors occurred while retrieving record summaries.  Some of the records on the server will not be shown.",
"field:notifyConfirmCorrectDefaultDetailsDatacause=Please confirm that the default details retrieved are correct.",
"field:notifyExportCompletecause=Successfully exported #BulletinsExported# of #TotalBulletinsToExport# records.",
"field:notifyExportCompleteMissingAttachmentscause=Exported #BulletinsExported# of #TotalBulletinsToExport# records.  Unfortunately #AttachmentsNotExported# attachments were not exported due to errors.",

"field:notifyErrorWritingFilecause=An error prevented the file from being written. Check to make sure the disk is not full or write protected.",
"field:notifyErrorReadingFilecause=An error prevented the file from being read.",
"field:notifyExportZeroBulletinscause=To export records, select them first, and then perform the export operation.",
"field:notifyPrintZeroBulletinscause=To print one or more records, select them first, and then perform the print operation.",

"field:notifyUserRejectedServerCompliancecause=You have chosen not to use this server",
"field:notifyExistingServerRemovedcause=You will have to select a server for any records to be backed up to that server, or to retrieve records from that server.",
"field:notifyErrorSavingBulletincause=An error prevented the record from being saved. Check to make sure the disk is not full or write protected.",		
"field:notifyExportFolderEmptycause=The folder you are trying to export does not contain any records.  Select a folder which has records before exporting the folder.",		
"field:notifyErrorBackingUpKeySharecause=An unexpected error occurred in generating the key backup files.",
"field:notifyRecoveryProcessKeySharecause=You will now have to provide #MinimumNumberOfFilesNeededForRecovery# out of the #TotalNumberOfFilesInBackup# files you previously saved when you backed up your key.",
"field:notifyRecoveredKeyShareSucceededNewUserNamePasswordRequiredcause=Key restore succeeded!  You now have to enter a username and password.",
"field:notifyRecoveryOfKeyShareCompletecause=You have successfully restored your key from your backup files.  It is very important that you now re-distribute your backup files.  We also recommend you backup your key as a single password-encrypted file, though you do not need to recreate the multiple file backup.",
"field:notifyVerifyKeyPairSharePassedcause=Verification of all disks passed.",
"field:notifyOperationCompletedcause=Operation completed.",
"field:notifycontactRejectedcause=The current Martus Server has refused to accept your contact info",
"field:notifyUserAlreadyExistscause=The username you chose already exists on this system, please choose a different username.",
"field:notifyRecoveryProcessBackupFilecause=To restore your key, first you must find the backup file you saved previously.  Using the 'Restore Account from Key Backup' dialog box find the file on your computer's hard drive, a network drive, or removable media disk then click the Open button.  Martus will then ask you for your username and password and restore your key so you can log in normally and access your account.",
"field:notifyRecoveryOfKeyPairCompletecause=Key restore is complete, you will now be logged into Martus.",
"field:notifyErrorRecoveringAccountDirectorycause=Error during key restore. This key backup copy may be damaged.",
"field:notifyServerSSLNotRespondingcause=Unable to make a secure connection with the Martus backup server.\n\nMartus can connect to a server using either of two ports.  When trying to connect to this server, neither port is accessible, probably because both ports are being blocked by a local firewall or by your ISP.\n\nWe recommend that any firewalls be configured to allow outgoing connections on both TCP port 443 and 987.  Please contact your LAN administrator or office technical support staff to verify that your firewall configuration allows this.\n\nIf the problem persists, you may need to select a different server.",
"field:notifyAlreadyRunningcause=An instance of Martus is already running on this computer. You must close that copy before starting a new copy.",
"field:notifyFilesWillNotBeDeletedcause=The original files have not been deleted from your computer, you must delete them manually if you wish.",
"field:notifyFolderMigrationFailedcause=An error occurred during the conversion of your folders. You can still use Martus, but if the Outbox or Drafts folders still exist, do not use them.",
"field:notifyResendErrorNotAuthorizedToSendcause=One or more records were unable to be resent because you are not authorized to upload them.",
"field:notifyResendErrorcause=One or more records were unable to be resent due to an unexpected error.",
"field:notifyErrorRenameFoldercause=Folder names cannot contain punctuation.  They also cannot begin with a space.",
"field:notifyErrorRenameFolderExistscause=You already have a folder with that name.",
"field:notifyAlreadyViewingThisVersioncause=You currently are viewing this version of the record.",
"field:notifyBulletinVersionNotInSystemcause=The record version you are trying to view is currently not on your computer.",
"field:notifyErrorExportingCustomizationTemplatecause=There was an error saving your template.",
"field:notifyErrorImportingCustomizationTemplatecause=There was an error importing this template.",
"field:notifyErrorImportingCustomizationTemplateFuturecause=The customization template you are trying to import was created by a newer version of Martus.  You need to upgrade to the latest version of Martus before you can import this template.",
"field:notifyImportingCustomizationTemplateSuccesscause=Successfully imported a customization template that was created by #CreatedBy#.",
"field:TemplateCreatedByThisAccount=this account",
"field:TemplateCreatedByUnknown=an unknown account",
"field:notifyCreatingFieldSpecCachecause=Martus needs to keep a list of all the fields in all the records in your system.\n\nThis list does not exist, so it will be created now. This may take a few seconds per record on a slower computer.",
"field:notifyRetrieveErrorcause=An error has occurred while retrieving a record.",
"field:notifyRetrieveInProgresscause=A retrieve is already in progress. You cannot start another retrieve until that one has finished.",
"field:notifyRetrieveFileDataVersionErrorcause=A retrieve was in progress when you upgraded to a newer version of Martus. That retrieve will be canceled, so you should re-select any records that had not yet been retrieved.",
"field:notifyRetrieveFileErrorcause=An error has prevented Martus from continuing the retrieve that was in progress. You should re-select any records that had not yet been retrieved.",
"field:notifyNoGridRowSelectedcause=No row selected.",
"field:notifyImportCompletecause=Import complete.  Imported #BulletinsSuccessfullyImported# of #TotalBulletinsToImport# records into folder #ImportFolder#",
"field:notifyErrorImportingBulletinscause=There was an error importing records into Martus.  Not all records were imported.",
"field:notifyErrorExportingBulletinscause=There was an error exporting records from Martus.  Not all records were exported.",
"field:notifyNoImportFileSpecifiedcause=No folder specified.  You must enter a folder you wish the files to be imported into.  This folder can already exist in the system, or can be a new folder.",
"field:notifyImportMissingAttachmentscause=Not all attachments were imported.  The following records had problems importing these attachments.\n\n#ImportMissingAttachments#",
"field:notifyImportBulletinsNotImportedcause=Not all records were imported.  The following records had problems during import.\n\n#ImportBulletinsNotImported#",
"field:notifyNoReportFieldsSelectedcause=You must select at least one field to be included in the report.",
"field:notifyNotValidReportFormatcause=This is not a valid Report Format file",
"field:notifyReportFormatIsOldcause=This report format was created with an earlier version of Martus, so it may not work correctly",
"field:notifyReportFormatIsTooNewcause=This report format was created with a newer version of Martus, so it may not work correctly",
"field:notifyReportFormatDifferentLanguagecause=This report format was created in a different language. As a result, some headings may not print properly, and some of the fields may not be available for sorting.",
"field:notifyViewAttachmentNotAvailablecause=This computer can only view JPEG, GIF, and PNG image attachments",
"field:notifyAddPermissionsZeroBulletinsOurscause=You must select at least one record that was created by this account",
"field:notifySealSelectedZeroBulletinsOurscause=You must select at least one record that was created by this account",


"field:notifyErrorImportingBulletinsTooOldcause=This XML file was created by an older version of Martus and cannot be read by this version.",
"field:notifyErrorImportingBulletinsTooNewcause=This XML file was created by a newer version of Martus and cannot be read by this version.  You must upgrade to the newer version of Martus to import this file.",
"field:notifyErrorSavingDictionarycause=Unknown error saving user dictionary",
"field:notifyErrorLoadingDictionarycause=Unknown error loading user dictionary for spell checking",
"field:notifyErrorUpdatingDictionarycause=Unable to update the dictionary. Be sure the word list is in the correct format.",
"field:SpellCheckUserDictionaryInstructions=The following words have been added to the user dictionary. \nYou can delete or edit them here, or you can add more words. \nEach word must be on a line by itself.",

"field:SingleEncryptedKeyBackupCreated=Single, Encrypted File #backupFileName created (this still requires remembering your username and password to restore your account).",

"field:IncompatibleMtfVersion=The version of this translation is not compatible with this version of Martus.  It is recommended that you do not continue to use this translation version and go to (https://martus.org) for the appropriate version.",
"field:IncompatibleMtfVersionTranslation=#MtfLanguage# Translation Version: #MtfVersionNumber#",
"field:IncompatibleMtfVersionProgram=Martus Software Version: #ProgramVersionNumber#",

"field:SaveAttachmentAction=save",
"field:ViewAttachmentAction=View",
// NOTE: the messageXxx are for swing, and the notifyXxx are for JavaFX
"field:messageServerNewscause=The current server has sent this message:",
"field:messageErrorDateInFuturecause=This date occurs in the future:",
"field:messageErrorDateRangeInvertedcause=This date range has an end date that is earlier than the start date: #FieldLabel#",
"field:messageErrorDateTooEarlycause=Dates entered in '#FieldLabel#' cannot be earlier than #MinimumDate#",
"field:messageErrorDateTooLatecause=Dates entered in '#FieldLabel#' cannot be later than #MaximumDate#",
"field:messageErrorAttachmentMissingcause=The attachment could not be located.",
"field:messageErrorRequiredFieldBlankcause=This field is required and cannot be left blank: #FieldLabel#",
"field:notifyErrorDateInFuturecause=This date occurs in the future:",
"field:notifyErrorDateRangeInvertedcause=This date range has an end date that is earlier than the start date: #FieldLabel#",
"field:notifyErrorDateTooEarlycause=Dates entered in '#FieldLabel#' cannot be earlier than #MinimumDate#",
"field:notifyErrorDateTooLatecause=Dates entered in '#FieldLabel#' cannot be later than #MaximumDate#",
"field:notifyErrorAttachmentMissingcause=The attachment could not be located.",
"field:notifyErrorRequiredFieldBlankcause=This field is required and cannot be left blank: #FieldLabel#",

"field:CreateCustomFieldsHelp1=The layout of the records is dictated by an XML document.  " +
	"The document must begin with <CustomFields> and end with </CustomFields>. " +
	"By default the standard record fields occur at the top of the Custom Field declaration, " +
	"but they can be moved if desired.  However, there are four required fields that cannot be removed:  " +
	"'author', 'entrydate', 'language' and 'title'.\n\n",
"field:CreateCustomFieldsHelp2=For custom (non-standard) fields, " +
	"you first select the type of field you want.  " +
	"The possible choices are 'BOOLEAN', 'DATE', 'DATERANGE', 'DROPDOWN', " +
	"'GRID', 'LANGUAGE', 'MESSAGE', 'MULTILINE', 'STRING', and 'SECTION'. " +
	"\n\n" +
	"For each custom field you will need a unique identification tag.  " +
	"This tag can be any word except those already used by the system " +
	"(eg. 'author', 'summary', 'location', 'title' etc.), " +
	"and cannot contain spaces or special characters.  " +
	"Examples of choices are 'VictimsName', 'EyeColorChoice', etc." +
	"\n\n" +
	"Then you need a label which is displayed next to your custom field.  " +
	"An example might be 'Name of 1st Witness'." +
	"\n\n" +
	"You can create sections (which you can hide/unhide) in your records " +
	"using a SECTION field type.  " +
	"\n\n" +
	"You can put multiple fields on a single row in your record by using " +
	"<KeepWithPrevious/> in the field definition." +
	"\n\n" +
	"You can require certain fields or grid columns to be entered before " +
	"saving a record by using </RequiredField> in the field definition." +
	"\n\n" +
	"You can restrict standard or custom date fields, date grid columns, date range fields, " +
	"and date range grid columns by using <MinimumDate> and/or <MaximumDate> " +
	"tags with a date in YYYY-MM-DD format. " +
	"Note: The year must always be a 'Gregorian' year like 2009, " +
	"even if Martus is configured to use Thai or Afghan or Persian dates. " +
	"A blank date, shown as <MaximumDate/>, means 'today', " +
	"although it may allow one day earlier or later, " +
	"due to time zone issues. " +
	"\n\n" +
	"You can populate drop-down lists (either inside or outside of a grid) " +
	"in three ways - " +
	"1) by entering a list of <Choices> values in the field definition, " +
	"2) with values that have been entered in a grid elsewhere " +
	"in your record by using <DataSource> (sometimes called \"data-driven dropdowns\"), and " +
	"3) by creating a list of \"Reusable Choices\" that can be referred to " +
	"by more than one field.  " +
	"See examples below for the correct XML definition syntax to use." +
	"\n\n" +
	"You can set a default value for text and dropdown list fields by using " +
	"<DefaultValue>ddd</DefaultValue>. " +
	"For dropdowns, you must use a value already in the list of choices you defined. " +
	"For Reusable Choices dropdowns it can be a partial or complete code, with each level separated by dots " +
	"(for a location dropdown that has both Region and City levels, " +
	"you could pick the default to be at either level, e.g. either R1 or R1.C1, see example below). " +
	"Default values can be set for both standard and custom fields, " +
	"but are NOT allowed for BOOLEAN, DATE, DATERANGE, GRID, LANGUAGE, MESSAGE, and SECTION field types, " +
	"and are not allowed for dropdowns where the values in the list are based on data entered in another field." +
	"(i.e. data-driven dropdowns).  " +
	"Please note that default values are only applied when a new record is created, " +
	"not when a new version of a record is created, " +
	"so that the value of the field in the previous version is not overwritten.  " +
	"This means that default values entered in a record using an earlier customization will be kept " +
	"even if you create a new version of the record with an updated customization that has a new default value" +
	"\n\n " +
	"Additional Comments" +
	"\n\n" +
	"1. XML is case-sensitive ('Witness' is not the same as 'witness')" +
	"\n\n" + 
	"2. Quotes around type name can be single or double as long as they match " +
	"(e.g. 'STRING\" is not valid. It must be 'STRING' or \"STRING\")" +
	"\n\n" +
	"3. A Boolean field will be displayed as a checkbox when editing and Yes/No when previewed or printed." +
	"\n\n" +
	"4.  Use \"MESSAGE\" fields to give guidance on how to enter data, and to create comments/notes " +
	"that will be displayed in every record." +
	"\n\n" +
	"5.  Dropdowns using a \"Reusable Choices\" list can have multiple levels " +
	"(e.g. for locations that might have Region and City), different fields can use one or more of the levels, " +
	"and the number of levels is not limited." +
	"\n\n" +
	"6. Both STRING and MULTILINE fields are text fields.  " +
	"STRING fields will expand to fit the size of the text you enter, " +
	"while MULTILINE fields have a scrollbar so that the field doesn't exceed its original height." +
	"\n\n" +
	"7.  A GRID can contain columns of various types:  BOOLEAN, DATE, DATERANGE, DROPDOWN, and STRING. " +
	"When you're entering data into a grid, press Enter to create a new line, press Tab to advance to the next cell, " +
	"and double-click to copy and paste text." +
	"\n\n" +
	"8.  You can update your customization if the information you are collecting over time changes, " +
	"for example by adding new fields.  If you are changing fields in a customization, " +
	"you should think about how you will want to search/report on records created with the old customization " +
	"as well as new records you create with the new customization, " +
	"and try to make the changes so that you can search/report on all records at the same time.  " +
	"Changing field types may cause your searching/reporting to be more complex, " +
	"so we always recommend that you test out creating records with a new customization and searching/reporting " +
	"on both old and new records before officially updating the customization for your project.  " +
	"When you change customizations, Martus will do its best to update the old formatted data to the new customization " +
	"if you create a new version of a record with the old customization.  " +
	"If you add completely new fields but don't change any of the old fields, " +
	"the new version of the record will have all the old fields filled in as they were in the previous version, " +
	"and the new fields will be blank and you can fill them in.  If you delete fields, " +
	"the new version of the record will not contain those fields, but you can go back to see the deleted fields " +
	"in the previous version if it was a sealed record instead of a draft " +
	"(by hitting the Record Details button in the Header section of the record), " +
	"and copy any info from the old version into a different field in the new one if desired.  " +
	"You have to be very careful if you make changes to the customization definitions of old fields.  " +
	"If you keep the same tag and label, but change the type of field, " +
	"Martus may be able to transfer the old data into the new field type, but not in all cases.  " +
	"For example, if you change field type from DROPDOWN to STRING, the data will be transferred over, " +
	"but if you change from STRING to BOOLEAN, the data will be lost. If you change from a DATE to DATERANGE, " +
	"your data will be transferred, but if you change from DATERANGE to DATE, you will lose the end date from your earlier data. " +
	"DROPDOWN and GRID fields are subject to additional rules regarding the modification of dropdown options/values and grid columns. " +
	"If you add options/values to a dropdown list, the old data will be transferred over. " +
	"But if you modify or delete an option/value, all records for which that option was selected will lose that data. " +
	"If you want to add columns to a grid, please make sure to do so at the end of the old grid definition and not in the middle, " +
	"or the old data will not be transferred to the new version of the record. " +
	"If you change fields and lose old data in the new version as a result, " +
	"please note that you can go back to see the deleted data in the previous version " +
	"if it was a sealed record instead of a draft (by hitting the Record Details button in the Header section of the record), " +
	"and copy any info from the old version into a different field in the new one if desired. " +
	"In this release, if you want to change the number of levels in a \"Reusable Choices\" dropdown field when updating a customization, " +
	"you should also change the field tag and/or label so that they are more easily distinguished from each other in searching and reporting.  " +
	"If you do not, the search/report results may be confusing since fields with the same label/tag will be treated differently " +
	"due to them having a different number of levels." +
	"\n\n" +
	"See examples below and if you need additional help with customization, " +
	"please email martus@benetech.org." +
	"\n\n",
"field:CreateCustomFieldsHelp2b= Notes on \"Reusable Choices\" dropdown fields:" +
	"\n\n " +
	"1. Dropdowns using a \"Reusable Choices\" list can have multiple levels (e.g. for locations that might have Region and City), " +
	"different fields can use one or more of the levels (e.g. if you have defined Region and City levels, " +
	"you could have a field that just uses the Region level, and another field that uses both levels), " +
	"and you do not need to define all levels for all entries " +
	"(e.g. you could have defined Neighborhood values as a lower level for some larger Cities, " +
	"but not all Cities need to have Neighborhoods defined).  " +
	"The number of levels is not limited by Martus, but please note that if you have large amounts of data in your definitions lists, " +
	"or a large number of levels, performance of certain Martus record operations may be affected." +
	"\n\n " +
	"2. In this release, we recommend that if you want to change the number of levels in a \"Reusable Choices\" dropdown field " +
	"when updating a customization, you also change the field tag and/or label so that they are more easily distinguished " +
	"from each other in searching and reporting.  If you do not, the search/report results may be confusing since fields " +
	"with the same label/tag will be treated differently due to them having a different number of levels." +
	"\n\n " +
	"3. \"Reusable Choices\" codes have the same restrictions that field tags do; they can be in any language, " +
	"but cannot contain spaces, special characters, or punctuation." +
	"\n\n " +
	"4. Please make sure to not use the same codes in Reusable Choices lists if you edit your customizations " +
	"unless you are just fixing typos in the labels, because using the same code for different labels can cause confusion " +
	"when searching or reporting on those fields. Ideally you should use codes that are not numeric, " +
	"but are letters that are a meaningful abbreviation of the label so there is no confusion over what they stand for " +
	"if you update the customization at a later date (e.g. use 2 or 3 letter abbreviations for locations instead of numbers).  " +
	"See the \"Search\" Help screen for more information about how code and label choices can affect searching on these fields." +
	"\n\n " +
	"5. You cannot use a multiple level / reusable choice dropdown as a data source for another dropdown inside or outside of grids. " +
	"If you try to do this you will see an error message." +
	"\n\n " +
	"6. Please note that when you save customization XML with a Reusable Choices list for dropdown fields, " +
	"Martus will move those choice definitions to the bottom of the XML when you reload it." +
	"\n\n",
"field:CreateCustomFieldsHelp3=\n" +
	"<Field type='SECTION'>\n" +
	"<Tag>summarysection</Tag>\n" +
	"<Label>Summary Section</Label>\n" +
	"</Field>\n" +
	"\n" +
	"<Field type='STRING'>\n" +
	"<Tag>office</Tag>\n" +
	"<Label>Regional office collecting the data</Label>\n" +
	"<DefaultValue>Region 3 field office</DefaultValue>\n" +
	"</Field>\n" +
	"\n" +
	"<Field type='DROPDOWN'>\n" +
	"<Tag>BulletinSource</Tag>\n" +
	"<Label>Source of record information</Label>\n" +
	"<RequiredField/>\n" +
	"<DefaultValue>Media/Press</DefaultValue>\n" +
	"<Choices>\n" +
	"<Choice>Media/Press</Choice>\n" +
	"<Choice>Legal Report</Choice>\n" +
	"<Choice>Personal Interview</Choice>\n" +
	"<Choice>Other</Choice>\n" +
	"</Choices>\n" +
	"</Field>\n\n<Field type='STRING'>\n" +
	"<Tag>SpecifyOther</Tag>\n" +
	"<Label>If Source = \"Other\", please specify:</Label>\n" +
	"</Field>\n\n<Field type='STRING'>\n" +
	"<Tag>IntervieweeName</Tag>\n" +
	"<Label>Interviewee Name</Label>\n" +
	"</Field>\n" +
	"\n" +
	"<Field type='LANGUAGE'>\n" +
	"<Tag>IntervieweeLanguage</Tag>\n" +
	"<Label>Interviewee Speaks</Label>\n" +
	"</Field>\n" +
	"\n" +
	"<Field type='DATERANGE'>\n" +
	"<Tag>InterviewDates</Tag>\n" +
	"<Label>Date(s) of interview(s)</Label>\n" +
	"</Field>\n" +
	"\n" +
	"<Field type='BOOLEAN'>\n" +
	"<Tag>Anonymous</Tag>\n" +
	"<Label>Does interviewee wish to remain anonymous?</Label>\n" +
	"</Field>\n\n<Field type='BOOLEAN'>\n" +
	"<Tag>AdditionalInfo</Tag>\n" +
	"<Label>Is interviewee willing to give additional information if needed?</Label>\n" +
	"<KeepWithPrevious/>\n" +
	"</Field>\n" +
	"\n" +
	"<Field type='BOOLEAN'>\n" +
	"<Tag>Testify</Tag>\n" +
	"<Label>Is interviewee willing to testify?</Label>\n" +
	"<KeepWithPrevious/>\n" +
	"</Field>\n" +
	"\n" +
	"<Field type='DROPDOWN'>\n" +
	"<Tag>EventLocation</Tag>\n" +
	"<Label>Event Location</Label>\n" +
	"<DefaultValue>R1</DefaultValue>\n" +
	"  <UseReusableChoices code='RegionChoices'></UseReusableChoices>\n" +
	"  <UseReusableChoices code='CityChoices'></UseReusableChoices>\n" +
	"</Field>" +
	"\n" +
	"\n" +
	"<Field type='SECTION'>\n" +
	"<Tag>peoplesection</Tag>\n" +
	"<Label>People Section</Label>\n" +
	"</Field>\n" +
	"\n" +
	"<Field type='GRID'>\n" +
	"<Tag>VictimInformationGrid</Tag>\n" +
	"<Label>Victim Information</Label>\n" +
	"<GridSpecDetails>\n" +
	"<Column type='STRING'><Tag></Tag><Label>First Name</Label></Column>\n" +
	"<Column type='STRING'><Tag></Tag><Label>Last Name</Label></Column>\n" +
	"<Column type='BOOLEAN'><Tag></Tag><Label>Is Identified?</Label></Column>\n" +
	"<Column type='DATE'><Tag></Tag><Label>Date of Birth</Label><MinimumDate>1910-01-01</MinimumDate><MaximumDate/></Column>\n" +
	"<Column type='DROPDOWN'><Tag></Tag><Label>Sex</Label><RequiredField/>\n" +
	"<Choices>\n<Choice>Male</Choice>\n" +
	"<Choice>Female</Choice>\n" +
	"<Choice>Unknown</Choice>\n" +
	"</Choices></Column>\n" +
	"<Column type='DROPDOWN'><Tag></Tag><Label>Region of Birth</Label>\n" +
	"  <UseReusableChoices code='RegionChoices'></UseReusableChoices></Column>\n" +
	"<Column type='STRING'><Tag></Tag><Label>Ethnicity</Label></Column>\n" +
	"</GridSpecDetails>\n</Field>\n\n<Field type='MESSAGE'>\n" +
	"<Tag>MessageProfession</Tag>\n" +
	"<Label>Profession History Table Note</Label>\n" +
	"<Message>If you have information about a person who has had different professions over time, " +
	"enter multiple rows with the same First and Last Names and show the date ranges " +
	"for each profession on a separate row.</Message>\n" +
	"</Field>\n" +
	"\n" +
	"<Field type='GRID'>\n" +
	"<Tag>ProfessionHistoryGrid</Tag>\n" +
	"<Label>Profession History</Label>\n" +
	"<GridSpecDetails>\n" +
	"<Column type='DROPDOWN'><Tag></Tag><Label>First Name</Label>\n" +
	" <DataSource>\n" +
	" <GridFieldTag>VictimInformationGrid</GridFieldTag>\n" +
	" <GridColumnLabel>First Name</GridColumnLabel>\n" +
	" </DataSource>\n" +
	"</Column>\n" +
	"<Column type='DROPDOWN'><Tag></Tag><Label>Last Name</Label>\n" +
	" <DataSource>\n <GridFieldTag>VictimInformationGrid</GridFieldTag>\n" +
	" <GridColumnLabel>Last Name</GridColumnLabel>\n" +
	" </DataSource>\n" +
	"</Column>\n" +
	"<Column type='STRING'><Tag></Tag><Label>Profession</Label></Column>\n" +
	"<Column type='DATERANGE'><Tag></Tag><Label>Dates of Profession</Label><MaximumDate/></Column>\n" +
	"</GridSpecDetails>\n" +
	"</Field>\n" +
	"\n" +
	"<Field type='MULTILINE'>\n" +
	"<Tag>narrative</Tag>\n" + 
	"<Label>Narrative description of events</Label>\n" + 
	"<DefaultValue>What happened in detail is as follows:</DefaultValue>\n" + 
	"</Field>\n" +
	"\n" +
	"\n" +
	"<ReusableChoices code='RegionChoices' label='Region'>\n" +
	"  <Choice code='R1' label='Region 1'></Choice>\n" +
	"  <Choice code='R2' label='Region 2'></Choice>\n" +
	"  <Choice code='R3' label='Region 3'></Choice>\n" +
	"</ReusableChoices>\n" +
	"\n" +
	"<ReusableChoices code='CityChoices' label='City'>\n" +
	"  <Choice code='R1.C1' label='City 1'></Choice>\n" +
	"  <Choice code='R1.C2' label='City 2'></Choice>\n" +
	"  <Choice code='R2.C3' label='City 3'></Choice>\n" +
	"  <Choice code='R2.C4' label='City 4'></Choice>\n" +
	"  <Choice code='R3.C5' label='City 5'></Choice>\n" +
	"  <Choice code='R3.C6' label='City 6'></Choice>\n" +
	"</ReusableChoices>",

"field:inputservermagicwordentry=If you want to request permission to upload to this server, enter the 'magic word' now:",
"field:inputGetShareFileNameentry=Enter a name for the exported backup file(s).",
"field:InputCustomFieldsTitle=Title",
"field:inputCustomFieldsDescription=Description",
"field:warningDeleteSingleBulletin=You have chosen to permanently delete a record from the Discarded Records folder. Even if this record was recently cut or copied, you will not be able to paste it. If this record has already been sent to a server, it will remain on the server. This action will only delete it from this computer.",
"field:warningDeleteMultipleBulletins=You have chosen to permanently delete records from the Discarded Records folder. Even if these records were recently cut or copied, you will not be able to paste them. If they have already been sent to a server, they will remain on the server. This action will only delete them from this computer.",
"field:warningDeleteSingleUnsentBulletin=Warning: This record has not been sent to a server since it was last modified. Deleting it will prevent the latest changes from being backed up.",
"field:warningDeleteMultipleUnsentBulletins=Warning: One or more of these records have not been sent to a server since they were last modified. Deleting them will prevent the latest changes from being backed up.",
"field:warningDeleteSingleBulletinWithCopies=Note: Copies of this record exist in other folders. Those copies will not be removed.",
"field:warningDeleteMultipleBulletinsWithCopies=Note: Copies of these records exist in one or more other folders. Those copies will not be removed.",

"wintitle:IncompatibleJavaVersion=Incompatible Java Version",
"field:IncompatibleJavaVersion=The current version of Martus cannot run with Java #HighVersion#. Please use Java #ExpectedVersion# or lower to run Martus.",

"field:username=Username ",
"field:email=Email Address ",
"field:webpage=Web Page ",
"field:phone=Phone Number ",
"field:address=Mailing Address ",
"field:password=Password ",
"field:securityServerConfigValidate=For security reasons, we must validate your username and password.",
"field:RetypeUserNameAndPassword=Please retype your username and password for verification.",
"field:CreateNewUserNamePassword=Please enter your new username and password.",
"field:HelpOnCreatingNewPassword=When choosing a password, it is important not to use a password that is easy to guess like names, important dates of events or simple words.  Try adding numbers to random letters and making the password long.  Remember your password, but do not share it.  No one else has access to the password if you forget it, so if you write it down, put it in a safe place.",
"field:timedout1=Martus is still running in the background, but has locked the screen for security.  You must sign in to Martus again to continue working.",
"field:timedout2=Any unsaved records will be lost unless you sign in to Martus again and save them.",
"field:defaultFolderName=New Folder",
"field:SearchBulletinRules=For each row, select a field to search (or choose to match any field), select what kind of comparison to perform, and then enter a value to search for.\n\nNote: Records that appear only in the Trash will not be searched.",
"field:SearchBulletinAddingRules=Press Enter to create a new row in the search query.",
"field:SearchBulletinHelp=Click '#SearchHelpButton#' to get additional information on searching.",
"field:AccountInfoUserName=Username ",
"field:AccountInfoPublicKey=Public Account ID ",
"field:AccountInfoPublicCode=Public Code (Old) ",
"field:AccountInfoPublicCode40=Public Code ",
"field:AccountInfoDirectory=Account Directory: ",
"field:AccountAccessToken=Access Token ",
"field:DefaultServerHeading=Default Server",
"field:AdvanceServerSetupHeading=Advanced Server Setup",
"field:NoContactsInTable=No Contacts.",
"field:NoBulletinsInTable=No Records.",
"button:ChooseDefaultServer=Use Default Server",
"field:ServerNameEntry=IP Address ",
"field:ServerNameEntryInformation=enter your server's IP address",
"field:ServerPublicCodeEntry=Server Public Code ",
"field:ServerPublicCodeEntryInformation=enter your server's public code",
"field:PublicCode=Public Code ",
"field:ServerMagicWordEntry=Magic Word ",
"field:ServerMagicWordEntryInformation=enter your server's magic word",
"field:ServerSelectionResults=The following server has been selected:",
"field:ServerAcceptsUploads=You will be allowed to upload records to this server.",
"field:ServerDeclinesUploads=You will not be allowed to upload records to this server.",
"field:ExportAttachments=Include attachments in export",
"field:ExportAllVersions=Include all versions of each record in export",
"field:ExportBulletinDetails=The selected records will be exported to an XML file that you specify. All information will be saved as plain text and anyone who gets a copy of the file will be able to read its contents. These files can be used to import record data back into Martus or into another application.\n\n Please email martus@benetech.org if you need assistance.",
"field:HowToCreateNewAccount=One or more accounts already exist on this computer. To create an additional account with a new username and passphrase, click OK.",
"field:HowToCreateInitialAccount=No accounts exist on this computer.  To create a new account click OK.  Otherwise you can click on the Restore Account tab to restore an account which was previously backed up.",
"field:UntitledBulletin=Untitled Record",
"field:GetShareFileNameDescription=This file name should be unique and identifiable to you but we recommend not using your username.  Each file will be generated with this name and its number sequence.",
"field:UnknownFieldType=Warning: This field requires a newer release of Martus to be viewed",
"field:BackupSecretShareCompleteInformation=Backup complete, please give each disk to someone you trust, so that if you forget your username and/or password in the future you can recreate your account.  You will need #MinimumNumberOfFilesNeededForRecovery# of these disks to recreate your account.",
"field:BackupRecoverKeyPairInsertNextDiskMessage=Please insert disk #",
"field:RemoveServerLabel1=The following server will be removed:",
"field:RemoveServerLabel2=Are you sure you want to do this?",
"wintitle:FileDialogSaveReportFormat=Save Report Format",
"wintitle:FileDialogPrintToFile=Print to File",
"field:ExportedBulletins=Exported Records",
"field:confirmSearchProgressCancelcause=The search in progress will be stopped immediately.",
"field:confirmSearchProgressCanceleffect=If you do this, the Search Results will only contain records that have been found so far.",
"wintitle:confirmSearchProgressCancel=Cancel Search",
"field:ErrorCustomFields=There is an error in the custom field definitions. " +
	"Each problem is identified with the following codes:\n" +
	"   100 - Required standard field is missing\n" +
	"   101 - Custom field does not have a correct tag\n" +
	"   102 - A previous Tag already exists with this name\n" +
	"   103 - Label cannot be blank\n" +
	"   104 - Unrecognized Field Type\n" +
	"   105 - A label was found in a standard field\n" +
	"   106 - XML Parse Error (e.g. mismatched quotation marks or bracket syntax)\n" +
	"   107 - Tag or Code contains illegal characters (e.g. spaces or punctuation)\n" +
	"   108 - Duplicate entry in Dropdown field\n" +
	"   109 - No Choices in Dropdown field\n" +
	"   110 - System/Standard field tag cannot be used for custom field\n" +
	"   111 - Standard field in top pane cannot be placed in bottom (always Private) pane\n" +
	"   112 - Standard field in bottom (always Private) pane cannot be placed in the top pane\n" +
	"   113 - Data Source Grid not found in same pane as resulting dropdown field\n" +
	"   114 - Unknown Data Source Grid Column Label\n" +
	"   115 - Dropdown cannot have both choices and data source\n" +
	"   116 - Invalid date must be in ISO format YYYY-MM-DD\n" +
	"   117 - No Reusable Choices list defined for Dropdown\n" +
	"   118 - Reusable dropdown definition missing code value.  Please verify that the definition looks like the following:\n" +
	"         <UseReusableChoices code='FieldChoices'></UseReusableChoices>\n" +
	"   119 - Reusable choice must have a code and label\n" +
	"   120 - Dropdown data source cannot be a single or multiple level dropdown with Reusable Choices\n" +
	"   121 - Default value is not valid for this field\n" +
	"   122 - Reusable Choices lists cannot have the same Label\n" +
	"   123 - Lower-level reusable choice code has no matching higher-level code\n" +
	"   200 - All fields empty\n" +
	"   201 - Unrecognized Contact created this template\n" +
	"   202 - Security validation error\n" +
	"   203 - File Error\n" +
	"   204 - Imported XML Missing Field",
"field:ErrorCustomFieldHeader1=CODE",
"field:ErrorCustomFieldHeader2=TYPE",
"field:ErrorCustomFieldHeader3=TAG",
"field:ErrorCustomFieldHeader4=LABEL",
"field:SplashProductDescription=Information Management and Data Collection Framework",
"field:DateExact=Exact Date",
"field:DateRange=Date Range",
"field:YearUnspecified=Unknown",
"field:ColumnGridRowNumber=Row #",
"field:SetFolderOrder=To change a folder's position, select the folder name and use the #MoveFolderUp# / #MoveFolderDown# buttons.",
"field:FancySearchHelpMsg1=---Overview---\n\n" +
	"Martus searches all versions of every record (both public and private, sealed and draft) in every folder, including the Discarded Records folder.  " +
		"When the search is completed, the Search Results folder lists copies of the records found in your search.\n\n" +
	"You can search for words in any language.  " +
		"Martus searches are not case-sensitive in English and other purely Latin character languages, " +
		"so it doesn't matter whether a word is capitalized or not.\n\n" +
	"Searches include attachment filenames, but not content of attachments.\n\n" +
	"You can search both standard and custom fields.\n\n",
"field:FancySearchHelpMsg2=---Search tips---\n\n" +
	"Martus will find any text you enter whether it's a complete word or part of a larger word. " +
		"For example, if you search for the word prison, you'll see records that include the words prison, imprison, and imprisonment. " +
		"Likewise, if you search for the word prison, " +
		"Martus will find records that include attachments with names such as photos-prison.jpg and prisoners-report.doc.\n\n" + 
	"To search for an exact phrase, type it with quotation marks around the phrase  (e.g.  \"Witness Testimony\"). " +
		"If you do not put quotation marks around the phrase, Martus will search for the words individually.\n\n" + 
	"Use the word \"#Or#\" to broaden your search, or the word \"#And#\" to narrow it.  " +
		"You can use the #And# / #Or# dropdowns to specify different fields you want search across " +
		"(e.g. you want to search for records that have \"Last Saved Date\" in the last week and have a certain author).  " +
		"If you want to search on multiple text values within a single record field, " +
		"you can use #Or# or #And# in-between words in the \"Search For...\" entry box.  " +
		"For example, if you search \"Any Field\" for:\n" +
		"   prison #Or# jail\n" +
		"you'll see a list of records that contain either word anywhere in the record. " + 
		"If you search for:\n" +
		"   prison #And# assault \n" +
		"you'll see a list of records that contain both words. " +
		"The keyword \"#And#\" is implied, so if you search for:\n" +
		"   prison assault \n" +
		"Martus will find the same records as if you searched for:\n" +
		"   prison #And# assault\n\n" + 
	"When you use both \"#And#\" and \"#Or#\", your search terms are grouped from the beginning of your list " +
		"(either across record fields or within a particular field). " +
		"For example, if you enter:\n" +
		"   prison #Or# jail #And# trial \n" +
		"in the \"Search For...\" entry box, " +
		"Martus will search for any records that contain either of the words prison or jail, and also contain the word trial. " +   
		"But if you enter:\n" +
		"   prison #And# jail #Or# trial \n" +
		"Martus will search for any records that contain both the words prison and jail, or contain the word trial.\n",
"field:FancySearchHelpMsg3=" + 
	"You can use the word #Or# or #And#, or you can use the English words \"#OrEnglish#\" and \"#AndEnglish#\" to search.\n\n" +  
	"You need to put spaces before and after any #Or# / #And# keywords you use in your search.\n",
"field:FancySearchHelpMsg4="
+ "If you want all of your multi-item list search criteria to be met in a single list item, "
+ "please check the \"Match multi-item list details\" checkbox. For example, "
+ "if you want to search for a specific victim name in a single item in your records created after a certain date, "
+ "select the checkbox and enter the following fields in the Search screen: \"Victim Information: First Name\" = x and "
+ "\"Victim Information: Last Name\" = y and \"Date Created\" >= YYYY-Mon-DD. "
+ "If you do not select the \"Match multi-item list details\" checkbox, "
+ "Martus will find records created after your specified date where any item has the first name you specified and any other item has the last name specified, "
+ "but not necessarily in the same item (you could have an item with \"First Name\" = x and \"Last Name\" = b, "
+ "and a different item with \"First Name\" = a and \"Last Name\" = y, "
+ "and Martus will find that record as matching the search because you did not specify that it had to match in a single item)\n\n",
"field:FancySearchHelpMsg5= Additional Search Notes:\n\n"
+ " 1. Because Martus searches all details of any multi-item list for your criteria, "
+ "it may find records where one item detail matches your criteria but other details do not. "
+ "For example you could have a record with an item that has a location field in it and you have "
+ "multiple details in the item with locations A, B, and C. "
+ "If you search for records where location != C (does not equal C), "
+ "Martus will find that record because there are 2 details in the record item where the location is not C, "
+ "even though there is one item where the locations IS C."
+ "\n\n"
+ " 2. In this release, if you are searching on a multi-level dropdown, "
+ "all searches are exact matches, not partial or \"starts with\" matches. "
+ "This means that you have to pick the exact level at which you want to be searching. "
+ "For example, an \"Event Location\" field that has three levels (Region/City/Neighborhood) "
+ "will have three entries in the search field list: Event Location: Region, "
+ "Event Location: City, and Event Location: Neighborhood. "
+ "So if you want to find any records that have an Event Location anywhere in Region X "
+ "(regardless of the City), you have to pick the \"Event Location: Region\" "
+ "field to search on and pick Region X off the dropdown list choices. "
+ "If you pick Event Location: City to search on and then pick Region X but leave the City level blank, "
+ "Martus will only find entries where there was no City data entered (City was blank), "
+ "as opposed to ANY location with Region X regardless of what data was entered at the City level."
+ "\n\n"
+ " 3. If you do not see your search terms/dates in the final version of the record displayed in the Search Results folder, "
+ "your criteria may have been matched in an earlier version of the record. "
+ "You can access previous versions by clicking the \"Record Details\" button at the bottom of the record. "
+ "To search only the most recent versions of records, select \"Only search most recent version of records\" in the Search dialog box."
+ "\n\n"
+ " 4. If you have fields in different records or from different customizations that are exactly the same, "
+ "Martus will combine them in any Search and Report field lists. "
+ "And while Martus warns you about duplicate labels when you are creating a new record customization, "
+ "it is possible that over time, you may have records with different customizations that ended up with the same labels "
+ "(e.g. maybe you changed a text field to a dropdown field but kept the same label). "
+ "In these cases, Martus will display both fields in the search screen, "
+ "and try to help you figure out the difference between the fields by displaying what the field type and tag are in the field selection lists. "
+ "Also, if you have fields with the same tag but different labels and/or field types, "
+ "Martus may use the tag and field type to try and determine when different fields were meant to be the same. "
+ "So we encourage you to make your field tags and labels in a customization clearly related to each other to avoid any confusion."
+ "\n\n"
+ " 5. You may sometimes see duplicate entries in dropdown list search criteria values in the search screen. "
+ "If you pick a Reusable Choices dropdown field to search on, "
+ "the values that are displayed as the criteria dropdown list options are the labels for each list entry, "
+ "but the codes you defined determine how many entries there will be in the search dropdown list. "
+ "So if you have different Reusable Choices codes with the same label in different record customizations in your account, "
+ "the labels will show up twice in search dropdown lists "
+ "(i.e. if you used label1 for both code1 and code2, "
+ "you will see 2 entries in the search dropdown that looks like \"label1\" and your search will be on records that have that label, "
+ "regardless of which code the customization had for the label). "
+ "And if you have the same Reusable Choices code with different labels in different record customizations in your account, "
+ "the search dropdown list for that code will show both values separated by a semicolon "
+ "(i.e. if you used code1 for both label1 and label2, you will see an entry in the search dropdown that looks like \"label1; label2\" "
+ "and your search will be on records that have either of those labels)."
+ "\n\n"
+ "For additional help with searching, see the documentation at martus.org or email martus@benetech.org.",
"field:SearchProgress=Progress: ",
"field:ReportSearchProgress=Progress: ",
"field:SearchFound=#NumberBulletinsFound# records matched the search, and have been added to the Search Results folder.",
"field:ReportChooseSortFields=Choose how the records will be sorted in the report. The records will be ordered by the first field chosen. When two records have the same value in that field, they will be sorted by the next sort field, and so on.",
"field:ReportDetailOnly=Print Record Information",
"field:ReportDetailWithSummaries=Print Record Information and Summary Counts",
"field:ReportSummariesOnly=Print Summary Counts Only",
"field:ChooseReportFields=Use Ctrl-Click or Shift-Click to select the fields (columns) that will appear in this report. ",
"field:MartusReportFormatFileFilter=Martus Report Format (.mrf)",
"field:MartusSearchSpecFileFilter=Martus Search Specification (.mss)",
"field:MBAFileFilter=Martus Record Archive (*.mba)",
"field:JPEGFileFilter=JPEG (*.jpeg, *.jpg)",
"field:KeyPairFileFilter=Martus Key (*.dat)",
"field:HtmlFileFilter=HTML (*.html, *.htm)",
"field:CustomXMLTopSection=Top Pane of Record",
"field:CustomXMLBottomSection=Bottom (always Private) Pane of Record",
"field:WasSentYes=Yes",
"field:WasSentNo=No",
"field:NotSorted=(none)",
"field:ReportNumberOfBulletins=Total Records:",

"wintitle:RemoveContact=Remove Contact",
"field:RemoveContactLabel=The following contact will be removed.\n\nName: #Name#\nPublic Code: #PublicCode#.\n\nAre you sure you want to do this?",


"menu:ManageContacts=Manage Contacts",
"field:ManageContacts=Manage Contacts",

"field:AllFiles=All Files",
"field:RemoveMartusFromSystemWarning=Warning!",
"field:RemoveMartusFromSystemMultipleAccountsWarning1=IMPORTANT: There are other Martus accounts on this system.",
"field:RemoveMartusFromSystemMultipleAccountsWarning2=All of their data will be removed as well!",
"field:QuickEraseFollowingItems=Clicking on Ok will do the following on this computer:",
"field:QuickEraseWillNotRemoveItems=Any records on the server, Martus files copied outside of the Martus directory, and any of your records or Martus files on another computer will NOT be removed.",
"field:RemoveMartusWillUninstall=Uninstall the Martus program.",
"field:RemoveMartusWillRemoveAllOtherAccounts=Delete all other Martus accounts, including their key, folders, and records.",
"field:RemoveMartusWillDeleteMartusDirectory=Delete the Martus directory and all of its contents.",
"field:QuickEraseWillExitMartus=Exit Martus when complete.",

"field:BulletinDetailsAuthorPublicCode=Author Public Code",
"field:BulletinDetailsBulletinId=Record ID",
"field:BulletinDetailsVersionNumber=Version #",
"field:BulletinDetailsVersionDate=Date Saved",
"field:BulletinDetailsVersionId=ID",
"field:BulletinDetailsVersionTitle=Title",
"field:BulletinDetailsHistory=History",
"field:BulletinDetailsExtendedHistory=Extended History",
"field:PreviousAuthor=Previous Author: #AUTHOR#",
"field:PreviousBulletinId=Record ID: #ID#",
"field:BulletinDetailsUnknownDate=(unknown)",
"field:BulletinDetailsUnknownTitle=(unknown)",
"field:Unknown=(unknown)",
"field:BulletinDetailsInProgressDate=(in progress)",
"field:BulletinDetailsInProgressTitle=(in progress)",
"field:ImportBulletinsIntoWhichFolder=Import records into which Folder?",
"field:ImportProgress=Importing",
"field:ExportProgress=Exporting",
"field:ImportExportBulletinTitle=Record",
"field:DataIsHidden=(Press the + button to show the hidden information)",
"field:LoadingFieldValuesFromAllBulletins=Processing record",
"field:LoadingFieldValuesFromAllBulletinsExplanation=Scanning all records to create a list of all the values in #FieldName#",

"field:_Section_BulletinSectionHeader=Header",
"field:_SectionTopSection= ",
"field:_SectionBottomSection= ",

"field:mdyOrder=Date Format",
"field:CalendarSystem=Calendar type",
"field:DatePartYear=Year",
"field:DatePartMonth=Month",
"field:DatePartDay=Day",
"field:DateDelimiterSlash=Slash (00/00/00)",
"field:DateDelimiterDash=Dash (00-00-00)",
"field:DateDelimiterDot=Dot (00.00.00)",
"field:CalendarSystemGregorian=Default (2005-05-31)",
"field:CalendarSystemThai=Thai Solar (2548-05-31)",
"field:CalendarSystemPersian=Persian (1384-03-10)",
"field:CalendarSystemAfghan=Afghan (1384-03-10)",

"field:FieldTypeSTRING=Text",
"field:FieldTypeBOOLEAN=Yes/No",
"field:FieldTypeDATE=Date",
"field:FieldTypeDATERANGE=Date Range",
"field:FieldTypeDROPDOWN=Dropdown List",
"field:FieldTypeLANGUAGE=Language",
"field:FieldTypeMULTILINE=Text with Scrollbar",
"field:FieldTypeMESSAGE=Message",
"field:FieldTypeGRID=Grid/Table",

"field:BackgroundPrinting=Preparing Report...",
"field:BackgroundWorking=Working...",
"field:RecordTitle=Title ",
"field:To=To ",
"field:From=From ",
"field:VersionNumber=Version # ",

"wintitle:FileDialogImportContactPublicKey=Import Contact Key",
"button:FileDialogOkImportContactPublicKey=Import",

"wintitle:FileDialogSaveAttachment=Save Attachment",

"wintitle:notifyAddPermissionsZeroBulletinsOurs=Update Contacts' Access to Records",
"wintitle:notifyAddPermissionsZeroHeadquartersSelected=No Contacts Selected",
"wintitle:RetrieveHQSealedBulletinProgress=Retrieving Records",
"wintitle:RetrieveHQDraftBulletinProgress=Retrieving Records",
"wintitle:RetrieveHQSealedBulletinSummaries=Retrieving Record Summaries",
"wintitle:RetrieveHQDraftBulletinSummaries=Retrieving Record Summaries",
"wintitle:AddPermissions=Update your contacts' access to Records",
"wintitle:AddingPermissionsToBulletins=Update your Contacts' access to Records",
"wintitle:Attachments=Attachments",
"wintitle:Details=Details",
"wintitle:Contacts=Contacts",

"menu:AddPermissions=Update Contact's Access",
"button:AddPermissions=Update Contact's Access",
"button:Contacts=Contacts",
"button:Create=Create",

"field:confirmWarningSwitchToNormalKeyboardeffect=If this record was shared with you by one of your contacts, it is especially important to use the on-screen keyboard, because an attacker could gain access to all the private data that you are authorized to view.",
"field:confirmCloneBulletinAsMineeffect=Clicking on Yes will create a new record that contains a copy of all the same information. You will be the official author of this new record, and any private data in it will only be visible by you (and your Contact's accounts that you have shared the record with). The original record will remain unchanged.",
"field:notifyAddPermissionsZeroHeadquartersSelectedcause=You must select at least one contact's account",
"field:ConfigureHQColumnHeaderPublicCode=Public Code",
"field:BulletinHeadQuartersHQLabel=Label",
"field:QuickEraseWillRemoveItems=Delete your Martus key, records, folders, and configuration information (such as your contacts, server, and custom field settings).",
"field:_Section_BulletinSectionHeadquarters=Contacts",
"field:HeadQuartersSelected=Selected?",
"field:ConfigureHeadQuartersDefault=Default?",
"field:AddPermissionsOverview=This allows you to update your contacts' access to existing records. " +
"Each of the records in the list below will be modified to add the selected contacts' accounts. " +
"If a record is sealed, a new version will be created.",
"field:ChooseHeadquartersToAdd=Select the contacts listed below that you want to add to all the records listed above.",
"field:TemplateCreatedByHeadquarters=Contacts #Name#",
"field:PublicCodeWithColon=Public Code ",

"wintitle:ViewAttachment=View Attachment",
"wintitle:confirmCloneUnverifiedFDBulletinAsMine=Confirm Create Copy of Record",

"folder:%RetrievedFieldOfficeBulletinDraft=Contacts' Draft Records",
"field:confirmCloneUnverifiedFDBulletinAsMinecause=You have chosen to modify a record that was created by a Martus user that has not been verified, so the record could contain incorrect information or potentially damaging attachments. We recommend that you verify this account before continuing (See Tools > Manage Contacts).",
"field:confirmCloneUnverifiedFDBulletinAsMineeffect=Clicking on Yes to continue with the modify operation will create a new record that contains a copy of all the same information.  You will be the official author of this new record, so you will become responsible for all the content. Any private data in it will only be visible to you (and any contacts accounts you have shared this with).  The original record will remain unchanged.",

"wintitle:notifyBulletinWithAnUnverifiedExternalAttachment=Unverified Attachment",
"field:notifyBulletinWithAnUnverifiedExternalAttachmentcause=Warning: This record includes attachments that need to be read outside of the Martus system. In addition, this file was sent to you from an [unknown/unverified] contact. Opening unknown attachments is one of the most common ways of introducing malware to your computer. Be sure of the safety of the attachment(s) before opening or sharing with others.",
"field:TemplateCreatedByFieldDesk=Contact #Name#",
"field:UnverifiedFDAttachment=The Martus account that created this record has not been verified, so there is an increased chance that this attachment could be dangerous. We recommend that you verify this Contact's account before you #action# this attachment (See Tools > Manage Contacts). Opening this attachment could put your computer at risk. ",
"field:statusCheckingForNewFieldOfficeBulletins=Checking for new records from Contacts...",
"field:statusNewFieldOfficeBulletins=New records found on server",
"field:SyncFrequencyMinutes=minutes",
"field:SyncFrequencyOnStartup=on log-in",
"field:SyncFrequencyOnceAnHour=once an hour",

"field:SyncFrequencyOneMinute=Every minute",
"field:SyncFrequencyTwoMinutes=Every 2 minutes",
"field:SyncFrequencyFiveMinutes=Every 5 minutes",
"field:SyncFrequencyTenMinutes=Every 10 minutes",
"field:SyncFrequencyFifteenMinutes=Every 15 minutes",
"field:SyncFrequencyThirtyMinutes=Every 30 minutes",
"field:SyncFrequencyFortyFiveMinutes=Every 45 minutes",

"field:AddContacts=Contacts",
"field:ManageContactsOverview1=To send information through Martus to your contacts, you can pull their public key off of the Martus server using their access token. You can verify that you have pulled the correct key, by checking their key's public code. Ask your contacts for their access token and public code to add your contacts here.",
"field:ManageContactsOverview2=Be sure to securely communicate access tokens and public codes using a method such as encrypted email, Off-the-record (OTR) chat, face to face, or another communication channel where nobody can intercept your information.",
"field:AddContactInstructions=To send information through Martus to your contacts, pull their public key from the Martus server using their access token. Verify that you have pulled the correct key, by checking their key's public code. Ask your contacts for their access token and public code to add them here. If you do not have this information now, you will be able to add contacts later.",

"button:ContactName=Contact Name",
"button:SendToByDefault=Send To by Default",
"button:PublicCode=Public Code",
"button:ContactVerified=Verified",
"button:ContactRemove=Remove",
"field:AccessToken=Access Token ",
"field:AccessTokenInstructions=Access Token",
"button:AddContact=Add Contact",
"button:ImportContactFromFile=Import Contact From File",
"button:ContactsSaveClose=Save and Close",
"button:CopyItem=Copy",
"wintitle:CopyItem=Copy",
"button:YesCopyWithUnknownContent=Yes, copy what you can over",

"wintitle:ExitWizard=Exit Martus Wizard",
"field:ExitWizard=If you close this wizard now, you will not be able to return and use it to complete this account's setup \n(however, you can access the wizard to create other new accounts).\n\n" +
"Are you sure you want to close the wizard?",

"field:Step1=1",
"field:Step2=2",
"field:Step3=3",
"field:Step4=4",
"field:Step5=5",
"field:Step6=6",

"field:WelcomeMessage1=Get started with Martus!",
"field:WelcomeMessage2=Securing your digital documentation",
"field:WelcomeMessage3=This wizard will walk you through the steps necessary to set up Martus, so that you can safely store and share your human rights documentation.",

"field:StepArrow=➔",
"field:WizardStep1=Create Account",
"field:WizardStep1Heading=Account Creation",
"field:WizardStep1Information=Your information will be kept safe, with a password protected, encrypted account.",
"field:ServerSettings=Settings",
"field:WizardStep2Information=Establish your calendar and Tor (a tool to help hide network activity) settings.",
"field:WizardStep3=Server Setup",
"field:WizardStep3Information=Backup your information to a secure server.",
"field:WizardStep4=Contacts",
"field:WizardStep4Information=Send and receive information to/from other Martus users of your choice.",
"field:WizardStep5=Import Forms",
"field:WizardStep5Heading=Import Form Template",
"field:WizardStep5Information=Select a template for creating Martus forms.",
"field:KeyBackup=Key Backup",
"field:WizardStep6Heading=Backup key",
"field:WizardStep6Information=Make a copy of your account's encryption key to ensure continued access to your account.",
"button:GetStarted=Get Started",
"button:GoBack=Go Back",

"field:CreateAccount=Create an Account",
"field:CreateAccountInstructions=When creating a username and password for your account, please be aware there is no way to retrieve your username and password if you forget it. This is for your safety.",
"field:CreateAccountUserName=Username ",
"field:AccountUserNameInstructions=create a username",
"field:Password=Password ",
"field:PasswordInstructions=create a password",
"field:BackupAccountInfo=Note: Later in this wizard, you will be led through a process to backup your account key. This will help protect your account from loss.",
"field:UserNameTips=Username Tips:",
"field:UserNameTip1=- Your username can contain letters, numbers, punctuation and spaces.",
"field:UserNameTip2=- It should have between 8-50 characters, and it is case sensitive.",
"field:UserNameTip3=- Remember to choose a username you can remember.",
"field:PasswordTips=Password Tips:",
"field:PasswordTipGeneral=Create secure passwords by using numbers, letters and symbols.",
"field:PasswordTip1=- Do not use your username",
"field:PasswordTip2=- Do not use a single dictionary word",
"field:PasswordTip3=- Use at least 15 characters for most security (8 are required)",
"field:PasswordTip4=- Use a combination of alphabet characters, numbers, and special characters (such as !@#$%^*&amp;)",
"field:PasswordTip5=- Use a combination of uppercase (capital) and lowercase characters if you use a case-sensitive language.",

"field:ConfirmAccountUserName=Confirm Username",
"field:ConfirmPassword=Confirm Password",
"field:UserNameAndPasswordMatches=Username and password match!",

"field:VerifyContactPublicCode=Verify your contact public code",
"field:VerifyContactPublicCodeNew=Public code ",
"field:VerifyContactPublicCodeOld=Public code (for Martus 4.4 and below) ",
"button:VerifyContactLater=Verify Later",
"button:VerifyContactNow=Verify Now",

"field:SettingUpTor=Setting Up Tor.",
"field:DisplaySettings=Your Display Settings",
"field:TorSettings=Your Tor Settings",
"field:UseTor=Use Tor",
"field:UseTorInstructions=Tor is a security tool you can use to hide your identity when connecting to sites on the Internet. If Martus servers are blocked from your location, using Tor may help reach the servers.",
"field:TorTip1=Turning Tor on here establishes the default setting for your use of Tor when in Martus. You can also, turn Tor ON or OFF at any time inside the Martus desktop application.",
"field:TorTip2=Note: Using Tor will make sending and receiving of records slower.",
"field:TorTip1Settings=Turn Tor ON or OFF at any time using the button in the task bar at the top of the main Martus screen.",
"field:NeverDeleteVersionOnServer=Never Delete from Server",

"field:GettingServerInformation=Getting server information",
"field:AdvanceServerSetup=Advanced Server Setup",
"field:AdvanceServerInformation=To store your records on a specific server, you will need the IP address, public code, and magic word for the server.",
"field:AdvanceMartusServerMailTo=If you are connecting to a Martus server, email martus@benetech.org for this info.",
"field:AdvanceOrganizationServer=If you are connecting to your organization's server, contact your administrator.",
"field:ConnectToAdvanceServer=Connect",

"field:ServerSetup=Server Setup",
"field:ConnectMartusServerReasons1=Connect to the Martus server to:",
"field:ConnectMartusServerReasons2=- Backup your documents on the Martus server in case your computer is lost or stolen",
"field:ConnectMartusServerReasons3=- Easily retrieve the public keys of your contacts, so you can send them records",
"field:ConnectMartusServerReasons4=- Import form templates from your contacts",
"field:SetupServerLater=Set up server later",
"field:UseDefaultServer=Use default server",
"field:SetupAdvanceServer=Advanced server settings",
"field:SetupServerTips=The data you store on Martus servers are completely encrypted using 3072-bit encryption. This means even Martus server administrators cannot read your data.",
"menu:SystemPreferences=System Preferences",
"menu:TorPreferences=Tor Preferences",

"menu:AccountInformation=Account Information",
"field:AccountInformation=Account Information",

"wintitle:AuthorInformation=Author Information",
"menu:AuthorInformation=Author Information",
"field:AuthorInformation=Author Information",
"field:AuthorContactInformation=This information is optional and is used to auto-fill your records for your convenience. It is stored on your local computer and not sent to the Martus server.",


"menu:DeleteAccount=Delete Account",
"menu:ServerSettings=Server Settings",
"menu:WizardStep6Heading=Backup key",
"menu:Settings=Settings",
"menu:KeyBackup=Key Backup",


"field:HelpForTokenAndPublicCode=If someone wishes to send you information, provide them with your access token and public code.",
"field:WarningOnTokenAndPublicCodeSecurity=Be sure to securely communicate access tokens and public codes using a method such as encrypted email, Off-the-record (OTR) chat, face to face, or another communication channel where nobody can intercept your information.",
"field:LinkToTokenAndPublicCode=Get your Token and Code",

"field:NoFormTemplateTitle=[no title]",
"button:GenericForms=Generic Template",
"button:CustomForms=Download Custom Template",
"field:FormsHint=You can switch forms later",
"field:ImportFormTemplates=Import Form Templates",
"field:ImportFormTemplateHelp=Martus allows you to create and share custom forms for data entry. Get started by selecting a form template if desired. Martus comes with several generic form templates you can use. You can also download a custom template from one of your current contacts or another Martus user.",
"field:DownloadFromContacts=Download from my contacts",
"field:ChooseContact=Choose a Contact",
"field:ChooseForm=Choose a Form",
"field:NoTemplatesAvailable=No Templates Available",
"field:FormTemplatesTip1=You can load and switch to other form templates later inside of Martus, if you do not have access to a server now, or you need one that is different from what you select here.",
"field:FormTemplatesTip2=Note: you can change templates, but you will not be able to import templates from the server, outside of the account creation process at this time.",
"field:DownloadFormFromAnotherUser=Download from Another Martus User",
"field:EnterAccessToken=Enter Access Token",
"button:SeeForms=See Forms",
"field:SuccessfullyImportedForm=Successfully imported the #templateName Form",

"field:KeypairTooLarge=keypair file too large!",
"field:BackupYourKey=Backup Your Key",
"field:BackupYourKeyInformation=Back up your encryption key to be able to restore your account.",
"button:CreateSingleEncryptedFile=Export Password Protected Key",
"field:CreateSingleEncryptedFileInformation=Note: This requires your username and password to restore your account.",
"field:BackupYourKeyTip1=Be sure to save a copy of your account key to a secure location that is different from the computer where you currently have Martus installed, so you have it if you need to restore your account.",
"field:BackupYourKeyTip2=Martus also offers a way to back up your key in 3 separate pieces, which is the only way for you to access your Martus account and data if you forget your username and password. You will be prompted to create the 3 piece backup after completing your account setup and we strongly encourage you to do so.",

"field:Tor=Tor ",
"button:On=On",
"button:Off=Off",

"wintitle:MoveRecords=Move",
"button:move=Move",

"field:TitleCaseManagement=Label Manager",
"field:MainFolderLabel=Label ",
"field:EmptyCustomName=Custom Name",
"field:FolderNameCases=Cases",
"field:FolderNameIncidents=Incidents",
"field:FolderNameProjects=Projects",
"field:FolerNameUserDefined=Custom Name",
"field:FolderCreateNewName=Name ",
"field:HintEnterFolderName=Enter Name",
"field:defaultCaseName=Untitled",
"field:HintFolderNameAlreadyExists=This name is already in the system.  Please choose a unique name.",
"field:HintFolderNameInvalid=You must enter a valid name.",
"field:HintFolderNameIsSame=Enter a new name.",
"field:FolderRename=New Name ",
"button:CreateFolder=Create",
"button:DeleteFolder=Delete",
"button:RenameFolder=Rename",
"wintitle:DeleteFolder=Delete",
"field:DeleteFolderMessage=You are about to delete #FolderName#",
"field:notifyUnableToCreateFoldercause=Unable to create Folder.",
"wintitle:notifyUnableToCreateFolder=Error Creating Folder",
"field:notifyErrorDeletingFoldercause=Unable to delete Folder.",
"wintitle:notifyErrorDeletingFolder=Error Deleting Folder",
"button:CaseSent=Saved",
"button:CaseAll=All",
"button:CaseReceived=Received",
"field:ChooseLanguage=Choose a Language",
"field:TokenNotAvailable=Not Available",
"wintitle:CaseAll=All",
"wintitle:CaseSent=Sent",
"wintitle:CaseReceived=Received",
"wintitle:CaseSaved=Saved",

"folder:%OutBox=Unsent Records",
"folder:%Sent=Saved",
"folder:%Draft=Old Draft Records",
"folder:%Discarded=Trash",
"folder:%RetrievedBulletins=Received Records",
"folder:%RetrievedMyBulletin=Received Records",
"folder:%RetrievedMyBulletinDraft=Received Records",
"folder:%SearchResults=Search Results",
"folder:%RecoveredBulletins=Recovered Records",
"folder:%DamagedBulletins=Damaged Records",
"folder:%Import=Imported Records",
"folder:%RetrievedFieldOfficeBulletin=Field Desk Records",

"month:jan=Jan",
"month:feb=Feb",
"month:mar=Mar",
"month:apr=Apr",
"month:may=May",
"month:jun=Jun",
"month:jul=Jul",
"month:aug=Aug",
"month:sep=Sep",
"month:oct=Oct",
"month:nov=Nov",
"month:dec=Dec",

"month:Thai1=Jan",
"month:Thai2=Feb",
"month:Thai3=Mar",
"month:Thai4=Apr",
"month:Thai5=May",
"month:Thai6=Jun",
"month:Thai7=Jul",
"month:Thai8=Aug",
"month:Thai9=Sep",
"month:Thai10=Oct",
"month:Thai11=Nov",
"month:Thai12=Dec",

"month:Persian1=Farvardin",
"month:Persian2=Ordibehesht",
"month:Persian3=Khordad",
"month:Persian4=Tir",
"month:Persian5=Mordad",
"month:Persian6=Shahrivar",
"month:Persian7=Mehr",
"month:Persian8=Aban",
"month:Persian9=Azar",
"month:Persian10=Dey",
"month:Persian11=Bahman",
"month:Persian12=Esfand",

"month:Afghan1=hamal",
"month:Afghan2=sawr",
"month:Afghan3=dʒawzɒ",
"month:Afghan4=saratɒn",
"month:Afghan5=asad",
"month:Afghan6=sonbola",
"month:Afghan7=mizɒn",
"month:Afghan8='aqrab",
"month:Afghan9=qaws",
"month:Afghan10=dʒadi",
"month:Afghan12=dalvæ",
"month:Afghan11=hut",

"keyword:and=and",
"keyword:or=or",
	};
}
