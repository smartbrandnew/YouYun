package com.broada.carrier.monitor.impl.generic;

import java.io.File;
import java.io.IOException;

import com.sshtools.j2ssh.transport.AbstractKnownHostsKeyVerification;
import com.sshtools.j2ssh.transport.InvalidHostFileException;
import com.sshtools.j2ssh.transport.publickey.SshPublicKey;


public class ConsoleKnownHostsKeyVerification extends
        AbstractKnownHostsKeyVerification {
	
    public ConsoleKnownHostsKeyVerification() throws InvalidHostFileException {
        super(new File(System.getProperty("user.home"), ".ssh" + File.separator
                + "known_hosts").getAbsolutePath());
    }
    public ConsoleKnownHostsKeyVerification(String knownhosts)
            throws InvalidHostFileException {
        super(knownhosts);
    }
    public void onHostKeyMismatch(String host, SshPublicKey pk,
            SshPublicKey actual) {
        try {
            System.out.println("The host key supplied by " + host + " is: "
                    + actual.getFingerprint());
            System.out.println("The current allowed key for " + host + " is: "
                    + pk.getFingerprint());
            getResponse(host, pk);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onUnknownHost(String host, SshPublicKey pk) {
        try {
            System.out.println("The host " + host
                    + " is currently unknown to the system");
            System.out.println("The host key fingerprint is: "
                    + pk.getFingerprint());
            getResponse(host, pk);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取用户输入的信息，判断是否接受主机公匙
     * <p>
     * 修改：xxx ，去掉从流中获取信息，直接接受公匙，注释掉的代码为源码
     * 
     * @param host
     *            主机ip
     * @param pk
     *            主机公匙
     * @throws InvalidHostFileException
     * @throws IOException
     */
    private void getResponse(String host, SshPublicKey pk)
            throws InvalidHostFileException, IOException {
        if (isHostFileWriteable()) {
            allowHost(host, pk, true);
        }
    }
}