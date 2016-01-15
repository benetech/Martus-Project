/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2002-2014, Beneficent
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

package org.martus.server.main;

import java.time.Instant;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

import org.martus.common.HeadquartersKey;
import org.martus.common.HeadquartersKeys;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.network.ServerBulletinSummary;
import org.martus.common.network.SummaryOfAvailableBulletins;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.UniversalId;
import org.martus.common.utilities.DateUtilities;
import org.miradi.utils.EnhancedJsonObject;

import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.index.OIndexCursor;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientBaseGraph;
import com.tinkerpop.blueprints.impls.orient.OrientEdge;
import com.tinkerpop.blueprints.impls.orient.OrientIndex;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;


public class ServerMetaDatabaseConnection implements ServerMetaDatabaseConstants 
{
	public ServerMetaDatabaseConnection(OrientBaseGraph graphToUse) 
	{
		graph = graphToUse;
		writeLock = new Object();
	}

	public void close() 
	{
		graph.shutdown();
	}
	
	public void putAccount(String publicKeyString) throws Exception
	{
		String publicCode = computePublicCode(publicKeyString);
		Vertex found = findExistingAccountVertex(publicCode);
		if(found != null)
			return;
		
		synchronized (writeLock) 
		{
			try
			{
				internalPutAccount(publicKeyString);
				commit();
				
			}
			catch(Exception e)
			{
				graph.rollback();
				throw(e);
			}
		}
	}

	public void revisionWasSaved(BulletinHeaderPacket bhp, Instant serverFileTimestamp) throws Exception 
	{
		synchronized (writeLock) 
		{
			try
			{
				internalBulletinWasSaved(bhp, serverFileTimestamp);
				commit();
			}
			catch(Exception e)
			{
				graph.rollback();
				throw(e);
			}
		}
	}

	public void revisionWasRemoved(UniversalId uid) throws Exception
	{
		Vertex bulletinVertex = findExistingBulletinVertex(uid);
		if(bulletinVertex == null)
			return;
		
		synchronized (writeLock) 
		{
			try
			{
				removeEdges(bulletinVertex.getEdges(Direction.IN, CLASS_NAME_WAS_AUTHORED_BY));
				removeEdges(bulletinVertex.getEdges(Direction.IN, CLASS_NAME_CAN_DOWNLOAD));
				
				graph.removeVertex(bulletinVertex);
				
				commit();
			}
			catch(Exception e)
			{
				graph.rollback();
				throw (e);
			}
		}
	}

	public long countAccounts() 
	{
		return graph.countVertices(CLASS_NAME_ACCOUNT);
	}

	public long countBulletins() 
	{
		return graph.countVertices(CLASS_NAME_BULLETIN);
	}

	public Instant getTimestamp(UniversalId uid) throws Exception
	{
		Vertex found = findExistingBulletinVertex(uid);
		if(found == null)
			throw new BulletinNotFoundException("Bulletin not found: " + computeDatabaseUid(uid));
		
		return parseIsoDateTime(found.getProperty(KEY_BULLETIN_TIMESTAMP));
	}

	public Instant getLastSavedTime(UniversalId uid) throws Exception
	{
		Vertex found = findExistingBulletinVertex(uid);
		if(found == null)
			throw new BulletinNotFoundException("No such bulletin: " + computeDatabaseUid(uid));
		
		return parseIsoDateTime(found.getProperty(KEY_BULLETIN_LAST_MODIFIED));
	}
	
	public static interface UidHandler
	{
		public void callback(UniversalId uid) throws Exception;
	}

	public void forEachBulletin(UidHandler handler) throws Exception
	{
		Iterable<Vertex> bulletinVertexes = graph.getVerticesOfClass(CLASS_NAME_BULLETIN);
		Iterator<Vertex> it = bulletinVertexes.iterator();
		while(it.hasNext())
		{
			Vertex bulletinVertex = it.next();
			if(bulletinVertex == null)
				break;
			ServerBulletinSummary summary = extractAvailableBulletinInformation(bulletinVertex);
			UniversalId uid = summary.getUniversalId();
			handler.callback(uid);
		}
	}
	
	public static interface AccountHandler
	{
		public void callback(String accountId) throws Exception;
	}

	public void forEachAccount(AccountHandler handler) throws Exception
	{
		Iterable<Vertex> accountVertexes = graph.getVerticesOfClass(CLASS_NAME_ACCOUNT);
		Iterator<Vertex> it = accountVertexes.iterator();
		while(it.hasNext())
		{
			Vertex accountVertex = it.next();
			String accountId = accountVertex.getProperty(KEY_ACCOUNT_ACCOUNT_ID);
			handler.callback(accountId);
		}
	}
	
	public SummaryOfAvailableBulletins listBulletinsDownloadableBy(String accountId) throws Exception
	{
		String earliestPossibleTimestamp = "";

		return listRecentBulletinsDownloadableBy(accountId, earliestPossibleTimestamp);
	}
	
	public EnhancedJsonObject getRecentBulletinsDownloadableBy(String accountId, String lowestTimestampIso) throws Exception
	{
		SummaryOfAvailableBulletins available = listRecentBulletinsDownloadableBy(accountId, lowestTimestampIso);

		EnhancedJsonObject json = available.toJson();
		
		return json;
	}
	
	public SummaryOfAvailableBulletins listRecentBulletinsDownloadableBy(String accountId, String lowestTimestampIso) throws Exception 
	{
		final int MAXIMUM_AVAILABLE_TO_RETURN = 100;
		String publicCode = computePublicCode(accountId);
		Instant lowestTimestamp = parseIsoDateTime(lowestTimestampIso);
		String actualKey = createPubCodeTimestamp(publicCode, lowestTimestamp);
		String startAt = createInternalOIndexSearchableKey(actualKey);
		String indexName = ServerMetaDatabaseSchema.getIndexName(CLASS_NAME_CAN_DOWNLOAD, KEY_CAN_DOWNLOAD_PUBCODETIMESTAMP);
		Index<Edge> index = graph.getIndex(indexName, Edge.class);
		OrientIndex orientIndex = (OrientIndex) index;
		OIndex oIndex = orientIndex.getUnderlying();
		boolean INCLUSIVE = true;
		boolean ASCENDING = true;
		// NOTE: iterateEntriesBetween always seems to return zero entries
		OIndexCursor cursor = oIndex.iterateEntriesMajor(startAt, INCLUSIVE, ASCENDING);
		SummaryOfAvailableBulletins available = new SummaryOfAvailableBulletins(lowestTimestampIso);
		while(cursor.hasNext())
		{
			OIdentifiable oid = cursor.next();
			// NOTE: hasNext might return true even when next will return null
			if(oid == null)
				break;
			ServerBulletinSummary summary = extractAvailableBulletinInformation(publicCode, oid);
			if(summary == null)
				break;
			if(available.size() >= MAXIMUM_AVAILABLE_TO_RETURN)
				break;
			available.addBulletin(summary);
		}
		
		return available;
	}
	
	protected void removeAccount(String publicKeyString) throws Exception
	{
		String publicCode = computePublicCode(publicKeyString);
		Vertex accountVertex = findExistingAccountVertex(publicCode);
		graph.removeVertex(accountVertex);
	}

	private Vertex internalPutAccount(String publicKeyString) throws Exception 
	{
		String publicCode = computePublicCode(publicKeyString);
		Vertex found = findExistingAccountVertex(publicCode);
		if(found != null)
			return found;
		
		OrientVertex vertex = createVertex(CLASS_NAME_ACCOUNT);
		vertex.setProperties(KEY_ACCOUNT_ACCOUNT_ID, publicKeyString);
		vertex.setProperties(KEY_ACCOUNT_PUBLIC_CODE, publicCode);

		// FIXME: This unique index isn't preventing dupe entries
		Index<Vertex> index = getVertexIndex(CLASS_NAME_ACCOUNT, KEY_ACCOUNT_PUBLIC_CODE);
		index.put(KEY_ACCOUNT_PUBLIC_CODE, publicCode, vertex);

		return vertex;
	}

	private void internalBulletinWasSaved(BulletinHeaderPacket bhp, Instant serverFileTimestamp) throws Exception 
	{
		UniversalId uid = bhp.getUniversalId();
		String authorAccountId = uid.getAccountId();
		internalPutAccount(authorAccountId);
		String authorPublicCode = computePublicCode(authorAccountId);
		Vertex bulletinVertex = findExistingBulletinVertex(uid);
		String duid = computeDatabaseUid(uid);
		
		if(bulletinVertex == null)
		{
			
			bulletinVertex = createVertex(CLASS_NAME_BULLETIN);
			bulletinVertex.setProperty(KEY_BULLETIN_LOCAL_ID, uid.getLocalId());
			bulletinVertex.setProperty(KEY_BULLETIN_DUID, duid);

			Index<Vertex> index = getVertexIndex(CLASS_NAME_BULLETIN, KEY_BULLETIN_DUID);
			index.put(KEY_BULLETIN_DUID, duid, bulletinVertex);

			addWasAuthoredByEdge(bulletinVertex, authorPublicCode);
		}

		// NOTE: Any field that might change needs to be set here
		bulletinVertex.setProperty(KEY_BULLETIN_TIMESTAMP, formatIsoDateTime(serverFileTimestamp));
		Instant lastSavedTime = getLastSavedTimeInstant(bhp);
		bulletinVertex.setProperty(KEY_BULLETIN_LAST_MODIFIED, formatIsoDateTime(lastSavedTime));
		
		{
			removeAllCanDownloadEdges(bulletinVertex);
			
			addCanDownloadEdge(authorAccountId, bulletinVertex, serverFileTimestamp);

			HeadquartersKeys authorizedKeys = bhp.getAuthorizedToReadKeys();
			for(int i = 0; i < authorizedKeys.size(); ++i)
			{
				HeadquartersKey key = authorizedKeys.get(i);
				String authorizedAccountId = key.getPublicKey();
				addCanDownloadEdge(authorizedAccountId, bulletinVertex, serverFileTimestamp);
			}
		}
	}
	
	// NOTE: I really want this inside BulletinHeaderPacket, but that class
	// must remain compatible with Java 7
	public static Instant getLastSavedTimeInstant(BulletinHeaderPacket bhp)
	{
		return new Date(bhp.getLastSavedTime()).toInstant();
	}
	
	private static String computePublicCode(String publicKeyString) throws Exception 
	{
		// NOTE: For now, we will use 20-digit public codes because the
		// entire server uses 20-digit codes. We can switch to 40 later. 
		// We are using formatted, although unformatted would be smaller
		return MartusCrypto.computeFormattedPublicCode(publicKeyString);
	}

	private Vertex findExistingAccountVertex(String publicCode) throws Exception 
	{
		Index<Vertex> index = getVertexIndex(CLASS_NAME_ACCOUNT, KEY_ACCOUNT_PUBLIC_CODE);
		Vector<Vertex> found = new Vector<Vertex>();
		index.get(KEY_ACCOUNT_PUBLIC_CODE, publicCode).forEach(v -> found.add(v));
		long size = found.size();
		if(size == 0)
			return null;
		else if(size == 1)
			return found.firstElement();
		else
			throw new DuplicateAccountException("Duplicate accounts " + publicCode + "(" + size + ")");
	}

	private void removeAllCanDownloadEdges(Vertex bulletinVertex) 
	{
		String edgeClassName = CLASS_NAME_CAN_DOWNLOAD;
		removeEdges(bulletinVertex.getEdges(Direction.IN, edgeClassName));
	}

	private void removeEdges(Iterable<Edge> edges) 
	{
		edges.forEach(edge -> removeEdge(edge));
	}

	private void removeEdge(Edge edge) 
	{
		graph.removeEdge(edge);
	}

	private void addWasAuthoredByEdge(Vertex bulletinVertex, String authorPublicCode) throws Exception 
	{
		String edgeClassName = CLASS_NAME_WAS_AUTHORED_BY;
		String edgeLabel = edgeClassName;
		String classString = ServerMetaDatabaseSchema.classStringFromName(edgeClassName);
		Vertex authorVertex = findExistingAccountVertex(authorPublicCode);
		graph.addEdge(classString, bulletinVertex, authorVertex, edgeLabel);
	}

	private void addCanDownloadEdge(String authorizedAccountId, Vertex bulletinVertex, Instant serverFileTimestamp) throws Exception 
	{
		Vertex accountVertex = internalPutAccount(authorizedAccountId);
		String edgeLabel = CLASS_NAME_CAN_DOWNLOAD;
		String classString = ServerMetaDatabaseSchema.classStringFromName(edgeLabel);
		
		OrientEdge edge = graph.addEdge(classString, accountVertex, bulletinVertex, edgeLabel);
		String publicCode = computePublicCode(authorizedAccountId);
		
		String pubCodeTimestamp = createPubCodeTimestamp(publicCode, serverFileTimestamp);
		edge.setProperty(KEY_CAN_DOWNLOAD_PUBCODETIMESTAMP, pubCodeTimestamp);

		String indexName = ServerMetaDatabaseSchema.getIndexName(CLASS_NAME_CAN_DOWNLOAD, KEY_CAN_DOWNLOAD_PUBCODETIMESTAMP);
		Index<Edge> index = graph.getIndex(indexName, Edge.class);
		index.put(KEY_CAN_DOWNLOAD_PUBCODETIMESTAMP, pubCodeTimestamp, edge);
	}

	private Vertex findExistingBulletinVertex(UniversalId uid) throws Exception 
	{
		String duid = computeDatabaseUid(uid);
		Index<Vertex> index = getVertexIndex(CLASS_NAME_BULLETIN, KEY_BULLETIN_DUID);
		Vector<Vertex> found = new Vector<Vertex>();
		index.get(KEY_BULLETIN_DUID, duid).forEach(v -> found.add(v));
		long size = found.size();
		if(size == 0)
			return null;
		else if(size == 1)
			return found.firstElement();
		else
			throw new DuplicateBulletinException("Duplicate bulletins " + duid + "(" + size + ")");
	}

	private Index<Vertex> getVertexIndex(String className, String key) 
	{
		return graph.getIndex(ServerMetaDatabaseSchema.getIndexName(className, key), Vertex.class);
	}

	private OrientVertex createVertex(String className) 
	{
		return graph.addVertex(ServerMetaDatabaseSchema.classStringFromName(className));
	}
	
	private String computeDatabaseUid(UniversalId uid) throws Exception 
	{
		String authorAccountId = uid.getAccountId();
		String authorPublicCode = computePublicCode(authorAccountId);
		String localId = uid.getLocalId();
		String duid = authorPublicCode + ":" + localId;
		return duid;
	}

	private String createPubCodeTimestamp(String publicCode, Instant lowestTimestamp) throws Exception
	{
		if(lowestTimestamp.equals(Instant.MIN))
			return publicCode;
		
		String isoTimestamp = formatIsoDateTime(lowestTimestamp);
		String actualKey = publicCode + " " + isoTimestamp;
		return actualKey;
	}

	private String createInternalOIndexSearchableKey(String actualKey) 
	{
		// NOTE: Keys passed to OIndex.iterateEntriesMajor must 
		// be in the (undocumented) format: EdgeLabel!=!ActualKey
		return KEY_CAN_DOWNLOAD_PUBCODETIMESTAMP + "!=!" + actualKey;
	}
	
	private ServerBulletinSummary extractAvailableBulletinInformation(String downloaderPublicCode, OIdentifiable edgeOid) 
	{
		Edge edge = graph.getEdge(edgeOid);
		String publicCodeAndTimestamp = edge.getProperty(KEY_CAN_DOWNLOAD_PUBCODETIMESTAMP);
		if(!publicCodeAndTimestamp.startsWith(downloaderPublicCode))
			return null;
		
		Vertex bulletinVertex = edge.getVertex(Direction.IN);

		return extractAvailableBulletinInformation(bulletinVertex);
	}

	private ServerBulletinSummary extractAvailableBulletinInformation(Vertex bulletinVertex)
	{
		String localId = bulletinVertex.getProperty(KEY_BULLETIN_LOCAL_ID);
		Iterable<Vertex> vertices = bulletinVertex.getVertices(Direction.OUT, CLASS_NAME_WAS_AUTHORED_BY);
		Vertex accountVertex = vertices.iterator().next();
		String authorAccountId = accountVertex.getProperty(KEY_ACCOUNT_ACCOUNT_ID);
		UniversalId uid = UniversalId.createFromAccountAndLocalId(authorAccountId, localId);
		
		String lastModified = bulletinVertex.getProperty(KEY_BULLETIN_LAST_MODIFIED);
		String serverTimestamp = bulletinVertex.getProperty(KEY_BULLETIN_TIMESTAMP);
		ServerBulletinSummary summary = new ServerBulletinSummary(uid, lastModified, serverTimestamp);
		return summary;
	}

	private void commit() 
	{
		graph.commit();
	}
	
	public static String formatIsoDateTime(Instant instant) throws Exception
	{
		Date date = Date.from(instant);
		return DateUtilities.formatIsoDateTime(date);
	}
	
	public static Instant parseIsoDateTime(String isoDateTime) throws Exception
	{
		if(isoDateTime.length() == 0)
			return Instant.MIN;

		Date date = DateUtilities.parseIsoDateTime(isoDateTime);
		return date.toInstant();
	}

	public static class DuplicateBulletinException extends Exception
	{
		public DuplicateBulletinException(String message) 
		{
			super(message);
		}
	}
	
	public static class BulletinNotFoundException extends Exception
	{
		public BulletinNotFoundException(String message) 
		{
			super(message);
		}
	}

	public static class DuplicateAccountException extends Exception
	{
		public DuplicateAccountException(String message)
		{
			super(message);
		}
	}
	
	private OrientBaseGraph graph;
	private Object writeLock;
}
