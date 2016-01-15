this.MartusFieldSpecs = [

new GridField("GridTag", "Grid Lable", "$$$griddata.csv", "\|", "id",[
	new StringField("FirstGridNameTag", "First Name", "firstname"),
	new MultilineField("MultiLineTag", "Description of Situation", 
	function (){return "line1\nline2\n";})
]),

new MartusRequiredLanguageField("language"),
new MartusRequiredAuthorField("author"),
new MartusRequiredTitleField("title"),
new MartusRequiredDateCreatedField("entrydate", "yyyyMMdd"),
new MartusRequiredPrivateField(function (){return "MY PRIVATE DATE = " + data2;})
]



