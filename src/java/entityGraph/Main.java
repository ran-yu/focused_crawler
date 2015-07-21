package entityGraph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static focusedCrawler.attritionFactor.getAttritions;

public class Main {

    public static void main(String[] args) throws Throwable {
        // TODO Auto-generated method stub
        if (args.length != 2) {
            System.out.println("Please input seeds & (candidates).nq file path");
            //	+ " and method 1:relevance, 2: centrality ");
            System.exit(1);
        }
        long time = System.currentTimeMillis();

        //Map<String, Double> attritions = new HashMap<String, Double>();
        //	attritionFactor.getAttritions(args[0], attritions);
        Map<String, Double> attritions = new HashMap<String, Double>();

        getAttritions(args[0], attritions);
        //
        //	attritionFactor.normalizeAttritions(args[0], attritions);

        //read seed uris
        Set<String> seedUris = new HashSet<String>();
        readSeeds(args[0], seedUris);

        //Build Graph
        entityGraph graph = new entityGraph(args[1], entityGraph.ENTITY_ONLY);


        //Scorer
        // graph.addEdgeBetweenSeed(seedUris, 10);
        //if(args[2].equalsIgnoreCase("1"))Scorer.nodeJaccard(graph, seedUris, 0.1);
        //else if(args[2].equalsIgnoreCase("2"))Scorer.closenessCentrality(graph, 0);
        //Scorer.pagerankScore(graph, seedUris, 0.2, 0);
        Scorer.nodeJaccard(args[0], graph, seedUris, 0, attritions);
        //Scorer.closenessCentrality(graph, 0);
        attritions.clear();
        long time1 = System.currentTimeMillis();
        System.out.println("Notice:\tFinish graph relevance with " + (time1 - time) + " ms.");
    }

    private static Integer readSeeds(String fileName, Set<String> seedUris) throws IOException {
        Integer cnt = 0;
        File seedsFile = new File(fileName);
        BufferedReader reader = null;
        reader = new BufferedReader(new FileReader(seedsFile));
        String seedUri = null;
        while ((seedUri = reader.readLine()) != null) {
            seedUris.add(seedUri);
//			System.out.println(seedUri);
            cnt++;
        }
        reader.close();
        System.out.println("Notice:\tGot " + cnt + " seeds.");
        return cnt;
    }
}
