package resources;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Data {

	// define class variables and constants
	JSONArray data = new JSONArray();
	public static final String[] static_data_column_names = {"title","locations"};
	public static final String static_JSONdataentrypoint = "data";
	public static final String static_JSONdataTableKey = "fieldName";	
	public static final String static_url = "https://data.sfgov.org/api/views/yitu-d5am/rows.json?accessType=DOWNLOAD";
			
	// class constructor
	public Data(String url, String JSONdataentrypoint, String JSONdataTableKey, String[] data_column_names) {	
		
		// simple try catch, should be extended in final version
        try {       	        	
        	if (url==null) url = static_url;
        	if (data_column_names==null) data_column_names = static_data_column_names;
        	if (JSONdataentrypoint==null) JSONdataentrypoint = static_JSONdataentrypoint;
        	if (JSONdataTableKey==null) JSONdataTableKey = static_JSONdataTableKey;
        	
        	// get Data from URL
        	URL urllink = new URL(url);      
        	BufferedReader br = new BufferedReader(new InputStreamReader(urllink.openStream()));
        	String line,str="";
        	while ((line = br.readLine()) != null) { str += line; }
        	br.close();    	
        	// parse data to JSON Objects
        	JSONParser parser = new JSONParser();
        	Object obj = parser.parse(str);
        	data = getJSONData(obj,JSONdataentrypoint,JSONdataTableKey,data_column_names);
        	
        } catch (Exception e) { e.printStackTrace(); }
	}	
        
    @SuppressWarnings("unchecked")
	private JSONArray getJSONData(Object obj,String JSONdataentrypoint,String JSONdataTableKey,String[] data_column_names) {
    	JSONArray internaldata = new JSONArray();
    	JSONObject jsonObject = (JSONObject) obj;
    	
    	// get data indices from JSON table structure
    	Map<String,Integer> ret = searchObjectRekursiv(jsonObject,JSONdataTableKey,new HashMap<String,Integer>(),0);
    	int[] data_column_indices = new int[data_column_names.length];
    	int i=0;
    	for (String n:data_column_names) {
    		data_column_indices[i]=ret.get(n);
    		i++;
    	}
    			
    	// create JSON Array out of JSON data
    	JSONArray jsonarray = (JSONArray) jsonObject.get(JSONdataentrypoint);
        for(i=0;i<jsonarray.size();i++){
        	JSONArray o = (JSONArray) jsonarray.get(i);
        	Map<String, String> map =  new HashMap<String, String>();
        	int j=0;
        	for (int ci : data_column_indices) {
        		map.put(data_column_names[j], (o.get(ci)==null) ? "" : o.get(ci).toString());
        		j++;
        	}
        	JSONObject jso = new JSONObject(map);
        	internaldata.add((JSONObject)jso);  	
        }
        return internaldata;
    }
	
    // find needed subdata for given indices in JSON data
    private Map<String,Integer> searchObjectRekursiv(JSONObject jsonObject, String searchString, Map<String,Integer> resultMap, Integer count) {
    	
    	JSONObject jo=jsonObject;    
    	for (Object k : jsonObject.keySet()) {  
    		if (k!=null) {
    			if (jsonObject.get(k).getClass()==JSONObject.class) {
    				jo = (JSONObject) jsonObject.get(k);
    				if (jo.containsKey(searchString)) {
    					resultMap.put(jo.get(searchString).toString(), count);
    					count++;
    				}
					searchObjectRekursiv(jo,searchString,resultMap,count);
    			}
				else {
					if (jsonObject.get(k)!=null && jsonObject.get(k).getClass()==JSONArray.class) {
						JSONArray ja = (JSONArray) jsonObject.get(k);
						for(int i = 0; i < ja.size(); i++) {
					        Object o = ja.get(i);
					        if (o.getClass()==JSONObject.class) {
					    	    jo = (JSONObject) o;
					    	    if (jo.containsKey(searchString)) {
			    					resultMap.put(jo.get(searchString).toString(), count);
			    					count++;
			    			  	}
					    	    searchObjectRekursiv((JSONObject)o,searchString,resultMap,count);
					        }
						}					
					}
				}
    		}
    	}
    	return resultMap;
    }
        
	// get all JSON data of input file
	public JSONArray getAllData() {
		return data;
	}
	
	// get filtered JSON data of input file
	@SuppressWarnings("unchecked")
	public JSONArray getTitleSubstrData(Map<String,String> map) {
		JSONArray ret = new JSONArray();
        for (int i = 0; i < data.size(); ++i) {
            JSONObject obj = (JSONObject)data.get(i);       
            Boolean contains = true;
            for (Map.Entry<String,String> entry : map.entrySet()) {
            	if (obj.containsKey(entry.getKey())) {
		            if (!obj.get(entry.getKey()).toString().contains(entry.getValue())) {	         
		            	contains = false;
		            }
            	}
            	else contains=false;
	        }
            if(contains) ret.add(obj);
	    }
		return ret;
	}
	
}
