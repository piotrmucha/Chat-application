package client;

import java.util.Scanner;

public class elo {
    public static String encrypt(String text){
        StringBuilder sb = new StringBuilder(text);
        int distance=7;
        for(int i=0;i<sb.length();i++){
            int c =(int) sb.charAt(i);
            if(c>31 && c<123){
                if(c+distance>122){
                    c = 31 + (distance -(122-c));
                }else{
                    c += distance;
                }
                sb.setCharAt(i,(char)c);
            }
        }
        return sb.toString();
    }
    public static String decrytp(String text){
        StringBuilder sb =new StringBuilder(text);
        int distance=7;
        for(int i=0;i<sb.length();i++){
            int c = (int) sb.charAt(i);
            if(c>31 && c<123){
                if(c-distance<32){
                    c = 123 - (32-(c-distance)) ;
                }   else{
                    c -=distance;
                }
                sb.setCharAt(i,(char)c);
            }
        }
        return sb.toString();
    }

    public static void main(String [] args){
        char c='\u265E';
        System.out.println(c);
        Scanner in=new Scanner(System.in);
        String input=in.nextLine();
        System.out.println(encrypt(input));
        String inp=in.nextLine();
        System.out.println(decrytp(inp));
    }
}
