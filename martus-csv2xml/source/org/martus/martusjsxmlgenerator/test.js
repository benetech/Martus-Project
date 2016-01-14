DATE_RANGE_DELIMETER = "_"

WitnessTag = "Witness"
CommentTag = "WitnessComment"

this.MartusFieldSpecs = [
new StringField(WitnessTag,	"Witness",
	function ()
	{
		return firstname + " " + lastname;
	}
),

new StringField(CommentTag, "Comment", "comment"),

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

//DateRange must be in the format StartDate_EndDate
//The DateFormat Field is optional if the dates are in the MartusDefaultFormat yyyy-MM-dd
//new DateRangeField("eventdate",	"",
//	function ()
//	{
//		return event_date_start + DATE_RANGE_DELIMETER + event_date_end;
//	}
//	, "MMddyyyy"
//),



new MartusDateOfEventField (
	function ()
	{
		return event_date_start + DATE_RANGE_DELIMETER + event_date_end;
	}, "MMddyyyy"
),

new DropDownField("gun_tag", "Where guns Used?", "guns", ["Yes","No","Unknown"]),

new BooleanField("anonymous_tag_bottom_section", "Does interviewee wish to remain anonymous?", "anonymous", true),

new MartusRequiredPrivateField(
	function ()
	{
		return "MY PRIVATE DATE = " + data2;
	}
),

new MessageField("MessageProfession", "Profession History Table Note", 
	function()
	{
		return "If you have <information> about a person who has had different professions over time, enter multiple rows with the same First and Last Names and show the date ranges for each profession on a separate row.";
	}
),

new GridField("GridTag", "Grid Lable", "$$$griddata.csv", "\|", "id",[
	new StringField("FirstGridNameTag", "First <Name>", "firstname"),
	new StringField("LastGridNameTag", "Last Name", "lastname"),
	new SingleDateField("SimpleDateGridTag", "Date of Birth", function(){ return date_of_Birth;}, "MMddyyyy"),
	new DateRangeField("DateRangeGridTag",	"Occured",
		function ()
		{
			return start_date + DATE_RANGE_DELIMETER + end_date;
		}
		, "yyyyMMdd"),
	new DropDownField("Grid_DD_tag", "Color Used", "color", ["red","<yellow>&<green>","blue"]),
	new BooleanField("grid_bool_tag", "Occurred at Night?", "night")
	
])

]



