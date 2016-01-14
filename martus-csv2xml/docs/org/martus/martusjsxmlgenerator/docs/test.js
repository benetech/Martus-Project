//test.js Version 1.2
//Requires Martus 3.0 or greater

//You must have the following Required Fields. 
//MartusRequiredLanguageField
//MartusRequiredAuthorField
//MartusRequiredTitleField
//MartusRequiredDateCreatedField
//MartusRequiredPrivateField

//Here are some of the Martus Default Fields
//MartusSummaryField
//MartusLocationField
//MartusOrganizationField
//MartusDetailsField
//MartusKeywordsField
//MartusDateOfEventField 

//Here are the optional fields you can use
//StringField
//MultilineField
//SingleDateField
//DateRangeField
//DropDownField
//BooleanField
//MessageField

//The parameters for each field are (Tag, Label, Value, [date format], [IsBottomSection]) 
//where [date format] is only required for date fields.
//where [IsBottomSection] is only required if you want this field to appear in the bottom section of your bulletin (i.e. the "private" section)
//  "true" means the field is in the bottom section.
//  "false" or leaving out the parameter will place the field in the top section.  
//NOTE ALL MartusRequired fields do not require optional parameter [IsBottomSection] since they can only be in their specified sections.

//Value can either be a single Header label in the CSV file, or a function.

//If Value is a single Header label then you must have it within "s
//Eg. new StringField("CommentTag", "Comment Lable User Sees", "comment")
//or  new StringField("CommentTag", "Comment Lable User Sees", "comment", false)
//Where "CommentTag" is the tag for this field
//"Comment Label User Sees" is the Label for the field
//"comment" is the 8'th row in the csv file, and for each bulletin its 8'th columns data will be placed in the bulletin.
//This will place this in the Top section of the bulletin

//To put this field in the bottom section of your bulletin change the [IsBottomSection] to true
//i.e. new StringField("CommentTag", "Comment Lable User Sees", "comment", true)

//If Value is a function then within the function if you want to print something directly add "s around the text you want inserted into each bulletin.  For inserting from the csv file do not put "s around the Header label.
//Eg 1. new StringField("CommentTag", "Comment Lable User Sees",function ({return comment;})
//Eg 2. new StringField("CommentTag", "Comment Lable User Sees",function ({return "Hard coded Comment " + comment;})
//Eg 3. new StringField("CommentTag", "Comment Lable User Sees",function ({return "Hard coded Comment " + comment;}, false)
//Where "CommentTag" is the tag for this field
//"Comment Label User Sees" is the Label for the field
//"Hard coded Comment " will be added to each bulletin with the field comment appended, which is the 8'th row in the csv file, and for each bulletin its 8'th columns data will be placed in the bulletin.
//This will place this in the Top Section of your bulletin

//Define static variables here if you want to reference them my name further on in the script

DATE_RANGE_DELIMETER = "_"
WitnessTag = "Witness" 
CommentTag = "WitnessComment" 

//This is the field spec we are creating and use for each Martus bulletin
this.MartusFieldSpecs = [

//Add all Fields you want now in the Martus Bulletin in the order you want them to appear.
//You can put the MartusRequiredPrivateField anywhere, it will always get added to the end of your Martus Bulletin.

new StringField(WitnessTag,	"Witness",
	function ()
	{
		return firstname + " " + lastname;
	},
	false
),

new StringField(CommentTag, "Comment", "comment"),

new MartusRequiredLanguageField("language"),


new MultilineField("MultiLineTag", "Description of Situation", 
	function ()
	{
		return "line1\nline2\n";
	}
),
	
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

new MartusDetailsField("data2"),

new MartusKeywordsField(
	function ()
	{
		return guns +", "+ data2;
	}
),

//DateRange must be in the format StartDate_EndDate
//The DateFormat Field is optional if the dates are in the MartusDefaultFormat yyyy-MM-dd
new DateRangeField("EventOccuredTag",	"Event Occured",
	function ()
	{
		return event_date_start + DATE_RANGE_DELIMETER + event_date_end;
	}
	, "MMddyyyy"
),

new SingleDateField("StartDateTag",	"Start Date",
	function ()
	{
		return event_date_start;
	}
	, "MMddyyyy"
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

new DropDownField("gun_tag", "Where guns Used?", "guns", ["Yes","No", "Unknown"]),

new BooleanField("anonymous_tag", "Does interviewee wish to remain anonymous?", "anonymous"),

new MessageField("MessageProfession", "Profession History Table Note", 
	function()
	{
		return "If you have information about a person who has had different professions over time, enter multiple rows with the same First and Last Names and show the date ranges for each profession on a separate row.";
	}
),

new GridField("GridTag", "Grid Lable", "griddata.csv", "\|", "id",[
	new StringField("FirstGridNameTag", "First Name", "firstname"),
	new StringField("LastGridNameTag", "Last Name", "lastname"),
	new SingleDateField("SimpleDateGridTag", "Date of Birth", 
		function(){ return date_of_Birth;}, "MMddyyyy"),
	new DateRangeField("DateRangeGridTag",	"Occured",
		function ()
		{
			return start_date + DATE_RANGE_DELIMETER + end_date;
		}
		, "yyyyMMdd"),
	new DropDownField("Grid_DD_tag", "Color Used", "color", ["red","yellow","blue"]),
	new BooleanField("grid_bool_tag", "Occurred at Night?", "night")
])

]	



