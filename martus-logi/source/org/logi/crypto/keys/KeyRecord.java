// Copyright (C) 1998-2000 Logi Ragnarsson

package org.logi.crypto.keys;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

import org.logi.crypto.Crypto;
import org.logi.crypto.InvalidCDSException;
import org.logi.crypto.sign.Fingerprint;
import org.logi.crypto.sign.HashState;
import org.logi.crypto.sign.SHA1State;

/** This class holds a particular key, linking it
 * to the owner's name and e-mail and annotation.
 * <p>
 * The CDS for a KeyRecord object is <code>KeyRecord(key,ownerName,ownerMail,notes)
 * </code>where <code>key</code> is a CDF for a <code>Key</code> object and
 * the other parameters are strings, which may be quoted.
 * <p>
 * Note that the key-certificate system is incomplete and is very likely to
 * change drastically.
 *
 * @see org.logi.crypto.keys.Key
 * @see org.logi.crypto.keys.KeySource
 *
 * @author <a href="http://www.logi.org/~logir/">Logi Ragnarsson</a>
 * (<a href="mailto:logir@logi.org">logir@logi.org</a>)
 */ 
public class KeyRecord extends Crypto {
    
    private String ownerName;
    private String ownerMail;
    private String notes;
    
    private Key key;
    
    Vector signatures;
    
    /** 
     * Create a new KeyRecord. It contains <code>key</code> which supposedly
     * belongs to <code>ownerName</code> who has e-mail adress
     * <code>ownerMail</code>. Additional notes are taken from
     * <code>notes</code>. 
     */
    public KeyRecord(Key key, String ownerName, String ownerMail, String notes){
        this.ownerName = ownerName;
        this.ownerMail = ownerMail;
        this.notes = notes;
        this.key = key;
    }
    
   /**
    * Used by Crypto.fromString when parsing a CDS.<p>

    * A valid CDS can be created by calling the toString() method.

    * @exception InvalidCDSException if the CDS is malformed.
    * @see org.logi.crypto.Crypto#fromString(String)
    */
   public static KeyRecord parseCDS(String[] param) throws InvalidCDSException{
      if(param.length!=4)
	throw new InvalidCDSException("invalid number of parameters in the CDS KeyRecord(key,ownerName,ownerMail,notes)");
      Object k = fromString(param[0]);
      if(!(k instanceof Key))
	throw new InvalidCDSException("CDS for a Key object expected as first argument to KeyRecord()");
      return new KeyRecord((Key)k, param[1], param[2], param[3]);
   }
    
    /** Return the key from this record. */
    public Key getKey(){
        return key;
    }
    
    /** Return the name of the key's owner. */
    public String getOwnerName(){
        return ownerName;
    }
    
    /** Return the e-mail address of the key's owner. */
    public String getOwnerMail(){
        return ownerMail;
    }
    
    /** Return the notes about this key. */
    public String getNotes(){
        return notes;
    }
    
    /**
     * Return the SHA1 fingerprint of this KeyRecord. Signing this is
     * equivalent to signing the record.
     */
    public Fingerprint getFingerprint(){
        byte[] empty={ 0,0,0,0 };
        HashState fs=new SHA1State();
        fs.update(key.getFingerprint().getBytes());
        fs.update(empty);
        fs.update(ownerName);
        fs.update(empty);
        fs.update(ownerMail);
        fs.update(empty);
        fs.update(notes);
        return fs.calculate();
    }
    
    /** Return a CDS for this KeyRecord. */
    public String toString(){
        StringBuffer sb=new StringBuffer();
        sb.append("KeyRecord(");
        sb.append(key);
        sb.append(",\"");
        sb.append(ownerName);
        sb.append("\",\"");
        sb.append(ownerMail);
        sb.append("\",\"");
        sb.append(notes);
        sb.append("\")");
        return sb.toString();
    }

   /**
    * Print this object to out, indented with ind tabs, going down at most
    * rec levels of recursion. */
   public void prettyPrint(PrintWriter out, int ind, int rec) throws IOException {
      if(rec<0)
	return;
      for(int i=0; i<ind; i++)
	out.print('\t');
      out.println("KeyRecord(");

      key.prettyPrint(out, ind+1, rec-1);
      out.println(",");
      
      for(int i=0; i<=ind; i++)
	out.print('\t');
      out.print(ownerName);
      out.println(",");
      
      for(int i=0; i<=ind; i++)
	out.print('\t');
      out.print(ownerMail);
      out.println(",");
      
      for(int i=0; i<=ind; i++)
	out.print('\t');
      out.println(notes);

      for(int i=0; i<ind; i++)
	out.print('\t');
      out.print(")");
    }
    
}
