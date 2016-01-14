/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2007, Beneficent
Technology, Inc. (The Benetech Initiative).

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

package org.martus.common.crypto;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyPair;

import org.martus.util.StreamableBase64;

public class MockMartusSecurity extends MartusSecurity
{
	public static MockMartusSecurity createClient() throws Exception
	{
		MockMartusSecurity security = new MockMartusSecurity();
		security.loadSampleAccount();
		return security;
	}
	
	public static MockMartusSecurity createOtherClient() throws Exception
	{
		MockMartusSecurity security = new MockMartusSecurity();
		security.createKeyPairForOtherClient();
		return security;
	}
	
	public static MockMartusSecurity createHQ() throws Exception
	{
		MockMartusSecurity security = new MockMartusSecurity();
		security.createKeyPairForHQ();
		return security;
	}
	
	public static MockMartusSecurity createServer() throws Exception
	{
		MockMartusSecurity security = new MockMartusSecurity();
		security.createKeyPairForServer();
		return security;
	}
	
	public static MockMartusSecurity createOtherServer() throws Exception
	{
		MockMartusSecurity security = new MockMartusSecurity();
		security.createKeyPairForOtherServer();
		return security;
	}
	
	public static MockMartusSecurity createAmplifier() throws Exception
	{
		MockMartusSecurity security = new MockMartusSecurity();
		security.createKeyPairForAmplifier();
		return security;
	}
	
	public MockMartusSecurity() throws Exception
	{
	}

	public void speedWarning(String message)
	{
		//System.out.println("MockMartusSecurity.speedWarning: " + message);
	}

	public void readKeyPair(InputStream inputStream, char[] passPhrase) throws
		IOException,
		InvalidKeyPairFileVersionException,
		AuthorizationFailedException
	{
		if(fakeKeyPairVersionFailure)
			throw new InvalidKeyPairFileVersionException();

		if(fakeAuthorizationFailure)
			throw new AuthorizationFailedException();

		super.readKeyPair(inputStream, passPhrase);
	}
	
	public void createKeyPairForOtherClient() throws Exception
	{
//		createKeyPair();
//		System.out.println(Base64.encode(getKeyPairData(getKeyPair())));
		setKeyPairFromData(StreamableBase64.decode(nonEncryptedSampleOtherClientKeyPair));
	}

	public void createKeyPairForHQ() throws Exception
	{
		setKeyPairFromData(StreamableBase64.decode(nonEncryptedSampleHQKeyPair));
	}

	public void createKeyPairForServer() throws Exception
	{
		// NOTE: We should use a hard-coded keypair like we do for clients,
		// for speed, but Java 7 requires SSL keys to be at least 1024 bits
		createKeyPair(SMALLEST_LEGAL_KEY_FOR_TESTING);
	}

	public void createKeyPairForOtherServer() throws Exception
	{
		// NOTE: We should use a hard-coded keypair like we do for clients,
		// for speed, but Java 7 requires SSL keys to be at least 1024 bits
		createKeyPair(SMALLEST_LEGAL_KEY_FOR_TESTING);
	}

	public void createKeyPairForAmplifier() throws Exception
	{
		// NOTE: We should use a hard-coded keypair like we do for clients,
		// for speed, but Java 7 requires SSL keys to be at least 1024 bits
		createKeyPair(SMALLEST_LEGAL_KEY_FOR_TESTING);
	}

	public void createKeyPair()
	{
		createKeyPair(SMALLEST_LEGAL_KEY_FOR_TESTING);
	}

	public void createKeyPair(int publicKeyBits)
	{
		speedWarning("Calling MockMartusSecurity.createKeyPair " + publicKeyBits);
		super.createKeyPair(SMALLEST_LEGAL_KEY_FOR_TESTING);
	}

	KeyPair createSunKeyPair(int bitsInKey) throws Exception
	{
		int smallKeySizeForTesting = 1024;
		return super.createSunKeyPair(smallKeySizeForTesting);
	}

	public void loadSampleAccount() throws Exception
	{
		setKeyPairFromData(StreamableBase64.decode(nonEncryptedSampleKeyPair));
	}

	public SignatureEngine createSignatureVerifier(String signedByPublicKey)
			throws Exception
	{
		if(fakeSigVerifyFailure)
			return null;

		return super.createSignatureVerifier(signedByPublicKey);
	}
	
	static final int SMALLEST_LEGAL_KEY_FOR_TESTING = 1024;

	public boolean fakeSigVerifyFailure;
	public boolean fakeAuthorizationFailure;
	public boolean fakeKeyPairVersionFailure;

	private final static String nonEncryptedSampleKeyPair = 
		"rO0ABXNyABVqYXZhLnNlY3VyaXR5LktleVBhaXKXAww60s0SkwIAAkwACnByaX" + 
		"ZhdGVLZXl0ABpMamF2YS9zZWN1cml0eS9Qcml2YXRlS2V5O0wACXB1YmxpY0tl" +
		"eXQAGUxqYXZhL3NlY3VyaXR5L1B1YmxpY0tleTt4cHNyADFvcmcuYm91bmN5Y2" +
		"FzdGxlLmpjZS5wcm92aWRlci5KQ0VSU0FQcml2YXRlQ3J0S2V5bLqHzgJzVS4C" +
		"AAZMAA5jcnRDb2VmZmljaWVudHQAFkxqYXZhL21hdGgvQmlnSW50ZWdlcjtMAA" +
		"5wcmltZUV4cG9uZW50UHEAfgAFTAAOcHJpbWVFeHBvbmVudFFxAH4ABUwABnBy" +
		"aW1lUHEAfgAFTAAGcHJpbWVRcQB+AAVMAA5wdWJsaWNFeHBvbmVudHEAfgAFeH" +
		"IALm9yZy5ib3VuY3ljYXN0bGUuamNlLnByb3ZpZGVyLkpDRVJTQVByaXZhdGVL" +
		"ZXmyNYtAHTGFVgIABEwAB21vZHVsdXNxAH4ABUwAEHBrY3MxMkF0dHJpYnV0ZX" +
		"N0ABVMamF2YS91dGlsL0hhc2h0YWJsZTtMAA5wa2NzMTJPcmRlcmluZ3QAEkxq" +
		"YXZhL3V0aWwvVmVjdG9yO0wAD3ByaXZhdGVFeHBvbmVudHEAfgAFeHBzcgAUam" +
		"F2YS5tYXRoLkJpZ0ludGVnZXKM/J8fqTv7HQMABkkACGJpdENvdW50SQAJYml0" +
		"TGVuZ3RoSQATZmlyc3ROb256ZXJvQnl0ZU51bUkADGxvd2VzdFNldEJpdEkABn" +
		"NpZ251bVsACW1hZ25pdHVkZXQAAltCeHIAEGphdmEubGFuZy5OdW1iZXKGrJUd" +
		"C5TgiwIAAHhw///////////////+/////gAAAAF1cgACW0Ks8xf4BghU4AIAAH" +
		"hwAAAAgIbZPktljeCh3opk2hs84uU3zZK9Dd/Yu9pSU4nC6Y5BMN158f0KXBqd" +
		"/LhLa2xWaPAFwl0YPsfIEWdleKAIhKQsg0iE6oAgvPzgxquTiQ3/MDCppoP+4s" +
		"lXe4DjyOvmEZbJ0D7BgprZfrydQQr4KgdGEhNqu0Sq6c+3NQ1IqiP5eHNyABNq" +
		"YXZhLnV0aWwuSGFzaHRhYmxlE7sPJSFK5LgDAAJGAApsb2FkRmFjdG9ySQAJdG" +
		"hyZXNob2xkeHA/QAAAAAAACHcIAAAAAwAAAAB4c3IAEGphdmEudXRpbC5WZWN0" +
		"b3LZl31bgDuvAQIAA0kAEWNhcGFjaXR5SW5jcmVtZW50SQAMZWxlbWVudENvdW" +
		"50WwALZWxlbWVudERhdGF0ABNbTGphdmEvbGFuZy9PYmplY3Q7eHAAAAAAAAAA" +
		"AHVyABNbTGphdmEubGFuZy5PYmplY3Q7kM5YnxBzKWwCAAB4cAAAAApwcHBwcH" +
		"BwcHBwc3EAfgAK///////////////+/////gAAAAF1cQB+AA4AAACAR2PzzZAd" +
		"72TBHBdGSqfDakq4III0hZDb7A13hSr0HiKDSBNh/m7ld4DRFkYLsdNku05X1u" +
		"630y2u3GLlgeZkViRcwjzhAHO7lMQhnlmom3dykfNMv0cB4z1BRc3VLC+74oCa" +
		"baQyTpDqnYIwiXp7w3Y4U7p1tugfWG+wSiTuvEV4c3EAfgAK//////////////" +
		"/+/////gAAAAF1cQB+AA4AAABAa0tFUKBgvpZouwpHA3N134myWvAZyFtE6xOo" +
		"vPz7I8rd/GsJPH/8aO2S1s/MmHuehKuNbf/aYV2ft5/bhCKjEHhzcQB+AAr///" +
		"////////////7////+AAAAAXVxAH4ADgAAAECTHJOB858hABjyTaTOXO9vA3lQ" +
		"3HsemzhYNr+H4KpR5vPWUD5SbHCCsKRCda8foH3qKmE1d7bN+8QI5OjzG67ZeH" +
		"NxAH4ACv///////////////v////4AAAABdXEAfgAOAAAAQHTskqjRMy6fRqba" +
		"jbjSr4zCBI0osNI+lTmDIEK0EZhiKnsafuzP0EaOHe1jbx5uvtbYiCdMYqdcWK" +
		"xLckYoJDl4c3EAfgAK///////////////+/////gAAAAF1cQB+AA4AAABA0Gh7" +
		"osPMGWrOAe3+zwOoh++Wh+MDwLE6fPg6AH5GnrHZb5xYShmfY8+TXia4F3iyYR" +
		"FfC77to89Vt0RKAxHiX3hzcQB+AAr///////////////7////+AAAAAXVxAH4A" +
		"DgAAAEClpHpvKF3XYaQXCvNwf84HaDEdTvp/Lf4RecMJKcOX4GbZEDPPe7xj8/" +
		"+694gVx45bCBY3rDZtGChJauHjY4ineHNxAH4ACv///////////////v////4A" +
		"AAABdXEAfgAOAAAAARF4c3IALW9yZy5ib3VuY3ljYXN0bGUuamNlLnByb3ZpZG" +
		"VyLkpDRVJTQVB1YmxpY0tleSUiag5b+myEAgACTAAHbW9kdWx1c3EAfgAFTAAO" +
		"cHVibGljRXhwb25lbnRxAH4ABXhwcQB+AA1xAH4AIw==";

	private final static String nonEncryptedSampleOtherClientKeyPair = 
		"rO0ABXNyABVqYXZhLnNlY3VyaXR5LktleVBhaXKXAww60s0SkwIAAkwACnByaX" +
		"ZhdGVLZXl0ABpMamF2YS9zZWN1cml0eS9Qcml2YXRlS2V5O0wACXB1YmxpY0tl" +
		"eXQAGUxqYXZhL3NlY3VyaXR5L1B1YmxpY0tleTt4cHNyADFvcmcuYm91bmN5Y2" +
		"FzdGxlLmpjZS5wcm92aWRlci5KQ0VSU0FQcml2YXRlQ3J0S2V5bLqHzgJzVS4C" +
		"AAZMAA5jcnRDb2VmZmljaWVudHQAFkxqYXZhL21hdGgvQmlnSW50ZWdlcjtMAA" +
		"5wcmltZUV4cG9uZW50UHEAfgAFTAAOcHJpbWVFeHBvbmVudFFxAH4ABUwABnBy" +
		"aW1lUHEAfgAFTAAGcHJpbWVRcQB+AAVMAA5wdWJsaWNFeHBvbmVudHEAfgAFeH" +
		"IALm9yZy5ib3VuY3ljYXN0bGUuamNlLnByb3ZpZGVyLkpDRVJTQVByaXZhdGVL" +
		"ZXmyNYtAHTGFVgIABEwAB21vZHVsdXNxAH4ABUwAEHBrY3MxMkF0dHJpYnV0ZX" +
		"N0ABVMamF2YS91dGlsL0hhc2h0YWJsZTtMAA5wa2NzMTJPcmRlcmluZ3QAEkxq" +
		"YXZhL3V0aWwvVmVjdG9yO0wAD3ByaXZhdGVFeHBvbmVudHEAfgAFeHBzcgAUam" +
		"F2YS5tYXRoLkJpZ0ludGVnZXKM/J8fqTv7HQMABkkACGJpdENvdW50SQAJYml0" +
		"TGVuZ3RoSQATZmlyc3ROb256ZXJvQnl0ZU51bUkADGxvd2VzdFNldEJpdEkABn" +
		"NpZ251bVsACW1hZ25pdHVkZXQAAltCeHIAEGphdmEubGFuZy5OdW1iZXKGrJUd" +
		"C5TgiwIAAHhw///////////////+/////gAAAAF1cgACW0Ks8xf4BghU4AIAAH" +
		"hwAAAAQLLWOqfG3T8IVINL2n1EmTLvxnqsIsmz7YfF5dxlwhYsQZLvFv8U7GjY" +
		"525AibDt3jhaBMv7iOK04fIL48zaqRN4c3IAE2phdmEudXRpbC5IYXNodGFibG" +
		"UTuw8lIUrkuAMAAkYACmxvYWRGYWN0b3JJAAl0aHJlc2hvbGR4cD9AAAAAAAAI" +
		"dwgAAAALAAAAAHhzcgAQamF2YS51dGlsLlZlY3RvctmXfVuAO68BAgADSQARY2" +
		"FwYWNpdHlJbmNyZW1lbnRJAAxlbGVtZW50Q291bnRbAAtlbGVtZW50RGF0YXQA" +
		"E1tMamF2YS9sYW5nL09iamVjdDt4cAAAAAAAAAAAdXIAE1tMamF2YS5sYW5nLk" +
		"9iamVjdDuQzlifEHMpbAIAAHhwAAAACnBwcHBwcHBwcHBzcQB+AAr/////////" +
		"//////7////+AAAAAXVxAH4ADgAAAEBpMrkXZeuOfV7j0kRJr+GlffxIKQVnlw" +
		"Qxv7Rjhybf3NB+MK/v1MWrO+hCeh8zZWQdEvNyA23ZrMKgO+CXfGZBeHNxAH4A" +
		"Cv///////////////v////4AAAABdXEAfgAOAAAAILYP9OqCCQ7/N8z04Xdinx" +
		"Qr6zKM2AEb0esy/TtxAbGqeHNxAH4ACv///////////////v////4AAAABdXEA" +
		"fgAOAAAAIDJohYy4qRthJ/CF2V3lgBC+9GVCYmRzweSoChbgZxcpeHNxAH4ACv" +
		"///////////////v////4AAAABdXEAfgAOAAAAIEtsfegVhAmadbxpc+DX7Bpf" +
		"fqeMbs0u1PfwMYMwYeAZeHNxAH4ACv///////////////v////4AAAABdXEAfg" +
		"AOAAAAINY8N5YQzrRc6b44288PYEcrjq5aIirr+AvKKuE5tiJveHNxAH4ACv//" +
		"/////////////v////4AAAABdXEAfgAOAAAAINWzZLw89hs1ousqyFJjx6AOkY" +
		"VjOfAEsL594Z5eaqWdeHNxAH4ACv///////////////v////4AAAABdXEAfgAO" +
		"AAAAARF4c3IALW9yZy5ib3VuY3ljYXN0bGUuamNlLnByb3ZpZGVyLkpDRVJTQV" +
		"B1YmxpY0tleSUiag5b+myEAgACTAAHbW9kdWx1c3EAfgAFTAAOcHVibGljRXhw" +
		"b25lbnRxAH4ABXhwcQB+AA1xAH4AIw==";

	private final static String nonEncryptedSampleHQKeyPair = 
		"rO0ABXNyABVqYXZhLnNlY3VyaXR5LktleVBhaXKXAww60s0SkwIAAkwACnByaX" +
		"ZhdGVLZXl0ABpMamF2YS9zZWN1cml0eS9Qcml2YXRlS2V5O0wACXB1YmxpY0tl" +
		"eXQAGUxqYXZhL3NlY3VyaXR5L1B1YmxpY0tleTt4cHNyADFvcmcuYm91bmN5Y2" +
		"FzdGxlLmpjZS5wcm92aWRlci5KQ0VSU0FQcml2YXRlQ3J0S2V5bLqHzgJzVS4C" +
		"AAZMAA5jcnRDb2VmZmljaWVudHQAFkxqYXZhL21hdGgvQmlnSW50ZWdlcjtMAA" +
		"5wcmltZUV4cG9uZW50UHEAfgAFTAAOcHJpbWVFeHBvbmVudFFxAH4ABUwABnBy" +
		"aW1lUHEAfgAFTAAGcHJpbWVRcQB+AAVMAA5wdWJsaWNFeHBvbmVudHEAfgAFeH" +
		"IALm9yZy5ib3VuY3ljYXN0bGUuamNlLnByb3ZpZGVyLkpDRVJTQVByaXZhdGVL" +
		"ZXmyNYtAHTGFVgIABEwAB21vZHVsdXNxAH4ABUwAEHBrY3MxMkF0dHJpYnV0ZX" +
		"N0ABVMamF2YS91dGlsL0hhc2h0YWJsZTtMAA5wa2NzMTJPcmRlcmluZ3QAEkxq" +
		"YXZhL3V0aWwvVmVjdG9yO0wAD3ByaXZhdGVFeHBvbmVudHEAfgAFeHBzcgAUam" +
		"F2YS5tYXRoLkJpZ0ludGVnZXKM/J8fqTv7HQMABkkACGJpdENvdW50SQAJYml0" +
		"TGVuZ3RoSQATZmlyc3ROb256ZXJvQnl0ZU51bUkADGxvd2VzdFNldEJpdEkABn" +
		"NpZ251bVsACW1hZ25pdHVkZXQAAltCeHIAEGphdmEubGFuZy5OdW1iZXKGrJUd" +
		"C5TgiwIAAHhw///////////////+/////gAAAAF1cgACW0Ks8xf4BghU4AIAAH" +
		"hwAAAAQLSmbZOBOTNjFa6Sfxt4pgNtDBw/+7VLxDH1xXNcfkEzKKkpI5Ak7lsK" +
		"/zaFqwh9P1RaQJCCEhWa8lKuLlvA17F4c3IAE2phdmEudXRpbC5IYXNodGFibG" +
		"UTuw8lIUrkuAMAAkYACmxvYWRGYWN0b3JJAAl0aHJlc2hvbGR4cD9AAAAAAAAI" +
		"dwgAAAALAAAAAHhzcgAQamF2YS51dGlsLlZlY3RvctmXfVuAO68BAgADSQARY2" +
		"FwYWNpdHlJbmNyZW1lbnRJAAxlbGVtZW50Q291bnRbAAtlbGVtZW50RGF0YXQA" +
		"E1tMamF2YS9sYW5nL09iamVjdDt4cAAAAAAAAAAAdXIAE1tMamF2YS5sYW5nLk" +
		"9iamVjdDuQzlifEHMpbAIAAHhwAAAACnBwcHBwcHBwcHBzcQB+AAr/////////" +
		"//////7////+AAAAAXVxAH4ADgAAAECKJOphvSu94lvf2XBCLxWKJjZv9LFshT" +
		"utu/FYN6vXgDCDa2Iejdiys1FyAUDnG60wyl/7KfVGltva3hsxhUIheHNxAH4A" +
		"Cv///////////////v////4AAAABdXEAfgAOAAAAIBDAcV1FJWmKY5A6cg4lP/" +
		"lZwTW0jh3jA+Plt/SG6ajHeHNxAH4ACv///////////////v////4AAAABdXEA" +
		"fgAOAAAAILOkM7OtmlvSY4DVkTHEX9kPE3KkQPlTnMCAqs3R0pkpeHNxAH4ACv" +
		"///////////////v////4AAAABdXEAfgAOAAAAIHy1zqu6gDBCfsQuAeuOWyeS" +
		"nLw8bgXyjqHrqtoaBvRJeHNxAH4ACv///////////////v////4AAAABdXEAfg" +
		"AOAAAAINoi0REJqSZa5oon5yolT9C24MIQmAourA5TGIw1pExFeHNxAH4ACv//" +
		"/////////////v////4AAAABdXEAfgAOAAAAINQB31cjc4U914C0nN0+zimscN" +
		"mZ7j1PjBND1aX5C9J9eHNxAH4ACv///////////////v////4AAAABdXEAfgAO" +
		"AAAAARF4c3IALW9yZy5ib3VuY3ljYXN0bGUuamNlLnByb3ZpZGVyLkpDRVJTQV" +
		"B1YmxpY0tleSUiag5b+myEAgACTAAHbW9kdWx1c3EAfgAFTAAOcHVibGljRXhw" +
		"b25lbnRxAH4ABXhwcQB+AA1xAH4AIw==";
			
}
