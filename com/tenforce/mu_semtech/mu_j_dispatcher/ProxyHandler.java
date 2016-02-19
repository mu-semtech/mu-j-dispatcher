package com.tenforce.mu_semtech.mu_j_dispatcher;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

/*
A generic proxy handler class. It will either try to handle a request based on some
configurations. It will either send the request as complete as possible to another
location (in this case the proxy handler acts as a dispatcher) or it will answer the
request with a http formatted string.

See the constructor for idiomatic usages
 */
public class ProxyHandler 
{
	// the start of the path, this is necessery to check if we can answer a given
	// request and also to replace this in the original request with something else
	private String pathStart = "";

	// the url to which this request needs to be dispatched
	private String redirect = "";

	// the reply we want to give (only use this if redirect is empty)
	private String reply = "";

	// if the request as a whole should match or just start with the pathStart string
	private boolean startsWith;

	// the maximum size we will a buffer allow to have
    private static final int BUFFER_SIZE = 32768;

    /*
    constructor
    This constructor will try to 'configure' this proxy handler. After this the proxy
    handler will EITHER handle redirects (if this.redirect is not empty) or it will
    reply all requests with the same string (if this.reply is not empty) but never both.

    If you try to instantiate it with bot a redirect and a reply it throw an excpetion and
    it will not be instantiated.

    Idiomatic usage for a dispatching proxy
    new ProxyHandler("/toGoogle", "http://www.google.com", "")
    this will create a proxy that will if you surf to /toGoogle on this local host it will
    dispatch your request to www.google.com.

    Idiomatic usage for a static text return
    new ProxyHandler("/hello","", "world", false)
    if you surf to localhost/hello, you will see the text world in your browser

    @param pathStart the route at which this proxyhandler should listen
    @param redirect the url to which the request should be redirected
    @param reply the textual reply (in HTML) we want the user to see
    @param startsWith the route should start with the param pathStart or exactly match it
    @result the proxyhandler is fully configured and can handle requests
    @result if both a redirect and a reply are passed that this constructor will throw an
             exception.
     */
	public ProxyHandler(String pathStart, String redirect, String reply, boolean startsWith) throws Exception
	{
		if((!(redirect.isEmpty() || reply.isEmpty())) && (!(redirect.isEmpty() && reply.isEmpty())))
		{
			throw new Exception("Cannot instantiate proxy handler, redirect or reply must be \"\"!");
		}
		this.pathStart = pathStart;
		this.redirect = redirect;
		this.reply = reply;
		this.startsWith = startsWith;
	}

    /*
    short version of the constructor in which startsWith is quickly set to true. For more information
    check the expanded constructor
     */
    public ProxyHandler(String pathStart, String redirect, String reply) throws Exception
    {
        this(pathStart, redirect, reply, true);
    }

    /*
    returns a string representation of this proxy handler
     */
	public String toString()
	{
		return "Proxyhandler for: " + this.pathStart + "  || " + ((this.startsWith)?(this.redirect):(this.reply));
	}

    /*
    this function returns true if the request can be handled by this handler, ie. if the
    routes path startsWith (or exactly matches if needed) the pathStart variable

    @param request the request that should be handler
    @return true if this request follows the expectations for this handler
     */
    private boolean canHandleThreadRequest(HttpRequest request)
    {
    	if(this.startsWith && request.getURL().startsWith(this.pathStart))
    		return true;
    	
    	if(request.getURL().equals(this.pathStart))
    		return true;
    	
    	return false;
    }

    /*
    this function will handle the request if possible.

    @param clientSocket the socket that was accepted by this server
    @param request the request that should be handled
    @return true if the request was handled correctly
    @result the client socket either got the response from the server to which it's request
            was dispatched or it got the http formatted text directly.
     */
	public boolean handleThreadRequest(Socket clientSocket, HttpRequest request) throws Exception
	{
		if(!this.canHandleThreadRequest(request))
		{
			return false;
		}
		
		if(!this.reply.isEmpty())
		{
			PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
			out.println("HTTP/1.1 200 OK");
			out.println("Content-Type: text/html");
			out.println("\r\n");
			out.println(reply);
			out.flush();
			out.close();
			return true;
		}
		
		if(!this.redirect.isEmpty())
		{
			String urlString = this.redirect + (request.getURL().substring(this.pathStart.length(), request.getURL().length()));
			HttpRequest nRequest = request.clone();
			System.out.println("\n[*] REDIRECTING TO:\n" + this.redirect);
			System.out.println("[*] using http:\n" + nRequest);
			handleProxyRedirect(clientSocket, nRequest, this.redirect);
			return true;
		}
		
		throw new Exception("com.tenforce.mu_semtech.mu_j_dispatcher.Proxy handler(" + this.pathStart + ", " + this.redirect + ", " + this.reply + ") could not handle thread input");
	}

    /*
    This function handles the dispatching of the request to another server and returns
    whatever that server answers to the passed socket.

    @param socket an active socket
    @param request the request that should be sent to the redirectURL
    @param redirectURL the url to which this request should be despatched
    @result the socket will receive the response that the given server replied
     */
	private void handleProxyRedirect(Socket socket, HttpRequest request, String redirectURL)
	{
		try
		{
			URL url = new URL(redirectURL);
			URLConnection connection = (URLConnection)url.openConnection();

			System.out.println("[>>] Connecting to " + redirectURL);
			BufferedReader inFromServer = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			PrintWriter outToClient = new PrintWriter(socket.getOutputStream());
			String inputFromServer;

            Map<String, List<String>> headers = connection.getHeaderFields();
            String headersString = "";

            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                if(entry.getKey() != null) {
                    for(String value: entry.getValue()) {
                        if(!value.equals("chunked")) {
                            headersString += entry.getKey() + ": ";
                            headersString += value + "\n";
                        }
                    }
                }
                else
                {
                    String headersStart = "";
                    for(String value:entry.getValue())
                    {
                        headersStart += value + " ";
                    }
                    headersString = headersStart + "\n" + headersString;
                }
            }

            System.out.println("[< headers]\n" + headersString);
            outToClient.println(headersString);


				System.out.println("[<] Receiving:");
				while ((inputFromServer = inFromServer.readLine()) != null) {
					System.out.println("[<] " + inputFromServer);
					outToClient.println(inputFromServer);
				}
				System.out.println("[*] Sending to client...");
				outToClient.flush();


			System.out.println("[*] Closing connections");
			inFromServer.close();
			outToClient.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
