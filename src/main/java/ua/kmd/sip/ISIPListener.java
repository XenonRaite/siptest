package ua.kmd.sip;

import android.javax.sip.ClientTransaction;
import android.javax.sip.RequestEvent;
import android.javax.sip.ResponseEvent;
import android.javax.sip.ServerTransaction;

/**
 * Created by Sontik on 06.06.2017.
 */
public interface ISIPListener {
    void processInvite(RequestEvent requestReceivedEvent, ServerTransaction serverTransactionId);

    void processAck(RequestEvent requestReceivedEvent, ServerTransaction serverTransactionId);

    void processBye(RequestEvent requestReceivedEvent, ServerTransaction serverTransactionId);

    void processCancel(RequestEvent requestReceivedEvent, ServerTransaction serverTransactionId);

    void processMessage(RequestEvent requestReceivedEvent, ServerTransaction serverTransactionId);

    void processOkMessage(ResponseEvent responseEvent, ClientTransaction tid);
}
