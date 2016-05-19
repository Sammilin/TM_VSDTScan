package trendmicro.coc;

import java.io.InputStream;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Properties;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;


/**
 * Created by sammi_lin on 3/27/16.
 */
public class SSHCommandExecutor {
    /**
     * @param args
     */

    //VSDT Tool Server Information
    private String host;
    private String user;
    private String password;

    public String localDir;
    public String executeToolDir;
    public String datatypedefpath;
    public String screenrspath;
    public String scanlogpath;
    public String screenlogpath;

    public String defdatatype;
    public String screenlog;
    public String screenrs;
    public String scanlog;

    private long timestamp;

    public String scan_resources = "";

    public SSHCommandExecutor(Properties properties){

        Enumeration e = properties.propertyNames();
        while (e.hasMoreElements()){
            String key = (String) e.nextElement();
            String val = properties.getProperty(key);
//            System.out.println(key + ":" + val);

            if (key.trim().equals("host")) this.host = val;
            if (key.trim().equals("user")) this.user = val;
            if (key.trim().equals("password")) this.password = val;
            if (key.trim().equals("localDir")) this.localDir = val;
            if (key.trim().equals("executeToolDir")) this.executeToolDir = val;
            if (key.trim().equals("datatypedefpath")) this.datatypedefpath = val;
            if (key.trim().equals("screenrspath")) this.screenrspath = val;
            if (key.trim().equals("scanlogpath")) this.scanlogpath = val;
            if (key.trim().equals("screenlogpath")) this.screenlogpath = val;

        }
        this.timestamp = setNowTimestamp();
        this.screenlogpath += this.timestamp;
        this.scanlogpath += this.timestamp;
        this.screenrspath += this.timestamp;

        this.defdatatype = executeToolDir+ this.datatypedefpath;
        this.screenlog = executeToolDir+this.screenlogpath;
        this.scanlog = executeToolDir+this.scanlogpath;
        this.screenrs = executeToolDir+this.screenrspath;

    }


    private static long setNowTimestamp() {
        DateFormat df = DateFormat.getTimeInstance();
        Calendar cal = Calendar.getInstance();
        long timestamp = cal.getTimeInMillis();
        return timestamp;
    }

    public void setScan_resources(String scan_resources) {
        this.scan_resources = scan_resources;
    }

    public static void main(String[] args) {

        //sed -e '/@{/,/@}/!d' tmvsdef.h > DataTypeDef.txt find out the data Type
//        SSHCommandExecutor vsdtscan = new SSHCommandExecutor();
//        String command = vsdtscan.get_vsdt_scan_command();
//        vsdtscan.execute(command);
//
//        //Parse Screen Log
//        ParseVSDTLog pvl = new ParseVSDTLog( vsdtscan.localDir,vsdtscan.scanlogpath,vsdtscan.screenrspath);
//        pvl.setDataTypedefname(vsdtscan.datatypedefpath);
//        pvl.replaceScreenRs();
//        pvl.replaceScanLogRs();

    }

    public void execute(String command1)
    {
        try{

            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            JSch jsch = new JSch();
            Session session=jsch.getSession(user, host, 22);
            session.setPassword(password);
            session.setConfig(config);
            session.connect();
            System.out.println("Connected");

            Channel channel=session.openChannel("exec");
            ((ChannelExec)channel).setCommand(command1);
            channel.setInputStream(null);
            ((ChannelExec)channel).setErrStream(System.err);

            InputStream in=channel.getInputStream();
            channel.connect();


            byte[] tmp=new byte[1024];
            while(true){
                while(in.available()>0){
                    int i=in.read(tmp, 0, 1024);
                    if(i<0)break;
                    System.out.print(new String(tmp, 0, i));
                }
                if(channel.isClosed()){
                    System.out.println("exit-status: "+channel.getExitStatus());
                    break;
                }
                try{Thread.sleep(1000);}catch(Exception ee){}
            }
            channel.disconnect();
            session.disconnect();
            System.out.println("SSH Connection DONE");
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public String get_vsdt_scan_command(){


        String command1 = "cd "+executeToolDir+"; pwd;"; //move your path to tool path
        command1 += "if [ -f "+defdatatype+" ] ; then " ;   //if DataTypeDef file exist
        command1 += "file1time=`stat -c %Y tmvsdef.h`;";             //get tmvsdef.h file last modify time
        command1 += "file2time=`stat -c %Y "+defdatatype+"`;";  //get DataTypeDef.txt file last modify time
        command1 += "if [ $file1time -nt $file2time ]; then";        //if file1time > file2time mean is tmvsdef.h renew
        command1 += " sed -e '/@{/,/@}/!d' tmvsdef.h > "+defdatatype+";"; //then renew reference DataTypeDef.txt
        command1 += " fi;";
        command1 += " else ";
        command1 += " sed -e '/@{/,/@}/!d' tmvsdef.h > "+defdatatype+";"; //then renew reference DataTypeDef.txt
        command1 += " fi;";
        command1 += "./vscanrhamd64 vscan -nc -nb -s -nm -vsdt "+scan_resources+" -LR+="+scanlog+" > "+screenlog+";"; //scan your file or folder
        command1 += "find \""+screenlog+"\" -exec grep \"Scanning\" {} \\; >"+screenrs+";";
//      System.out.println(command1);
        return command1;
    }

}
