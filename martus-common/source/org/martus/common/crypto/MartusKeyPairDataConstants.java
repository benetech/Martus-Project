/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006-2007, Beneficent
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

public class MartusKeyPairDataConstants
{

	public static final int INITIAL_HANDLE = 8257536;
	
	public static final String JAVA_LANG_OBJECT_CLASS_NAME = "[Ljava.lang.Object;";
	public static final String JAVA_SECURITY_KEY_PAIR_CLASS_NAME = "java.security.KeyPair";
	public static final String JAVA_UTIL_VECTOR_CLASS_NAME = "java.util.Vector";
	public static final String JAVA_UTIL_HASHTABLE_CLASS_NAME = "java.util.Hashtable";
	public static final String JAVA_LANG_NUMBER_CLASS_NAME = "java.lang.Number";
	public static final String JAVA_MATH_BIG_INTEGER_CLASS_NAME = "java.math.BigInteger";
	public static final String LJAVA_LANG_OBJECT_CLASS_NAME = "[Ljava/lang/Object;";
	public static final String LJAVA_UTIL_VECTOR_CLASS_NAME = "Ljava/util/Vector;";
	public static final String LJAVA_UTIL_HASHTABLE_CLASS_NAME = "Ljava/util/Hashtable;";
	public static final String LJAVA_MATH_BIG_INTEGER_CLASS_NAME = "Ljava/math/BigInteger;";
	public static final String LJAVA_SECURITY_PUBLIC_KEY_CLASS_NAME = "Ljava/security/PublicKey;";
	public static final String LJAVA_SECURITY_PRIVATE_KEY_CLASS_NAME = "Ljava/security/PrivateKey;";
	public static final String BCE_JCE_PROVIDER_JCERSAPRIVATE_CRT_KEY_CLASS_NAME = "org.bouncycastle.jce.provider.JCERSAPrivateCrtKey";
	public static final String BCE_JCE_PROVIDER_JCERSAPUBLIC_KEY_CLASS_NAME = "org.bouncycastle.jce.provider.JCERSAPublicKey";
	public static final String BCE_JCE_PROVIDER_JCERSAPRIVATE_KEY_CLASS_NAME = "org.bouncycastle.jce.provider.JCERSAPrivateKey";
	public static final String BYTE_ARRAY_CLASS_NAME = "[B";
	
	public static final long BCE_JCE_RSA_PUBLIC_KEY_CLASS_UID = 2675817738516720772L;
	public static final long BCE_JCE_RSA_PRIVATE_KEY_CLASS_UID = 7834723820638524718L;
	public static final long OBJECT_CLASS_UID = -8012369246846506644L;
	public static final long VECTOR_CLASS_UID = -2767605614048989439L;
	public static final long HASHTABLE_CLASS_UID = 1421746759512286392L;
	public static final long ARRAY_CLASS_UID = -5984413125824719648L;
	public static final long BIG_INTEGER_CLASS_UID = -8287574255936472291L;
	public static final long KEY_PAIR_CLASS_UID = -7565189502268009837L;
	public static final long LANG_NUMBER_CLASS_UID = -8742448824652078965L;
	public static final long PRIVATE_KEY_SUPER_CLASS_UID = -5605421053708761770L;
	
	public static final String ELEMENT_DATA_FIELD_NAME = "elementData";
	public static final String ELEMENT_COUNT_FIELD_NAME = "elementCount";
	public static final String CAPACITY_INCREMENT_FIELD_NAME = "capacityIncrement";
	public static final String THRESHOLD_FIELD_NAME = "threshold";
	public static final String LOAD_FACTOR_FIELD_NAME = "loadFactor";
	public static final String MAGNITUDE_FIELD_NAME = "magnitude";
	public static final String SIGNUM_FIELD_NAME = "signum";
	public static final String LOWEST_SET_BIT_FIELD_NAME = "lowestSetBit";
	public static final String FIRST_NONZERO_BYTE_NUM_FIELD_NAME = "firstNonzeroByteNum";
	public static final String BIT_LENGTH_FIELD_NAME = "bitLength";
	public static final String BIT_COUNT_FIELD_NAME = "bitCount";
	public static final String MODULUS_FIELD_NAME = "modulus";
	public static final String PKCS12ATTRIBUTES_FIELD_NAME = "pkcs12Attributes";
	public static final String PKCS12ORDERING_FIELD_NAME = "pkcs12Ordering";
	public static final String PRIVATE_EXPONENT_FIELD_NAME = "privateExponent";
	public static final String PUBLIC_EXPONENT_FIELD_NAME = "publicExponent";
	public static final String PRIME_Q_FIELD_NAME = "primeQ";
	public static final String PRIME_P_FIELD_NAME = "primeP";
	public static final String PRIME_EXPONENT_Q_FIELD_NAME = "primeExponentQ";
	public static final String PRIME_EXPONENT_P_FIELD_NAME = "primeExponentP";
	public static final String CRT_COEFFICIENT_FIELD_NAME = "crtCoefficient";
	public static final String PUBLIC_KEY_FIELD_NAME = "publicKey";
	public static final String PRIVATE_KEY_FIELD_NAME = "privateKey";
	public static final String BYTE_ARRAY_FIELD_NAME = "[B";
	
	public static final char FIELD_TYPE_CODE_INTEGER = 'I';
	public static final char FIELD_TYPE_CODE_FLOAT = 'F';
	public static final char FIELD_TYPE_CODE_ARRAY = '[';
	public static final char FIELD_TYPE_CODE_OBJECT = 'L';
	
	public static final int BIGINTEGER_SIGNUM = 1;
	public static final int BIGINTEGER_LOWEST_SET_BIT = -2;
	public static final int BIGINTEGER_FIRST_NONZERO_BYTE_NUMBER = -2;
	public static final int BIGINTEGER_BIT_LENGTH = -1;
	public static final int BIGINTEGER_BIT_COUNT = -1;
	
	public static final int HASHTABLE_NUMBER_OF_ELEMENTS = 0;
	public static final int HASHTABLE_BYTE_COUNT = 8;
	public static final int HASHTABLE_THRESHOLD = 8;
	public static final float HASHTABLE_LOADFACTOR = 0.75f;
	
	public static final int VECTOR_ELEMENT_COUNT = 0;
	public static final int VECTOR_CAPACITY_INCREMENT = 0;
	public static final int VECTOR_ARRAY_LENGTH=10;

	public static final int VECTOR_FIELD_COUNT = 3;
	public static final int VECTOR_FIELD_3_COUNT = 0;
	public static final int HASHTABLE_FIELD_COUNT = 2;
	public static final int MAGNITUDE_FIELD_COUNT = 0;
	public static final int BIGINTEGER_FIELD_COUNT = 6;
	public static final int BIGINTEGER_SUPER_CLASS_FIELD_COUNT = 0;
	public static final int PRIVATE_KEY_FIELD_COUNT = 4;
	public static final int PUBLIC_KEY_FIELD_COUNT = 2;

	public final static String[] PRIVATE_CRT_KEY_FIELD_NAMES = {
	CRT_COEFFICIENT_FIELD_NAME, PRIME_EXPONENT_P_FIELD_NAME, PRIME_EXPONENT_Q_FIELD_NAME,
	PRIME_P_FIELD_NAME, PRIME_Q_FIELD_NAME, PUBLIC_EXPONENT_FIELD_NAME};

	public static final String[] PUBLIC_KEY_FIELD_NAMES = {MODULUS_FIELD_NAME, PUBLIC_EXPONENT_FIELD_NAME};
	public static final String[] KEY_PAIR_FIELD_NAMES = {PRIVATE_KEY_FIELD_NAME, PUBLIC_KEY_FIELD_NAME};
	public final static String[] KEY_PAIR_FIELD_CLASS_NAMES = {LJAVA_SECURITY_PRIVATE_KEY_CLASS_NAME, LJAVA_SECURITY_PUBLIC_KEY_CLASS_NAME};


}
