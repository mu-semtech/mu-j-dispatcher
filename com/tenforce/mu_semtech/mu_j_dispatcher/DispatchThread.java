package com.tenforce.mu_semtech.mu_j_dispatcher;

import java.net.*;
import java.io.*;
import java.util.*;

/*
the DispatchThread class represents a thread that will either return a string (formatted as http)
or it will dispatch the request to a certain location. The configuration for this dispatch thread
is read from a file as a comma separated list.
 */
public class DispatchThread extends Thread 
{
	// to client socket that our server accepted
    private Socket socket = null;

	// list of proxyhandlers
    private List<ProxyHandler> proxyHandlers = null;

	/*
	constructor
	@param socket a socket that has an active (open) connection and that was accepted by the server
	@result the thread has a socket an all proxyhandlers have been set
	 */
    public DispatchThread(Socket socket) 
    {
        super("com.tenforce.mu_semtech.mu_j_dispatcher.DispatchThread");
        this.socket = socket;
        this.setProxyHandlers();
    }

	/*
	loads the proxy handlers into memory

	to be done
	    the dispatchers should not be hard coded but loaded out of a file
	 */
    private void setProxyHandlers()
    {
    	this.proxyHandlers = new ArrayList<ProxyHandler>();
    	try
    	{
    		this.proxyHandlers.add(new ProxyHandler("/toGoogle", "http://www.google.com", ""));
    		this.proxyHandlers.add(new ProxyHandler("/hello","", "world", false));
    		this.proxyHandlers.add(new ProxyHandler("/","", "plug", false));
    		this.proxyHandlers.add(new ProxyHandler("/session", "http://login/", ""));
    		this.proxyHandlers.add(new ProxyHandler("/comments", "http://comments/", ""));
    		this.proxyHandlers.add(new ProxyHandler("/catalogs/", "http://resource/catalogs", ""));
    		this.proxyHandlers.add(new ProxyHandler("/catalogs", "http://resource/catalogs", ""));
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    }

	/*
	This function iterates over the list of proxy handlers in chronological order
	handing each one of them the request that the server has received.
	When one proxy can handle the request the iteration will stop, in other words
	it looks for the first handler that is able to handle the request.

	@param request the request that was passed.
	@result the request was handled by only one handler
	 */
    private void iterateOverProxyHandlers(HttpRequest request)
    {
	System.out.println("Reveived input on following line:\n" + request.toString());
    	for(ProxyHandler p : this.proxyHandlers)
    	{
    		System.out.print("Trying proxy handler " + p);
    		try{
					if(p.handleThreadRequest(this.socket, request))
					{
						System.out.println(" proxy worked!");
						return;
					}
					System.out.println(" proxy did not work...");
    		}
    		catch(Exception e)
    		{
    			e.printStackTrace();
    		}
    	}

        // if none of the proxy handlers could handle this request we send a 404
        try {
            PrintWriter out = new PrintWriter(this.socket.getOutputStream());
            out.println("HTTP/1.1 404 NOT FOUND");
            out.flush();
            out.close();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

	/*
	standard function that all classes that inherit from Thread should override.
	After the connection has been accepted this function will be called automatically.
	It will construct the HTTPRequest and try to find a proxy that can handle it.
	 */
    public void run() 
    {
    	System.out.println("connection accepted");
        try 
        {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String request = "";
           
            String inputLine = in.readLine();
            
            HttpRequest httpRequest = new HttpRequest();
            
            int nZeroLines = 0, MaxZeroLines = 1;
            
            while (inputLine != null) 
            {
            	if(inputLine.length() == 0)++nZeroLines;
            	if(nZeroLines>MaxZeroLines)break;
            	System.out.println("[*] inline: " + inputLine);
            	request += inputLine;
            	httpRequest.addLine(inputLine);
            	if(httpRequest.getRequestType()==HttpRequest.HTTPREQUEST_GET)
            		MaxZeroLines = 0;
            	inputLine = in.readLine();
            }
            
            httpRequest.setAcceptString("Accept: application/vnd.api+json");

            this.iterateOverProxyHandlers(httpRequest);
            
            System.out.println(request);
            
            socket.close();
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
    }
}
