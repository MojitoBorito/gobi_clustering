package com.example;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;

public class CmdParser {

    private String errorMessage = "";
    /**
     * Geparste Parameter werden in einer HashMap als (key,value) Paare gespeichert
     * (z.B. key="-int", value="10")
     */
    private HashMap<String, String> parameterMap;
    /**
     * fuer erlaubte Optionen bieten sich HashSets als Datenstruktur an, da insb.
     * contains viel effizienter (konstante Laufzeit) als bei anderen Collections
     * wie z.B. ArrayList ist
     */
    private HashSet<String> allowedOptions;
    // statt obligatorischen koennen natuerlich auch optionale Optionen gespeichert werden
    private HashSet<String> mandatoryOptions;
    private HashSet<String> switches;
    private HashSet<String> trueSwitches;
    // Instanzvariablen fuer die Erweiterung
    private HashSet<String> intOptions;
    private HashSet<String> doubleOptions;
    private HashSet<String> fileOptions;

    /**
     * (a) Parser-Konstruktor
     * Wird der Typangabe in der Parameterliste ein "..." nachgestellt,
     * so kann ma der Methode eine variable Anzahl an Parameter dieses Typs
     * uebergeben. Diese Parameter werden in einem Array gespeichert.
     * Beispielaufruf: new CmdParser("--a","--c","hallo","42")
     * allowedOptions[0] ist dann "--a"
     * @param allowedOptions String[] das erlaubte Optionen enthaelt
     */
    public CmdParser(String... allowedOptions){
        this.allowedOptions = new HashSet<>();
        this.mandatoryOptions = new HashSet<>();
        this.parameterMap = new HashMap<>();
        for (String option : allowedOptions) {
            this.allowedOptions.add(option);
        }
        this.switches = new HashSet<>();
        this.trueSwitches = new HashSet<>();
        // Erweiterung
        this.intOptions = new HashSet<>();
        this.doubleOptions = new HashSet<>();
        this.fileOptions = new HashSet<>();
    }
    /**
     * (a) definieren von obligatorischen (nicht-optionalen) Optionen
     * @param options Optionen die obligatorisch
     */
    public void setMandatory(String... options){
        for(String o : options) {
            if(!allowedOptions.contains(o)) {
                throw new RuntimeException("Option " + o + " should be mandatory, but is not defined. Will be skipped");
            } else {
                this.mandatoryOptions.add(o);
            }
        }
    }

    /**
     * (b) definieren von Switches
     */
    public void setSwitches(String... switches){
        for(String s : switches) {
            this.switches.add(s);
            allowedOptions.add(s);
        }
    }

    /**
     * (c) Diese Methode parst die Kommandozeilenparameter aus dem String Array
     * und speichert sie in der HashMap parameterMap
     * @param args Kommandozeilenparameter
     */
    public void parse(String[] args){
        for(int i=0; i<args.length; i++){
            String option=args[i];
            if(!allowedOptions.contains(option)){
                throw new RuntimeException("Parameter not allowed: " + option + "\n" + errorMessage);
            }
            if(switches.contains(option)){
                trueSwitches.add(option);
                continue;
            }
            if(i+1==args.length){
                throw new RuntimeException("Last parameter has no value: " + option + "\n" + errorMessage);
            }
            if(parameterMap.containsKey(option)){
                throw new RuntimeException("Parameter already set: " + option + "\n" + errorMessage);
            }
            // in der Map werden (key,value) Paare gespeichert
            // z.B. --double 4.5 als ("--double","4.5")
            // key = Option (faengt normalerweise mit "-" an)
            // value = der Parameter nach der Option
            i++;
            String value = args[i];
            parameterMap.put(option, value);
        }
        // Testen ob alle obligatorischen Optionen gesetzt wurden
        for(String option : mandatoryOptions){
            if(!parameterMap.containsKey(option)){
                throw new RuntimeException("Mandatory option missing: " + option + "\n" + errorMessage);
            }
        }
    }
    /**
     * (a) Rueckgabe der geparsten Werte fuer valide Optionen
     * @param option Option fuer die geparster Wert zurueckgegeben werden soll
     * @return geparsten Wert fuer die gegebene Option
     */
    public String getValue(String option){
        if(!allowedOptions.contains(option)) {
            throw new RuntimeException("Option " + option + " is not allowed");
        }
        // wenn die Option nicht gesetzt ist (z.B. fuer optionale Optionen), also der
        // key nicht vorhanden ist gibt die get Methode der HashMap null zurueck
        return parameterMap.get(option);
    }

    /**
     * Rueckgabe der geparsten Werte fuer Switches
     * @param s switch Name
     * @return true wenn Switch gesetzt war
     */
    public boolean isSet(String s){
        if(!allowedOptions.contains(s)) {
            throw new RuntimeException("Switch " + s + " is not allowed");
        }
        return trueSwitches.contains(s);
    }

    /**
     * Erweiterung: Festlegen welche Optionen einen gewissen Typ haben (int/double/File)
     * Um Code-Duplikationen zu vermeiden benutzen wir eine allgemeinere private Methode
     * die das HashSet in das die gegebenen Optionen des Typs gespeichert werden sollen
     * (eigentlich eine Instanzvariable) als Parameter bekommt. Fuer die sichtbaren Methoden
     * muessen wir dann nur noch diese Methode mit dem entsprechenden HashSet aufrufen
     * @param savedTypedOptions HashSet in dem die gegebenen Optionen gespeichert werden
     * @param options Optionen die einen gewissen Typ haben sollen
     */
    private void setType(HashSet<String> savedTypedOptions, String... options){
        for(String o : options) {
            if(!allowedOptions.contains(o)) {
                throw new RuntimeException("Option " + o + " should be mandatory, but is not defined. Will be skipped");
            } else {
                savedTypedOptions.add(o);
            }
        }
    }

    public void setDouble(String... doubleOptions){
        setType(this.doubleOptions, doubleOptions);
    }

    public void setInt(String... intOptions){
        setType(this.intOptions, intOptions);
    }

    public void setFile(String... fileOptions){
        setType(this.fileOptions, fileOptions);
    }
    public Integer getInt(String option){
        if(!intOptions.contains(option)){
            throw new RuntimeException("Option value is not defined to be an Integer "+option + "\n" + errorMessage);
        }
        return Integer.parseInt(getValue(option));
    }
    public Double getDouble(String option){
        if(!doubleOptions.contains(option)){
            throw new RuntimeException("Option value is not defined to be a Double "+option);
        }
        return Double.parseDouble(getValue(option));
    }
    public File getFile(String option) {
        if (!fileOptions.contains(option)) {
            throw new RuntimeException("Option value is not defined to be a File " + option + "\n" + errorMessage);
        }
        return new File(getValue(option));
    }
}
