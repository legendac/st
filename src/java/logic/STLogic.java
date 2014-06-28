/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.util.PDFMergerUtility;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Adam
 */
public class STLogic extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, JSONException, COSVisitorException {
        
        
        JSONObject jo = (JSONObject) new JSONTokener(IOUtils.toString(new URL("http://stapp.straitstimes.com/ipad_layout?device=platform=android-tablet&v=1&day=0&device_name=tablet"))).nextValue();
        System.out.println(jo.names());
               
        List<String> list = new ArrayList<String>();
        JSONArray array = jo.getJSONArray("sections");
        
        for(int i = 0 ; i < array.length() ; i++) {
            JSONArray array2 = array.getJSONObject(i).getJSONArray("pdf_pages");
            for (int j = 0 ; j < array2.length() ; j++) {
                list.add(array2.getString(j));
            }
            
            //list.add(array.getJSONObject(i).getJSONArray("pdf_pages"));
        }
        String pdf_url = null;
        while (pdf_url == null) {
            for (int k = 0 ; k < array.length() ; k++) {
                pdf_url = array.getJSONObject(k).getString("pdf_url");
            }
        }
        
        String directory = "/../Straits Times/";
        System.out.println("<<<<<<<<<<<<" + pdf_url);
        
        HashMap<String,ArrayList<String>> hMap = new HashMap<>();
        boolean first = true;
        String date = "";
        ArrayList<String> newCat = new ArrayList<>();
        ArrayList<String> contents = new ArrayList<>();
        String laststring = null;
        for (String x : list) {
            if(first) {
                date = x.substring(0,10);
                first = false;
            }
            
            String filename = directory + x.substring(0,10) + "/" + x.substring(11,14) + "/" + x.substring(15);
            //String filename = directory + x.substring(0,9) + "/" + x.substring(15);
            
            File file = new File(filename);
            FileUtils.copyURLToFile(new URL(pdf_url + x),file);
            
            if (!newCat.contains(x.substring(11,14))) { //doesn't contain category
                //put Collection of previous category into into Map
                hMap.put(laststring, contents);
                
                //new category
                newCat.add(x.substring(11,14)); 
                //create new Collection for new categories
                contents = new ArrayList<>();
                //add contents into new Collection
                contents.add(x.substring(15));
                
                laststring = x.substring(11,14);
            } else {
                contents.add(x.substring(15));
            }
            
        }
        
        hMap.put(laststring, contents);
        String currentDirectory = directory + date + "/";
        System.out.println("done");
        for (String x : newCat) {
            System.out.println(x);
        }
        
        
        /*for (String x : newCat) {
            
            PDFMergerUtility ut = new PDFMergerUtility();
            //different category folders
            String folderDirectory = currentDirectory + x + "/";
            ArrayList<String> folderContents = hMap.get(x);
            for (String y : folderContents) {
                ut.addSource(folderDirectory + y);
                System.out.print(folderDirectory + y);
            }
            ut.setDestinationFileName(currentDirectory + x + ".pdf");
            ut.mergeDocuments();
            
        }*/
        
        
        for (String x : newCat) {
            File folder = new File(currentDirectory + x);
            File[] listOfFiles = folder.listFiles();
            System.out.println(listOfFiles);
            System.out.println(folder);
            PDFMergerUtility ut = new PDFMergerUtility();
            
            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()) {
                    System.out.println("File " + listOfFiles[i].getName());
                    ut.addSource(currentDirectory + x + "/" + listOfFiles[i].getName());
                    
                }
            }
            
            if (x.equals("PRI")) {
                ut.setDestinationFileName(currentDirectory + "Prime" + ".pdf");
            } else if (x.equals("HOM")) {
                ut.setDestinationFileName(currentDirectory + "Home" + ".pdf");
            } else if (x.equals("MON")) {
                ut.setDestinationFileName(currentDirectory + "Money" + ".pdf");
            } else if (x.equals("LIF")){
                ut.setDestinationFileName(currentDirectory + "Life" + ".pdf");
            } else if (x.equals("URB")){
                ut.setDestinationFileName(currentDirectory + "Urban" + ".pdf");
            } else if (x.equals("MIN")){
                ut.setDestinationFileName(currentDirectory + "MindYourBody" + ".pdf");
            } else if (x.equals("DIG")){
                ut.setDestinationFileName(currentDirectory + "DigitalLife" + ".pdf");
            } else if (x.equals("SUN")){
                ut.setDestinationFileName(currentDirectory + "SundayTimes" + ".pdf");
            } else if (x.equals("XSJ")){
                ut.setDestinationFileName(currentDirectory + "Saturday" + ".pdf");
            } else if (x.equals("XSH")){
                ut.setDestinationFileName(currentDirectory + "HomeCoverAd" + ".pdf");
            } else if (x.equals("XSP")){
                ut.setDestinationFileName(currentDirectory + "CoverAd" + ".pdf");
            } else {
                ut.setDestinationFileName(currentDirectory + x + ".pdf");
            }
            ut.mergeDocuments();
        }
        
        for (String x : newCat) {
            deleteFileFolder(currentDirectory + x);
        }
    }
    public static void deleteFileFolder(String path) {

        File file = new File(path);
        if(file.exists()) {
            do{
                delete(file);
            } while(file.exists());
        } else {
            System.out.println("File or Folder not found : "+path);
        }
    }
    
    private static void delete(File file) {
        if(file.isDirectory()) {
            String fileList[] = file.list();
            if(fileList.length == 0) {
                System.out.println("Deleting Directory : "+file.getPath());
                file.delete();
            } else {
                int size = fileList.length;
                for(int i = 0 ; i < size ; i++) {
                    String fileName = fileList[i];
                    System.out.println("File path : "+file.getPath()+" and name :"+fileName);
                    String fullPath = file.getPath()+"/"+fileName;
                    File fileOrFolder = new File(fullPath);
                    System.out.println("Full Path :"+fileOrFolder.getPath());
                    delete(fileOrFolder);
                }
            }
        } else {
            System.out.println("Deleting file : "+file.getPath());
            file.delete();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (JSONException ex) {
            Logger.getLogger(STLogic.class.getName()).log(Level.SEVERE, null, ex);
        } catch (COSVisitorException ex) {
            Logger.getLogger(STLogic.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (JSONException ex) {
            Logger.getLogger(STLogic.class.getName()).log(Level.SEVERE, null, ex);
        } catch (COSVisitorException ex) {
            Logger.getLogger(STLogic.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
