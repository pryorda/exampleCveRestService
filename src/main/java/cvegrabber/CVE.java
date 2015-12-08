/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cvegrabber;

public class CVE {

    private final String cve;
    private final String description;
    private final String references;
    
    public CVE(String id, String description, String references) {
        this.cve = id;
        this.description = description;
        this.references = references;
    }

    public String getCVE() {
        return cve;
    }

    public String getDescription() {
        return description;
    }
    
    public String getReferences() {
        return references;
    }
}