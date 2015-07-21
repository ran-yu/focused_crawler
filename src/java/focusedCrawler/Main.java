package focusedCrawler;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.ontologycentral.ldspider.ldspiderMain;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.parser.Callback;
import org.semanticweb.yars.nx.parser.NxParser;
import entityFilter.lexicalMain;
import entityGraph.graphMain;

//import main.java.entityFilter.lexicalMain;

public class Main {

	/**
	 * @param args
	 * @throws Throwable 
	 * @throws IOException 
	 */

	public static void main(String[] args) throws Throwable {
		if (args.length != 3) {
			System.out
					.println("Please input the seedlist path, its coherence and the hop number.");
			System.exit(1);
		}

		Map<String, Double> attritions = new HashMap<String, Double>();
		//attritionFactor.getAttritions(args[0], attritions);
		//runPlainBFCrawl(args[0], args[2]);
		attritionFactor.normalizeAttritions(args[0], attritions);
		long time = System.currentTimeMillis();
		//runBFCrawl(args[0], args[2], attritions);
		runLexicalRelevance(args[0], args[2], attritions);
		//runGraphRelevance(args[0], args[2], attritions);

		long time1 = System.currentTimeMillis();
		System.out.println("Notice:\tFinish running focused crawler with "
				+ (time1 - time) + " ms.");

	}
	
	public static void runGraphRelevance(String seedlist, String hop,Map<String, Double> attritions) throws Throwable{
		String cmdline = seedlist+ " "  + seedlist + "_bf" + hop + ".nq";
		String[] cmd = cmdline.split(" ");
		graphMain.graphMain(cmd, attritions);
	}
	
	public static void runLexicalRelevance(String seedlist, String hop, Map<String, Double> attritions) throws Exception{
		
		String cmdline = seedlist + "_bf" + hop + ".nq " + seedlist + " " +
				seedlist + "_bf" + hop + ".nq 0.02 0.02";
		String[] cmd = cmdline.split(" ");
		lexicalMain.lexicalMain(cmd);
	}
	private static void runPlainBFCrawl(String seedlist, String maxhop){
		System.out.println("Notice:\tStart run ldspider for " + maxhop + " hops.");
		String cmdline = "-s " + seedlist
				+ " -b " + maxhop + " -e -o " + seedlist + "_bf" +maxhop + ".nq";
		//System.out.println(cmdline);
		String[] ldspiderCmd = cmdline.split(" ");
		ldspiderMain bfCrawl = new ldspiderMain();
		bfCrawl.ldspiderMain(ldspiderCmd);
		//bfCrawl = null;

	}
	private static void runBFCrawl(String seedlist, String maxhop, Map<String, Double> attritions) throws Throwable {
		int max_hop = Integer.parseInt(maxhop);

		System.out.println("Notice:\tStart run ldspider for " + maxhop + " hops.");
		
		Integer hop = 0;
		File source = new File(seedlist);
		File dest = new File(seedlist+"_0");
		FileUtils.copyFile(source, dest);
		
		for (hop=0; hop <= max_hop; hop++) {
			System.out.println("Notice:\tLdspider hop " + hop + ".");
			String cmdline = "-s " + seedlist + "_" + hop.toString()
					+ " -b 0 -e -o out_" + seedlist + "_bf" + hop.toString() + ".nq";
			//System.out.println(cmdline);
			String[] ldspiderCmd = cmdline.split(" ");
			ldspiderMain bfCrawl = new ldspiderMain();
			bfCrawl.ldspiderMain(ldspiderCmd);
			
			filterCrawlResult(seedlist, hop, attritions);
		}
		
		System.out.println("Notice:\tFinish run ldspider for " + maxhop + " hops.");
	}
	
	private static void filterCrawlResult(String seedlist, Integer hop, Map<String, Double> attritions) throws Throwable{
		System.out.println("Notice:\tStart to filter results for hop " + hop + ".");
		
		BufferedReader seedReader = new BufferedReader(new FileReader(seedlist+"_" + hop.toString()));
		String seedline = seedReader.readLine();
		
		Integer next_hop = hop + 1;
		BufferedWriter out = new BufferedWriter(new FileWriter(seedlist + "_bf" + hop.toString() + ".nq"));
		BufferedWriter nextHopSeed = new BufferedWriter(new FileWriter(seedlist+ "_" + next_hop.toString() ));
		
		Set<String> currentSeedlist = new HashSet<String>();
		Set<String> nextSeedlist = new HashSet<String>();
		
		System.out.println("Logging:\t" + seedline);
		
		while(seedline != null){
			//System.out.println("Logging:\t" + seedline);
			currentSeedlist.add(seedline);
			nextHopSeed.write(seedline);
			nextSeedlist.add(seedline);
			nextHopSeed.newLine();
			System.out.println(seedline);
			seedline = seedReader.readLine();
		}
		
		FileInputStream is = new FileInputStream("out_" + seedlist + "_bf" + hop.toString() + ".nq");
		NxParser nxp = new NxParser(is);
		for (Node[] nxx : nxp) {		
			if(nxx.length != 4){
				continue;
			}
			String object = nxx[2].toString();
			if(currentSeedlist.contains(nxx[0].toString())){
				if(nxx[1].toString().contains("sameAs")) continue;
				if(object.contains("dbpedia.org")&& 
						!(object.contains("http://dbpedia.org/")||object.contains("http://en.dbpedia.org/")))
					continue;
				if(object.contains("wikidata.org")&& 
						!(object.contains("http://wikidata.org/")||object.contains("http://en.wikidata.org/")))
					continue;
					
					nextHopSeed.write(object);
					nextHopSeed.newLine();
				out.write(nxx[0].toN3()+ " " + nxx[1].toN3()+ " "
						+ nxx[2].toN3() + " " + nxx[3].toN3()+ " .");
				out.newLine();
			}
		}
		
		nextSeedlist.clear();
		if(hop>=1){		
		
	//		runGraphRelevance(seedlist, hop.toString(), attritions);
			runLexicalRelevance(seedlist, hop.toString(), attritions);
			
	//		BufferedReader rankReader = new BufferedReader(new FileReader("neighborJaccard"));
	//		BufferedReader rankReader = new BufferedReader(new FileReader("pagerank"));
			BufferedReader rankReader = new BufferedReader(new FileReader(seedlist+"_entityTerm.res"));
			String resline = rankReader.readLine();
			
			int cnt_line = 1;
			while(resline != null && cnt_line < 2000){
				String[] splitstr = resline.split("\t");
				if(!currentSeedlist.contains(splitstr[1]) && !nextSeedlist.contains(splitstr[1])
						&& Double.valueOf(splitstr[2]) > 0)
				{
					nextHopSeed.write(splitstr[1]);
					nextHopSeed.newLine();
					nextSeedlist.add(splitstr[1]);
				}
				resline = rankReader.readLine();
				cnt_line++;
			}
			rankReader.close();
		}
		
		currentSeedlist.clear();
		nextSeedlist.clear();
		
		out.close();
		nextHopSeed.close();
		seedReader.close();
		//rankReader.close();
		
		System.out.println("Notice:\tFinish filtering results for hop " + hop + ".");
	}

}
