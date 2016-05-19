package trendmicro.coc;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by sammi_lin on 16/3/18.
 */


public class StringFinder {

    public String filePath="/Users/sammi_lin/Documents/workshare/VSDT_Tool_Linux/9.850-1008";
    public String dataTypeDef = "/DataTypeDef.txt";
    public String VSDTScanCode = "1-0"; //Sample

    private String dataTypeCode = "";
    private int subTypeCode = 0;


    public static void main(String[] args)
    {
        StringFinder sf = new StringFinder();
//        String str = sf.search_code_string("4045-3"); //using tmvsdef.h find out all of type

        String str = sf.search_data_type_string();
//        System.out.println(str);
        String rs  = sf.search_sub_type_string(str);
//        System.out.println(rs);
    }

    public StringFinder() {

        String[] codeType = VSDTScanCode.split("-");
        setDataTypeCode(codeType[0]);
        setSubTypeCode(Integer.parseInt(codeType[1].toString()));

    }

    public StringFinder(String filePath, String dataTypeDef, String VSDTScanCode) {
        this.filePath = filePath;
        this.dataTypeDef = dataTypeDef;
        this.VSDTScanCode = VSDTScanCode;

        String[] codeType = VSDTScanCode.split("-");
        setDataTypeCode(codeType[0]);
        setSubTypeCode(Integer.parseInt(codeType[1].toString()));

    }

    public String getDataTypeCode() {
        return dataTypeCode;
    }

    public int getSubTypeCode() {
        return subTypeCode;
    }

    public String getDataTypeDef() {
        return dataTypeDef;
    }

    public void setDataTypeCode(String dataTyeCode) {
        this.dataTypeCode = dataTyeCode;
    }

    public void setSubTypeCode(int subTypeCode) {
        this.subTypeCode = subTypeCode;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }


    /* Search DataType
     * Using DataTypeDef file to search data type
     * Sammi Lin,2016/3/28
     */
    public String search_data_type_string() {
        String data_type = "";
        String DataTypeFile = getFilePath() + getDataTypeDef();
        String line = "";
        String inputSearch = getDataTypeCode();

//        System.out.println("inputSearch = "+ inputSearch);
//        System.out.println("DataTypeFile = "+ DataTypeFile);

        BufferedReader br;
        int hit =0;

        try {
            br = new BufferedReader(new FileReader(DataTypeFile));
            try {
                while ((line = br.readLine()) != null) {
//                    System.out.println(line);
                    String[] words = line.split("[ ]+");

                    for (String word : words) {
                        if (word.equals(inputSearch)) {
                            String[] define_str_ary = line.split(" ");
                            data_type = define_str_ary[1];
//                            System.out.format("data_type is %s%n", data_type);
                            hit =1;
                            break;
                        }
                    }
                    if (hit >0) break;
                }
                br.close();
            } catch (Exception e){
                e.printStackTrace();
            }
        }catch(FileNotFoundException e){
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return data_type;
    }
    /* Search subtype
     * Search tmvsdef.h to find out subtype
     * Sammi Lin,2016/3/28
     */
    public String search_sub_type_string(String VSDTDataType){
        String match_type="";

        filePath = filePath + "/tmvsdef.h";
        String line = "";
        BufferedReader br;
        Map sub_type = new HashMap();

        try {
            br = new BufferedReader(new FileReader(filePath));
            try {
                while ((line = br.readLine()) != null) {
                    if (line.indexOf(VSDTDataType)>0){
                        String[] subtype_ary = line.split("[ ]+"); //split " "(one or more space)
                        sub_type.put(Integer.parseInt(subtype_ary[2].trim().toString()),subtype_ary[1]);
//                        System.out.println(line.split("[ ]+")[2].toString());
//                        System.out.format("Sub_type = %s,%s%n", subtype_ary[1].toString(), subtype_ary[2].toString());
                    }
                }
                br.close();
            } catch (Exception e){
                e.printStackTrace();
            }
        }catch(FileNotFoundException e){
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        match_type = (String) sub_type.get(subTypeCode);

        if (match_type == null) match_type = VSDTDataType;

//        System.out.println("sub_type is: "+match_type);


        return match_type;

    }

    /* Search all of type define (Sample)
     * Search tmvsdef.h to find out subtype
     * Sammi Lin,2016/3/18
     */
    public String search_code_string(String VSDT_CodeType){
        double count = 0,countBuffer=0,countLine=0;
        String match_type = "";
        //split datatype code and subtype code
        String[] codeType = VSDT_CodeType.trim().split("-");

        this.dataTypeCode = codeType[0];
        this.subTypeCode = Integer.parseInt(codeType[1].toString());
//        setDataTypeCode(codeType[0]);
//        setSubTypeCode(Integer.parseInt(codeType[1].toString()));
//        System.out.println("this.datatype = "+ this.dataTypeCode + " codeType[1] is " + this.subTypeCode);

        String inputSearch = codeType[0];
        int input_subtype = Integer.parseInt(codeType[1].toString());

        String str = search_data_type_string();
//        System.out.println("search_code_string ->DataType is "+ str);
        match_type = search_sub_type_string(str);

//        System.out.println("match_type is "+ match_type);

//        String lineNumber = "";
//        filePath = filePath + "/tmvsdef.h";
//        BufferedReader br;
//
//
//        String line = "";
//        String data_type = "";
//        int hit = 0;
//        Map sub_type = new HashMap();
//
//        try {
//            br = new BufferedReader(new FileReader(filePath));
//            try {
//                while((line = br.readLine()) != null)
//                {
//                    //looking for first keyword "@addtogroupo DataType
//                    countLine++;
//                    //System.out.println(line);
//                    String[] words = line.split("[ ]+");
//
//                    for (String word : words) {
//                        if (word.equals(inputSearch)) {
//                            count++;
//                            countBuffer++;
//                            String[] define_str_ary = line.split(" ");
//                            data_type = define_str_ary[1];
//                            System.out.format("data_type = %s%n",data_type);
//                            hit = 1;
//                        }
//
//                    }
//                    if (hit>1 && line.indexOf(data_type)>0){
//                        String[] subtype_ary = line.split("[ ]+");
//                        sub_type.put(Integer.parseInt(subtype_ary[2].trim().toString()),subtype_ary[1]);
////                        System.out.println(line.split("[ ]+")[2].toString());
////                        System.out.format("Sub_type = %s,%s%n", subtype_ary[1].toString(), subtype_ary[2].toString());
//                    }
//                    hit++;
//                    if(countBuffer > 0)
//                    {
//                        countBuffer = 0;
//                        lineNumber += countLine + ",";
//                    }
//
//                }
//                br.close();
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        } catch (FileNotFoundException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//        match_type = (String) sub_type.get(input_subtype);
//
//        if (match_type == null) match_type = data_type;
//
////        System.out.println("Times found at--"+count);
////        System.out.println("Word found at--"+lineNumber);
////        System.out.println("sub_type is: "+match_type);

        return match_type;
    }

}
