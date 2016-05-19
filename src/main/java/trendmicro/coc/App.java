package trendmicro.coc;

import java.io.*;
import java.util.*;

/**
 * Created by sammi_lin on 4/18/16.
 */
public class App {

    public static String configname = "config.properties";
    private String screenResultFile = "";
    private String scanResultFile = "";
    private boolean debug = false;

    public App(boolean debug){
        this.debug =debug;
    }


    public App(){

    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public String getScanResultFile() {
        return scanResultFile;
    }

    public String getScreenResultFile() {
        return screenResultFile;
    }

    private static void produceProperties(Map<String,String> properties){
        Properties prop = new Properties();
        OutputStream output = null;

        try {
            output = new FileOutputStream(configname);

//            for(Map.Entry<String, Integer> entry : properties.entrySet())
            for (String key : properties.keySet()) {
//                System.out.println(key + " : " + properties.get(key));
                prop.setProperty(key, properties.get(key));

            }

            // save properties to project root folder
            prop.store(output, null);
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            if (output != null){
                try {
                    output.close();
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void printProbAll(){
        Properties prob = new Properties();
        InputStream input = null;

        try {

            String filename = configname;
            input = getClass().getClassLoader().getResourceAsStream(filename);
            if (input == null){
                System.out.println("unknow properties,filename is "+ filename);
                return;
            }
            prob.load(input);
//            Enumeration e = prob.propertyNames();
//            while (e.hasMoreElements()){
//                String key = (String) e.nextElement();
//                String val = prob.getProperty(key);
//                System.out.println(key + ":" + val);
//            }
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            if(input != null){
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Properties getProperties(){

        Properties prop = new Properties();
        InputStream input = null;

        try {

            input = new FileInputStream(configname);

            // load a properties file
            prop.load(input);

            // get the property value and print it out
//            System.out.println(prop.getProperty("host"));
//            System.out.println(prop.getProperty("user"));
//            System.out.println(prop.getProperty("password"));

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return prop;
    }

    public List<String> VSDTScan(String resource){
        Properties prob = getProperties();

        SSHCommandExecutor vsdtscan = new SSHCommandExecutor(prob);
//        System.out.println(resource);
        vsdtscan.setScan_resources(resource);
        String command = vsdtscan.get_vsdt_scan_command();
        vsdtscan.execute(command);

        //Parse Screen Log
        ParseVSDTLog pvl = new ParseVSDTLog( vsdtscan.localDir,vsdtscan.scanlogpath,vsdtscan.screenrspath);
        pvl.setDataTypedefname(vsdtscan.datatypedefpath);
        pvl.replaceScanLogRs();
        pvl.replaceScreenRs();
//        System.out.println("=================== Debug End ============================");

//        System.out.println("");

        this.scanResultFile = pvl.getScanResultFile();
        this.screenResultFile = pvl.getScreenResultFile();
        List<String> list = pvl.getScanResult();

        if (debug) {
            System.out.println("=================== Result Files =========================");
            System.out.println("Screen Result File Path in " + getScreenResultFile());
            System.out.println("Scan Result File Path in " + getScanResultFile());

            System.out.println("");

            System.out.println("=================== Result List ==========================");

            for (String line : list) {
                System.out.println(line);
            }
        }
        return list;
    }
    public static void main(String[] args) {

//        String host="192.168.0.17";
//        String user="multid";
//        String password="****";
//        String localDir = "/Users/sammi_lin/Documents/workshare/VSDT_Tool_Linux/9.850-1008";
//        String executeToolDir = "/mnt/VSDT_Tool_Linux/9.850-1008";
//
//        String datatypedefpath = "/tmp/DataTypeDef.txt";
//
//        String scanlogpath = "/tmp/scan_log";
//        String screenrspath = "/tmp/screen_rs"; //only filename response
//        String screenlogpath = "/tmp/screen_log";
//        HashMap<String,String> properties = new HashMap<String,String>();
//
//        properties.put("host",host);
//        properties.put("user",user);
//        properties.put("password",password);
//        properties.put("localDir",localDir);
//        properties.put("executeToolDir",executeToolDir);
//        properties.put("datatypedefpath",datatypedefpath);
//        properties.put("scanlogpath",scanlogpath);
//        properties.put("screenrspath",screenrspath);
//        properties.put("screenlogpath",screenlogpath);

//        System.out.println("=================== Debug Start =========================");

        App app = new App();
//        if (args[1] !=null) app.setDebug(Boolean.valueOf(args[1]));
        app.setDebug(true);
        List<String> list = app.VSDTScan(args[0]);
        System.out.println("Done");

//        System.out.println("=================== Result List ==========================");
//        for (String line: list){
//            System.out.println(line);
//        }
    }


}
