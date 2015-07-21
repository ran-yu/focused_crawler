package entityFilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;

public class Seeds {
	private Map<String, String> _terms;
	private Map<String, Set<String> > _properties;
	//private int _propertiesize;

	public Seeds(String input, String seedlist, Set<String> properties)
			throws IOException {
		_terms = new HashMap<String, String>();
		_properties = new HashMap<String, Set<String>>();
	//	_propertiesize = 0;

		readSeeds(seedlist);
		readSeedProperties(input, properties);
	}

	private void readSeedProperties(String path, Set<String> properties)
			throws IOException {
		FileInputStream is = new FileInputStream(path);
		NxParser nxp = new NxParser(is);

		for (Node[] nxx : nxp) {
			
			if (4 == nxx.length && (_terms.containsKey(nxx[0].toString()))) {
				// add property type for uri
				String key = nxx[0].toString();
				String prop = nxx[1].toString();
				
				Set<String> tmp_set = _properties.get(key);
				tmp_set.add(prop);
				_properties.put(key, tmp_set);
				
				// add term sequence for uri
				if (properties.contains(prop)) {// check type
					String tmpStr = _terms.get(key).toString();
					tmpStr = tmpStr.concat(nxx[2].toString());
					//System.out.println(tmpStr);
					_terms.put(key, tmpStr);
				}
			}
		}
	}

	private Integer readSeeds(String fileName) throws IOException {
		Integer cnt = 0;
		File seedsFile = new File(fileName);
		BufferedReader reader = new BufferedReader(new FileReader(seedsFile));
		String seedUri = null;
		while ((seedUri = reader.readLine()) != null) {
			_terms.put(seedUri, "");
			_properties.put(seedUri, new HashSet());
			// System.out.println(seedUri);
			cnt++;
		}
		reader.close();
		System.out.println("Notice:\tGot " + cnt + " seeds.");
		return cnt;
	}
	
	boolean containsKey(String testKey){
		return _terms.containsKey(testKey);
	}
	public Map<String, String> getTerms(){
		return _terms;
	}
	
	public Map<String, Set<String> > getProperties(){
		return _properties;
	}
	//for debug 
	public void printSeeds(){
		for(Entry<String, String> entry: _terms.entrySet()){
			String key = entry.getKey();
			String term = entry.getValue();
			System.out.println("Seed\t"+ key);
			System.out.println("Term\t"+ term);
			Set<String> tmp_set = _properties.get(key);
			for(String prop: tmp_set){
				System.out.println("Property\t"+prop);
			}
		}
	}
}
