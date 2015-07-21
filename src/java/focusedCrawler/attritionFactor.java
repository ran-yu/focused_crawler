package focusedCrawler;

import java.io.BufferedReader;
import java.lang.Math;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

public class attritionFactor {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		Map<String, Double> attritions = new HashMap<String, Double>();
		getAttritions("Rihanna", attritions);
	}
	public static void normalizeAttritions(String seedpath, Map<String, Double> attritions) throws IOException{
		File seedsFile = new File(seedpath);
		BufferedReader reader = new BufferedReader(new FileReader(seedsFile));
		String seedUri = null;
	
		
		while( (seedUri = reader.readLine()) != null){
		    attritions.put(seedUri, 1.0);
		}
		reader.close();
	}
	public static void getAttritions(String seedpath, Map<String, Double> attritions) throws IOException{
		
		String sparqlEndpoint = "http://dbpedia-live.openlinksw.com/sparql";
		
		File seedsFile = new File(seedpath);
		BufferedReader reader = new BufferedReader(new FileReader(seedsFile));
		String seedUri = null;
		int cnt = 0;
		
		Double total_katz = (double) 0;
		while( (seedUri = reader.readLine()) != null){
			
			String sparqlQuery = "" +
				"SELECT (COUNT(DISTINCT ?n ) AS ?no) WHERE {" +
			    "{ ?n ?p <" + seedUri + ">  } " +
			    "UNION " +
			    "{ <" + seedUri + "> ?p2 ?n } " +
			    "FILTER(!isBlank(?n) && !isLiteral(?n)) " +
			"}";
			Query query = QueryFactory.create(sparqlQuery, Syntax.syntaxARQ) ;
			QuerySolutionMap querySolutionMap = new QuerySolutionMap();

		    QueryEngineHTTP httpQuery = new QueryEngineHTTP(sparqlEndpoint,query);
		    // execute a Select query
		    ResultSet results = httpQuery.execSelect();
		    
		    String katz_str = "";
		    while (results.hasNext()) {
		        QuerySolution solution = results.next();
		        katz_str = solution.get("no").asLiteral().getLexicalForm();
		      }
		    
		    System.out.println(seedUri + "\t" + katz_str);
		    Double katz = Double.valueOf(katz_str);
		    attritions.put(seedUri, katz);
		    total_katz += katz;
			cnt++;
		}
		if(total_katz == 0){
			System.out.print("ERROR:\tCalculate katz error.");
			attritions.clear();
			return;
		}
		//calculate katz
		for(Entry<String, Double> entry: attritions.entrySet()){
			Double attrition_tmp;
			if(entry.getValue() == 0.0){
				attrition_tmp = 0.0;
			}
			else attrition_tmp = 1/Math.log(total_katz/entry.getValue());
			attritions.put(entry.getKey(), attrition_tmp);
			System.out.println(entry.getKey() + "\t" + attrition_tmp);
		}
		
		reader.close();	
	}

}
