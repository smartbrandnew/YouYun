package uyun.bat.agent.impl.autosync.entity;

import java.io.File;
import org.junit.Test;

public class EntityClassTest {

	private static String TEST_ID = "94baaadca64344d2a748dff88fe7159e";
	private static String TEST_NAME = "testName";
	private static String SYNC_FILE_NAME = "1232/123";
	@Test
	public void testAction() {
		Action action = new Action();
		Condition cond = new Condition();
		cond.setName(TEST_NAME);
		cond.setValue(TEST_NAME);
		action.addCondition(cond);
		action.getConditions(TEST_NAME);
	}
	
	@Test
	public void testClient(){
		Client client = new Client();
		Fileset fileset = new Fileset();
		fileset.setId(TEST_ID);
		client.addFileset(fileset);
		Action action = new Action();
		action.setEvent("testEvent");
		client.addAction(action);
		client.listFiles();
		client.getFileset(TEST_ID);
		Event event = Event.UPGRADE_SUCCESSFUL;
		client.getActions(event);
		client.checkFileset(TEST_ID);	
	}

	@Test
	public void testLocalFile(){
		File file = new File(TEST_NAME);
		LocalFile localFile = new LocalFile(TEST_NAME, TEST_NAME, file);
		localFile.getFile();
	}
	
	@Test
	public void testSyncFile(){
		SyncFile syncFile = new SyncFile(TEST_NAME,SYNC_FILE_NAME, (long)123, (long)10);
		SyncFile other = new SyncFile();
		other.setLastModified((long)123);
		SyncFile []copy = {};
		SyncFile.copy(copy);
		syncFile.equalsFile(other);
		syncFile.retDeletedName();
		syncFile.retMarkDeleted();
	}
	
	@Test
	public void testSyncFileset(){
		SyncFileset syncFileset = new SyncFileset();
		syncFileset.setVersion(TEST_NAME);
		SyncFile[]files = {};
		syncFileset.setFiles(files);
		syncFileset.getVersion();
		syncFileset.getFiles();
		syncFileset.getFile(TEST_NAME);
		syncFileset.toString();
	}
	
	@Test
	public void testSyncFilesetV2(){
		SyncFileV2[]files = {};
		SyncFilesetV2 syncFilesetV2 = new SyncFilesetV2(files);
		syncFilesetV2.setFiles(files);
		syncFilesetV2.getFile(SYNC_FILE_NAME);
		syncFilesetV2.getFiles();
	}
	
	@Test
	public void testSyncFileV2(){
		SyncFileV2 syncFileV2 = new SyncFileV2(TEST_NAME,SYNC_FILE_NAME, (long)10, (long)10,"md5");
		SyncFile other = new SyncFile();
		other.setSize((long)10);
		syncFileV2.equalsFile(other);
		SyncFileV2[]copy = {};
		SyncFileV2.copy(copy);
	}
}
