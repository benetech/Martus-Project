package org.martus.common.crypto;

/**
 * @author roms
 *         Date: 6/17/13
 */
public class MobileMartusSecurity extends MartusSecurity
{
	public MobileMartusSecurity() throws CryptoInitializationException
	{
		super(new MobileSecurityContext());
	}
}
