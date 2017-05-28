package ScraperClass;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class ScrapeUrlAndEmail {

    private Semaphore s = new Semaphore(1);
    private HashSet<String> emailList = new HashSet<>();
    private HashSet<Element> urlListUnique = new HashSet<>();
    private ArrayList<Element> urlList = new ArrayList<>();
    private int index = 1;
    private Document doc;
    private int threadsFinished;
    private int threadCount;
    private Matcher email;
    private String emailRegex = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";
    private DataBase db = new DataBase();

    public ScrapeUrlAndEmail(String startUrl, int threadCount) {
        this.threadCount = threadCount;

        try {
            doc = Jsoup.connect(startUrl).userAgent("Mozilla").get();
        } catch (IOException ex) {
            Logger.getLogger(ScrapeUrlAndEmail.class.getName()).log(Level.SEVERE, null, ex);
        }

        email = Pattern.compile(emailRegex).matcher(doc.text());
        while (email.find()) {
            emailList.add(email.group());
        }

        urlListUnique.addAll(doc.select("a[href]"));

        urlList.addAll(urlListUnique);

        //System.out.println(urlList.size());
        if (!urlList.isEmpty()) {
            for (int i = 0; i < threadCount; i++) {
                ScraperThread st = new ScraperThread(i);
                st.start();
            }
        }

    }

    private class ScraperThread extends Thread {

        private Document doc;
        private int threadId;

        public ScraperThread(int threadId) {
            this.threadId = threadId;
        }

        @Override
        public void run() {
            //I understand that it will go over this number because of the
            //threads and that i could fix it using boolean flags but
            //considering the 1k urls is just because of proof of concept/sample size
            //I am leaving it as is
            while (urlList.size() - 1 > index && urlList.size() < 1000) {

                try {
                    s.acquire();
                } catch (InterruptedException ex) {
                    Logger.getLogger(ScrapeUrlAndEmail.class.getName()).log(Level.SEVERE, null, ex);
                }

                try {
                    //pulls from next link in the urlList
                    doc = Jsoup.connect(urlList.get(index).absUrl("href")).userAgent("Mozilla").get();
                } catch (IOException | java.lang.IllegalArgumentException ex) {
                    Logger.getLogger(ScrapeUrlAndEmail.class.getName()).log(Level.SEVERE, null, ex);
                }

                //System.out.println(doc.select("a[href]").size() + " id is " + threadId);
                ++index;
                s.release();

                for (Element e : doc.select("a[href]")) {
                    /*
                    This in my opion is as effecient as it could get in terms 
                    of speed (not memorey). urlListUnique is a hash set so will
                    use hash to determine if its already added that url. if it hasnt,
                    it will return true and store it in an arraylist.
                    Effectivley this will insure unique URLs in our arraylis
                    which could be accessed in constant time using array arithmatic
                     */
                    if (urlListUnique.add(e)) {
                        urlList.add(e);
                    }
                }

                email = Pattern.compile(emailRegex).matcher(doc.text());
                while (email.find()) {
                    try {
                        emailList.add(email.group());
                        //System.out.println(email.group());
                    } catch (Exception e) {
                    }
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    //Logger.getLogger(ScrapeUrlAndEmail.class.getName()).log(Level.SEVERE, null, ex);
                }

            }

            System.out.println(threadId + " finished  threadsFinished=" + ++threadsFinished + urlList.size());

            if (threadsFinished == threadCount) {
                System.out.println("finish " + urlList.size());

                for (String string : emailList) {
                    db.addEmail(string);
                }
                
                for (Element e : urlList) {
                    db.addUrl(e.absUrl("href"));
                }
            }
        }
    }
}
