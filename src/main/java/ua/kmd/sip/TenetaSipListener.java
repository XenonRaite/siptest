package ua.kmd.sip;

import android.javax.sip.*;
import android.javax.sip.header.CSeqHeader;
import android.javax.sip.message.Request;
import android.javax.sip.message.Response;

import java.text.ParseException;

/**
 * Created by Sontik on 29.05.2017.
 */
public class TenetaSipListener implements SipListener {
    private final SIP sip;
    private ISIPListener listener;

    public TenetaSipListener(SIP sip) {
        this.sip = sip;
        this.listener = sip.getSipRequestListener();
    }

    public void processRequest(RequestEvent requestReceivedEvent) {

        Request request = requestReceivedEvent.getRequest();
        ServerTransaction serverTransactionId = requestReceivedEvent
                .getServerTransaction();

        System.out.println("\n\nRequest " + request.getMethod()
                + " received at " + sip.getSipStack().getStackName()
                + " with server transaction id " + serverTransactionId);

        // We are the UAC so the only request we get is the BYE.
        if (request.getMethod().equals(Request.INVITE)) {
            listener.processInvite(requestReceivedEvent, serverTransactionId);
        } else if (request.getMethod().equals(Request.ACK)) {
            listener.processAck(requestReceivedEvent, serverTransactionId);
        } else if (request.getMethod().equals(Request.BYE)) {
            listener.processBye(requestReceivedEvent, serverTransactionId);
        } else if (request.getMethod().equals(Request.CANCEL)) {
            listener.processCancel(requestReceivedEvent, serverTransactionId);
        } else if (request.getMethod().equals(Request.MESSAGE)) {
            System.out.println("processMessage");
            listener.processMessage(requestReceivedEvent, serverTransactionId);
            System.out.println("sendOk");
            sendOk(request, requestReceivedEvent);
        } else {
            try {
                serverTransactionId.sendResponse(sip.getMessageFactory().createResponse(202, request));
            } catch (SipException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvalidArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void sendOk(Request request, RequestEvent requestEvent) {
        try {
            Response okResponse = sip.getMessageFactory().createResponse(Response.OK, request);
            ServerTransaction serverTransaction = sip.getServerTransaction(requestEvent);
            serverTransaction.sendResponse(okResponse);
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (SipException e) {
            e.printStackTrace();
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
    }

    public void processResponse(ResponseEvent responseEvent) {
        System.out.println("Got a response");
        Response response = (Response) responseEvent.getResponse();
        ClientTransaction tid = responseEvent.getClientTransaction();
        CSeqHeader cseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME);

        System.out.println("Response received : Status Code = "
                + response.getStatusCode() + " " + cseq);


        if (tid == null) {

//            // RFC3261: MUST respond to every 2xx
//            if (ackRequest!=null && dialog!=null) {
//                System.out.println("re-sending ACK");
//                try {
//                    dialog.sendAck(ackRequest);
//                } catch (SipException se) {
//                    se.printStackTrace();
//                }
//            }
//            return;
        }

        System.out.println("transaction state is " + tid.getState());
        System.out.println("Dialog = " + tid.getDialog());
        //System.out.println("Dialog State is " + tid.getDialog().getState());

        if (response.getStatusCode() == Response.OK) {
            if (cseq.getMethod().equals(Request.MESSAGE)) {
                listener.processOkMessage(responseEvent, tid);
            }
        }
    }

    public void processTimeout(TimeoutEvent timeoutEvent) {

    }

    public void processIOException(IOExceptionEvent ioExceptionEvent) {

    }

    public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {

    }

    public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {

    }

}
