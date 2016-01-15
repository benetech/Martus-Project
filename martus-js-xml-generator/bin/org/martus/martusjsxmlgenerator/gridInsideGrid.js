this.MartusFieldSpecs = [

new GridField("GridTag", "Grid Lable", "$$$griddata.csv", "\|", "id",[
	new StringField("FirstGridNameTag", "First Name", "firstname"),
	new GridField("GridTag2", "Grid Lable2", "$$$griddata.csv", "\|", "id",[
		new StringField("FirstGridNameTag2", "First Name", "firstname")])

]),

new MartusRequiredLanguageField("language"),
new MartusRequiredAuthorField("author"),
new MartusRequiredTitleField("title"),
new MartusRequiredDateCreatedField("entrydate", "yyyyMMdd"),
new MartusRequiredPrivateField(function (){return "MY PRIVATE DATE = " + data2;})
]
