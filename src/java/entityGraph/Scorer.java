package entityGraph;

import java.io.BufferedReader;
import java.util.Map.Entry;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import edu.uci.ics.jung.algorithms.scoring.*;
import edu.uci.ics.jung.graph.*;

public class Scorer {
	public static void closenessCentrality(entityGraph graph, double thresh) throws IOException{
		System.out.println("Notice:\tStart computing closenessCentrality score.");
		
		ClosenessCentrality<String, Integer> CC = new ClosenessCentrality<String, Integer>(graph.ug);
		Iterator<String> vertex = graph.ug.getVertices().iterator();
		
		FileWriter fe = new FileWriter(new File("closenessCentrality"));
		BufferedWriter febw = new BufferedWriter(fe);
		DecimalFormat df = new DecimalFormat("0.0000");
		
		int cnt = 0;
		while(vertex.hasNext()){
			String tmpnode = vertex.next();
		//	System.out.println(tmpnode);
			double score = CC.getVertexScore(tmpnode);
			if(score >= thresh){
				febw.write(tmpnode+'\t'+df.format(score)+'\n');
				System.out.println(tmpnode+'\t'+df.format(score));
				cnt++;
			}
		//	if(cnt > 2000) break;
		}
		
		febw.close();
		fe.close();
		System.out.println("Notice:\tFinish computing closenessCentrality score.");
	}
	
	public static void pagerankScore(entityGraph graph, Set<String> seeds, double alpha, double thresh) throws IOException{
		System.out.println("Notice:\tStart computing pagerank score.");
		PageRank<String, Integer> pr = new PageRank<String, Integer>(graph.ug, alpha);
		//pr.initialize();
		pr.evaluate();
		Iterator<String> vertex = graph.ug.getVertices().iterator();
		
		FileWriter fe = new FileWriter(new File("pagerank"));
		BufferedWriter febw = new BufferedWriter(fe);
		DecimalFormat df = new DecimalFormat("0.000000000");
		
		FileWriter ci = new FileWriter(new File("seeds_CI"));
		BufferedWriter cibw = new BufferedWriter(ci);
		
		int cnt = 0;
		while(vertex.hasNext()){
			String tmpnode = vertex.next();
	//		System.out.println(tmpnode);
			if(!graph.isSubject(tmpnode))continue;
			double score = pr.getVertexScore(tmpnode);
			if(seeds.contains(tmpnode)){
				cibw.write(tmpnode+'\t'+df.format(score)+'\n');
			}
			if(true){
				//score > thresh){
				febw.write(tmpnode+'\t'+df.format(score)+'\n');
				System.out.println(tmpnode+'\t'+df.format(score));
				cnt++;
			}
//			if(cnt > 2000) break;
		}
		cibw.close();
		ci.close();
		febw.close();
		fe.close();
		System.out.println("Notice:\tFinish computing pagerank score.");
		
	}
	
	public static void nodeJaccard(String seedlist, entityGraph graph, Set<String> seeds, double thresh, Map<String, Double> attritions) throws Exception{
		Iterator<String> it = seeds.iterator();

		System.out.println("Notice:\tStart computing neighborJaccard score.");
		FileWriter fe = new FileWriter(new File(seedlist+"_neighborJaccard.res"));
		BufferedWriter febw = new BufferedWriter(fe);
		DecimalFormat df = new DecimalFormat("0.0000000");
		
		Iterator<String> vertex = graph.ug.getVertices().iterator();
		
		//Map<String, Double> CI_score = CIscore.ReadCIScore();
		
		//get seeds neighbor number
		Iterator<String> seediter = seeds.iterator();
		Map<String, Integer> seed_nei_num = new HashMap<String, Integer>();
		while(seediter.hasNext()){
			String seed = seediter.next();
			if(graph.ug.containsVertex(seed) ){
				seed_nei_num.put(seed, graph.ug.getNeighborCount(seed));
			}
			else seed_nei_num.put(seed, 0);
		}
		
		HashMap<String, Double> node_score_map = new HashMap<String, Double>();
		
		while(vertex.hasNext()){
			double score = 0;
			double neighborSize = 0;
			double cnt = 0;
			String node = vertex.next();
			if(!graph.isSubject(node))continue;
			
			Iterator<String> nb= graph.ug.getNeighbors(node).iterator();
			int neiSize = graph.ug.getNeighborCount(node);
			//Set<String> nbbb=(Set<String>) graph.ug.getNeighbors(node);
			
		/*	Map<String, Integer> pair_cnt = new HashMap<String, Integer>();
			Map<String, Integer> pair_cnt2 = new HashMap<String, Integer>();
			Iterator<String> seediter2 = seeds.iterator();
			while(seediter2.hasNext()){
				String seed = seediter2.next();
				pair_cnt.put(seed, 0);
				pair_cnt2.put(seed, 0);
			}
*/
			
			while(nb.hasNext()){
				neighborSize += 1;
				String tmpstr = nb.next();
				if(seeds.contains(tmpstr)){
				//	pair_cnt.put(tmpstr, pair_cnt.get(tmpstr)+1 );
					cnt += 0.7*attritions.get(tmpstr)/(neiSize+seed_nei_num.get(tmpstr));
					continue;
				}
				
				Iterator<String> nbb = graph.ug.getNeighbors(tmpstr).iterator();
				while(nbb.hasNext()){
					//neighborSize +=1;
					String tmpstr2=nbb.next();
					if(seeds.contains(tmpstr2)){
						cnt += 0.49*attritions.get(tmpstr2)/(neiSize+seed_nei_num.get(tmpstr2));
				//		pair_cnt2.put(tmpstr, pair_cnt2.get(tmpstr)+1);
						break;
					}
				}
				//todo
			}
			
/*			score = 0;
			Iterator<String> seediter3 = seeds.iterator();
			while(seediter3.hasNext()){
				String seed = seediter3.next();
				score += 0.7*attritions.get(seed)*pair_cnt.get(seed);
				score += 0.49*attritions.get(seed)*pair_cnt.get(seed);
			}*/
			
			if( seeds.size() > 0)
				score = cnt/seeds.size();
			
			node_score_map.put(node, score);

			
		//	pair_cnt.clear();
		//	pair_cnt2.clear();
		}
		
		TreeMap<String, Double> sortedMap = SortByValue(node_score_map);
		int cnt = 0;
		for(Entry<String, Double> entry: sortedMap.entrySet()){
			//if(cnt >= 1000) break;
			febw.write(entry.getKey()+'\t'+df.format(entry.getValue())+'\n');
			//System.out.println(node+'\t'+df.format(score));
			cnt++;
		}
		
		sortedMap.clear();
		seed_nei_num.clear();
		node_score_map.clear();
		
		febw.close();
		fe.close();
	}
	
	public static TreeMap<String, Double> SortByValue 
	(HashMap<String, Double> map) {
		ValueComparator vc =  new ValueComparator(map);
		TreeMap<String,Double> sortedMap = new TreeMap<String,Double>(vc);
		sortedMap.putAll(map);
		return sortedMap;
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
