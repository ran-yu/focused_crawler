package focusedCrawler;

import com.ontologycentral.ldspider.Crawler;
import com.ontologycentral.ldspider.Util;
import com.ontologycentral.ldspider.frontier.BasicFrontier;
import com.ontologycentral.ldspider.frontier.Frontier;
import com.ontologycentral.ldspider.hooks.content.ContentHandler;
import com.ontologycentral.ldspider.hooks.content.ContentHandlerRdfXml;
import com.ontologycentral.ldspider.hooks.error.ErrorHandler;
import com.ontologycentral.ldspider.hooks.error.ErrorHandlerLogger;
import com.ontologycentral.ldspider.hooks.links.LinkFilter;
import com.ontologycentral.ldspider.hooks.links.linkFilterType;
import com.ontologycentral.ldspider.hooks.sink.Sink;
import com.ontologycentral.ldspider.hooks.sink.SinkCallback;
import com.ontologycentral.ldspider.ldspiderMain;
import com.ontologycentral.ldspider.queue.HashTableRedirects;
import com.ontologycentral.ldspider.queue.Redirects;
import com.ontologycentral.ldspider.seen.HashSetSeen;
import com.ontologycentral.ldspider.seen.Seen;
import entityFilter.lexicalMain;
import org.apache.commons.io.FileUtils;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.Callback;
import org.semanticweb.yars.nx.parser.NxParser;
import org.semanticweb.yars.util.CallbackNQOutputStream;
import org.semanticweb.yars.util.CallbackNxOutputStream;
import org.semanticweb.yars.util.Node2uriConvertingIterator;
import org.semanticweb.yars.util.PleaseCloseTheDoorWhenYouLeaveIterator;

import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.zip.GZIPInputStream;

//import main.java.entityFilter.lexicalMain;

public class focusedCrawlerMain {

    /**
     * @param args
     * @throws Throwable
     * @throws IOException
     */

    public static void focusedCrawlerMain(String[] args) throws Exception {
        if (args.length != 3) {
            System.out.println("Please input the seedlist path, its coherence and the hop number.");
            System.exit(1);
        }
        System.out.println("Notice:\tStart focused crawler.");

        Map<String, Double> attritions = new HashMap<String, Double>();
        //attritionFactor.getAttritions(args[0], attritions);
        runBFCrawlAPI(args[0], args[2]);
        System.out.println("Notice:\tFinished ldspider.");
        attritionFactor.normalizeAttritions(args[0], attritions);
        long time = System.currentTimeMillis();
        //runBFCrawl(args[0], args[2], attritions);
        //runLexicalRelevance(args[0], args[2], attritions);
        runGraphRelevance(args[0], args[2], attritions);

        long time1 = System.currentTimeMillis();
        System.out.println("Notice:\tFinish running focused crawler with "
                + (time1 - time) + " ms.");

    }

    public static void runGraphRelevance(String seedlist, String hop, Map<String, Double> attritions) throws Exception {
        String cmdline = seedlist + " " + seedlist + "_bf" + hop + ".nq";
        String[] cmd = cmdline.split(" ");
        entityGraph.graphMain.graphMain(cmd, attritions);
    }

    public static void runLexicalRelevance(String seedlist, String hop, Map<String, Double> attritions) throws Exception {

        String cmdline = seedlist + "_bf" + hop + ".nq " + seedlist + " " +
                seedlist + "_bf" + hop + ".nq 0.02 0.02";
        String[] cmd = cmdline.split(" ");
        lexicalMain.lexicalMain(cmd);
    }

    private static void runPlainBFCrawl(String seedlist, String maxhop) {
        System.out.println("Notice:\tStart run ldspider for " + maxhop + " hops.");
        String cmdline = "-s " + seedlist
                + " -b " + maxhop + " -e -o " + seedlist + "_bf" + maxhop + ".nq";
        //System.out.println(cmdline);
        String[] ldspiderCmd = cmdline.split(" ");
        ldspiderMain bfCrawl = new ldspiderMain();
        bfCrawl.ldspiderMain(ldspiderCmd);
        //bfCrawl = null;

    }

    private static void runBFCrawlAPI(String seedlist, String maxhop) throws IOException {
        System.out.println("Notice:\tStart run ldspider for " + maxhop + " hops.");

        Crawler crawler = new Crawler(3);
        Frontier frontier = new BasicFrontier();
        //frontier.setBlacklist(CrawlerConstants.BLACKLIST);
        Iterable<URI> seeds = null;
//		if (cmd.hasOption("s")) {
        File seedList = new File(seedlist);
        if (!seedList.exists()) {
            throw new FileNotFoundException("No file found at " + seedList.getAbsolutePath());
        }
        seeds = prepareSeedsIterable(seedList);
        for (URI u : seeds)
            frontier.add(u);
        LinkFilter linkFilter = null;
        linkFilterType lft = new linkFilterType(frontier);

        lft.addDomain("xmlns.com");
        lft.addDomain("w3c.org");
        lft.addDomain("www.w3c.org");
        lft.addDomain("w3.org");
        lft.addDomain("www.w3.org");
        lft.addDomain("cyc.com");
        lft.addDomain("bklyn-genealogy-info.com");
        lft.addDomain("wdl.org ");
        lft.addDomain("time.com");
        lft.addDomain("umbel.org");
        lft.addDomain("purl.org");
        lft.addDomain("schema.org");
        lft.addLanguageAllowed("http://dbpedia.org");
        lft.addLanguageAllowed("http://en.dbpedia.org");
        lft.addLanguageAllowed("http://wikidata.dbpedia.org");
        lft.addLanguageAllowed("http://en.wikipedia.org");
        lft.addLanguageAllowed("http://de.wikipedia.org");
        lft.addLanguageAllowed("http://simple.dbpedia.org");
        lft.addLanguageAllowed("http://www.wikidata.org");
        lft.addLanguageAllowed("http://en.wikiquote.org");
        lft.addLanguageAllowed("http://simple.wikipedia.org");

        linkFilter = lft;
        crawler.setLinkFilter(linkFilter);

        ContentHandler contentHandler = new ContentHandlerRdfXml();
        ;
        crawler.setContentHandler(contentHandler);

        OutputStream os = new BufferedOutputStream(
                new FileOutputStream(seedlist + "_bf" + maxhop + ".nq"));
        Sink sink = new SinkCallback(new CallbackNxOutputStream(os, false));
        crawler.setOutputCallback(sink);

        //Print to Stdout
        PrintStream ps = System.out;
        //Print to file
        FileOutputStream fos = new FileOutputStream("errorLogFile");

        //Add printstream and file stream to error handler
        Callback rcb = new CallbackNQOutputStream(fos);
        rcb.startDocument();
        ErrorHandler eh = new ErrorHandlerLogger(ps, rcb);
        //Connect hooks with error handler
        crawler.setErrorHandler(eh);
        frontier.setErrorHandler(eh);
        linkFilter.setErrorHandler(eh);

        int depth = Integer.valueOf(maxhop);
        int maxURIs = 100;
        boolean includeABox = true;
        boolean includeTBox = false;

        Seen seen = new HashSetSeen();

        Redirects redirects = new HashTableRedirects();
        crawler.evaluateBreadthFirst(frontier, seen, redirects, depth, -1, -1, -1, includeTBox);

        os.close();
        fos.close();
        System.out.println("Notice:\tFinish run ldspider for " + maxhop + " hops.");
    }

    static Iterable<URI> prepareSeedsIterable(File seedList) throws IOException {
        List<URI> seeds = new LinkedList<URI>();

        final Iterator<URI> it;

        if (seedList.getPath().endsWith(".nx.gz")) {
            InputStream is = new GZIPInputStream(new FileInputStream(seedList));
            it = new PleaseCloseTheDoorWhenYouLeaveIterator<URI>(
                    new Node2uriConvertingIterator(new NxParser(is), 0), is);
        } else if (seedList.getPath().endsWith(".gz")) {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new GZIPInputStream(new FileInputStream(seedList))));
            it = new PleaseCloseTheDoorWhenYouLeaveIterator<URI>(
                    new Util.StringToURIiterable(
                            new Util.LineByLineIterable(br)).iterator(), br);
        } else if (seedList.getPath().endsWith(".nx")) {
            FileReader fr = new FileReader(seedList);
            it = new PleaseCloseTheDoorWhenYouLeaveIterator<URI>(
                    new Node2uriConvertingIterator(new NxParser(fr), 0), fr);
        } else {
            FileReader fr = new FileReader(seedList);
            it = new PleaseCloseTheDoorWhenYouLeaveIterator<URI>(
                    new Util.StringToURIiterable(new Util.LineByLineIterable(
                            new BufferedReader(fr))).iterator(), fr);
            //fr.close();
        }

        return new Iterable<URI>() {
            public Iterator<URI> iterator() {
                return it;
            }
        };

//		int i = 0;
//
//		while (it.hasNext()) {
//			seeds.add(it.next());
//			++i;
//		}
//
//		_log.info("read " + i + " proper URIs from seed file");
//
//		return seeds;
    }

    private static void runBFCrawl(String seedlist, String maxhop, Map<String, Double> attritions) throws Throwable {
        int max_hop = Integer.parseInt(maxhop);

        System.out.println("Notice:\tStart run ldspider for " + maxhop + " hops.");

        Integer hop = 0;
        File source = new File(seedlist);
        File dest = new File(seedlist + "_0");
        FileUtils.copyFile(source, dest);

        for (hop = 0; hop <= max_hop; hop++) {
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

    private static void filterCrawlResult(String seedlist, Integer hop, Map<String, Double> attritions) throws Throwable {
        System.out.println("Notice:\tStart to filter results for hop " + hop + ".");

        BufferedReader seedReader = new BufferedReader(new FileReader(seedlist + "_" + hop.toString()));
        String seedline = seedReader.readLine();

        Integer next_hop = hop + 1;
        BufferedWriter out = new BufferedWriter(new FileWriter(seedlist + "_bf" + hop.toString() + ".nq"));
        BufferedWriter nextHopSeed = new BufferedWriter(new FileWriter(seedlist + "_" + next_hop.toString()));

        Set<String> currentSeedlist = new HashSet<String>();
        Set<String> nextSeedlist = new HashSet<String>();

        System.out.println("Logging:\t" + seedline);

        while (seedline != null) {
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
            if (nxx.length != 4) {
                continue;
            }
            String object = nxx[2].toString();
            if (currentSeedlist.contains(nxx[0].toString())) {
                if (nxx[1].toString().contains("sameAs")) continue;
                if (object.contains("dbpedia.org") &&
                        !(object.contains("http://dbpedia.org/") || object.contains("http://en.dbpedia.org/")))
                    continue;
                if (object.contains("wikidata.org") &&
                        !(object.contains("http://wikidata.org/") || object.contains("http://en.wikidata.org/")))
                    continue;

                nextHopSeed.write(object);
                nextHopSeed.newLine();
                out.write(nxx[0].toN3() + " " + nxx[1].toN3() + " "
                        + nxx[2].toN3() + " " + nxx[3].toN3() + " .");
                out.newLine();
            }
        }

        nextSeedlist.clear();
        if (hop >= 1) {

            //		runGraphRelevance(seedlist, hop.toString(), attritions);
            runLexicalRelevance(seedlist, hop.toString(), attritions);

            //		BufferedReader rankReader = new BufferedReader(new FileReader("neighborJaccard"));
            //		BufferedReader rankReader = new BufferedReader(new FileReader("pagerank"));
            BufferedReader rankReader = new BufferedReader(new FileReader(seedlist + "_entityTerm.res"));
            String resline = rankReader.readLine();

            int cnt_line = 1;
            while (resline != null && cnt_line < 2000) {
                String[] splitstr = resline.split("\t");
                if (!currentSeedlist.contains(splitstr[1]) && !nextSeedlist.contains(splitstr[1])
                        && Double.valueOf(splitstr[2]) > 0) {
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
