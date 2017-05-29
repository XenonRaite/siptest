package ua.kmd.sip;

/**
 * Created by Sontik on 29.05.2017.
 */
public class SIP {
    private TenetaSipProfile sipProfile;
    private TenetaSipListener sipListener;


    public static void main(String args[]) {
        SIP sip = new SIP();
        sip.sipProfile = new TenetaSipProfile("XenonRaite").init();
        sip.sipListener = new TenetaSipListener();
        sip.sipProfile.setListener(sip.sipListener);

    }
}
