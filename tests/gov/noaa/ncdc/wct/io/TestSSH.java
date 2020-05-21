package gov.noaa.ncdc.wct.io;

import java.io.IOException;
import java.io.InputStream;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.junit.Test;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

/* -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*- */



public class TestSSH {

    
    @Test
    public void testSSH() throws JSchException, IOException {

        JSch jsch = new JSch();

        String host = JOptionPane.showInputDialog("Enter username@hostname", System.getProperty("user.name")
                + "@localhost");
        String user = host.substring(0, host.indexOf('@'));
        host = host.substring(host.indexOf('@') + 1);

        Session session = jsch.getSession(user, host, 22);

        /*
        String xhost="127.0.0.1";
        int xport=0;
        String display=JOptionPane.showInputDialog("Enter display name", 
                                                 xhost+":"+xport);
        xhost=display.substring(0, display.indexOf(':'));
        xport=Integer.parseInt(display.substring(display.indexOf(':')+1));
        session.setX11Host(xhost);
        session.setX11Port(xport+6000);
         */

        // username and password will be given via UserInfo interface.
        UserInfo ui = new MyUserInfo();
        session.setUserInfo(ui);
        session.connect();

        String command = JOptionPane.showInputDialog("Enter command", "set|grep SSH");

        Channel channel = session.openChannel("exec");
        ((ChannelExec) channel).setCommand(command);
        //channel.setXForwarding(true);

        //channel.setInputStream(System.in);
        channel.setInputStream(null);

        //channel.setOutputStream(System.out);

        //FileOutputStream fos=new FileOutputStream("/tmp/stderr");
        //((ChannelExec)channel).setErrStream(fos);
        ((ChannelExec) channel).setErrStream(System.err);

        InputStream in = channel.getInputStream();

        channel.connect();

        byte[] tmp = new byte[1024];
        while (true) {
            while (in.available() > 0) {
                int i = in.read(tmp, 0, 1024);
                if (i < 0)
                    break;
                System.out.print(new String(tmp, 0, i));
            }
            if (channel.isClosed()) {
                System.out.println("exit-status: " + channel.getExitStatus());
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (Exception ee) {
            }
        }
        channel.disconnect();
        session.disconnect();
    }

    
    public static class MyUserInfo implements UserInfo {
        public String getPassword() {
            return passwd;
        }

        public boolean promptYesNo(String str) {
            Object[] options = { "yes", "no" };
            int foo = JOptionPane.showOptionDialog(null, str, "Warning", JOptionPane.DEFAULT_OPTION,
                    JOptionPane.WARNING_MESSAGE, null, options, options[0]);
            return foo == 0;
        }

        String passwd;

        JTextField passwordField = (JTextField) new JPasswordField(20);

        public String getPassphrase() {
            return null;
        }

        public boolean promptPassphrase(String message) {
            return true;
        }

        public boolean promptPassword(String message) {
            Object[] ob = { passwordField };
            int result = JOptionPane.showConfirmDialog(null, ob, message, JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                passwd = passwordField.getText();
                return true;
            }
            else {
                return false;
            }
        }

        public void showMessage(String message) {
            JOptionPane.showMessageDialog(null, message);
        }
    }
}
