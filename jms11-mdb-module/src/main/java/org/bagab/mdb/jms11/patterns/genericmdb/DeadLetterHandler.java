package org.bagab.mdb.jms11.patterns.genericmdb;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author adam-bien.com
 */
@Stateless
public class DeadLetterHandler {
    @Resource(name = "jms/DeadLetterQueue")
    private Queue deadLetterQueue;
    @Resource(name = "jms/DeadLetterQueueFactory")
    private ConnectionFactory deadLetterQueueFactory;


    public void invalidMessageType(Message message) {
        try {
            this.sendJMSMessageToDeadLetterQueue(message);
        } catch (JMSException ex) {
            Logger.getLogger(DeadLetterHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    private void sendJMSMessageToDeadLetterQueue(Message message) throws JMSException {
        Connection connection = null;
        Session session = null;
        try {
            connection = deadLetterQueueFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer messageProducer = session.createProducer(deadLetterQueue);
            messageProducer.send(message);
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (JMSException e) {
                    Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Cannot close session", e);
                }
            }
            if (connection != null) {
                connection.close();
            }
        }
    }
}
