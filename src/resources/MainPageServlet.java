package resources;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

@WebServlet("/MainPageServlet")
public class MainPageServlet extends HttpServlet {	
	
	private static final long serialVersionUID = 1L;

    public MainPageServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		//get data, if necessary filtered
		String query = "";
		if(request.getQueryString()!=null) query = java.net.URLDecoder.decode(request.getQueryString(), "UTF-8");
		
		Data data = new Data(request.getParameter("link_box_data"),"data","fieldName",new String[]{"title","locations"});
		JSONArray jdata = getQueryData(query, data); 		
		response.setContentType("text/html");
		//use json type as standard format
		String outputtype = "JSON";	
		//write data out
		int numlines = writeData(outputtype,response.getWriter(),jdata);		
		if (numlines==0) response.getWriter().println("No Data found");
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		//get data, if necessary filtered
		String query="";
		if(request.getQueryString()!=null) 
			query = request.getQueryString();
		query = java.net.URLDecoder.decode(query, "UTF-8");
		if(query.length()>5 && query.substring(0,5).compareTo("e.g. ")==0) query = query.substring(5);
		
		Data data = new Data(request.getParameter("link_box_data"),"data","fieldName",new String[]{"title","locations"});
		JSONArray jdata = getQueryData(query, data); 
		
		//write data out, take chosen format
		response.setContentType("text/html");
		String outputtype = request.getParameter("outputtype");			
		int numlines = writeData(outputtype,response.getWriter(),jdata);
		if (numlines==0) response.getWriter().println("No Data found");
	}
	
	// get JSON Data filtered with query string
	protected JSONArray getQueryData(String query, Data data) {
		String[] str = new String[0];
		if (query!="") str = query.split("&");
		JSONArray jdata;
		Map<String,String> map = new HashMap<String,String>();
		if (str.length>0 && str[0].contains("=")) {
			String f1="",f2="";
			for (int i=0;i<str.length;i++) {
				if (str[i].contains("=") && str[i].split("=")[0]!=null)
					f1 = str[i].split("=")[0];
				if (str[i].contains("=") && str[i].split("=")[1]!=null)
					f2 = str[i].split("=")[1];				
		        map.put(f1,f2);
			}		
			jdata = data.getTitleSubstrData(map);
		}
		else jdata = data.getAllData();
		return jdata;
	}
	
	// Data output in the choosen format
	protected int writeData(String outputtype, PrintWriter out, JSONArray jdata) {
		
		if (jdata.toArray().length==0) return 0;		
		switch (outputtype) {
        case "JSON" :
        	out.println(jdata);        	
        	break;
        case "html_simple" :
			for (int i = 0; i < jdata.size(); ++i) {
			    JSONObject obj = (JSONObject)jdata.get(i);  
			    @SuppressWarnings("rawtypes")
				Iterator itr = obj.entrySet().iterator();
				while(itr.hasNext())
					out.println(itr.next());
				out.println("<br>");
			}
			break;
        case "html_table" :
	         out.println("<style>\r\n" + 
	         		"#films {\r\n" + 
	         		"    font-family: \"Trebuchet MS\", Arial, Helvetica, sans-serif;\r\n" + 
	         		"    border-collapse: collapse;\r\n" + 
	         		"    width: 100%;\r\n" + 
	         		"}\r\n" + 
	         		"\r\n" + 
	         		"#films td, #films th {\r\n" + 
	         		"    border: 1px solid #ddd;\r\n" + 
	         		"    padding: 8px;\r\n" + 
	         		"}\r\n" + 
	         		"\r\n" + 
	         		"#films tr:nth-child(even){background-color: #f2f2f2;}\r\n" + 
	         		"\r\n" + 
	         		"#films tr:hover {background-color: #ddd;}\r\n" + 
	         		"\r\n" + 
	         		"#films th {\r\n" + 
	         		"    padding-top: 12px;\r\n" + 
	         		"    padding-bottom: 12px;\r\n" + 
	         		"    text-align: left;\r\n" + 
	         		"    background-color: #4CAF50;\r\n" + 
	         		"    color: white;\r\n" + 
	         		"}\r\n" + 
	         		"</style>");
	         if (!jdata.isEmpty())
	        	 out.println("<table id=\"films\"><tr><th>"+((JSONObject)jdata.get(0)).keySet().toArray()[0]+"</th><th>"+((JSONObject)jdata.get(0)).keySet().toArray()[1]+"</th></tr><tr>");
	       	 for (int i = 0; i < jdata.size(); ++i) {	       		 
	             @SuppressWarnings("unchecked")
				Collection<Object> obj = ((JSONObject)jdata.get(i)).values();  
	             @SuppressWarnings("rawtypes")
				 Iterator itr = obj.iterator();
	             while(itr.hasNext()) {
	                 out.println("<td>"+(itr.next()) + "</td>");
	              }
	             out.println("</tr>");
	    	 }
	       	out.println("</table>");
    	 break;
		}
		return jdata.toArray().length;
	}
}
