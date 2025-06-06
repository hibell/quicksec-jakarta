package com.ibm.ws.svt.quicksec.wclient;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;

import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.RunAs;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.authentication.mechanism.http.BasicAuthenticationMechanismDefinition;
import jakarta.security.enterprise.identitystore.LdapIdentityStoreDefinition;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.HttpConstraint;
import jakarta.servlet.annotation.HttpMethodConstraint;
import jakarta.servlet.annotation.ServletSecurity;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletConfig;
import com.ibm.ws.svt.quicksec.EJB.Delegate;
//import com.ibm.ws.webcontainer.servlet.ServletConfig;

//@ApplicationScoped
@DeclareRoles({"BasicLogin_WebUsers","BasicLoginDummy_WebUsers","RunAsSpecified_servlet"})
@RunAs("RunAsSpecified_servlet")
@WebServlet(name="QuickSecBaisc",urlPatterns={"/webclient1","/QuickSecBasic"})
@ServletSecurity(value=@HttpConstraint(rolesAllowed={"BasicLogin_WebUsers"}), httpMethodConstraints={@HttpMethodConstraint(value="POST",rolesAllowed={"delegateUsers"})})

@BasicAuthenticationMechanismDefinition(
        realmName = "basicRealm"
)

/*@LdapIdentityStoreDefinition(
        url = "ldap://isdsldapsvt2.rtp.raleigh.ibm.com:389/",
        callerBaseDn = "o=ibm,c=us",
        callerSearchBase = "o=ibm,c=us",
        //callerSearchFilter = "(&(uid=%v)(objectclass=ePerson))",
        	callerSearchFilter =	"(&(objectclass=ePerson)(uid=%s))",
        callerNameAttribute = "uid",
        	groupSearchFilter = "ibm-allGroups:member;ibm-allGroups:uniqueMember;groupOfNames:member;groupOfUniqueNames:uniqueMember",	
        groupNameAttribute = "cn",
         priority=90 // default is 80
	
     ) */

/**
 * Servlet implementation class for Servlet: QuickSecForm
 *
 */

public class QuickSecBasic extends HttpServlet  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String rtnString = "Msg: <br>";
	boolean print_out = false;

//	@EJB(name="delegateRef")
	@EJB 
	private Delegate myDelegate;
	boolean callEJB = false;

	private String testAccessNotAllowed(Delegate quickSession,
			String methodName) {
		String rtnString2 = "";
		@SuppressWarnings("rawtypes")
		Class parTypes[] = new Class[0];
	//	parTypes[0] = String.class;
		//parTypes[1] = String.class;

		Object parameters[] = new Object[0];
		//parameters[0] = host;
		//parameters[1] = newLookUp;

		try {
			Method testMethod = quickSession.getClass().getMethod(methodName,
					parTypes);
			try {
				rtnString2 = (String) testMethod.invoke(quickSession, parameters);
				throw new Exception("Test Failed as this method call should not have been allowed, yet it was executed with out exception.");
			} catch (java.lang.reflect.InvocationTargetException ite) {
				Throwable cause = ite.getCause();
				if (cause == null) {
					throw ite;
				}
				if (cause instanceof jakarta.ejb.EJBAccessException) {
					throw (jakarta.ejb.EJBAccessException) cause;
				} else {
					throw ite;
				}
			}
		} catch (jakarta.ejb.EJBAccessException ejbAE) {
			rtnString2 = "Test Passed: With EJBAccessException as expected";
		} catch (Exception e) {
			rtnString2 = "Test Failed: Unexpected Exception: " + e.toString();
			System.err.println("Found unexpected exceptions when expecting jakarta.ejb.EJBAccessException");
			e.printStackTrace();
		}
		return rtnString2;
	}
	
	public void doGet (HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
	{			

		PrintWriter out;

		//Accessing the LOGINTIME attribute set during the LoginFilter.
		String successMsg = null;
		String timeStamp = (String) req.getSession().getAttribute("LOGINTIME");

		if (timeStamp != null) {
			successMsg = "Successful login on " + timeStamp;
		}
		else {
			successMsg = "Successful login";
		} 	

		res.setContentType("text/html");
		res.setHeader("Pragma", "No-cache");
		res.setHeader("Cache-Control", "no-cache");
		res.setDateHeader("Expires",0);

		out = res.getWriter();
		try {
			out.println("<HTML><TITLE>QuickSec Security Test App</TITLE><BODY>");
			out.println("<H1>QuickSec Security Test App</H1>");
			out.println("<font size=\"2\"><strong> Welcome user: " + req.getRemoteUser()+ "</strong></font>");
			/*out.println("<FORM METHOD=POST ACTION=\"ibm_security_logout\" NAME=\"logout\">");
			out.println("<font size=\"2\"><strong> Welcome user: " + req.getRemoteUser()+ "</strong></font>");
			out.println("<input type=\"submit\" name=\"logout\" value=\"Logout\"> ");
			out.println("<INPUT TYPE=\"HIDDEN\" name=\"logoutExitPage\" VALUE=\"webclient\"> ");*/


			out.println("<p>" + successMsg);
			out.println("</p></FORM>");
			out.println("<FORM METHOD=POST ACTION=\""+ req.getRequestURI() + "\">");
			out.println("<p>Click Below to call EJB methods");
			out.println("<p><INPUT TYPE=SUBMIT VALUE=\"Go & Get..\">");
			out.println("<p><p>"+rtnString);
			//reset the rtnString
			rtnString = "Msg: <br>";
			out.println("</FORM>");

			String securl =res.encodeURL("/QuickSecBasic855L/secSession");
			String unsecurl =res.encodeURL("/QuickSecBasic855L/unsecSession");
			out.println("<br><A href=\"" + securl + "\">Get Session Info by calling servlet with secure URI</A>");
			out.println("<br><A href=\"" + unsecurl + "\">Get Session Info by calling servlet with unsecure URI</A>");
			//ut.println("<br><A href=\"/QuickSecForm/unsecSession\">Get Session Info by calling servlet with unsecure URI</A>");
			out.println("</BODY></HTML>");

		} catch ( Exception e )	{
			rtnString = rtnString + "<p> Exception: " + e.toString();
			out.println("<p><p>"+rtnString);
			System.out.println("Inside the catch"+rtnString);
			out.close();
			e.printStackTrace();
		}
		out.close();           
	}

	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		// TODO Auto-generated method stub

		//System.out.println("I am in doPost");
		rtnString = "Msg: <br>";
		try
		{
			rtnString = rtnString + "<br> User log-in: " + req.getRemoteUser();                 
			rtnString = rtnString + "<br> <B>webclient servlet 'RunAs' id: Below userid should be same as user mapped to RunASSpecified_servlet </B>";
			rtnString = rtnString + "<br>    " + myDelegate.getMessagePermitAll();
		//	rtnString = rtnString + "<br> <B>  Below userid should be same as the server id (SYSTEM_IDENTITY): </B>";
		//rtnString = rtnString + "<br>" + myDelegate.getMessageForSystem ();
			rtnString = rtnString + "<br>    " + myDelegate.getMessage();		
			rtnString = rtnString + "<br> <br> <B>delegateEJB Bean Level 'RunAs' id: Below userid should be same as same as user mapped to delegateEJBBeanLevelRunAs </B>";
			rtnString = rtnString + "<br>    " + myDelegate.getMessageRunAsBeanLevel();
			rtnString = rtnString + "<br> <br> <B>delegateEJB Method Level 'RunAs' ids (WAS extension): </B>";
			rtnString = rtnString + "<br> <B>  Below userid should be same as the server id (SYSTEM_IDENTITY): </B>";
			try {
		    	rtnString = rtnString + "<br>" + myDelegate.getMessageForSystem ();
		    } catch (jakarta.ejb.EJBAccessException ejbAE){
		    	rtnString = rtnString + "<br>" + "SYSTEM_IDENTITY not supported";
			}
			rtnString = rtnString + "<br> <B>  Below userid should be same as the client user id (CLIENT_IDENTITY) </B>";
			rtnString = rtnString + "<br>" + myDelegate.getMessageForClient ();
			rtnString = rtnString + "<br> <B>  Below userid should be same as user mapped to delegateEJBMethodLevelRunAs (SPECIFIED_IDENTITY)</B>";
			rtnString = rtnString + "<br>" + myDelegate.getMessageForSpecifiedUser ();
			
			//ACCESS DENIED TESTS!  Note the Negative is positive in these cases..
						
			rtnString = rtnString + "<br> <br> <B>The following methods should fail.. </B>";
			rtnString = rtnString + "<br> <br> <B>This method getMessageForAnotherSpecifiedUser is excluded in DD so it should fail. </B>";
			rtnString = rtnString + "<br>" + testAccessNotAllowed(myDelegate, "getMessageForAnotherSpecifiedUser");  
			//rtnString = rtnString + "<br>" + myDelegate.getMessageForAnotherSpecifiedUser();
			rtnString = rtnString + "<br> <br> <B>This method getMessageDenyAll with @Deny annotation should fail. </B>";
			rtnString = rtnString + "<br>" + testAccessNotAllowed(myDelegate, "getMessageDenyAll"); 
			//rtnString = rtnString + "<p><br>" + myDelegate.getMessageDenyAll (); 
			//rtnString = rtnString + "<br> <br> <B>This method is excluded in DD so it should fail. </B>";
			//rtnString = rtnString + "<br>" + myDelegate.getMessageForAnotherSpecifiedUser (mHost, newlookup);

		} catch ( Exception e )	{
			rtnString = rtnString + "<p> Exception: " + e.toString();
			e.printStackTrace();
		}
		doGet(req,res);
	}
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		//System.out.println("I am in init");
		String is_out = System.getProperty("QuickSec.PrintOut");
		if (is_out != null)
			print_out = true;
	}

}
