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
package org.martus.client.core;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;

import org.martus.client.search.SaneCollator;
import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.fieldspec.MiniFieldSpec;
import org.martus.common.packet.UniversalId;

public class SortableBulletinList
{
	public SortableBulletinList(MiniLocalization localizationToUse, MiniFieldSpec[] specsForSorting, MiniFieldSpec[] extraSpecsToUse)
	{
		localization = localizationToUse;
		sortSpecs = specsForSorting;
		specsToStore = new MiniFieldSpec[sortSpecs.length + extraSpecsToUse.length];
		System.arraycopy(sortSpecs, 0, specsToStore, 0, sortSpecs.length);
		System.arraycopy(extraSpecsToUse, 0, specsToStore, sortSpecs.length, extraSpecsToUse.length);
		sorter = new PartialBulletinSorter(localization.getCurrentLanguageCode(), sortSpecs);
		partialBulletins = new HashSet();
		
	}
	
	public SortableBulletinList(MiniLocalization localizationToUse, MiniFieldSpec[] specsForSorting)
	{
		this(localizationToUse, specsForSorting, new MiniFieldSpec[0]);
	}
	
	public void add(Bulletin b)
	{
		SafeReadableBulletin readableBulletin = new SafeReadableBulletin(b, localization);
		PartialBulletin pb = new PartialBulletin(readableBulletin, specsToStore);
		add(pb);
	}

	public void add(PartialBulletin pb)
	{
		partialBulletins.add(pb);
	}
	
	public void remove(PartialBulletin pb)
	{
		partialBulletins.remove(pb);
	}
	
	public int size()
	{
		return partialBulletins.size();
	}
	
	public MiniFieldSpec[] getSortSpecs()
	{
		return sortSpecs;
	}
	
	public PartialBulletin[] getUnsortedPartialBulletins()
	{
		return (PartialBulletin[])partialBulletins.toArray(new PartialBulletin[0]);
	}
	
	public UniversalId[] getUniversalIds()
	{
		UniversalId[] uids = new UniversalId[size()];
		Iterator iter = partialBulletins.iterator();
		int next = 0;
		while(iter.hasNext())
		{
			PartialBulletin pb = (PartialBulletin)iter.next();
			uids[next++] = pb.getUniversalId();
		}
		
		return uids;
	}
	
	public UniversalId[] getSortedUniversalIds()
	{
		PartialBulletin[] bulletins = getSortedPartialBulletins();
		UniversalId[] uids = new UniversalId[bulletins.length];
		for(int i = 0; i < bulletins.length; ++i)
			uids[i] = bulletins[i].getUniversalId();
		
		return uids;
	}

	public PartialBulletin[] getSortedPartialBulletins()
	{
		PartialBulletin[] bulletins = (PartialBulletin[])partialBulletins.toArray(new PartialBulletin[0]);
		Arrays.sort(bulletins, sorter);
		return bulletins;
	}
	
	static class PartialBulletinSorter implements Comparator
	{
		public PartialBulletinSorter(String languageCode, MiniFieldSpec[] specsToSortBy)
		{
			specs = specsToSortBy;
			collator = new SaneCollator(languageCode);
		}
		
		public int compare(Object o1, Object o2)
		{
			PartialBulletin pb1 = (PartialBulletin)o1;
			PartialBulletin pb2 = (PartialBulletin)o2;
			for(int i = 0; i < specs.length; ++i)
			{
				String s1 = pb1.getData(specs[i].getTag());
				String s2 = pb2.getData(specs[i].getTag());
				if(s1 == null && s2 == null)
					return 0;
				if(s1 == null)
					return -1;
				if(s2 == null)
					return 1;
				int result = collator.compare(s1, s2);
				if(result != 0)
					return result;
			}
			return 0;
		}

		MiniFieldSpec[] specs;
		SaneCollator collator;
	}
	
	MiniLocalization localization;
	Comparator sorter;
	MiniFieldSpec[] sortSpecs;
	MiniFieldSpec[] specsToStore;
	HashSet partialBulletins;
}
