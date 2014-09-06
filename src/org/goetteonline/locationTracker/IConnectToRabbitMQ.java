package org.goetteonline.locationTracker;

import java.io.IOException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class IConnectToRabbitMQ {
	protected String server, username, password;
    public String mExchange;

    public String getServer() {
		return server;
	}

	protected Channel mModel = null;
    protected Connection  mConnection;

    protected boolean Running ;

    protected  String MyExchangeType ;

    /**
     *
     * @param server The server address
     * @param exchange The named exchange
     * @param exchangeType The exchange type name
     */
    public IConnectToRabbitMQ(String server, String username, String password, String exchange, String exchangeType)
    {
        this.server = server;
        this.username = username;
        this.password = password;
        
        mExchange = exchange;
        MyExchangeType = exchangeType;
    }

    @Override
	public boolean equals(Object o) {
    	IConnectToRabbitMQ other = (IConnectToRabbitMQ)o;
		return (other.server.endsWith(server) && other.username.equals(username) && other.password.equals(password));
	}

	public void setServer(String server) {
		this.server = server;
	}

	public void dispose()
    {
        Running = false;

          try {
              if (mConnection!=null)
                  mConnection.close();
              if (mModel != null)
                  mModel.abort();
          } catch (IOException e) {
              e.printStackTrace();
          }

    }

    /**
     * Connect to the broker and create the exchange
     * @return success
     */
    public boolean connectToRabbitMQ()
    {
        if(mModel!= null && mModel.isOpen() )//already declared
            return true;
        try
        {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setHost(server);
            connectionFactory.setUsername(username);
            connectionFactory.setPassword(password);
            connectionFactory.setConnectionTimeout(2000);
            mConnection = connectionFactory.newConnection();
            mModel = mConnection.createChannel();
            mModel.exchangeDeclare(mExchange, MyExchangeType, true);

            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

	public Channel getmModel() {
		return mModel;
	}
}
