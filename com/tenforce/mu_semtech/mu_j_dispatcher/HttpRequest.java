package com.tenforce.mu_semtech.mu_j_dispatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * ATTENTION: !!this class needs to be rewritten...!!
 *
 * it serves to represent an HTTP Request object, I suspect there
 * is some library out there that does what this does but [hopefully]
 * does it better.
 *
 * For now I will leave this in here as it is, the API largely speaks for
 * itself.
 *
 * I use this class to build a HTTP request by reading in lines from a ...
 * linereader. However I only implemented the class as far that I needed it
 * so do not expect it to be able to construct every type of HTTP Request. IT
 * will simply not. It can however be extended with the missing functionality
 * as I orginially intended this class to work just like that (give it a bunch
 * of lines and it will try to create a sensible HTTP request out of it).
 */
public class HttpRequest
{
	public static int HTTPREQUEST_GET = 0;
	public static int HTTPREQUEST_POST = 1;
	private int requestType; // 0: GET, 1: POST
	private String url;
	private String httpType;
	private String host;
	private boolean keepAlive = true;
	private String userAgent;
	private String origin;
	private List<String> header = new ArrayList<String>();
	private String accept;
	private String body;
	
	// NASTY CODE TO MAKE THINGS WORK FASTER!! FIX THIS BY PARSING
	// THE HTTP CORRECTLY!!!!!!
	private List<String> nastyMsgStash = new ArrayList<String>();
	
	public HttpRequest()
	{
		
	}
	
	public void addLine(String line)
	{
		if(line.startsWith("POST") || line.startsWith("GET"))
		{
			this.readRequestType(line);
			return;
		}
		if(line.startsWith("Accept:"))
		{
			this.readAcceptHeader(line);
			return;
		}
		this.nastyMsgStash.add(line);
	}
	
	public void readAcceptHeader(String line)
	{
		this.accept = line;
	}
	
	public void readRequestType(String line)
	{
		int fs = line.indexOf(' '), ls = line.lastIndexOf(' ');
		String requestString = line.substring(0, fs).trim();
		this.requestType = ((requestString.equals("GET"))?HTTPREQUEST_GET:HTTPREQUEST_POST);
		this.url = line.substring(fs, ls).trim();
		this.httpType = line.substring(ls, line.length()).trim();
	}
	
	public String getURL()
	{
		return this.url;
	}
	public void setURL(String URL)
	{
		this.url = URL;
	}
	public int getRequestType()
	{
		return this.requestType;
	}
	public void setRequestType(int r)
	{
		this.requestType = r;
	}
	public String getHttpType()
	{
		return this.httpType;
	}
	public void setHttpType(String httpType)
	{
		this.httpType = httpType;
	}
	public List<String> getRequestHeaders()
	{
		List<String> l = new ArrayList<String>();
		for(String h: this.header)
		{
			l.add(h);
		}
		return l;
	}
	public String getAcceptString()
	{
		return this.accept;
	}
	public void setAcceptString(String accept)
	{
		this.accept = accept;
	}
	
	public HttpRequest clone()
	{
		HttpRequest request = new HttpRequest();
		request.setHttpType(this.httpType);
		request.setRequestType(this.requestType);
		request.setURL(this.url);
		request.setAcceptString(this.accept);
		
		// adding the headers
		for(String h:this.header)
		{
			request.addLine(h);
		}
		
		// INSERTING THE NASTY STUFF
		for(String msg: this.nastyMsgStash)
		{
			request.addLine(msg);
		}
		return request;
	}
	
	public String toString()
	{
		String s = this.getRequestType()==HTTPREQUEST_GET?"GET ":"POST ";
		s += this.url + " " + this.httpType;
		s += "\n" + this.accept;
		for(String h:this.header)
			s += "\n" + h;
		for(String msg: this.nastyMsgStash)
			s += "\n" + msg;
		s += '\n';
		return s;
	}
}