/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cvegrabber;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.BeanNameViewResolver;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

@RestController
public class CVEController {
    
    private static Log logger = LogFactory.getLog(CVEController.class);
    private String description = "";
    private String references = "";
    
    
    @RequestMapping(value="/newest", produces={"application/json"})
    public CVE[] cve(){
        
        String url = "https://web.nvd.nist.gov/view/vuln/search-results?query=&search_type=all&cves=on";
        CVE [] cvearray = new CVE[10];
        try {
            Document doc = Jsoup.connect(url).get();
            Elements newest = doc.select("a[id*=BodyPlaceHolder_cplPageContent_plcZones_lt_zoneCenter_VulnerabilitySearchResults_VulnResultsRepeater_CveDetailAnchor_]");
            int counter = 0; 
            for(Element cveid : newest){
                if (counter == 10) break;
                cvearray[counter] = new CVE(cveid.text(), grabMitreData(cveid.text(), "description"), grabMitreData(cveid.text(), "references"));
                counter++;
            }
            for( int i = 0; i < 10; i++){
                logger.info("CVEID: " + cvearray[i].getCVE() + " CVE Description: " + cvearray[i].getDescription() + " CVE References: " + cvearray[i].getReferences() );
            }
        }
        catch(Exception ex) {
            logger.error("Unable to fetch latest cves. " + ex.getMessage());
        }
        return cvearray;
    }
    
    
    
    @RequestMapping(value="/cve", produces={"application/json"})
    public CVE cve(@RequestParam(value="cveid", defaultValue="None") String cveid) {
        if(cveid.matches("None")) {
            logger.info("CVEID: " + cveid);
            description = "No CVE Provided";
            references = "No CVE Providede";
        }
        else {
            try {
                description = grabMitreData(cveid, "description");
            }
            catch(Exception ex) {
                System.out.println("Error: Failed to get description - " + ex.getMessage());
            }
            try {
                references = grabMitreData(cveid, "references");
            }
            catch(Exception ex) {
                System.out.println("Error: Failed to get reference - " + ex.getMessage());
            }
            
        }
        return new CVE(cveid, description, references);
    }
    // Data should be description or references
    private String grabMitreData(String cveid, String data) throws IOException{
        //String url = "http://www.cvedetails.com/cve/" + cveid + "/";
        String url = "http://cve.mitre.org/cgi-bin/cvename.cgi?name=" + cveid;
        Document doc = Jsoup.connect(url).get();
        String dataToReturn = "";
        
        if(doc.select("h2").text().contains("ERROR")){
            dataToReturn = "CVE " + cveid + " Unknown or CVE Not Loaded Yet.";
            return dataToReturn;
        }
        else if(data.matches("references")){
            //Elements references = doc.select("td.r_average");
            Elements references = doc.select("li");
            int counter = 0;
            for ( Element reference : references ){
                if(counter == 0){
                    //dataToReturn += link.select("a[href]").text();
                    dataToReturn += reference.text();
                    counter++;
                }
                else{
                    //dataToReturn += "," + link.select("a[href]").text();
                    dataToReturn += "," + reference.text();
                }
            }
        }
        else if (data.matches("description")){
            //Element description = doc.select("div.cvedetailssummary").first();
            Elements tds = doc.select("td[colspan=\"2\"]");
            if(tds.eq(2).text().contains("** RESERVED **")){
                return "No data on mitre yet.";
            }
            dataToReturn = tds.eq(2).text();
        }
        return dataToReturn;
    }
}




