this.MartusFieldSpecs = [

new GridField("GridTag", "Grid Lable", "$$$griddata.csv", "\|", "id",[
	new StringField("FirstGridNameTag", "First Name", "firstname"),
	new MessageField("MessageProfession", "Profession History Table Note", function(){return "message";})
]),

new MartusRequiredLanguageField("language"),
new MartusRequiredAuthorField("author"),
new MartusRequiredTitleField("title"),
new MartusRequiredDateCreatedField("entrydate", "yyyyMMdd"),
new MartusRequiredPrivateField(function (){return "MY PRIVATE DATE = " + data2;})
]
