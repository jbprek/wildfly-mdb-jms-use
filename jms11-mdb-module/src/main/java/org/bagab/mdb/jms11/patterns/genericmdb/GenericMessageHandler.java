package org.bagab.mdb.jms11.patterns.genericmdb;

import javax.ejb.EJB;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;
import java.lang.reflect.ParameterizedType;

/**
 * @author Jonathan Wright
 */
public abstract class GenericMessageHandler<T> implements MessageListener {

    private final Class expectedType;
    @EJB
    private DeadLetterHandler deadLetterHandler;

    public GenericMessageHandler() {
        final ParameterizedType parameterizedType =
                (ParameterizedType) getClass().getGenericSuperclass();
        expectedType = (Class) parameterizedType.getActualTypeArguments()[0];
    }

    public void onMessage(final Message message) {
        if (matchesExpectedType(message)) {
            invokeConsume(message);
        } else {
            final Object payload = extractPayload(message);
            if (matchesExpectedType(payload)) {
                invokeConsume(payload);
            } else {
                deadLetterHandler.invalidMessageType(message);
            }
        }
    }

    protected DeadLetterHandler getDeadLetterHandler() {
        return deadLetterHandler;
    }

    protected abstract void consume(T obj);

    private boolean matchesExpectedType(final Object obj) {
        return obj != null && expectedType.isAssignableFrom(obj.getClass());
    }

    private Object extractPayload(final Message message) {
        try {
            if (message instanceof ObjectMessage) {
                return ((ObjectMessage) message).getObject();
            } else if (message instanceof TextMessage) {
                return ((TextMessage) message).getText();
            }
            return null;
        } catch (JMSException e) {
            throw new IllegalStateException("Error extracting payload", e);
        }
    }

    private void invokeConsume(final Object obj) {
        consume((T) obj);
    }
}
