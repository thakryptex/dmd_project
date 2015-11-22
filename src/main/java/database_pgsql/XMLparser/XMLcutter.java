package database_pgsql.XMLparser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class XMLcutter {

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(new File("dblp.xml"));
        FileWriter writer = new FileWriter("dblp_example.xml");
        StringBuilder temp = new StringBuilder();
        int k;
        for (int i = 0; i < 1000; i++) {
            temp.append(scanner.nextLine());
            if (temp.indexOf("&") != -1 && temp.indexOf(";") != -1) {
                for (int j = 0; j < htmlEscape.length; j++) {
                    k = temp.indexOf(htmlEscape[j][0]);
                    if (k != -1) {
                        temp.replace(k, k+htmlEscape[j][0].length(), htmlEscape[j][1]);
                    }
                }
            }
            writer.append(temp.toString());
            writer.append("\n");
            temp.setLength(0);
            System.out.println(i);
        }
        writer.append("</dblp>");
        writer.close();
    }


    private static String [][] htmlEscape =
                   {{  "&agrave;" , "a" } ,  {  "&Agrave;" , "A" } ,
                    {  "&acirc;"  , "a" } ,  {  "&auml;"   , "a" } ,
                    {  "&Auml;"   , "A" } ,  {  "&Acirc;"  , "A" } ,
                    {  "&aring;"  , "a" } ,  {  "&Aring;"  , "A" } ,
                    {  "&aelig;"  , "a" } ,  {  "&AElig;"  , "a" } ,
                    {  "&ccedil;" , "c" } ,  {  "&Ccedil;" , "C" } ,
                    {  "&eacute;" , "e" } ,  {  "&Eacute;" , "E" } ,
                    {  "&egrave;" , "e" } ,  {  "&Egrave;" , "E" } ,
                    {  "&ecirc;"  , "e" } ,  {  "&Ecirc;"  , "E" } ,
                    {  "&euml;"   , "e" } ,  {  "&Euml;"   , "E" } ,
                    {  "&iuml;"   , "i" } ,  {  "&Iuml;"   , "I" } ,
                    {  "&ocirc;"  , "o" } ,  {  "&Ocirc;"  , "O" } ,
                    {  "&ouml;"   , "o" } ,  {  "&Ouml;"   , "O" } ,
                    {  "&oslash;" , "o" } ,  {  "&Oslash;" , "O" } ,
                    {  "&szlig;"  , "s" } ,  {  "&ugrave;" , "u" } ,
                    {  "&Ugrave;" , "U" } ,  {  "&ucirc;"  , "u" } ,
                    {  "&Ucirc;"  , "U" } ,  {  "&uuml;"   , "u" } ,
                    {  "&Uuml;"   , "U" } ,  {  "&aacute;" , "a" } ,
                    {  "&oacute;" , "o" } ,  {  "&iacute;" , "i" } ,
                    {  "&ograve;" , "o" } ,  {  "&Oacute;" , "O" } ,
                    {  "&ntilde;" , "n" } ,  {  "&eacute;" , "e" } ,
                    {  "&aacute;" , "a" } ,  {  "&auml;"   , "a" } ,
                    {  "&#964;"   , "T" } ,
                    {  "&icirc;"  , "i" } ,  {  "&uacute;" , "u" } ,
                    {  "&Aacute;" , "A" } ,  {  "&uuml;"   , "u" } ,
                    {  "&atilde;" , "a" } ,  {  "&#8482;"  , ""  } ,
                    {  "&auml;"   , "a" } ,  {  "&reg;"    , ""  } ,
                    {  "&#8727;"  , "*" } ,  {  "&iacute;" , "i" }
            };

}
