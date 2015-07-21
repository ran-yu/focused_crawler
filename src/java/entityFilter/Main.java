package entityFilter;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static focusedCrawler.attritionFactor.normalizeAttritions;

//import main.java.entityFilter.termRelevanceFilter;


public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length != 5) {
            System.out.println("Please input:\n 1. (seed).nq\n 2. seeds\n 3. (candidates).nq\n"
                    + " 4. Property type threshold coeficient parameter, suggest 0.02\n"
                    + " 5. Term similarity threshold coeficient parameter,suggest 0.02.\n");
            System.exit(1);
        }
        long time = System.currentTimeMillis();
        //map of <seedUri, termSequence>, read seeds

        Map<String, Double> attritions = new HashMap<String, Double>();
        //attritionFactor.getAttritions(args[1], attritions);
        normalizeAttritions(args[1], attritions);

        //get term properties
        Set<String> properties = new HashSet<String>();
        getProperties(properties);

        //read stop words
        Set<String> stop_words = load_stop_words();
        //get seed terms
        Seeds seeds = new Seeds(args[0], args[1], properties);
        //	seeds.printSeeds();

        //Get candidates terms
        Candidates candidates = new Candidates(args[2], properties);
        //candidates.printCandidates();
        termRelevanceFilter TRelFilter = new termRelevanceFilter(args[1]);
//		//TRelFilter.cosineRelevance(seeds, candidates);
        TRelFilter.relevanceFilter(seeds, candidates, Double.valueOf(args[3]),
                Double.valueOf(args[4]), attritions, stop_words);
        //TRelFilter.printResult();
        long time1 = System.currentTimeMillis();
        System.out.println("Notice:\tFinish running main with " + (time1 - time) + " ms.");

    }

    private static Set<String> load_stop_words() throws IOException {
        //int cnt = 0;

        Set<String> stop_words = new HashSet<String>();
        BufferedReader br = new BufferedReader(new FileReader("stop.dict"));
        String line = br.readLine();

        while (line != null) {
            //cnt++;
            stop_words.add(line);
            line = br.readLine();
        }
        br.close();
        return stop_words;
    }

    private static void getProperties(Set<String> prop) {
        prop.add(new String("http://dbpedia.org/ontology/abstract"));
        prop.add(new String("http://www.w3.org/2000/01/rdf-schema#comment"));
        prop.add(new String("http://www.freebase.com/common/topic/description"));
        prop.add(new String("http://www.w3.org/2004/02/skos/core#altLabel"));
        prop.add(new String("http://www.w3.org/2000/01/rdf-schema#label"));
        prop.add(new String("http://www.w3.org/2004/02/skos/core#definition"));
        prop.add(new String("<http://purl.org/dc/elements/1.1/description"));
    }

}
