package entityFilter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;

import static similarityAssessment.JaccardSimilarity.avgSeedPropertySimilarity;
import static similarityAssessment.JaccardSimilarity.avgSimilarity;

/*import com.aliasi.tokenizer.EnglishStopTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;*/


public class termRelevanceFilter {

    private Map<String, Vector<Double>> _result;
    private String _seedlist;

    public termRelevanceFilter(String seedlist) throws IOException {
        _result = new HashMap<String, Vector<Double>>();
        _seedlist = seedlist;
    }


    public void relevanceFilter(Seeds seeds, Candidates candidates, double propAlpha, double termAlpha,
                                Map<String, Double> attritions, Set<String> stop_words) throws IOException {
        //HashMap<String, Double> simPropertyEntities = propertyFilter(seeds.getProperties(),
        //		candidates.getProperties(), propAlpha, attritions);
        HashMap<String, Double> simTermEntities = termFilter(seeds.getTerms(),
                candidates.getTerms(), termAlpha, attritions, stop_words);

        //return PropertyAndTermFilter(simPropertyEntities, simTermEntities);
        //PropertyAndTermFilter(simPropertyEntities, simTermEntities);
        //printFilterResult("propertyType", simPropertyEntities);
        printFilterResult("entityTerm", simTermEntities);
    }

    private void printFilterResult(String feature, HashMap<String, Double> list) throws IOException {
        FileWriter fw = new FileWriter(new File(_seedlist + "_" + feature + ".res"));
        BufferedWriter bw = new BufferedWriter(fw);
        DecimalFormat df = new DecimalFormat("0.00000000");

        TreeMap<String, Double> sortedMap = SortByValue(list);
        int cnt = 0;

        for (Entry<String, Double> entry : sortedMap.entrySet()) {
            bw.write(feature + "\t" + entry.getKey().toString() + "\t"
                    + df.format(entry.getValue()) + "\n");
        }

        sortedMap.clear();
        bw.close();
        fw.close();
    }

    public static TreeMap<String, Double> SortByValue
            (HashMap<String, Double> map) {
        ValueComparator vc = new ValueComparator(map);
        TreeMap<String, Double> sortedMap = new TreeMap<String, Double>(vc);
        sortedMap.putAll(map);
        return sortedMap;
    }

    private HashMap<String, Double> propertyFilter(Map<String, Set<String>> seeds, Map<String, Set<String>> candidates,
                                                   double alpha, Map<String, Double> attritions) {
        HashMap<String, Double> res = new HashMap<String, Double>();
        //double threshold = globleVaribles.propertySimRatio *
        double threshold = alpha * avgSeedPropertySimilarity(seeds);
        System.out.println("Property threshold:\t" + threshold
                + " with ratio:" + alpha);
        for (Entry<String, Set<String>> entry : candidates.entrySet()) {
            if (seeds.containsKey(entry.getKey())) continue;
            Double tmp_sim = avgSimilarity(seeds, entry.getValue(), attritions);
            if (threshold <= tmp_sim) {
                res.put(entry.getKey(), tmp_sim);
            }
        }
        return res;
    }

    private HashMap<String, Double> termFilter(Map<String, String> seeds, Map<String, String> candidates,
                                               double alpha, Map<String, Double> attritions, Set<String> stop_words) {
        HashMap<String, Double> res = new HashMap<String, Double>();
        //	Set<Document> seedDocs = new HashSet<Document>();
        Map<String, Document> seedDocs = new HashMap<String, Document>();
        for (Entry<String, String> entry : seeds.entrySet()) {
            Document tmp_doc = new Document(entry.getValue(), stop_words);
            seedDocs.put(entry.getKey(), tmp_doc);
        }
        //double threshold = globleVaribles.termSimRatio *
        double threshold = 0;
        //alpha * JaccardSimilarity.avgSeedTermSimilarity(seedDocs);
        System.out.println("Term threshold:\t" + threshold
                + " with ratio:" + alpha);
        for (Entry<String, String> entry : candidates.entrySet()) {
            if (seeds.containsKey(entry.getKey())) continue;
            Document candidateDoc = new Document(entry.getValue(), stop_words);
            Double tmp_sim = avgSimilarity(seedDocs, candidateDoc, attritions);

            //	System.out.println(tmp_sim);

            if (threshold <= tmp_sim) {
                res.put(entry.getKey(), tmp_sim);
            }
        }
        return res;
    }

    private int PropertyOrTermFilter(Map<String, Double> simP, Map<String, Double> simT) {
        for (Entry<String, Double> entry : simP.entrySet()) {
            Vector<Double> tmp_vec = new Vector<Double>();
            tmp_vec.add(entry.getValue());
            if (simT.containsKey(entry.getKey())) {
                tmp_vec.add(simT.get(entry.getKey()));
            } else tmp_vec.add(0.0);
            _result.put(entry.getKey(), tmp_vec);
        }
        for (Entry<String, Double> entry2 : simT.entrySet()) {
            if (!_result.containsKey(entry2.getKey())) {
                Vector<Double> tmp_vec = new Vector<Double>();
                tmp_vec.add(0.0);
                tmp_vec.add(entry2.getValue());
                _result.put(entry2.getKey(), tmp_vec);
            }
        }
        return _result.size();
    }

    private int PropertyAndTermFilter(Map<String, Double> simP, Map<String, Double> simT) {
        for (Entry<String, Double> entry : simP.entrySet()) {
            if (simT.containsKey(entry.getKey())) {
                Vector<Double> tmp_vec = new Vector<Double>();
                tmp_vec.add(entry.getValue());
                tmp_vec.add(simT.get(entry.getKey()));
                _result.put(entry.getKey(), tmp_vec);
            }
        }
        return _result.size();
    }

    public void printResult() throws IOException {
        FileWriter fe = new FileWriter(new File("termRelevanceFiltered"));
        BufferedWriter febw = new BufferedWriter(fe);

        DecimalFormat df = new DecimalFormat("0.00000");

        for (Entry<String, Vector<Double>> entry : _result.entrySet()) {
            //System.out.printf("%s", entry.getKey());
            Vector<Double> tmp_vec = entry.getValue();
//			for(int i = 0; i < tmp_vec.size(); i++){
//				Double tmp_double = tmp_vec.elementAt(i);
//				System.out.printf("\t%.6f", tmp_double);
//			}
//			System.out.printf("\n");
            febw.write(entry.getKey().toString() + "\t"
                    + df.format(tmp_vec.elementAt(0)) + "\t"
                    + df.format(tmp_vec.elementAt(1)) + "\n");
        }
        febw.close();
        fe.close();
    }

    public int resultSize() {
        return _result.size();
    }
}

class ValueComparator implements Comparator<String> {

    Map<String, Double> map;

    public ValueComparator(Map<String, Double> base) {
        this.map = base;
    }

    public int compare(String a, String b) {
        if (map.get(a) > map.get(b)) {
            return -1;
        } else {
            return 1;
        } // returning 0 would merge keys 
    }
}

