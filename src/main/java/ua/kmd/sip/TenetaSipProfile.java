package ua.kmd.sip;

import android.javax.sip.PeerUnavailableException;
import android.javax.sip.SipFactory;
import android.javax.sip.SipStack;

import java.util.Properties;

/**
 * Created by Sontik on 29.05.2017.
 */
public class TenetaSipProfile {

    private static SipStack sipStack;

    private String callsign;

    public TenetaSipProfile(String callsign) {
        this.callsign = callsign;
    }

    public TenetaSipProfile init(){
        SipFactory sipFactory = null;
        sipStack = null;
        sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("android.gov.nist");
        Properties properties = new Properties();
        properties.setProperty("android.javax.sip.STACK_NAME", "shootme");
        // You need 16 for logging traces. 32 for debug + traces.
        // Your code will limp at 32 but it is best for debugging.
        properties.setProperty("android.gov.nist.javax.sip.TRACE_LEVEL", "DEBUG");

        try {
            // Create SipStack object
            sipStack = sipFactory.createSipStack(properties);
            System.out.println("createSipStack " + sipStack);
        } catch (PeerUnavailableException e) {
            // could not find
            // gov.nist.jain.protocol.ip.sip.SipStackImpl
            // in the classpath
            e.printStackTrace();
            System.err.println(e.getMessage());
            System.exit(0);
        }

        return this;
    }

    public void setListener(TenetaSipListener listener) {
        sipStack.createListeningPoint();
    }
}
