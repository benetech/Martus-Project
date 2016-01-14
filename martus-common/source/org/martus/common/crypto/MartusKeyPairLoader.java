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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectStreamConstants;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPublicKeySpec;

public class MartusKeyPairLoader
{

	public static KeyPair load(DataInputStream in, SecurityContext securityContext) throws Exception
	{
		MartusKeyPairLoader loader = new MartusKeyPairLoader(securityContext);
		return loader.readKeyPair(in);
	}

	private MartusKeyPairLoader(SecurityContext securityContext)
	{
		this.providerAccessor = securityContext;
	}

	KeyPair readKeyPair(DataInputStream in) throws Exception
	{
		nextHandle = MartusKeyPairDataConstants.INITIAL_HANDLE;
		boolean privateSuperHasWriteObject = false;
		
		int magic = in.readShort();
		throwIfNotEqual(ObjectStreamConstants.STREAM_MAGIC, magic);
		int streamVersion = in.readShort();
		throwIfNotEqual(ObjectStreamConstants.STREAM_VERSION, streamVersion);
					
		// KeyPair
		{
			readObjectClassHeader(in, MartusKeyPairDataConstants.JAVA_SECURITY_KEY_PAIR_CLASS_NAME, MartusKeyPairDataConstants.KEY_PAIR_CLASS_UID);
			
			byte classDescFlags = in.readByte();
			throwIfNotEqual(ObjectStreamConstants.SC_SERIALIZABLE, classDescFlags);
			short fieldCount = in.readShort();
			
			
			throwIfNotEqual(MartusKeyPairDataConstants.KEY_PAIR_FIELD_NAMES.length, fieldCount);
			for(int field=0; field < fieldCount; ++field)
			{
				readObjectFieldDescription(in, MartusKeyPairDataConstants.KEY_PAIR_FIELD_CLASS_NAMES[field], MartusKeyPairDataConstants.KEY_PAIR_FIELD_NAMES[field]);
			}
			
			readClassFooter(in);
		}
		
		// JCERSAPrivateCrtKey
		{
			readObjectClassHeader(in, MartusKeyPairDataConstants.BCE_JCE_PROVIDER_JCERSAPRIVATE_CRT_KEY_CLASS_NAME, MartusKeyPairDataConstants.BCE_JCE_RSA_PRIVATE_KEY_CLASS_UID);
			
			byte classDescFlagsForPrivate = in.readByte();
			throwIfNotEqual(ObjectStreamConstants.SC_SERIALIZABLE, classDescFlagsForPrivate);
			short fieldCountForPrivate = in.readShort();
			
			throwIfNotEqual(MartusKeyPairDataConstants.PRIVATE_CRT_KEY_FIELD_NAMES.length, fieldCountForPrivate);

			bigIntStringHandle = readObjectFieldDescription(in, MartusKeyPairDataConstants.LJAVA_MATH_BIG_INTEGER_CLASS_NAME, MartusKeyPairDataConstants.PRIVATE_CRT_KEY_FIELD_NAMES[0]);
			for(int field=1; field < fieldCountForPrivate; ++field)
			{
				throwIfNotEqual(MartusKeyPairDataConstants.PRIVATE_CRT_KEY_FIELD_NAMES[field], readBigIntegerFieldReference(in));
			}
			
			byte endDataFlagForPrivate = in.readByte();
			throwIfNotEqual(ObjectStreamConstants.TC_ENDBLOCKDATA, endDataFlagForPrivate);
			
			// Super Class
			byte superClassFlagForPrivate = in.readByte();
			throwIfNotEqual(ObjectStreamConstants.TC_CLASSDESC, superClassFlagForPrivate);
			String classNameForPrivateSuper = in.readUTF();
			throwIfNotEqual(MartusKeyPairDataConstants.BCE_JCE_PROVIDER_JCERSAPRIVATE_KEY_CLASS_NAME, classNameForPrivateSuper);
			long uidForPrivateKeyClassSuper = in.readLong();
			throwIfNotEqual(MartusKeyPairDataConstants.PRIVATE_KEY_SUPER_CLASS_UID, uidForPrivateKeyClassSuper);
			// new handle
			nextHandle++;
			
			byte classDescFlagsForPrivateSuper = in.readByte();
			throwIfNotEqual(ObjectStreamConstants.SC_SERIALIZABLE, classDescFlagsForPrivateSuper & ObjectStreamConstants.SC_SERIALIZABLE);
			if(hasFlag(classDescFlagsForPrivateSuper, ObjectStreamConstants.SC_WRITE_METHOD))
				privateSuperHasWriteObject = true;
			short fieldCountForPrivateSuper = in.readShort();
			throwIfNotEqual(MartusKeyPairDataConstants.PRIVATE_KEY_FIELD_COUNT, fieldCountForPrivateSuper);
			
			// Private Key field 1
			throwIfNotEqual(MartusKeyPairDataConstants.MODULUS_FIELD_NAME, readBigIntegerFieldReference(in));
						
			// Private Key field 2
			readObjectFieldDescription(in, MartusKeyPairDataConstants.LJAVA_UTIL_HASHTABLE_CLASS_NAME, MartusKeyPairDataConstants.PKCS12ATTRIBUTES_FIELD_NAME);
			
			// Private Key field 3
			readObjectFieldDescription(in, MartusKeyPairDataConstants.LJAVA_UTIL_VECTOR_CLASS_NAME, MartusKeyPairDataConstants.PKCS12ORDERING_FIELD_NAME);
			
			// Private Key field 4
			throwIfNotEqual(MartusKeyPairDataConstants.PRIVATE_EXPONENT_FIELD_NAME, readBigIntegerFieldReference(in));
			
			readClassFooter(in);
		}			

		// BigInteger
		{
			bigIntClassHandle = readObjectClassHeader(in, MartusKeyPairDataConstants.JAVA_MATH_BIG_INTEGER_CLASS_NAME, MartusKeyPairDataConstants.BIG_INTEGER_CLASS_UID);
						
			byte classDescFlags = in.readByte();
			throwIfNotEqual(ObjectStreamConstants.SC_SERIALIZABLE | ObjectStreamConstants.SC_WRITE_METHOD, classDescFlags);
			short fieldCount = in.readShort();
			throwIfNotEqual(MartusKeyPairDataConstants.BIGINTEGER_FIELD_COUNT, fieldCount);
			// Big Integer field 1
			throwIfNotEqual(MartusKeyPairDataConstants.BIT_COUNT_FIELD_NAME, readIntFieldDescription(in));

			// Big Integer field 2
			throwIfNotEqual(MartusKeyPairDataConstants.BIT_LENGTH_FIELD_NAME, readIntFieldDescription(in));
			
			// Big Integer field 3
			throwIfNotEqual(MartusKeyPairDataConstants.FIRST_NONZERO_BYTE_NUM_FIELD_NAME, readIntFieldDescription(in));
			
			// Big Integer field 4
			throwIfNotEqual(MartusKeyPairDataConstants.LOWEST_SET_BIT_FIELD_NAME, readIntFieldDescription(in));
			
			// Big Integer field 5
			throwIfNotEqual(MartusKeyPairDataConstants.SIGNUM_FIELD_NAME, readIntFieldDescription(in));
			
			// Big Integer field 6
			throwIfNotEqual(MartusKeyPairDataConstants.MAGNITUDE_FIELD_NAME, readByteArrayFieldDescription(in));
			
			byte endDataFlag = in.readByte();
			throwIfNotEqual(ObjectStreamConstants.TC_ENDBLOCKDATA, endDataFlag);
			byte superClassFlag = in.readByte();
			throwIfNotEqual(ObjectStreamConstants.TC_CLASSDESC, superClassFlag);
			String superClassName = in.readUTF();
			throwIfNotEqual(MartusKeyPairDataConstants.JAVA_LANG_NUMBER_CLASS_NAME, superClassName);
			long superUid = in.readLong();
			throwIfNotEqual(MartusKeyPairDataConstants.LANG_NUMBER_CLASS_UID, superUid);
			// new handle
			nextHandle++;
			
			int superClassDescFlags = in.readByte();
			throwIfNotEqual(ObjectStreamConstants.SC_SERIALIZABLE, superClassDescFlags);
			int superFieldCount = in.readShort();
			throwIfNotEqual(MartusKeyPairDataConstants.BIGINTEGER_SUPER_CLASS_FIELD_COUNT, superFieldCount);
			
			modulusObjectHandle = readClassFooter(in);
			
			//BigInt Modulus Data
			{				
				int bitCount = in.readInt();
				throwIfNotEqual(MartusKeyPairDataConstants.BIGINTEGER_BIT_COUNT, bitCount);
				int bitLength = in.readInt();
				throwIfNotEqual(MartusKeyPairDataConstants.BIGINTEGER_BIT_LENGTH, bitLength);
				int firstNonZeroByteNum = in.readInt();
				throwIfNotEqual(MartusKeyPairDataConstants.BIGINTEGER_FIRST_NONZERO_BYTE_NUMBER, firstNonZeroByteNum);
				int lowestSetBit = in.readInt();
				throwIfNotEqual(MartusKeyPairDataConstants.BIGINTEGER_LOWEST_SET_BIT, lowestSetBit);
				int signum = in.readInt();
				throwIfNotEqual(MartusKeyPairDataConstants.BIGINTEGER_SIGNUM, signum);
								
				byteArrayClassHandle = readArrayClassHeader(in, MartusKeyPairDataConstants.BYTE_ARRAY_CLASS_NAME, MartusKeyPairDataConstants.ARRAY_CLASS_UID);
				
				byte magnitudeClassDescFlags = in.readByte();
				throwIfNotEqual(ObjectStreamConstants.SC_SERIALIZABLE, magnitudeClassDescFlags);
				//int superClassDescFlags = in.readByte();
				short magnitudeFieldCount = in.readShort();
				throwIfNotEqual(MartusKeyPairDataConstants.MAGNITUDE_FIELD_COUNT, magnitudeFieldCount);
				
				readClassFooter(in);
				
				int arrayLength = in.readInt();

				byte[] magnitude = new byte[arrayLength];
				in.read(magnitude);
				
				byte arrayEndDataFlag = in.readByte();
				throwIfNotEqual(ObjectStreamConstants.TC_ENDBLOCKDATA, arrayEndDataFlag);
				
				modulus = new BigInteger(signum, magnitude);
				
			}
			
			// Hashtable
			{
				readObjectClassHeader(in, MartusKeyPairDataConstants.JAVA_UTIL_HASHTABLE_CLASS_NAME, MartusKeyPairDataConstants.HASHTABLE_CLASS_UID);
				
				byte hashTableClassDescFlags = in.readByte();
				throwIfNotEqual(ObjectStreamConstants.SC_SERIALIZABLE | ObjectStreamConstants.SC_WRITE_METHOD, hashTableClassDescFlags);
				short hashTableFieldCount = in.readShort();
				throwIfNotEqual(MartusKeyPairDataConstants.HASHTABLE_FIELD_COUNT, hashTableFieldCount);
				
				// Hash Table field 1
				throwIfNotEqual(MartusKeyPairDataConstants.LOAD_FACTOR_FIELD_NAME, readFloatFieldDescription(in));
				
				// Hash Table field 2
				throwIfNotEqual(MartusKeyPairDataConstants.THRESHOLD_FIELD_NAME, readIntFieldDescription(in));

				readClassFooter(in);
				
				readEmptyHashTableData(in);
			}
			
			//Vector
			{
				readObjectClassHeader(in, MartusKeyPairDataConstants.JAVA_UTIL_VECTOR_CLASS_NAME, MartusKeyPairDataConstants.VECTOR_CLASS_UID);
				
				byte vectorClassDescFlags = in.readByte();
				throwIfNotEqual(ObjectStreamConstants.SC_SERIALIZABLE, vectorClassDescFlags & ObjectStreamConstants.SC_SERIALIZABLE);
				boolean vectorHasWriteObject = hasFlag(vectorClassDescFlags, ObjectStreamConstants.SC_WRITE_METHOD);
				short vectorFieldCount = in.readShort();
				throwIfNotEqual(MartusKeyPairDataConstants.VECTOR_FIELD_COUNT, vectorFieldCount);
				
				// Vector field 1
				throwIfNotEqual(MartusKeyPairDataConstants.CAPACITY_INCREMENT_FIELD_NAME, readIntFieldDescription(in));
				
				// Vector field 2
				throwIfNotEqual(MartusKeyPairDataConstants.ELEMENT_COUNT_FIELD_NAME, readIntFieldDescription(in));
				 
				// Vector field 3
				throwIfNotEqual(MartusKeyPairDataConstants.ELEMENT_DATA_FIELD_NAME, readArrayFieldDecription(in));
				
				readClassFooter(in);
				
				// Vector Field1 Data
				int capacityIncrement = in.readInt();
				throwIfNotEqual(MartusKeyPairDataConstants.VECTOR_CAPACITY_INCREMENT, capacityIncrement);
				
				//Vector Field2 Data
				int elementCount = in.readInt();
				throwIfNotEqual(MartusKeyPairDataConstants.VECTOR_ELEMENT_COUNT, elementCount);
				
				readArrayClassHeader(in, MartusKeyPairDataConstants.JAVA_LANG_OBJECT_CLASS_NAME, MartusKeyPairDataConstants.OBJECT_CLASS_UID);
				
				byte vectorField3DescFlags = in.readByte();
				throwIfNotEqual(ObjectStreamConstants.SC_SERIALIZABLE, vectorField3DescFlags);
				short vectorField3Count = in.readShort();
				throwIfNotEqual(MartusKeyPairDataConstants.VECTOR_FIELD_3_COUNT, vectorField3Count);
				
				readClassFooter(in);
				
				int arrayLength = in.readInt();
				for(int b = 0; b < arrayLength; ++b)
				{
					byte nullObjectMarker = in.readByte();
					throwIfNotEqual(ObjectStreamConstants.TC_NULL, nullObjectMarker);
				}
				
				if(vectorHasWriteObject)
				{
					int arrayEndDataFlag = in.readByte();
					throwIfNotEqual(ObjectStreamConstants.TC_ENDBLOCKDATA, arrayEndDataFlag);
				}
				
			}
			
			//BigInt privateExponent (Private Key Field4)  
			readBigIntegerObjectHeader(in);
			privateExponent = readBigIntegerData(in);
			
			if(privateSuperHasWriteObject)
			{
				int endOfWriteObjectData = in.readByte();
				throwIfNotEqual(ObjectStreamConstants.TC_ENDBLOCKDATA, endOfWriteObjectData);
			}
			
			// BigInt crtCoefficient (PrivateCRTKey Field 1)
			readBigIntegerObjectHeader(in);
			crtCoefficient = readBigIntegerData(in);

			// BigInt primeExponentP (PrivateCRTKey Field 2)
			readBigIntegerObjectHeader(in);			
			primeExponentP = readBigIntegerData(in);
			
			// BigInt primeExponentQ (PrivateCRTKey Field 3)
			readBigIntegerObjectHeader(in);			
			primeExponentQ = readBigIntegerData(in);
					
			// BigInt primeP (PrivateCRTKey Field4) 
			readBigIntegerObjectHeader(in);		
			primeP = readBigIntegerData(in);
			
			// BigInt primeQ (PrivateCRTKey Field5)
			readBigIntegerObjectHeader(in);		
			primeQ = readBigIntegerData(in);
			
			// BigInt publicExponent (PrivateCRTKey Field6)
			publicExponentObjectHandle = readBigIntegerObjectHeader(in);
			publicExponent = readBigIntegerData(in);				
			
			// Public Key Description
			{
				readObjectClassHeader(in, MartusKeyPairDataConstants.BCE_JCE_PROVIDER_JCERSAPUBLIC_KEY_CLASS_NAME, MartusKeyPairDataConstants.BCE_JCE_RSA_PUBLIC_KEY_CLASS_UID);
				
				int classDescFlagsForPublic = in.readByte();
				throwIfNotEqual(ObjectStreamConstants.SC_SERIALIZABLE, classDescFlagsForPublic);
				int fieldCountForPublic = in.readShort();
				throwIfNotEqual(MartusKeyPairDataConstants.PUBLIC_KEY_FIELD_COUNT, fieldCountForPublic);
				
				for(int i = 0; i < MartusKeyPairDataConstants.PUBLIC_KEY_FIELD_NAMES.length; ++i)
				{
					String fieldName = readBigIntegerFieldReference(in);
					throwIfNotEqual(MartusKeyPairDataConstants.PUBLIC_KEY_FIELD_NAMES[i], fieldName);
				}
				
				readClassFooter(in);
				
			}
			
			//Public Key Data
			{
				// BigInt modulus (Field 1) 
				int refModulusObjectHandle = readObjectReference(in);
				throwIfNotEqual(modulusObjectHandle, refModulusObjectHandle);

				//BigInt publicExponent Reference (Field 2)
				int refPublicExponentObjectHandle = readObjectReference(in);
				throwIfNotEqual(publicExponentObjectHandle, refPublicExponentObjectHandle);
			}
		}
		
		// Reconstitute Keypair
		RSAPublicKeySpec publicSpec = new RSAPublicKeySpec(modulus, publicExponent);
		RSAPrivateCrtKeySpec privateSpec = new RSAPrivateCrtKeySpec(modulus, publicExponent, privateExponent, primeP, primeQ, primeExponentP, primeExponentQ, crtCoefficient);
		KeyFactory factory = KeyFactory.getInstance("RSA",  providerAccessor.getSecurityProviderName());
		PublicKey publicKey = factory.generatePublic(publicSpec);
		PrivateKey privateCRTKey = factory.generatePrivate(privateSpec);
		
		KeyPair keyPair = new KeyPair(publicKey, privateCRTKey);
		return keyPair;
	}

	private boolean hasFlag(int variable, int flag)
	{
		return (variable & flag) == flag;
	}
	private int readClassFooter(DataInputStream in) throws IOException
	{
		byte superEndDataFlag = in.readByte();
		throwIfNotEqual(ObjectStreamConstants.TC_ENDBLOCKDATA, superEndDataFlag);
		byte superNoSuperFlag = in.readByte();
		throwIfNotEqual(ObjectStreamConstants.TC_NULL, superNoSuperFlag);
		// new handle		
		return nextHandle++;
	}

	private int readObjectClassHeader(DataInputStream in, String className, long serialUid) throws IOException
	{
		return readClassHeader(in, ObjectStreamConstants.TC_OBJECT, className, serialUid);
	}
	
	private int readArrayClassHeader(DataInputStream in, String className, long serialUid) throws IOException
	{
		return readClassHeader(in, ObjectStreamConstants.TC_ARRAY, className, serialUid);
	}

	private int readClassHeader(DataInputStream in, byte classType, String className, long serialUid) throws IOException
	{
		int objectForPublic = in.readByte();
		throwIfNotEqual(classType, objectForPublic);
		int classFlagForPublic = in.readByte();
		throwIfNotEqual(ObjectStreamConstants.TC_CLASSDESC, classFlagForPublic);
		String classNameForPublic = in.readUTF();
		throwIfNotEqual(className, classNameForPublic);
		long uidForPublicKeyClass = in.readLong();
		throwIfNotEqual(serialUid, uidForPublicKeyClass);
		// new handle
		return nextHandle++;
	}

	private int readObjectFieldDescription(DataInputStream in, String expectedClassName, String expectedFieldName) throws IOException
	{
		byte typeCode = in.readByte();
		throwIfNotEqual(MartusKeyPairDataConstants.FIELD_TYPE_CODE_OBJECT, typeCode);
		String fieldName = in.readUTF();
		throwIfNotEqual(expectedFieldName, fieldName);
		int refFlag = in.readByte();
		throwIfNotEqual(ObjectStreamConstants.TC_STRING, refFlag);
		String fieldClassName = in.readUTF();
		throwIfNotEqual(expectedClassName, fieldClassName);
		// new handle
		return nextHandle++;
	}

	private String readByteArrayFieldDescription(DataInputStream in) throws IOException
	{
		byte typeCode = in.readByte();
		throwIfNotEqual(MartusKeyPairDataConstants.FIELD_TYPE_CODE_ARRAY, typeCode);
		String fieldName = in.readUTF();
		int refFlag = in.readByte();
		throwIfNotEqual(ObjectStreamConstants.TC_STRING, refFlag);
		String fieldClassName = in.readUTF();
		throwIfNotEqual(MartusKeyPairDataConstants.BYTE_ARRAY_FIELD_NAME, fieldClassName);
		// new handle
		nextHandle++;
		return fieldName;
	}

	private String readFloatFieldDescription(DataInputStream in) throws IOException
	{
		byte typeCode = in.readByte();
		throwIfNotEqual(MartusKeyPairDataConstants.FIELD_TYPE_CODE_FLOAT, typeCode);
		String fieldName = in.readUTF();
		return fieldName;
	}

	private void readEmptyHashTableData(DataInputStream in) throws IOException
	{
		float loadFactor = in.readFloat();
		throwIfNotEqual("loadfactor wrong?", MartusKeyPairDataConstants.HASHTABLE_LOADFACTOR, loadFactor, MartusKeyPairDataConstants.HASHTABLE_LOADFACTOR/100);
		
		int threshold = in.readInt();
		throwIfNotEqual("threshold wrong?", MartusKeyPairDataConstants.HASHTABLE_THRESHOLD, threshold);
		
		byte hashTableBlockDataFlag = in.readByte();
		throwIfNotEqual(ObjectStreamConstants.TC_BLOCKDATA, hashTableBlockDataFlag);
		
		byte blockDataByteCount = in.readByte();
		throwIfNotEqual("wrong block data byte count?", MartusKeyPairDataConstants.HASHTABLE_BYTE_COUNT, blockDataByteCount);
		
		// originalLength
		in.readInt();
		
		int elements = in.readInt();
		throwIfNotEqual("Hashtable not empty?", MartusKeyPairDataConstants.HASHTABLE_NUMBER_OF_ELEMENTS, elements);
		
		byte hashTableEndDataFlag = in.readByte();
		throwIfNotEqual(ObjectStreamConstants.TC_ENDBLOCKDATA, hashTableEndDataFlag);
	}

	private String readArrayFieldDecription(DataInputStream in) throws IOException
	{
		byte typeCode = in.readByte();
		throwIfNotEqual(MartusKeyPairDataConstants.FIELD_TYPE_CODE_ARRAY, typeCode);
		String fieldName = in.readUTF();
		byte vecString = in.readByte();
		throwIfNotEqual(ObjectStreamConstants.TC_STRING, vecString);
		String fieldClassName = in.readUTF();
		throwIfNotEqual(MartusKeyPairDataConstants.LJAVA_LANG_OBJECT_CLASS_NAME, fieldClassName);
		nextHandle++;
		return fieldName;
	}

	private String readIntFieldDescription(DataInputStream in) throws IOException
	{
		byte typeCode = in.readByte();
		throwIfNotEqual(MartusKeyPairDataConstants.FIELD_TYPE_CODE_INTEGER, typeCode);
		String fieldName = in.readUTF();
		return fieldName;
	}

	private int readBigIntegerObjectHeader(DataInputStream in) throws IOException
	{
		int publicExponentObjectFlag = in.readByte();
		throwIfNotEqual(ObjectStreamConstants.TC_OBJECT, publicExponentObjectFlag);
		int refFlag = in.readByte();
		throwIfNotEqual(ObjectStreamConstants.TC_REFERENCE, refFlag);
		int refBigIntClassHandle = in.readInt();
		throwIfNotEqual(bigIntClassHandle, refBigIntClassHandle);
		int thisHandle = nextHandle++;
		return thisHandle;
	}

	private BigInteger readBigIntegerData(DataInputStream in) throws IOException
	{
		int bitCount = in.readInt();
		throwIfNotEqual(MartusKeyPairDataConstants.BIGINTEGER_BIT_COUNT, bitCount);
		int bitLength = in.readInt();
		throwIfNotEqual(MartusKeyPairDataConstants.BIGINTEGER_BIT_LENGTH, bitLength);
		int firstNonZeroByteNum = in.readInt();
		throwIfNotEqual(MartusKeyPairDataConstants.BIGINTEGER_FIRST_NONZERO_BYTE_NUMBER, firstNonZeroByteNum);
		int lowestSetBit = in.readInt();
		throwIfNotEqual(MartusKeyPairDataConstants.BIGINTEGER_LOWEST_SET_BIT, lowestSetBit);
		int signum = in.readInt();
		throwIfNotEqual(MartusKeyPairDataConstants.BIGINTEGER_SIGNUM, signum);

		byte typeCode = in.readByte();
		throwIfNotEqual(ObjectStreamConstants.TC_ARRAY, typeCode);
		byte typeCodeRefFlag = in.readByte();
		throwIfNotEqual(ObjectStreamConstants.TC_REFERENCE, typeCodeRefFlag);
		int refbyteArrayClassHandle = in.readInt();
		throwIfNotEqual(byteArrayClassHandle, refbyteArrayClassHandle);
		nextHandle++;
		
		int arrayLength = in.readInt();
		
		byte[] magnitude = new byte[arrayLength];
		in.read(magnitude);
		
		int arrayEndDataFlag = in.readByte();
		throwIfNotEqual(ObjectStreamConstants.TC_ENDBLOCKDATA, arrayEndDataFlag);

		BigInteger gotBigInteger = new BigInteger(signum, magnitude);
		return gotBigInteger;
	}

	private String readBigIntegerFieldReference(DataInputStream in) throws IOException
	{
		byte typeCode = in.readByte();
		String fieldName = in.readUTF();
		byte refFlag = in.readByte();
		int refBigIntStringHandle = in.readInt();
		throwIfNotEqual(MartusKeyPairDataConstants.FIELD_TYPE_CODE_OBJECT, typeCode);
		throwIfNotEqual(ObjectStreamConstants.TC_REFERENCE, refFlag);
		throwIfNotEqual(bigIntStringHandle, refBigIntStringHandle);
		return fieldName;
	}

	private int readObjectReference(DataInputStream in) throws IOException
	{
		int modulusRefFlag = in.readByte();
		throwIfNotEqual(ObjectStreamConstants.TC_REFERENCE, modulusRefFlag);
		int refModulusObjectHandle = in.readInt();
		return refModulusObjectHandle;
	}
	
	void throwIfNotEqual(String text, Object expected, Object actual)
	{
		if(!expected.equals(actual))
			throw new RuntimeException(text + "expected " + expected + " but was " + actual);
	}
	
	void throwIfNotEqual(Object expected, Object actual)
	{
		if(!expected.equals(actual))
			throw new RuntimeException("expected " + expected + " but was " + actual);
	}
	
	void throwIfNotEqual(String text, int expected, int actual)
	{
		if(expected != actual)
			throw new RuntimeException(text + "expected " + expected + " but was " + actual);
	}
	
	void throwIfNotEqual(long expected, long actual)
	{
		if(expected != actual)
			throw new RuntimeException("expected " + expected + " but was " + actual);
	}
	
	void throwIfNotEqual(String text, double expected, double actual, double tolerance)
	{
		if(expected < actual - tolerance || expected > actual + tolerance)
			throw new RuntimeException(text + "expected " + expected + " but was " + actual);
	}
	
	int nextHandle;	
	int bigIntStringHandle;
	int bigIntClassHandle;
	int byteArrayClassHandle;
	int modulusObjectHandle;
	int publicExponentObjectHandle;

	
	BigInteger modulus;
	BigInteger publicExponent;
	BigInteger crtCoefficient;
	BigInteger primeExponentP;
	BigInteger primeExponentQ;
	BigInteger primeP;
	BigInteger primeQ;
	BigInteger privateExponent;
	MartusKeyPair gotKeyPair;

	private SecurityContext providerAccessor;
}
