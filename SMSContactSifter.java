/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smscontactsifter;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;
import static java.nio.file.StandardOpenOption.*;
import java.nio.file.*;
import java.io.*;

/**
 *
 * @author VLAD
 */
public class SMSContactSifter {
    private static final String headerString="<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>\n" +
"<!--Original file Created By SMS Backup & Restore v10.01.136. This contact extract is created by Vladimir Ventura's SMSContactSifter-->\n" +
"<?xml-stylesheet type=\"text/xsl\" href=\"sms.xsl\"?>\n" +
"<smses count=\"4353\" backup_set=\"1d5c1c68-85c3-4f73-815f-c69e9d37ce76\" backup_date=\"1498523321021\">\n";
    private static final String footerString="</smses>\n";
    private static byte header[] = headerString.getBytes();
    private static byte footer[] = footerString.getBytes();    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception{
        System.out.println(System.getProperty("user.dir"));
        File file = new File("sms-20170626202840.xml");
        Scanner scan = new Scanner(file);
        
        //3 ways to do this.
        //option 1: hard-drive memory intensive (keep several files open, 1 for each contact I have). This will cause the heapsace to run out. (memory O(n^k) where k is # of contacts). terribly bad.
        //option 2: Open file only when storing data into it; then close it. The reference for the file will still be there. Memory is O(n); time complexity is O(n) (additive). OK
        //option 3: most time-consuming one: parse the entirety of the data for each person. If there are n data, and n/100 ppl (average 100 mesages per person), it is O(n^2). not good.
        ArrayList<File> contactFiles = new ArrayList<>();
        ArrayList<String> contactNames = new ArrayList<>();//keeps track of all the filenames to see if they exist yet.
        ArrayList<Integer> counts = new ArrayList<>();
        String s;
        System.out.println(scan.nextLine() + "\n" + scan.nextLine() + "\n" + scan.nextLine() + "\n" + scan.nextLine());//goes through <smses count etc...>
        int i = 0;
        while(scan.hasNextLine()){
            s = scan.nextLine();
            if(s.contains("<sms")){
                parsesms(s, contactNames, contactFiles, counts);
            }
            else if(s.contains("<mms")){
                parsemms(s, scan, contactNames, contactFiles, counts);
            }
            //i++;
            //System.out.println(i);
            
        }//parsing is done
        appendFooter(contactNames);
        System.out.println(contactNames.toString());
        /*
        <mms smilext='&lt;smil ve="1"&gt;&lt;head&gt;&lt;layout&gt;&lt;root-layout width="540" height="888"/&gt;&lt;region id="Image" left="0" top="0" width="540" height="622" fit="meet"/&gt;&lt;region id="Text" left="0" top="622" width="540" height="266" fit="scroll"/&gt;&lt;/layout&gt;&lt;/head&gt;&lt;body&gt;&lt;par dur="10000ms"&gt;&lt;text src="text_0.txt" region="Text" ct="text/plain" si="3" pn="null" dn="text_0.txt" ur="content://mms/part/2" co="Yes"/&gt;&lt;/par&gt;&lt;/body&gt;&lt;/smil&gt;' text_only="1" htc_category="0" ct_t="application/vnd.wap.multipart.related" msg_box="1" v="16" sub="null" seen="0" rr="null" ct_cls="null" retr_txt_cs="null" ct_l="null" phone_type="0" m_size="301" exp="null" sub_cs="null" st="null" creator="com.htc.sense.mms" tr_id="D71231185442600019000040005" sub_id="-1" sim_slot="0" read="1" resp_st="null" date="1483210482000" m_id="123118544260001900004" date_sent="0" pri="null" m_type="132" extra="0" cs_id="null" cs_synced="0" address="+19732164695~+12012572424~+18456531220~+16467096125~+18455980082~+18455215734" date2="1483210497" cs_timestamp="-1" d_rpt="129" d_tm="null" read_status="null" locked="0" retr_txt="null" resp_txt="null" rpt_a="null" retr_st="null" m_cls="null" readable_date="Dec 31, 2016 1:54:42 PM" contact_name="Dan Wolanski (D&amp;D), Mike Wasielewski, Chuck From D&amp;D, Carlo, Kenji D&amp;D">
    <parts>
      <part seq="-1" ct="application/smil" name="null" chset="null" cd="null" fn="null" cid="&lt;0.smil&gt;" cl="null" ctt_s="null" ctt_t="null" ExtraUri="null" vCALs="0" vCALe="0" text='&lt;smil&gt;&#10;&lt;head&gt;&#10;&lt;layout&gt;&#10; &lt;root-layout/&gt;&#10;&lt;region id="Text" top="70%" left="0%" height="30%" width="100%" fit="scroll"/&gt;&#10;&lt;region id="Image" top="0%" left="0%" height="70%" width="100%" fit="meet"/&gt;&#10;&lt;/layout&gt;&#10;&lt;/head&gt;&#10;&lt;body&gt;&#10;&lt;par dur="10s"&gt;&#10;&lt;text src="text_0.txt" region="Text"/&gt;&#10;&lt;/par&gt;&#10;&lt;/body&gt;&#10;&lt;/smil&gt;&#10;' />
      <part seq="0" ct="text/plain" name="null" chset="3" cd="attachment" fn="null" cid="&lt;0&gt;" cl="text_0.txt" ctt_s="null" ctt_t="null" ExtraUri="null" vCALs="0" vCALe="0" text="Yes" />
    </parts>
    <addrs>
      <addr address="+19732164695" type="137" charset="106" />
      <addr address="+16467096125" type="151" charset="106" />
      <addr address="+18456531220" type="151" charset="3" />
      <addr address="+18455215734" type="151" charset="3" />
      <addr address="+18455980082" type="151" charset="106" />
      <addr address="+19734600666" type="151" charset="106" />
      <addr address="+12012572424" type="151" charset="3" />
    </addrs>
  </mms>
        */
        
        
    }

    private static void parsesms(String s, ArrayList<String> contactNames, ArrayList<File> contactFiles, ArrayList<Integer> counts) {
        /*
                <sms protocol="0" address="+19733808923" date="1483209521000" type="1" subject="null" body="Hey Vlad what are your plans for tonight?" toa="145" sc_toa="0" service_center="+12404492167" read="1" status="-1" locked="0" date_sent="1483209521000" readable_date="Dec 31, 2016 1:38:41 PM" contact_name="Vanessa Ventura" />
        */
        String phoneNum = s.substring(s.indexOf("address=\"")+9,s.indexOf("\" date="));
        String name = s.substring(s.indexOf("contact_name=")+14,s.indexOf(" />")-1);
        if(name.contains("\"")) {
            name=name.replace("\"", "QT");
            System.out.println(name);
        }
        
        s+="\n";
        byte data[] = s.getBytes();
        Path p = Paths.get("./Output/"+name+".xml"); //the file name will have the contactname on it.
        
        if(!contactNames.contains(name)){
            contactNames.add(name); //brand new file.
            try (OutputStream out = new BufferedOutputStream(
                Files.newOutputStream(p, CREATE, APPEND))) {
                out.write(header, 0, header.length);
              } catch (IOException x) {
                System.err.println(x);
            }
        }
        try (OutputStream out = new BufferedOutputStream(
          Files.newOutputStream(p, CREATE, APPEND))) {
          out.write(data, 0, data.length);
        } catch (IOException x) {
          System.err.println(x);
        }

        
    }

    private static void parsemms(String s, Scanner scan, ArrayList<String> contactNames, ArrayList<File> contactFiles, ArrayList<Integer> counts) {
        String phoneNums = s.substring(s.indexOf("address=\"")+9,s.indexOf("\" date2"));
        String name = s.substring(s.indexOf("contact_name=\"")+14,s.indexOf("\">"));
        
        
        Path p = Paths.get("./Output/"+name+".xml"); //the file name will have the contactname on it.
        byte data[];
        
        if(!contactNames.contains(name)){
            contactNames.add(name); //brand new file.
            try (OutputStream out = new BufferedOutputStream(
                Files.newOutputStream(p, CREATE, APPEND))) {
                out.write(header, 0, header.length);
              } catch (IOException x) {
                System.err.println(x);
            }
        }
        
        
        String lines="";
        do{
        lines += s+"\n";
        s=scan.nextLine();
        } while(!s.contains("</mms>"));
        lines+=s+"\n";
        
        //System.out.println(lines);
        
        data = lines.getBytes();
        try (OutputStream out = new BufferedOutputStream(
          Files.newOutputStream(p, CREATE, APPEND))) {
          out.write(data, 0, data.length);
        } catch (IOException x) {
          System.err.println(x);
        }

    }

    private static void appendFooter(ArrayList<String> contactNames) {
        Path p;
        for(String name: contactNames) {
            p= Paths.get("./Output/"+name+".xml");
        
            try (OutputStream out = new BufferedOutputStream(
              Files.newOutputStream(p, CREATE, APPEND))) {
              out.write(footer, 0, footer.length);
            } catch (IOException x) {
              System.err.println(x);
            }
        }
    }

    
    
}
