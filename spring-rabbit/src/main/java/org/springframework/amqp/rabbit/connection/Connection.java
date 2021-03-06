/**
 * 
 */
package org.springframework.amqp.rabbit.connection;

import java.io.IOException;

import com.rabbitmq.client.Channel;

/**
 * @author Dave Syer
 *
 */
public interface Connection {

    /**
     * Create a new channel, using an internally allocated channel number.
     * @param transactional true if the channel should support transactions
     * @return a new channel descriptor, or null if none is available
     * @throws IOException if an I/O problem is encountered
     */
    Channel createChannel(boolean transactional) throws IOException;

    /**
     * Close this connection and all its channels
     * with the {@link com.rabbitmq.client.AMQP#REPLY_SUCCESS} close code
     * and message 'OK'.
     *
     * Waits for all the close operations to complete.
     *
     * @throws IOException if an I/O problem is encountered
     */
    // TODO: throws AmqpException
    void close() throws IOException;

}
