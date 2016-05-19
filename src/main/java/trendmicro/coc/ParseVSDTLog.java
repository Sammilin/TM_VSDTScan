package trendmicro.coc;

import java.io.*;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by sammi_lin on 16/3/18.
 */
public class ParseVSDTLog {
    private String rsdir = "/Users/sammi_lin/Documents/workshare/VSDT_Tool_Linux/9.850-1008";
    private String scanlogFile = "/scan_folder/scan_log";
    private String screenFile = "/tmp/screen_rs";
    private String dataTypedefname = "/DataTypeDef.txt";

    private String screenResultFile;
    private String scanResultFile;
    private long timestamp ;


    public ParseVSDTLog(String rsdir,String scanlogfile, String screenfile) {
        this.rsdir = rsdir;
        this.scanlogFile = rsdir + scanlogfile;
        this.screenFile = rsdir + screenfile;
        this.timestamp = setNowTimestamp();

    }


    public ParseVSDTLog(String scanlogFile) {
        this.scanlogFile = scanlogFile;
        this.timestamp = setNowTimestamp();

    }

    public ParseVSDTLog() {
        this.scanlogFile = rsdir + this.scanlogFile;
        this.screenFile = rsdir + this.screenFile;
        this.timestamp = setNowTimestamp();
    }

    private static long setNowTimestamp() {
        DateFormat df = DateFormat.getTimeInstance();
        Calendar cal = Calendar.getInstance();
        long timestamp = cal.getTimeInMillis();
        return timestamp;
    }

    public void setScanlogFile(String filepath){
        this.scanlogFile = filepath;
    }
    public void setScreenFile(String filepath){
        this.screenFile = filepath;
    }
    public void setDataTypedefname(String defname){
        this.dataTypedefname = defname;
    }
    public String getScreenResultFile() {
        return screenResultFile;
    }
    public void setScreenResultFile(String screenResultFile) {
        this.screenResultFile = screenResultFile;
    }
    public String getScanResultFile() {
        return scanResultFile;
    }
    public void setScanResultFile(String scanResultFile) {
        this.scanResultFile = scanResultFile;
    }

    public static void main(String[] args) {
        ParseVSDTLog pvl = new ParseVSDTLog();

//        pvl.CoverCodeTypeByScreenLog(" ");
        pvl.replaceScreenRs();

    }

    public List<String> getScanResult(){
        String filepath = this.screenResultFile;
        BufferedReader br;
        String line ="";
        List<String> lines = new ArrayList<String>();
        try {
            br = new BufferedReader(new FileReader(filepath));
            while ((line = br.readLine()) !=null) {
                lines.add(line);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return lines;
    }

    public void replaceScanLogRs(){
        String filepath = scanlogFile;
        BufferedReader br;
        String line ="";
        String code_type = "";
        String code_string = "";
        // we need to store all the lines
        List<String> lines = new ArrayList<String>();
        try {
            br = new BufferedReader(new FileReader(filepath));
            while ((line = br.readLine()) !=null) {

                String rs = CoverCodeTypeByScanLog(line);
                if (rs !="") {
                    code_type = rs.split(",")[0];
                    code_string = rs.split(",")[1];
                    line = line.replace(code_type, code_string);
                }
//                System.out.println("scan line = "+line);
                lines.add(line);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
//        System.out.println(line);


        setScanResultFile(filepath+"_"+this.timestamp);
        File file = new File(getScanResultFile());
        // now, write the file again with the changes
        PrintWriter out = null;
        try {
            out = new PrintWriter(file);
            for (String l : lines)
                out.println(l);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        out.close();

    }

    /*
     * Response short result (screen result)
     */
    public void replaceScreenRs(){
        String filepath = this.screenFile;
        BufferedReader br;
        String line ="";
        // we need to store all the lines
        List<String> lines = new ArrayList<String>();
        try {
            br = new BufferedReader(new FileReader(filepath));
            while ((line = br.readLine()) !=null) {
                if (line.trim().length() >0) {
                    line = CoverCodeTypeByScreenLog(line); //convert line
//                    System.out.println(line);
                    lines.add(line);
                }
            }
        }catch (Exception e){
            e.printStackTrace();

        }

        this.screenResultFile = filepath+"_"+this.timestamp;
        File file = new File(screenResultFile);
        // now, write the file again with the changes
        PrintWriter out = null;
        try {
            out = new PrintWriter(file);
            for (String l : lines)
                out.println(l);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        out.close();

    }

    public String CoverCodeTypeByScreenLog(String line){

//        line = "Scanning scan_folder/endpoint.docx->(MS Office 2007 Word 4045-1)";
        Matcher matcher = Pattern.compile("\\d+-\\d+").matcher(line);
        String code_type = "";
        String code_string = "";
        if (matcher.find()) {
            code_type = matcher.group().toString();
//            System.out.println("matcher.group():\t"+matcher.group()+" "+ matcher.toMatchResult());
            //Find Out DataType String
            StringFinder sf = new StringFinder(rsdir,dataTypedefname,code_type);
            String str = sf.search_data_type_string(); //looking for data type
            code_string  = sf.search_sub_type_string(str); //looking for sub type
//            System.out.format("CoverCodeTypeByScreenLog %s : %s%n",code_type,code_string);
            line = matcher.replaceAll(code_string);
        }
//        System.out.println(line);
        return line;

//        String[] rs_codes = matcher_code.split("-");

    }

    private String CoverCodeTypeByScanLog(String line) {

//        line = "Undet [test           ][                ](     ) in poc.pptx,([Content_Types].xml) [MS Office 2007 PowerPoint] [4045-3]";
        String code_type="";
        String code_string="";
//        System.out.println("CoverCodeTypeByScanLog input:");
//        System.out.println(line);
//        System.out.println("=============Debug===================");

        Matcher matcher = Pattern.compile("\\[([^\\]]+)").matcher(line);

        List<String> tags = new ArrayList<String>();

        int pos = -1;
        while (matcher.find(pos+1)){
            pos = matcher.start();
//            System.out.println(matcher.groupCount());
//            System.out.println("end(): "+matcher.end());
            Matcher matcher_code = Pattern.compile("\\d+-\\d+").matcher(matcher.group(1));
//            System.out.format("scanlog pattern matcher_code %s%n",matcher_code);
            if (matcher_code.find()){
                code_type = matcher_code.group(0).toString();
//                System.out.println("CoverCodeTypeByScanLog code_type is "+ code_type);
                StringFinder sf = new StringFinder(this.rsdir,this.dataTypedefname, code_type);
                code_string = sf.search_code_string(code_type);
//                System.out.format("matcher_code %s : %s%n",code_type,code_string);
                String[] rs_codes = code_type.split("-");

            }
            tags.add(matcher.group(1));
        }
//        System.out.println("==============Debug End====================");
//        System.out.println(tags);
        if (code_type == ""){
            return "";
        }else{
            return code_type+","+code_string;
        }

    }

}

