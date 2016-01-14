package org.martus.common.utilities;

/**
 * @author roms
 *         Date: 3/29/13
 */
public class BurmeseUtilities
{
	public static String getDisplayable(String storedText)
	{
		return MString.getUni2Z(storedText);
	}

	public static String getStorable(String displayed)
	{
		return MString.getZ2Uni(displayed);
	}
}

/**
 * ကိုငွေထွန်း၏ Javascript ကွန်ဗာတာကို Java သို့ပြောင်းထားပါသည်။
 */

/**
 * @author RENN
 *
 */
class MString 
{
	static String tallAA = "\u102B";
	static String AA = "\u102C";
	static String vi = "\u102D";
	static String ii = "\u102E";
	static String u = "\u102F";
	static String uu = "\u1030";
	static String ve = "\u1031";
	static String ai = "\u1032";
	static String ans = "\u1036";
	static String db = "\u1037";
	static String visarga = "\u1038";
	static String asat = "\u103A";
	static String ya = "\u103B";
	static String ra = "\u103C";
	static String wa = "\u103D";
	static String ha = "\u103E";
	static String zero = "\u1040";

	static String PTN_1 = "(([\u1000-\u101C\u101E-\u102A\u102C\u102E-\u103F\u104C-\u109F]))(\u1040)(?=\u0020)?";
	static String PTN_2 = "((\u101D))(\u1040)(?=\u0020)?";
	static String PTN_3 = "(([\u1000-\u101C\u101E-\u102A\u102C\u102E-\u103F\u104C-\u109F\u0020]))(\u1047)";

	public static String getZ2Uni(String zString) 
	{
		if(zString == null)
			return null;
		String output = new String(zString);
		output = output.replaceAll("\u106A", " \u1009");
		output = output.replaceAll("\u1025(?=[\u1039\u102C])", "\u1009"); // new
		output = output.replaceAll("\u1025\u102E", "\u1026"); // new
		output = output.replaceAll("\u106B", "\u100A");
		output = output.replaceAll("\u1090", "\u101B");
		output = output.replaceAll("\u1040", zero);
		output = output.replaceAll("\u108F", "\u1014");
		output = output.replaceAll("\u1012", "\u1012");
		output = output.replaceAll("\u1013", "\u1013");
		output = output.replaceAll("[\u103D\u1087]", ha);
		output = output.replaceAll("\u103C", wa);
		output = output.replaceAll(
				"[\u103B\u107E\u107F\u1080\u1081\u1082\u1083\u1084]", ra);
		output = output.replaceAll("[\u103A\u107D]", ya);
		output = output.replaceAll("\u103E\u103B", ya + ha);
		output = output.replaceAll("\u108A", wa + ha);
		output = output.replaceAll("\u103E\u103D", wa + ha);
		output = output.replaceAll("(\u1031)?(\u103C)?([\u1000-\u1021])\u1064",
				"\u1064$1$2$3");
		output = output.replaceAll("(\u1031)?(\u103C)?([\u1000-\u1021])\u108B",
				"\u1064$1$2$3\u102D");
		output = output.replaceAll("(\u1031)?(\u103C)?([\u1000-\u1021])\u108C",
				"\u1064$1$2$3\u102E");
		output = output.replaceAll("(\u1031)?(\u103C)?([\u1000-\u1021])\u108D",
				"\u1064$1$2$3\u1036");
		output = output.replaceAll("\u105A", tallAA + asat);
		output = output.replaceAll("\u108E", vi + ans);
		output = output.replaceAll("\u1033", u);
		output = output.replaceAll("\u1034", uu);
		output = output.replaceAll("\u1088", ha + u);
		output = output.replaceAll("\u1089", ha + uu);
		output = output.replaceAll("\u1039", "\u103A");
		output = output.replaceAll("[\u1094\u1095]", db);
		output = output
				.replaceAll(
						"([\u1000-\u1021])([\u102C\u102D\u102E\u1032\u1036]){1,2}([\u1060\u1061\u1062\u1063\u1065\u1066\u1067\u1068\u1069\u1070\u1071\u1072\u1073\u1074\u1075\u1076\u1077\u1078\u1079\u107A\u107B\u107C\u1085])",
						"$1$3$2"); // new
		output = output.replaceAll("\u1064", "\u1004\u103A\u1039");
		output = output.replaceAll("\u104E", "\u104E\u1004\u103A\u1038");
		output = output.replaceAll("\u1086", "\u103F");
		output = output.replaceAll("\u1060", "\u1039\u1000");
		output = output.replaceAll("\u1061", "\u1039\u1001");
		output = output.replaceAll("\u1062", "\u1039\u1002");
		output = output.replaceAll("\u1063", "\u1039\u1003");
		output = output.replaceAll("\u1065", "\u1039\u1005");
		output = output.replaceAll("[\u1066\u1067]", "\u1039\u1006");
		output = output.replaceAll("\u1068", "\u1039\u1007");
		output = output.replaceAll("\u1069", "\u1039\u1008");
		output = output.replaceAll("\u106C", "\u1039\u100B");
		output = output.replaceAll("\u1070", "\u1039\u100F");
		output = output.replaceAll("[\u1071\u1072]", "\u1039\u1010");
		output = output.replaceAll("[\u1073\u1074]", "\u1039\u1011");
		output = output.replaceAll("\u1075", "\u1039\u1012");
		output = output.replaceAll("\u1076", "\u1039\u1013");
		output = output.replaceAll("\u1077", "\u1039\u1014");
		output = output.replaceAll("\u1078", "\u1039\u1015");
		output = output.replaceAll("\u1079", "\u1039\u1016");
		output = output.replaceAll("\u107A", "\u1039\u1017");
		output = output.replaceAll("\u107B", "\u1039\u1018");
		output = output.replaceAll("\u107C", "\u1039\u1019");
		output = output.replaceAll("\u1085", "\u1039\u101C");
		output = output.replaceAll("\u106D", "\u1039\u100C");
		output = output.replaceAll("\u1091", "\u100F\u1039\u100D");
		output = output.replaceAll("\u1092", "\u100B\u1039\u100C");
		output = output.replaceAll("\u1097", "\u100B\u1039\u100B");
		output = output.replaceAll("\u106F", "\u100E\u1039\u100D");
		output = output.replaceAll("\u106E", "\u100D\u1039\u100D");
		output = output.replaceAll(
				"(\u103C)([\u1000-\u1021])(\u1039[\u1000-\u1021])?", "$2$3$1");
		output = output
				.replaceAll("(\u103E)(\u103D)([\u103B\u103C])", "$3$2$1");
		output = output.replaceAll("(\u103E)([\u103B\u103C])", "$2$1");
		output = output.replaceAll("(\u103D)([\u103B\u103C])", "$2$1");

		// PTN1
		output = output.replaceAll(PTN_1, (null != "$1") ? "$1" + "\u101D"
				: "$0$1");

		// PTN2
		output = output.replaceAll(PTN_2, (null != "$1") ? "$1" + "\u101D"
				: "$0$1");

		// PTN3
		output = output.replaceAll(PTN_3, (null != "$1") ? "$1" + "\u101B"
				: "$0$1");

		output = output
				.replaceAll(
						"(\u1047)( ? = [\u1000 - \u101C\u101E - \u102A\u102C\u102E - \u103F\u104C - \u109F\u0020])",
						"\u101B");
		output = output
				.replaceAll(
						"(\u1031)?([\u1000-\u1021])(\u1039[\u1000-\u1021])?([\u102D\u102E\u1032])?([\u1036\u1037\u1038]{0,2})([\u103B-\u103E]{0,3})([\u102F\u1030])?([\u1036\u1037\u1038]{0,2})([\u102D\u102E\u1032])?",
						"$2$3$6$1$4$9$7$5$8");
		output = output.replaceAll(ans + u, u + ans);
		output = output.replaceAll("(\u103A)(\u1037)", "$2$1");

		return output;
	}

	public static String getUni2Z(String inString) 
	{
		if(inString == null)
			return null;
		String output = new String(inString);
		output = output.replaceAll("\u104E\u1004\u103A\u1038", "\u104E");
		output = output.replaceAll("\u102B\u103A", "\u105A");
		output = output.replaceAll("\u102D\u1036", "\u108E");
		output = output.replaceAll("\u103F", "\u1086");
		output = output.replaceAll("(\u102F[\u1036]?)\u1037",
				(null != "$1") ? "$1" + "\u1094" : "$0" + "$1");
		output = output.replaceAll("(\u1030[\u1036]?)\u1037",
				(null != "$1") ? "$1" + "\u1094" : "$0" + "$1");
		output = output.replaceAll("(\u1014[\u103A\u1032]?)\u1037",
				(null != "$1") ? "$1" + "\u1094" : "$0" + "$1");
		output = output.replaceAll("(\u103B[\u1032\u1036]?)\u1037",
				(null != "$1") ? "$1" + "\u1095" : "$0" + "$1");
		output = output.replaceAll("(\u103D[\u1032]?)\u1037",
				(null != "$1") ? "$1" + "\u1095" : "$0" + "$1");
		output = output.replaceAll(
				"([\u103B\u103C\u103D][\u102D\u1036]?)\u102F",
				(null != "$1") ? "$1" + "\u1033" : "$0" + "$1");
		output = output.replaceAll(
				"((\u1039[\u1000-\u1021])[\u102D\u1036]?)\u102F",
				(null != "$1") ? "$1" + "\u1033" : "$0" + "$1");
		output = output.replaceAll(
				"([\u100A\u100C\u1020\u1025\u1029][\u102D\u1036]?)\u102F",
				(null != "$1") ? "$1" + "\u1033" : "$0" + "$1");
		output = output.replaceAll(
				"([\u103B\u103C][\u103D]?[\u103E]?[\u102D\u1036]?)\u1030",
				(null != "$1") ? "$1" + "\u1034" : "$0" + "$1");
		output = output.replaceAll(
				"((\u1039[\u1000-\u1021])[\u102D\u1036]?)\u1030",
				(null != "$1") ? "$1" + "\u1034" : "$0" + "$1");
		output = output.replaceAll(
				"([\u100A\u100C\u1020\u1025\u1029][\u102D\u1036]?)\u1030",
				(null != "$1") ? "$1" + "\u1034" : "$0" + "$1");
		output = output.replaceAll("(\u103C)\u103E", (null != "$1") ? "$1"
				+ "\u1087" : "$0" + "$1");
		output = output.replaceAll("\u1009(?=[\u103A])", "\u1025");
		output = output.replaceAll("\u1009(?=\u1039[\u1000-\u1021])", "\u1025");
		// E render
		output = output
				.replaceAll(
						"([\u1000-\u1021\u1029])(\u1039[\u1000-\u1021])?([\u103B-\u103E\u1087]*)?\u1031",
						"\u1031$1$2$3");
		// Ra render
		output = output
				.replaceAll(
						"([\u1000-\u1021\u1029])(\u1039[\u1000-\u1021\u1000-\u1021])?(\u103C)",
						"$3$1$2");
		// Kinzi
		output = output.replaceAll("\u1004\u103A\u1039", "\u1064");
		// kinzi
		output = output.replaceAll(
				"(\u1064)([\u1031]?)([\u103C]?)([\u1000-\u1021])\u102D",
				"$2$3$4\u108B");
		// reordering kinzi lgt
		output = output.replaceAll(
				"(\u1064)(\u1031)?(\u103C)?([ \u1000-\u1021])\u102E",
				"$2$3$4\u108C");
		// reordering kinzi lgtsk
		output = output.replaceAll(
				"(\u1064)(\u1031)?(\u103C)?([ \u1000-\u1021])\u1036",
				"$2$3$4\u108D");
		// reordering kinzi ttt
		output = output.replaceAll(
				"(\u1064)(\u1031)?(\u103C)?([ \u1000-\u1021])", "$2$3$4\u1064");
		// reordering kinzi
		// Consonant
		output = output.replaceAll("\u100A(?=[\u1039\u102F\u1030])", "\u106B");
		// nnya - 2
		output = output.replaceAll("\u100A", "\u100A");
		// nnya
		output = output.replaceAll("\u101B(?=[\u102F\u1030])", "\u1090");
		// ra - 2
		output = output.replaceAll("\u101B", "\u101B");
		// ra
		output = output.replaceAll(
				"\u1014(?=[\u1039\u103D\u103E\u102F\u1030])", "\u108F");
		// na - 2
		output = output.replaceAll("\u1014", "\u1014");
		// na
		// Stacked consonants
		output = output.replaceAll("\u1039\u1000", "\u1060");
		output = output.replaceAll("\u1039\u1001", "\u1061");
		output = output.replaceAll("\u1039\u1002", "\u1062");
		output = output.replaceAll("\u1039\u1003", "\u1063");
		output = output.replaceAll("\u1039\u1005", "\u1065");
		output = output.replaceAll("\u1039\u1006", "\u1066");
		// 1067
		output = output
				.replaceAll(
						"([\u1001\u1002\u1004\u1005\u1007\u1012\u1013\u108F\u1015\u1016\u1017\u1019\u101D])\u1066",
						(null != "$1") ? "$1" + "\u1067" : "$0" + "$1");
		// 1067
		output = output.replaceAll("\u1039\u1007", "\u1068");
		output = output.replaceAll("\u1039\u1008", "\u1069");
		output = output.replaceAll("\u1039\u100F", "\u1070");
		output = output.replaceAll("\u1039\u1010", "\u1071");
		// 1072 omit (little shift to right)
		output = output
				.replaceAll(
						"([\u1001\u1002\u1004\u1005\u1007\u1012\u1013\u108F\u1015\u1016\u1017\u1019\u101D])\u1071",
						(null != "$1") ? "$1" + "\u1072" : "$0" + "$1");
		// 1067
		output = output.replaceAll("\u1039\u1011", "\u1073");
		// \u1074 omit(little shift to right)
		output = output
				.replaceAll(
						"([\u1001\u1002\u1004\u1005\u1007\u1012\u1013\u108F\u1015\u1016\u1017\u1019\u101D])\u1073",
						(null != "$1") ? "$1" + "\u1074" : "$0" + "$1");
		// 1067
		output = output.replaceAll("\u1039\u1012", "\u1075");
		output = output.replaceAll("\u1039\u1013", "\u1076");
		output = output.replaceAll("\u1039\u1014", "\u1077");
		output = output.replaceAll("\u1039\u1015", "\u1078");
		output = output.replaceAll("\u1039\u1016", "\u1079");
		output = output.replaceAll("\u1039\u1017", "\u107A");
		output = output.replaceAll("\u1039\u1018", "\u107B");
		output = output.replaceAll("\u1039\u1019", "\u107C");
		output = output.replaceAll("\u1039\u101C", "\u1085");
		output = output.replaceAll("\u100F\u1039\u100D", "\u1091");
		output = output.replaceAll("\u100B\u1039\u100C", "\u1092");
		output = output.replaceAll("\u1039\u100C", "\u106D");
		output = output.replaceAll("\u100B\u1039\u100B", "\u1097");
		output = output.replaceAll("\u1039\u100B", "\u106C");
		output = output.replaceAll("\u100E\u1039\u100D", "\u106F");
		output = output.replaceAll("\u100D\u1039\u100D", "\u106E");
		output = output.replaceAll("\u1009(?=\u103A)", "\u1025");
		// u
		output = output.replaceAll("\u1025(?=[\u1039\u102F\u1030])", "\u106A");
		// u - 2
		output = output.replaceAll("\u1025", "\u1025");
		// u
		// //////////////////////////////////"
		output = output.replaceAll("\u103A", "\u1039");
		// asat
		output = output.replaceAll("\u103B\u103D\u103E", "\u107D\u108A");
		// ya wa ha
		output = output.replaceAll("\u103D\u103E", "\u108A");
		// wa ha
		output = output.replaceAll("\u103B", "\u103A");
		// ya
		output = output.replaceAll("\u103C", "\u103B");
		// ra
		output = output.replaceAll("\u103D", "\u103C");
		// wa
		output = output.replaceAll("\u103E", "\u103D");
		// ha
		output = output.replaceAll("\u103A(?=[\u103C\u103D\u108A])", "\u107D");
		// ya - 2
		output = output
				.replaceAll(
						"(\u100A(?:[\u102D\u102E\u1036\u108B\u108C\u108D\u108E])?)\u103D",
						(null != "$1") ? "$1" + "\u1087" : "$0");
		// ha - 2
		output = output
				.replaceAll(
						"\u103B(?=[\u1000\u1003\u1006\u100F\u1010\u1011\u1018\u101A\u101C\u101E\u101F\u1021])",
						"\u107E");
		// great Ra with wide consonants
		output = output
				.replaceAll(
						"\u107E([\u1000-\u1021\u108F])(?=[\u102D\u102E\u1036\u108B\u108C\u108D\u108E])",
						"\u1080$1");
		// great Ra with upper sign
		output = output.replaceAll(
				"\u107E([\u1000-\u1021\u108F])(?=[\u103C\u108A])", "\u1082$1");
		// great Ra with under signs
		output = output
				.replaceAll(
						"\u103B([\u1000-\u1021\u108F])(?=[\u102D \u102E \u1036 \u108B \u108C \u108D \u108E])",
						"\u107F$1");
		// little Ra with upper sign
		output = output.replaceAll(
				"\u103B([\u1000-\u1021\u108F])(?=[\u103C\u108A])", "\u1081$1");
		// little Ra with under signs
		output = output.replaceAll("(\u1014[\u103A\u1032]?)\u1037",
				(null != "$1") ? "$1" + "\u1094" : "$0" + "$1");
		// aukmyint
		output = output.replaceAll("(\u1033[\u1036]?)\u1094",
				(null != "$1") ? "$1" + "\u1095" : "$0" + "$1");
		// aukmyint
		output = output.replaceAll("(\u1034[\u1036]?)\u1094",
				(null != "$1") ? "$1" + "\u1095" : "$0" + "$1");
		// aukmyint
		output = output.replaceAll("([\u103C\u103D\u108A][\u1032]?)\u1037",
				(null != "$1") ? "$1" + "\u1095" : "$0" + "$1");
		// aukmyint
		return output;
	}
}