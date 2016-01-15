DATE_RANGE_DELIMETER = "_"

this.MartusFieldSpecs = [
new StringField("Witness",	"Witness",
	function ()
	{
		return firstname + " " + lastname;
	}
),

new StringField("WitnessComment", "Comment", "comment"),

new MartusRequiredLanguageField("language"),
	
new MartusRequiredAuthorField("author"),

new MartusRequiredTitleField("title"),

new MartusRequiredDateCreatedField("entrydate", "yyyyMMdd"),

new MartusSummaryField("data2"),

new MartusLocationField("comment"),

new MartusOrganizationField(	
	function ()
	{
		return "XYZ NGO";
	}
),

new MartusDetailsField("id"),

new MartusKeywordsField(
	function ()
	{
		return id +", "+ data2;
	}
),

new MartusDateOfEventField (
	function ()
	{
		return event_date_start + DATE_RANGE_DELIMETER + event_date_end;
	}, "MMddyyyy"
),

new MartusRequiredPrivateField(
	function ()
	{
		return "MY PRIVATE DATE = " + data2;
	}
),

new MartusTopSectionAttachments("public_attachments"),

new MartusBottomSectionAttachments("private_attachments", ";"),
new MartusBottomSectionAttachments(
	function ()
	{
		return private_attachments_2;
	}, ";","C:\\PrivateAttachments")

]



