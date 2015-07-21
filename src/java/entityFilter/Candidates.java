package entityFilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;

public class Candidates {
	private Map<String, String> _terms;
	private Map<String, Set<String>> _properties;
	// private int _propertiesize;
	private Set<String> _filterDomain;

	public Candidates(String input, Set<String> properties) throws IOException,
			URISyntaxException {
		_terms = new HashMap<String, String>();
		_properties = new HashMap<String, Set<String>>();
		_filterDomain = new HashSet<String>();

		_filterDomain.add("xmlns.com");
		_filterDomain.add("www.w3c.org");
		_filterDomain.add("w3c.org");
		_filterDomain.add("w3.org");
		_filterDomain.add("www.w3.org");
		_filterDomain.add("cyc.com");
		_filterDomain.add("bklyn-genealogy-info.com");
		_filterDomain.add("wdl.org ");
		_filterDomain.add("time.com");
		_filterDomain.add("umbel.org");
		_filterDomain.add("purl.org");
		_filterDomain.add("schema.org");
		// _filterDomain.add("dbpedia.org");
		// _propertiesize = 0;

		readProperties(input, properties);
	}

	private void readProperties(String path, Set<String> properties)
			throws IOException {
		FileInputStream is = new FileInputStream(path);
		NxParser nxp = new NxParser(is);

		for (Node[] nxx : nxp) {
			if (4 == nxx.length) {
				// add property type for uri
				String key = nxx[0].toString();
				String prop = nxx[1].toString();

				URI u = null;
				try {
					u = new URI(key);
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (u != null) {
					String host = u.getHost();

					if (_filterDomain.contains(host)) {
						// System.out.println(host);
						continue;
					}
				}
				if (prop.contains("sameAs"))
					continue;

				if (!_terms.containsKey(key)) {
					_terms.put(key, "");
					_properties.put(key, new HashSet());
				}

				// add property name of entity
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

	boolean containsKey(String testKey) {
		return _terms.containsKey(testKey);
	}

	public Map<String, String> getTerms() {
		return _terms;
	}

	public Map<String, Set<String>> getProperties() {
		return _properties;
	}

	// for debug
	public void printCandidates() {
		for (Entry<String, String> entry : _terms.entrySet()) {
			String key = entry.getKey();
			String term = entry.getValue();
			System.out.println("candidate\t" + key);
			System.out.println("Term\t" + term);
			Set<String> tmp_set = _properties.get(key);
			for (String prop : tmp_set) {
				System.out.println("Property\t" + prop);
			}
		}
	}
}
