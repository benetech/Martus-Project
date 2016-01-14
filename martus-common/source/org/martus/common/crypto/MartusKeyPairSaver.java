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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectStreamConstants;
import java.math.BigInteger;
import java.security.KeyPair;

import java.security.interfaces.RSAPrivateCrtKey;

public class MartusKeyPairSaver
{
	public static void save(DataOutputStream out, KeyPair keyPairToSave) throws Exception
	{		
		MartusKeyPairSaver saver = new MartusKeyPairSaver();		
		saver.writeKeyPair(out, keyPairToSave);
	}

	private MartusKeyPairSaver()
	{	
	}
	
	void writeKeyPair(DataOutputStream out, KeyPair keyPair) throws Exception
	{
		RSAPrivateCrtKey privateKey = (RSAPrivateCrtKey) keyPair.getPrivate();
		nextHandle = MartusKeyPairDataConstants.INITIAL_HANDLE;
		
		out.writeShort(ObjectStreamConstants.STREAM_MAGIC);
		out.writeShort(ObjectStreamConstants.STREAM_VERSION);

		// KeyPair
		{
			writeObjectClassHeader(out, MartusKeyPairDataConstants.JAVA_SECURITY_KEY_PAIR_CLASS_NAME, MartusKeyPairDataConstants.KEY_PAIR_CLASS_UID);
			
			out.writeByte(ObjectStreamConstants.SC_SERIALIZABLE);
			out.writeShort(MartusKeyPairDataConstants.KEY_PAIR_FIELD_NAMES.length);

			for(int field=0; field < MartusKeyPairDataConstants.KEY_PAIR_FIELD_CLASS_NAMES.length; ++field)
			{
				writeObjectFieldDescription(out, MartusKeyPairDataConstants.KEY_PAIR_FIELD_CLASS_NAMES[field], MartusKeyPairDataConstants.KEY_PAIR_FIELD_NAMES[field]);
			}
			
			writeClassFooter(out);
		}
	
		// JCERSAPrivateCrtKey
		{
			writeObjectClassHeader(out, MartusKeyPairDataConstants.BCE_JCE_PROVIDER_JCERSAPRIVATE_CRT_KEY_CLASS_NAME, MartusKeyPairDataConstants.BCE_JCE_RSA_PRIVATE_KEY_CLASS_UID);

			out.writeByte(ObjectStreamConstants.SC_SERIALIZABLE);
			out.writeShort(MartusKeyPairDataConstants.PRIVATE_CRT_KEY_FIELD_NAMES.length);

			bigIntStringHandle = writeObjectFieldDescription(out, MartusKeyPairDataConstants.LJAVA_MATH_BIG_INTEGER_CLASS_NAME, MartusKeyPairDataConstants.PRIVATE_CRT_KEY_FIELD_NAMES[0]);
			for(int field=1; field < MartusKeyPairDataConstants.PRIVATE_CRT_KEY_FIELD_NAMES.length; ++field)
			{
				writeBigIntegerFieldReference(out, MartusKeyPairDataConstants.PRIVATE_CRT_KEY_FIELD_NAMES[field]);
			}
			
			out.writeByte(ObjectStreamConstants.TC_ENDBLOCKDATA);
			
			// Super Class
			out.writeByte(ObjectStreamConstants.TC_CLASSDESC);
			out.writeUTF(MartusKeyPairDataConstants.BCE_JCE_PROVIDER_JCERSAPRIVATE_KEY_CLASS_NAME);
			out.writeLong(MartusKeyPairDataConstants.PRIVATE_KEY_SUPER_CLASS_UID);
			// new handle
			nextHandle++;
			
			out.writeByte(ObjectStreamConstants.SC_SERIALIZABLE);
			out.writeShort(MartusKeyPairDataConstants.PRIVATE_KEY_FIELD_COUNT);

			// Private Key field 1
			writeBigIntegerFieldReference(out, MartusKeyPairDataConstants.MODULUS_FIELD_NAME);
						
			// Private Key field 2
			writeObjectFieldDescription(out, MartusKeyPairDataConstants.LJAVA_UTIL_HASHTABLE_CLASS_NAME, MartusKeyPairDataConstants.PKCS12ATTRIBUTES_FIELD_NAME);
			
			// Private Key field 3
			writeObjectFieldDescription(out, MartusKeyPairDataConstants.LJAVA_UTIL_VECTOR_CLASS_NAME, MartusKeyPairDataConstants.PKCS12ORDERING_FIELD_NAME);
			
			// Private Key field 4
			writeBigIntegerFieldReference(out, MartusKeyPairDataConstants.PRIVATE_EXPONENT_FIELD_NAME);
			writeClassFooter(out);
		}			

		// BigInteger
		{
			bigIntClassHandle = writeObjectClassHeader(out, MartusKeyPairDataConstants.JAVA_MATH_BIG_INTEGER_CLASS_NAME, MartusKeyPairDataConstants.BIG_INTEGER_CLASS_UID);
						
			out.writeByte(ObjectStreamConstants.SC_SERIALIZABLE | ObjectStreamConstants.SC_WRITE_METHOD);
			out.writeShort(MartusKeyPairDataConstants.BIGINTEGER_FIELD_COUNT);

			// Big Integer field 1
			writeIntFieldDescription(out, MartusKeyPairDataConstants.BIT_COUNT_FIELD_NAME);

			// Big Integer field 2
			writeIntFieldDescription(out, MartusKeyPairDataConstants.BIT_LENGTH_FIELD_NAME);
			
			// Big Integer field 3
			writeIntFieldDescription(out, MartusKeyPairDataConstants.FIRST_NONZERO_BYTE_NUM_FIELD_NAME);

			// Big Integer field 4
			writeIntFieldDescription(out, MartusKeyPairDataConstants.LOWEST_SET_BIT_FIELD_NAME);
			
			// Big Integer field 5
			writeIntFieldDescription(out, MartusKeyPairDataConstants.SIGNUM_FIELD_NAME);
			
			// Big Integer field 6
			writeByteArrayFieldDescription(out, MartusKeyPairDataConstants.MAGNITUDE_FIELD_NAME);

			out.writeByte(ObjectStreamConstants.TC_ENDBLOCKDATA);
			out.writeByte(ObjectStreamConstants.TC_CLASSDESC);
			out.writeUTF(MartusKeyPairDataConstants.JAVA_LANG_NUMBER_CLASS_NAME);
			out.writeLong(MartusKeyPairDataConstants.LANG_NUMBER_CLASS_UID);
			// new handle
			nextHandle++;
			
			out.writeByte(ObjectStreamConstants.SC_SERIALIZABLE);
			out.writeShort(MartusKeyPairDataConstants.BIGINTEGER_SUPER_CLASS_FIELD_COUNT);
			
			modulusObjectHandle = writeClassFooter(out);
			
			//BigInt Modulus Data
			{
				out.writeInt(MartusKeyPairDataConstants.BIGINTEGER_BIT_COUNT);
				out.writeInt(MartusKeyPairDataConstants.BIGINTEGER_BIT_LENGTH);
				out.writeInt(MartusKeyPairDataConstants.BIGINTEGER_FIRST_NONZERO_BYTE_NUMBER);
				out.writeInt(MartusKeyPairDataConstants.BIGINTEGER_LOWEST_SET_BIT);
				out.writeInt(MartusKeyPairDataConstants.BIGINTEGER_SIGNUM);
				
				byteArrayClassHandle = writeArrayClassHeader(out, MartusKeyPairDataConstants.BYTE_ARRAY_CLASS_NAME, MartusKeyPairDataConstants.ARRAY_CLASS_UID);
				out.writeByte(ObjectStreamConstants.SC_SERIALIZABLE);
				out.writeShort(MartusKeyPairDataConstants.MAGNITUDE_FIELD_COUNT);		
				
				writeClassFooter(out);
				
				byte[] magnitude = privateKey.getModulus().toByteArray();
				
				out.writeInt(magnitude.length);
				out.write(magnitude);
				out.writeByte(ObjectStreamConstants.TC_ENDBLOCKDATA);				
			}
			
			// Hashtable
			{
				writeObjectClassHeader(out, MartusKeyPairDataConstants.JAVA_UTIL_HASHTABLE_CLASS_NAME, MartusKeyPairDataConstants.HASHTABLE_CLASS_UID);
				
				out.writeByte(ObjectStreamConstants.SC_SERIALIZABLE | ObjectStreamConstants.SC_WRITE_METHOD);
				out.writeShort(MartusKeyPairDataConstants.HASHTABLE_FIELD_COUNT);
				
				// Hash Table field 1
				writeFloatFieldDescription(out, MartusKeyPairDataConstants.LOAD_FACTOR_FIELD_NAME);
				
				// Hash Table field 2
				writeIntFieldDescription(out, MartusKeyPairDataConstants.THRESHOLD_FIELD_NAME);
				writeClassFooter(out);
				
				writeEmptyHashTableData(out);
			}
			
			//Vector
			{
				writeObjectClassHeader(out, MartusKeyPairDataConstants.JAVA_UTIL_VECTOR_CLASS_NAME, MartusKeyPairDataConstants.VECTOR_CLASS_UID);
				
				out.writeByte(ObjectStreamConstants.SC_SERIALIZABLE);
				out.writeShort(MartusKeyPairDataConstants.VECTOR_FIELD_COUNT);
				
				// Vector field 1
				writeIntFieldDescription(out, MartusKeyPairDataConstants.CAPACITY_INCREMENT_FIELD_NAME);
				
				// Vector field 2
				writeIntFieldDescription(out, MartusKeyPairDataConstants.ELEMENT_COUNT_FIELD_NAME);
				
				// Vector field 3
				writeArrayFieldDecription(out, MartusKeyPairDataConstants.ELEMENT_DATA_FIELD_NAME);
				
				writeClassFooter(out);
				
				// Vector Field1 Data
				out.writeInt(MartusKeyPairDataConstants.VECTOR_CAPACITY_INCREMENT);
				
				//Vector Field2 Data
				out.writeInt(MartusKeyPairDataConstants.VECTOR_ELEMENT_COUNT);
				
				writeArrayClassHeader(out, MartusKeyPairDataConstants.JAVA_LANG_OBJECT_CLASS_NAME, MartusKeyPairDataConstants.OBJECT_CLASS_UID);
				
				out.writeByte(ObjectStreamConstants.SC_SERIALIZABLE);
				out.writeShort(MartusKeyPairDataConstants.VECTOR_FIELD_3_COUNT);
				
				writeClassFooter(out);
				
				out.writeInt(MartusKeyPairDataConstants.VECTOR_ARRAY_LENGTH);
				for(int b = 0; b < MartusKeyPairDataConstants.VECTOR_ARRAY_LENGTH; ++b)
				{
					out.writeByte(ObjectStreamConstants.TC_NULL);
				}
				
			}
			
			//BigInt privateExponent (Private Key Field4)
			writeBigIntegerObjectHeader(out);
			
			writeBigIntegerData(out, privateKey.getPrivateExponent());

			// BigInt crtCoefficient (PrivateCRTKey Field 1)
			writeBigIntegerObjectHeader(out);
			writeBigIntegerData(out, privateKey.getCrtCoefficient());
			
			// BigInt primeExponentP (PrivateCRTKey Field 2)
			writeBigIntegerObjectHeader(out);		
			writeBigIntegerData(out, privateKey.getPrimeExponentP());
			
			// BigInt primeExponentQ (PrivateCRTKey Field 3)
			writeBigIntegerObjectHeader(out);		
			writeBigIntegerData(out, privateKey.getPrimeExponentQ());

			// BigInt primeP (PrivateCRTKey Field4) 
			writeBigIntegerObjectHeader(out);		
			writeBigIntegerData(out, privateKey.getPrimeP());
			
			// BigInt primeQ (PrivateCRTKey Field5)
			writeBigIntegerObjectHeader(out);		
			writeBigIntegerData(out, privateKey.getPrimeQ());
			
			// BigInt publicExponent (PrivateCRTKey Field6)
			publicExponentObjectHandle = writeBigIntegerObjectHeader(out);		
			writeBigIntegerData(out, privateKey.getPublicExponent());	
			
			// Public Key Description
			{
				writeObjectClassHeader(out, MartusKeyPairDataConstants.BCE_JCE_PROVIDER_JCERSAPUBLIC_KEY_CLASS_NAME, MartusKeyPairDataConstants.BCE_JCE_RSA_PUBLIC_KEY_CLASS_UID);
				
				out.writeByte(ObjectStreamConstants.SC_SERIALIZABLE);
				out.writeShort(MartusKeyPairDataConstants.PUBLIC_KEY_FIELD_COUNT);

				for(int i = 0; i < MartusKeyPairDataConstants.PUBLIC_KEY_FIELD_NAMES.length; ++i)
				{
					writeBigIntegerFieldReference(out, MartusKeyPairDataConstants.PUBLIC_KEY_FIELD_NAMES[i]);
				}
				
				writeClassFooter(out);
				
			}
			
			//Public Key Data
			{
				// BigInt modulus (Field 1) 
				writeObjectReference(out, modulusObjectHandle);
				
				//BigInt publicExponent Reference (Field 2)
				writeObjectReference(out, publicExponentObjectHandle);
			}
		}
		
	}

		private int writeClassFooter(DataOutputStream out) throws IOException
		{
			out.writeByte(ObjectStreamConstants.TC_ENDBLOCKDATA);
			out.writeByte(ObjectStreamConstants.TC_NULL);
			// new handle		
			return nextHandle++;
		}

		private int writeObjectClassHeader(DataOutputStream out, String className, long serialUid) throws IOException
		{
			return writeClassHeader(out, ObjectStreamConstants.TC_OBJECT, className, serialUid);
		}
		
		private int writeArrayClassHeader(DataOutputStream out, String className, long serialUid) throws IOException
		{
			return writeClassHeader(out, ObjectStreamConstants.TC_ARRAY, className, serialUid);
		}

		private int writeClassHeader(DataOutputStream out, byte classType, String className, long serialUid) throws IOException
		{
			out.writeByte(classType);
//			throwIfNotEqual(classType, objectForPublic);
			out.writeByte(ObjectStreamConstants.TC_CLASSDESC);
			out.writeUTF(className);
			out.writeLong(serialUid);
			// new handle
			return nextHandle++;
		}

		private int writeObjectFieldDescription(DataOutputStream out, String expectedClassName, String expectedFieldName) throws IOException
		{
			out.writeByte(MartusKeyPairDataConstants.FIELD_TYPE_CODE_OBJECT);
			out.writeUTF(expectedFieldName);
			out.writeByte(ObjectStreamConstants.TC_STRING);
			out.writeUTF(expectedClassName);
			// new handle
			return nextHandle++;
		}

		private void writeByteArrayFieldDescription(DataOutputStream out, String fieldName) throws IOException
		{
			out.writeByte(MartusKeyPairDataConstants.FIELD_TYPE_CODE_ARRAY);
			out.writeUTF(fieldName);
			out.writeByte(ObjectStreamConstants.TC_STRING);
			out.writeUTF(MartusKeyPairDataConstants.BYTE_ARRAY_FIELD_NAME);
			// new handle
			nextHandle++;
		}

		private void writeFloatFieldDescription(DataOutputStream out, String fieldName) throws IOException
		{
			out.writeByte(MartusKeyPairDataConstants.FIELD_TYPE_CODE_FLOAT);
			out.writeUTF(fieldName);
		}

		private void writeEmptyHashTableData(DataOutputStream out) throws IOException
		{
			out.writeFloat(MartusKeyPairDataConstants.HASHTABLE_LOADFACTOR);
			out.writeInt(MartusKeyPairDataConstants.HASHTABLE_THRESHOLD);
			out.writeByte(ObjectStreamConstants.TC_BLOCKDATA);
			out.writeByte(MartusKeyPairDataConstants.HASHTABLE_BYTE_COUNT);
			
			// originalLength
			out.writeInt(0);
			
			out.writeInt(MartusKeyPairDataConstants.HASHTABLE_NUMBER_OF_ELEMENTS);
			out.writeByte(ObjectStreamConstants.TC_ENDBLOCKDATA);
		}

		private void writeArrayFieldDecription(DataOutputStream out, String fieldName) throws IOException
		{
			out.writeByte(MartusKeyPairDataConstants.FIELD_TYPE_CODE_ARRAY);
			out.writeUTF(fieldName);
			out.writeByte(ObjectStreamConstants.TC_STRING);
			out.writeUTF(MartusKeyPairDataConstants.LJAVA_LANG_OBJECT_CLASS_NAME);
			nextHandle++;
		}

		private void writeIntFieldDescription(DataOutputStream out, String fieldName) throws IOException
		{
			out.writeByte(MartusKeyPairDataConstants.FIELD_TYPE_CODE_INTEGER);
			out.writeUTF(fieldName);
		}

		private int writeBigIntegerObjectHeader(DataOutputStream out) throws IOException
		{
			out.writeByte(ObjectStreamConstants.TC_OBJECT);
			out.writeByte(ObjectStreamConstants.TC_REFERENCE);
			out.writeInt(bigIntClassHandle);
			int thisHandle = nextHandle++;
			return thisHandle;
		}

		private void writeBigIntegerData(DataOutputStream out, BigInteger bigint) throws IOException
		{
			out.writeInt(MartusKeyPairDataConstants.BIGINTEGER_BIT_COUNT);
			out.writeInt(MartusKeyPairDataConstants.BIGINTEGER_BIT_LENGTH);
			out.writeInt(MartusKeyPairDataConstants.BIGINTEGER_FIRST_NONZERO_BYTE_NUMBER);
			out.writeInt(MartusKeyPairDataConstants.BIGINTEGER_LOWEST_SET_BIT);
			out.writeInt(MartusKeyPairDataConstants.BIGINTEGER_SIGNUM);
			
			out.writeByte(ObjectStreamConstants.TC_ARRAY);
			out.writeByte(ObjectStreamConstants.TC_REFERENCE);
			out.writeInt(byteArrayClassHandle);
			nextHandle++;
		
			byte[] magnitude = bigint.toByteArray();
			
			out.writeInt(magnitude.length);
			out.write(magnitude);
			out.writeByte(ObjectStreamConstants.TC_ENDBLOCKDATA);	
			
		}

		private void writeBigIntegerFieldReference(DataOutputStream out, String fieldName) throws IOException
		{
			out.writeByte(MartusKeyPairDataConstants.FIELD_TYPE_CODE_OBJECT);
			out.writeUTF(fieldName);
			out.writeByte(ObjectStreamConstants.TC_REFERENCE);
			out.writeInt(bigIntStringHandle);
		}

		private void writeObjectReference(DataOutputStream out, int objectRefHandle) throws IOException
		{
			out.writeByte(ObjectStreamConstants.TC_REFERENCE);
			out.writeInt(objectRefHandle);
		}
		
		int nextHandle;	
		int bigIntStringHandle;
		int bigIntClassHandle;
		int modulusObjectHandle;
		int byteArrayClassHandle;
		int publicExponentObjectHandle;
}
