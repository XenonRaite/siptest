package ua.kmd.sip;

import android.javax.sip.*;
import android.javax.sip.address.Address;
import android.javax.sip.address.AddressFactory;
import android.javax.sip.address.SipURI;
import android.javax.sip.header.*;
import android.javax.sip.message.MessageFactory;
import android.javax.sip.message.Request;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Sontik on 29.05.2017.
 */
public class SIP implements Const {
    private final ISIPListener sipRequestListener;
    private TenetaSipProfile sipProfile;
    private TenetaSipListener sipListener;
    private HeaderFactory headerFactory;
    private AddressFactory addressFactory;
    private MessageFactory messageFactory;
    private SipFactory sipFactory = SipFactory.getInstance();
    private SipProvider sipProvider;
    private ListeningPoint listeningPoint;
    private SipStack sipStack;

    public SIP(ISIPListener sipRequestListener) {
        this.sipRequestListener = sipRequestListener;
    }


    public static void main(String args[]) throws PeerUnavailableException, InvalidArgumentException, TransportNotSupportedException, ObjectInUseException, TooManyListenersException {
        final SIP sip = new SIP(new ISIPListener() {
            public void processInvite(RequestEvent requestReceivedEvent, ServerTransaction serverTransactionId) {

            }

            public void processAck(RequestEvent requestReceivedEvent, ServerTransaction serverTransactionId) {
                System.out.println("receive ack");
                System.out.println(requestReceivedEvent.toString());
            }

            public void processBye(RequestEvent requestReceivedEvent, ServerTransaction serverTransactionId) {

            }

            public void processCancel(RequestEvent requestReceivedEvent, ServerTransaction serverTransactionId) {

            }

            public void processMessage(RequestEvent requestReceivedEvent, ServerTransaction serverTransactionId) {
                System.out.println("Message : " + new String(requestReceivedEvent.getRequest().getRawContent()));
            }

            public void processOkMessage(ResponseEvent responseEvent, ClientTransaction tid) {
                System.out.println("processOkMessage : ");
            }
        });
        sip.init("XenonRaite");

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
                System.out.println("Task started");
                sip.sendMessage("message " + simpleDateFormat.format(new Date()), "127.0.0.1:5060", "xenonraite");
            }
        }, 5000);
    }

    public void init(String myCallsigm) throws PeerUnavailableException, TransportNotSupportedException, InvalidArgumentException, ObjectInUseException, TooManyListenersException {
        sipProfile = new TenetaSipProfile(myCallsigm);
        sipListener = new TenetaSipListener(this);

        createFactories();
        listenOnPort();
    }

    private void listenOnPort() throws InvalidArgumentException, TransportNotSupportedException, ObjectInUseException, TooManyListenersException {
        sipStack = getSipStack();
        listeningPoint = sipStack.createListeningPoint(LOCALHOST,
                SIP_PORT, TRANSPORT_PROTOCOL);

        sipProvider = sipStack.createSipProvider(listeningPoint);
        System.out.println("udp provider " + sipProvider);
        sipProvider.addSipListener(sipListener);
    }

    public SipStack getSipStack() {
        return sipProfile.getSipStack();
    }

    private void createFactories() throws PeerUnavailableException {
        headerFactory = sipFactory.createHeaderFactory();
        addressFactory = sipFactory.createAddressFactory();
        messageFactory = sipFactory.createMessageFactory();
    }

    public HeaderFactory getHeaderFactory() {
        return headerFactory;
    }

    public AddressFactory getAddressFactory() {
        return addressFactory;
    }

    public MessageFactory getMessageFactory() {
        return messageFactory;
    }

    public ISIPListener getSipRequestListener() {
        return sipRequestListener;
    }

    public ServerTransaction getServerTransaction(RequestEvent event)
            throws SipException {
        ServerTransaction transaction = event.getServerTransaction();
        if (transaction == null) {
            Request request = event.getRequest();
            return sipProvider.getNewServerTransaction(request);
        } else {
            return transaction;
        }
    }

    /**
     * @param message
     * @param address host:port
     * @param toUser
     */
    public String sendMessage(String message, String address, String toUser) {
        try {
            String toSipAddress = address;
            String toDisplayName = address;

            String fromSipAddress = listeningPoint.getIPAddress();
            String fromName = sipProfile.getCallsign();
            String fromDisplayName = sipProfile.getCallsign();
            // create >From Header
            SipURI fromAddress = addressFactory.createSipURI(fromName,
                    fromSipAddress);

            Address fromNameAddress = addressFactory.createAddress(fromAddress);
            fromNameAddress.setDisplayName(fromDisplayName);
            FromHeader fromHeader = headerFactory.createFromHeader(
                    fromNameAddress, "12345");
            // Create a new Cseq header
            CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1L,
                    Request.MESSAGE);
            // Create a new CallId header
            CallIdHeader callIdHeader = sipProvider.getNewCallId();

            // create Request URI
            SipURI requestURI = addressFactory.createSipURI(toUser, address);


            // create To Header
            SipURI toAddress = addressFactory
                    .createSipURI(toUser, toSipAddress);
            Address toNameAddress = addressFactory.createAddress(toAddress);
            toNameAddress.setDisplayName(toDisplayName);
            ToHeader toHeader = headerFactory.createToHeader(toNameAddress,
                    null);

            // Create ViaHeaders

            ArrayList viaHeaders = new ArrayList();
            String ipAddress = listeningPoint.getIPAddress();
            ViaHeader viaHeader = headerFactory.createViaHeader(ipAddress,
                    sipProvider.getListeningPoint(TRANSPORT_PROTOCOL).getPort(),
                    TRANSPORT_PROTOCOL, null);

            // add via headers
            viaHeaders.add(viaHeader);

            // Create a new MaxForwardsHeader
            MaxForwardsHeader maxForwards = headerFactory
                    .createMaxForwardsHeader(70);

            ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("text", "plain");

            // Create the request.
            Request request = messageFactory.createRequest(requestURI,
                    Request.MESSAGE, callIdHeader, cSeqHeader, fromHeader,
                    toHeader, viaHeaders, maxForwards, contentTypeHeader, message);            // Create the client transaction.
            ClientTransaction inviteTid = sipProvider.getNewClientTransaction(request);

            System.out.println("inviteTid = " + inviteTid);

            // send the request out.

            inviteTid.sendRequest();

            Dialog dialog = inviteTid.getDialog();

            return inviteTid.getBranchId();

        } catch (ParseException e) {
            e.printStackTrace();
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        } catch (TransactionUnavailableException e) {
            e.printStackTrace();
        } catch (SipException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setNewListenerAddress(String host) {
        try {
            if (listeningPoint != null) {
                sipProvider.removeListeningPoint(listeningPoint);
            }
            listeningPoint = sipStack.createListeningPoint(host,
                    SIP_PORT, TRANSPORT_PROTOCOL);

            sipProvider = sipStack.createSipProvider(listeningPoint);
            System.out.println("udp provider " + sipProvider);
            sipProvider.addSipListener(sipListener);
        } catch (ObjectInUseException e) {
            e.printStackTrace();
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        } catch (TooManyListenersException e) {
            e.printStackTrace();
        } catch (TransportNotSupportedException e) {
            e.printStackTrace();
        }
    }
}
